package appAsis.example.asistenciaugelcorongo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Docentes_Director extends AppCompatActivity {

    // Variables recibidas del Intent
    private String colegio;
    private String idcolegio;
    private String docente;
    private String rol;             // "Docente" o "Director"
    private String dataLatitud = "-8.81907000";  // valor de ejemplo, se actualiza desde datacolegio
    private String dataLongitud = "-77.46168000"; // valor de ejemplo, se actualiza desde datacolegio
    private double latitudActual, longitudActual;
    private String finalUbicacionEnvio = "DESCONOCIDO";
    private boolean llegadaRegistrada = false;
    private boolean salidaRegistrada = false;
    private Double metros_mostrar = 0.0;

    // Elementos de la interfaz definidos en activity_docentes_director.xml
    private TextView txtCricketer;      // Usado para mostrar el nombre del colegio (puedes cambiarlo por el que prefieras)
    private TextView txt_director;  // Segundo TextView (en el layout, lo dejamos como ejemplo)
    private String str_docente = "";
    private LinearLayout container;     // Contenedor donde se agregarán las filas de docentes

    // Listas que contendrán los datos extraídos del archivo
    private List<String> listColegios = new ArrayList<>();
    private List<String> dataExcelcargada = new ArrayList<>();
    private List<String> listDocentes = new ArrayList<>();
    private List<String> listIdDocentes = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_docentes_director);
        // Forzar modo claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Inicializamos los elementos de la UI: asegúrate de que los IDs coincidan con los definidos en tu layout XML.
        txtCricketer = findViewById(R.id.txtCricketer);
        txt_director = findViewById(R.id.txt_director);
        container = findViewById(R.id.layout_list);

        // Obtener los datos que fueron enviados mediante el Intent
        colegio     = getIntent().getStringExtra("colegio");
        idcolegio   = getIntent().getStringExtra("idcolegio");
        docente     = getIntent().getStringExtra("docente");
        rol         = getIntent().getStringExtra("rol"); // "Docente" o "Director"

        // Actualizar la UI, por ejemplo, mostrar el nombre del colegio en el TextView txtCricketer
        if (colegio != null && !colegio.isEmpty()) {
            txtCricketer.setText(colegio);
            //txt_director.setText(docente);
        } else {
            txtCricketer.setText("Colegio no definido");
        }

        // Leer el archivo de docentes y guardar los datos en las listas
        leerDocentesDelArchivo();

        // Mostrar los docentes filtrados o todos, según corresponda, en el contenedor
        mostrarDocentes();
    }

    /**
     * Metodo para leer el archivo de docentes (R.raw.datadocentes).
     * Separa cada línea usando ";" y almacena los datos en las listas, considerando el filtro si se utiliza.
     */
    private void leerDocentesDelArchivo() {
        try {
            InputStream is = getResources().openRawResource(R.raw.datadocentes);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] parts = linea.split(";");

                // Construir el nombre completo del docente (columnas 2, 3 y 4)
                String auxDocenteLine = parts[2] + " " + parts[3] + " " + parts[4];

                if (colegio.equals(parts[0])) {
                    listColegios.add(parts[0]);
                    dataExcelcargada.add(parts[1]);
                    listDocentes.add(auxDocenteLine);
                    listIdDocentes.add(parts[5]);
                }
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarDocentes() {
        boolean noExiste = true;
        for (int i = 0; i < listColegios.size(); i++) {
            if (listColegios.get(i).equals(colegio) && rol.equals("Director")) {
                noExiste = false;
                View docenteView = getLayoutInflater().inflate(R.layout.row_add_docentes_director, container, false);

                // Asignar el nombre del docente y su ID
                EditText editName = docenteView.findViewById(R.id.edit_docente_name);
                editName.setText(listDocentes.get(i));
                EditText txtIdDocente = docenteView.findViewById(R.id.txtiddocente);
                txtIdDocente.setText(listIdDocentes.get(i));

                // Se mantiene el TextView de hora tal como está en el layout
                TextView txtHora = docenteView.findViewById(R.id.txtHora);

                // Configurar el botón btnReloj para registrar asistencia
                ImageButton btnReloj = docenteView.findViewById(R.id.btnReloj);
                btnReloj.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        str_docente = editName.getText().toString();
                        // Realiza las tres acciones en serie:
                        actualizarCoordenadasIE();    // Actualizar coordenadas de la I.E.
                        obtenerCoordenadasActual();   // Obtener la ubicación actual
                        // Verificar la asistencia consultando el servidor y luego mostrar el popup:
                        verificarAsistencias(new VerificacionCallback() {
                            @Override
                            public void onVerificacion(boolean entradaRegistrada, boolean salidaRegistrada) {
                                llegadaRegistrada = entradaRegistrada;
                                salidaRegistrada = salidaRegistrada;
                                mostrarPopupAsistencia();
                            }
                        });
                    }
                });

                container.addView(docenteView);
            }
        }
        if (noExiste) {
            TextView tv = new TextView(this);
            tv.setText("No se encontraron docentes para este colegio.");
            container.addView(tv);
        }
    }

    // --- Metodos de asistencia y ubicación ---

    private void actualizarCoordenadasIE() {
        try {
            InputStream isUbicacion = getResources().openRawResource(R.raw.datacolegio);
            BufferedReader reader = new BufferedReader(new InputStreamReader(isUbicacion));
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] partes = linea.split(";");
                // Se asume que partes[0] es el nombre del colegio
                if (colegio != null && colegio.equalsIgnoreCase(partes[0].trim())) {
                    dataLatitud = partes[5].trim();
                    dataLongitud = partes[6].trim();
                }
            }
            isUbicacion.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void obtenerCoordenadasActual() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Docentes_Director.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            getCoordenada();
        }
    }

    public void getCoordenada() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            return;

        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(Docentes_Director.this)
                                .removeLocationUpdates(this);
                        if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                            int latestIndex = locationResult.getLocations().size() - 1;
                            latitudActual = locationResult.getLocations().get(latestIndex).getLatitude();
                            longitudActual = locationResult.getLocations().get(latestIndex).getLongitude();

                            double metros = calcularDistancia(latitudActual, longitudActual,
                                    Double.parseDouble(dataLatitud), Double.parseDouble(dataLongitud));
                            if (metros <= 150) {
                                finalUbicacionEnvio = "DENTRO DE LA I.E.";
                            } else {
                                finalUbicacionEnvio = "FUERA DE LA I.E.";
                            }
                        }
                    }
                }, Looper.myLooper());
    }

    public double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        double radioTierra = 6371.0; // en kilómetros
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        metros_mostrar = radioTierra * c * 1000;
        return metros_mostrar;
    }

    private void mostrarPopupAsistencia() {
        if (!"DENTRO DE LA I.E.".equals(finalUbicacionEnvio)) {
            Toast.makeText(this, "Estás lejos de la I.E: " + dataLatitud + " log: " + longitudActual, Toast.LENGTH_SHORT).show();
            return;
        }

        // Se muestra el diálogo para registrar asistencia basándose en la verificación
        LayoutInflater inflater = LayoutInflater.from(Docentes_Director.this);
        View dialogView = inflater.inflate(R.layout.dialog_asistencia, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(Docentes_Director.this);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        TextView tvTitulo = dialogView.findViewById(R.id.tv_asistencia_titulo);
        final EditText etComentario = dialogView.findViewById(R.id.et_comentario);
        TextView btnSi = dialogView.findViewById(R.id.btn_si);
        TextView btnNo = dialogView.findViewById(R.id.btn_no);

        if (llegadaRegistrada && salidaRegistrada) {
            tvTitulo.setText("Registro completo por hoy");
            etComentario.setEnabled(false);
            btnSi.setEnabled(false);
        } else {
            if (!llegadaRegistrada) {
                tvTitulo.setText("Registrar su hora de ingreso");
            } else if (llegadaRegistrada && !salidaRegistrada) {
                tvTitulo.setText("Registrar su hora de salida");
            }
        }

        btnSi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comentario = etComentario.getText().toString().trim();
                String horaRegistro = obtenerFechaHoraActual();
                String tipoRegistro = "";
                if (!llegadaRegistrada) {
                    tipoRegistro = "Entrada";
                    llegadaRegistrada = true;
                    Toast.makeText(Docentes_Director.this, "Llegada registrada a las " + horaRegistro, Toast.LENGTH_LONG).show();
                } else if (llegadaRegistrada && !salidaRegistrada) {
                    tipoRegistro = "Salida";
                    salidaRegistrada = true;
                    Toast.makeText(Docentes_Director.this, "Salida registrada a las " + horaRegistro, Toast.LENGTH_LONG).show();
                }
                if (!tipoRegistro.isEmpty()) {
                    enviarAsistencia(comentario, horaRegistro, tipoRegistro);
                }
                dialog.dismiss();
            }
        });

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private String obtenerFechaHoraActual() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void enviarAsistencia(String comentario, String horaRegistro, String tipoRegistro) {
        String url = "https://ugelcorongo.pe/ugelasistencias_docente/sesion.php";

        // Información del dispositivo
        String ModelInfodevice = Build.SERIAL;
        String IdInfodevice = Build.ID;
        String ManufactInfodevice = Build.MANUFACTURER;
        String BrandInfodevice = Build.BRAND;
        String TypeInfodevice = Build.TYPE;
        String UserInfodevice = Build.USER;
        String BaseInfodevice = String.valueOf(Build.VERSION_CODES.BASE);
        String SdkInfodevice = Build.VERSION.SDK;  // Considera usar Build.VERSION.SDK_INT
        String BoardInfodevice = Build.BOARD;
        String HostInfodevice = Build.HOST;
        String FingeprintInfodevice = Build.FINGERPRINT;
        String VCodeInfodevice = Build.VERSION.RELEASE;

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    // Toast.makeText(HomeDocente.this, "Registro enviado correctamente.", Toast.LENGTH_LONG).show();
                },
                error -> {
                    Toast.makeText(Docentes_Director.this, "Error al enviar registro.", Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // Datos de la asistencia
                params.put("colegio", colegio);
                params.put("docente", str_docente);
                params.put("horaRegistro", horaRegistro);
                params.put("tipoRegistro", tipoRegistro);
                params.put("idcolegio", idcolegio);
                params.put("comentario", comentario);

                // Datos del dispositivo
                params.put("ModelInfodevice", ModelInfodevice);
                params.put("IdInfodevice", IdInfodevice);
                params.put("ManufactInfodevice", ManufactInfodevice);
                params.put("BrandInfodevice", BrandInfodevice);
                params.put("TypeInfodevice", TypeInfodevice);
                params.put("UserInfodevice", UserInfodevice);
                params.put("BaseInfodevice", BaseInfodevice);
                params.put("SdkInfodevice", SdkInfodevice);
                params.put("BoardInfodevice", BoardInfodevice);
                params.put("HostInfodevice", HostInfodevice);
                params.put("FingeprintInfodevice", FingeprintInfodevice);
                params.put("VCodeInfodevice", VCodeInfodevice);

                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(postRequest);
    }

    // --------------------------------------------------------------------------
    // METODO: verificarAsistencias (igual que el de btc_asistencia)
    // --------------------------------------------------------------------------
    private void verificarAsistencias(final VerificacionCallback callback) {
        final int[] count = {0};
        final boolean[] entradaExiste = {false};
        final boolean[] salidaExiste = {false};
        String fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        String urlEntrada = "https://ugelcorongo.pe/ugelasistencias_docente/model/auxasistencia/verAsistenciaDocentesDirector.php" +
                "?colegio=" + colegio +
                "&periodo=" + fecha +
                "&docente=" + str_docente +
                "&turno=ENTRADA";

        String urlSalida = "https://ugelcorongo.pe/ugelasistencias_docente/model/auxasistencia/verAsistenciaDocentesDirector.php" +
                "?colegio=" + colegio +
                "&periodo=" + fecha +
                "&docente=" + str_docente +
                "&turno=SALIDA";

        StringRequest requestEntrada = new StringRequest(Request.Method.GET, urlEntrada,
                response -> {
                    if (response != null && !response.equals("[]")) {
                        entradaExiste[0] = true;
                    }
                    count[0]++;
                    if (count[0] == 2) {
                        callback.onVerificacion(entradaExiste[0], salidaExiste[0]);
                    }
                },
                error -> {
                    count[0]++;
                    if (count[0] == 2) {
                        callback.onVerificacion(entradaExiste[0], salidaExiste[0]);
                    }
                }
        );

        StringRequest requestSalida = new StringRequest(Request.Method.GET, urlSalida,
                response -> {
                    if (response != null && !response.equals("[]")) {
                        salidaExiste[0] = true;
                    }
                    count[0]++;
                    if (count[0] == 2) {
                        callback.onVerificacion(entradaExiste[0], salidaExiste[0]);
                    }
                },
                error -> {
                    count[0]++;
                    if (count[0] == 2) {
                        callback.onVerificacion(entradaExiste[0], salidaExiste[0]);
                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(requestEntrada);
        queue.add(requestSalida);
    }

    // --------------------------------------------------------------------------
    // INTERFAZ DE CALLBACK (la misma que usas en btc_asistencia)
    // --------------------------------------------------------------------------
    public interface VerificacionCallback {
        void onVerificacion(boolean entradaRegistrada, boolean salidaRegistrada);
    }
}