package appAsis.example.asistenciaugelcorongo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import appAsis.example.asistenciaugelcorongo.domain.models.AttendancePrefs;
import appAsis.example.asistenciaugelcorongo.domain.models.AttendanceRecord;
import appAsis.example.asistenciaugelcorongo.remote.ApiService;
import appAsis.example.asistenciaugelcorongo.repository.DataRepository;
import appAsis.example.asistenciaugelcorongo.utils.LocationHelper;
import appAsis.example.asistenciaugelcorongo.utils.NetworkHelper;
import appAsis.example.asistenciaugelcorongo.utils.RawFileReader;
import appAsis.example.asistenciaugelcorongo.utils.URLPostHelper;
import appAsis.example.asistenciaugelcorongo.work.SyncAttendanceWorker;
import appAsis.example.asistenciaugelcorongo.remote.ApiService.ApiCallback;

public class HomeEspecialista extends AppCompatActivity {
    private static final double MAX_DISTANCE_M = 50.0;
    private static final int REQUEST_LOCATION_PERMISSION = 100;
    private static final int REQUEST_CAMERA = 101;
    private static final int REQUEST_IMAGE_CAPTURE = 102;
    private Handler coordHandler = new Handler();
    private Runnable coordRunnable;
    private DataRepository repo;
    private AttendancePrefs prefs;

    private String colegioName;
    private String docenteName;
    private String rol;
    private String idColegio;
    private Location lastLocation;

    private Bitmap    evidenciaBitmap;
    private AlertDialog dialogEvidencia;
    private ImageView ivEvidenciaPreview;
    private Button btnTomarFoto, btnSubirEvidencia, btnCancelarEvidencia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_home_especialista);

        repo    = DataRepository.getInstance(this);
        prefs   = new AttendancePrefs(this);

        colegioName = getIntent().getStringExtra("colegio");
        docenteName = getIntent().getStringExtra("docente");
        rol         = getIntent().getStringExtra("turnos");
        idColegio   = getIntent().getStringExtra("idcolegio");

        // Enviar coordenadas para el monitoreo
        String url_registrocoordenadas = URLPostHelper.Coordenadas.REGISTRAR;
        coordRunnable = new Runnable() {
            @Override
            public void run() {
                if (!NetworkHelper.isOnline(HomeEspecialista.this)) {
                    coordHandler.postDelayed(this, 5000);
                    return;
                }

                LocationHelper.requestSingleLocation(HomeEspecialista.this, new LocationHelper.LocationResultCallback() {
                    @Override
                    public void onLocationResult(Location location) {
                        repo.sendCoordinates(url_registrocoordenadas, docenteName, rol,
                                location.getLatitude(), location.getLongitude(),
                                new ApiCallback<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean ok) {
                                    }
                                    @Override
                                    public void onError(Exception e) {
                                    }
                                });
                    }
                    @Override
                    public void onError(Exception e) {
                    }
                });
                coordHandler.postDelayed(this, 5000);
            }
        };
        coordHandler.post(coordRunnable);

        // Descarga la lista actualizada de colegios en background
        String urlCole = URLPostHelper.Colegio.VER;
        repo.updateDatacolegioRemote(urlCole, new ApiCallback<String>() {
            @Override public void onSuccess(String result) { /* archivo interno actualizado */ }
            @Override public void onError(Exception e) { /* se mantiene el raw */ }
        });

        findViewById(R.id.btc_asistencias_especialista)
                .setOnClickListener(v -> startAttendanceFlow());

        // Botón de evidencias: invoca nuestro nuevo diálogo
        ImageButton btcEvidencia = findViewById(R.id.btc_evidencias_especialistas);
        btcEvidencia.setOnClickListener(v -> mostrarPopupEvidencia());

        // Botón FICHAS
        findViewById(R.id.btc_fichas_especialistas)
                .setOnClickListener(v -> startFichasFlow());
    }

    /** 1. Determina si toca Entrada/Salida o ya completó ambos */
    private String getNextTipoRegistro(String colegio) {
        if (!prefs.isRegistered("Entrada", colegio)) return "Entrada";
        if (!prefs.isRegistered("Salida", colegio))  return "Salida";
        return null;
    }

    /** 2. Inicia flujo: pide ubicación y luego valida rango */
    private void startAttendanceFlow() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{ Manifest.permission.ACCESS_FINE_LOCATION },
                    REQUEST_LOCATION_PERMISSION
            );
            return;
        }

        LocationHelper.requestSingleLocation(this, new LocationHelper.LocationResultCallback() {
            @Override
            public void onLocationResult(Location location) {
                lastLocation = location;
                validateRangeAndShowPopup(); //
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(HomeEspecialista.this,
                        "No se pudo obtener ubicación",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarPopupEvidencia() {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_evidencia, null);

        ivEvidenciaPreview = view.findViewById(R.id.ivEvidenciaPreview);
        btnTomarFoto       = view.findViewById(R.id.btnTomarFoto);
        btnSubirEvidencia = view.findViewById(R.id.btnSubirEvidencia);
        btnCancelarEvidencia = view.findViewById(R.id.btnCancelarEvidencia);

        // Deshabilitamos el botón de subir hasta que haya foto
        btnSubirEvidencia.setEnabled(false);

        btnTomarFoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{ Manifest.permission.CAMERA },
                        REQUEST_CAMERA
                );
            } else {
                dispatchTakePictureIntent();
            }
        });

        btnSubirEvidencia.setOnClickListener(v -> {
            if (evidenciaBitmap == null) {
                Toast.makeText(this, "Toma una foto antes de subir", Toast.LENGTH_SHORT).show();
                return;
            }
            enviarEvidencia();
            dialogEvidencia.dismiss();
        });

        btnCancelarEvidencia.setOnClickListener(v -> dialogEvidencia.dismiss());

        dialogEvidencia = new AlertDialog.Builder(this)
                .setView(view)
                .create();
        dialogEvidencia.show();
    }

    /** 3. Valida distancia ≤50 m y muestra diálogo dinámico */
    private void validateRangeAndShowPopup() {
        try {
            List<String[]> rows = RawFileReader.readRawDatacolegio(this);
            String colegioName = null;
            boolean inRange = false;
            double bestDist = Double.MAX_VALUE;
            double dist = Double.MAX_VALUE;

            for (String[] parts : rows) {
                if (parts.length < 7) continue;

                double lat = Double.parseDouble(parts[5]);
                double lon = Double.parseDouble(parts[6]);
                dist = LocationHelper.calculateDistance(
                        lastLocation.getLatitude(),
                        lastLocation.getLongitude(),
                        lat,
                        lon
                );
                if (dist < bestDist) {
                    bestDist = dist;
                    colegioName = parts[0];
                    inRange = dist <= MAX_DISTANCE_M;
                }
            }

            if (!inRange || colegioName == null) {
                Toast.makeText(this,
                        "No estás dentro del rango de ningún colegio",
                        Toast.LENGTH_LONG).show();
                return;
            }

            String tipo = getNextTipoRegistro(colegioName);
            if (tipo == null) {
                Toast.makeText(this,
                        "Ya completaste tus registros de hoy para " + colegioName,
                        Toast.LENGTH_LONG).show();
                return;
            }

            showPopupAsistencia(tipo, colegioName);

            Toast.makeText(this,
                    "latn: " + lastLocation.getLatitude() + " logn: " + lastLocation.getLongitude() + " d: " + dist,
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this,
                    "Error validando ubicación",
                    Toast.LENGTH_LONG).show();
        }
    }

    /** 4. Diálogo para comentario y confirmación */
    private void showPopupAsistencia(String tipoRegistro, String colegio) {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_asistencia, null);
        EditText etComentario = view.findViewById(R.id.et_comentario);
        Button btnSi = view.findViewById(R.id.btn_si);
        Button btnNo = view.findViewById(R.id.btn_no);

        AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle("Registrar " + tipoRegistro)
                .setView(view)
                .create();

        btnSi.setOnClickListener(v -> {
            String comentario = etComentario.getText().toString().trim();
            String hora = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
            ).format(new Date());

            AttendanceRecord rec = new AttendanceRecord(
                    colegio,
                    docenteName,
                    rol,
                    tipoRegistro,
                    hora,
                    "0",  // sin cálculo de tardanza
                    comentario,
                    lastLocation.getLatitude(),
                    lastLocation.getLongitude()
            );

            // Envía o guarda offline y programa sync
            String url_registrarasistencia = URLPostHelper.Asistencia.REGISTRAR;
            repo.sendAttendance(url_registrarasistencia, rec,
                    new ApiCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean ok) {
                            Toast.makeText(HomeEspecialista.this,
                                    tipoRegistro + " registrada",
                                    Toast.LENGTH_LONG).show();
                            // Marcar en prefs
                            prefs.setRegistered(tipoRegistro, colegio);
                        }
                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(HomeEspecialista.this,
                                    "Sin conexión. Guardando localmente.",
                                    Toast.LENGTH_LONG).show();
                            prefs.setRegistered(tipoRegistro, colegio);
                            scheduleSyncWork();
                        }
                    }
            );

            dlg.dismiss();
        });

        btnNo.setOnClickListener(v -> dlg.dismiss());
        dlg.show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePic.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePic, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "No se encontró cámara", Toast.LENGTH_SHORT).show();
        }
    }

    private void enviarEvidencia() {
        String urlEvidencia = URLPostHelper.Imagen.REGISTRAR;

        repo.sendEvidence(
                urlEvidencia,
                colegioName,
                docenteName,
                rol,
                idColegio,
                evidenciaBitmap,
                new ApiCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean ok) {
                        Toast.makeText(
                                HomeEspecialista.this,
                                "Evidencia subida correctamente",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(
                                HomeEspecialista.this,
                                "Sin conexión. Evidencia guardada localmente.",
                                Toast.LENGTH_LONG
                        ).show();
                        scheduleSyncWork();
                    }
                }
        );
    }

    /** Comprueba permiso de ubicación antes de listar las fichas */
    private void startFichasFlow() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{ Manifest.permission.ACCESS_FINE_LOCATION },
                    REQUEST_LOCATION_PERMISSION
            );
        } else {
            loadAndShowFichas();
        }
    }

    /** Llama al repositorio para descargar o leer offline las fichas */
    private void loadAndShowFichas() {
        String url = URLPostHelper.Fichas.VER;
        repo.fetchFichas(url, new ApiService.ApiCallback<JSONArray>() {
            @Override
            public void onSuccess(JSONArray fichasJson) {
                runOnUiThread(() -> mostrarDialogoFichas(fichasJson));
            }
            @Override
            public void onError(Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(HomeEspecialista.this,
                                "No se pudo cargar fichas",
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    /** Muestra diálogo con la lista usando tu FichasAdapter */
    private void mostrarDialogoFichas(JSONArray fichasJson) {
        // Extraer solo el campo “nombre” de cada objeto
        List<String> lista = new ArrayList<>();
        for (int i = 0; i < fichasJson.length(); i++) {
            lista.add(fichasJson.optJSONObject(i).optString("nombre"));
        }

        View popup = getLayoutInflater()
                .inflate(R.layout.dialog_fichas, null);
        AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle("Fichas")
                .setView(popup)
                .setNegativeButton("Cerrar", (d,w)->d.dismiss())
                .create();

        ListView lv = popup.findViewById(R.id.listFichas);
        FichasAdapter adapter = new FichasAdapter(
                this,
                lista,
                colegioName,
                idColegio,
                docenteName,
                rol
        );
        lv.setAdapter(adapter);
        dlg.show();
    }

    /** 5. Programa reintento cuando haya red */
    private void scheduleSyncWork() {
        Constraints cons = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(SyncAttendanceWorker.class)
                .setConstraints(cons)
                .build();

        WorkManager.getInstance(this).enqueue(work);
    }

    // Atiende la respuesta al pedir permiso
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Una vez concedido, reintenta el flujo
                startAttendanceFlow();
            } else {
                Toast.makeText(this,
                        "Permiso de ubicación requerido para continuar",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}