package appAsis.example.asistenciaugelcorongo;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatDelegate;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

// Importa otras librerías necesarias (Volley, etc.)
public class HomeEspecialista extends BaseActivity {

    private String colegio;
    private String docente;
    private String idcolegio;
    // Variables para coordenadas:
    // latEnvio/ lonEnvio se definen cuando se presiona el botón (no se actualizan automáticamente aquí)
    private double latEnvio = 0.0, lonEnvio = 0.0;
    // latActual/ lonActual se utilizarán para enviar la ubicación actual (deben actualizarse en getCoordenada())

    private ImageButton btc_asistencias_especialista;
    private ImageButton btc_evidencia;

    // Handler para enviar coordenadas cada 5 segundos (solo para Especialista)
    private Handler coordHandler = new Handler();
    private Runnable coordRunnable = new Runnable() {
        @Override
        public void run() {
            if(isOnline()){
                Log.d("COORD_RUN", "CoordRunnable ejecutado");
                Toast.makeText(HomeEspecialista.this, "run: " , Toast.LENGTH_LONG).show();
                obtenerCoordenadasActual();
                enviarCoordenadas();
            }
            coordHandler.postDelayed(this, 5000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Forzar modo claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_especialista);

        // Recupera datos del Intent
        colegio = getIntent().getStringExtra("colegio");
        idcolegio = getIntent().getStringExtra("idcolegio");
        docente = getIntent().getStringExtra("docente");
        rol = getIntent().getStringExtra("rol");

        btc_asistencias_especialista = findViewById(R.id.btc_asistencias_especialista);

        // Al hacer clic en el botón de asistencia, se actualizan las coordenadas de la I.E.,
        // se obtiene la ubicación actual y, posteriormente, se muestra el popup de registro.
        btc_asistencias_especialista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actualizarCoordenadasIEBase();
                obtenerCoordenadasActual();
                // Se llama al popup; en escenarios reales se podría invocar desde el callback
                // de ubicación para asegurarse de que se haya obtenido la localización.
                //mostrarPopupAsistencia();
                Toast.makeText(HomeEspecialista.this, "la: " + GlobalData.latActual + " lo:" + isOnline(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // La sincronización de archivos offline se gestiona en BaseActivity
        // Para Especialista, iniciar el envío periódico de coordenadas
        if("Especialista".equalsIgnoreCase(rol)){
            coordHandler.post(coordRunnable);
            // Se llama a actualizarCoordenadasIEBase() para actualizar datacolegio.txt, y a obtenerCoordenadasActual() para actualizar latActual y lonActual.
            actualizarCoordenadasIEBase();
            obtenerCoordenadasActual();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        coordHandler.removeCallbacks(coordRunnable);
    }
    /**
     * Envío de asistencia.
     * Si no hay conexión, guarda el registro localmente usando OfflineStorageManager.
     */
    private void enviarAsistencia(String comentario, String horaRegistro, String tipoRegistro, String tardanza) {
        String url = "https://ugelcorongo.pe/ugelasistencias_docente/sesion.php";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    // Registro enviado correctamente.
                },
                error -> {
                    Toast.makeText(HomeEspecialista.this, "Sin conexión. Guardando asistencia localmente.", Toast.LENGTH_LONG).show();
                    // Se construye la cadena de coordenadas:
                    String coordenadas = latEnvio + "," + lonEnvio + "|" + GlobalData.latActual + "," + GlobalData.lonActual + "_sinconexion";
                    OfflineStorageManager.saveAssistanceRecord(HomeEspecialista.this, colegio, docente, comentario, horaRegistro, tipoRegistro, tardanza, rol, coordenadas);
                });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(postRequest);
    }

    /**
     * Envío de evidencia (imagen).
     */
    private void enviarEvidencia(final Bitmap bitmapEvidencia) {
        if (!isOnline()){
            String coordenadas = latEnvio + "," + lonEnvio + "|" + GlobalData.latActual + "," + GlobalData.lonActual + "_sinconexion";
            OfflineStorageManager.saveImageOffline(HomeEspecialista.this, colegio, docente, rol, idcolegio, bitmapEvidencia, coordenadas);
            Toast.makeText(HomeEspecialista.this, "Sin conexión. Evidencia guardada localmente.", Toast.LENGTH_SHORT).show();
            return;
        }
        String urlEvidencia = "https://ugelcorongo.pe/ugelasistencias_docente/model/file/img/uploadEvidencia.php";
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(
                Request.Method.POST,
                urlEvidencia,
                response -> {
                    Toast.makeText(HomeEspecialista.this, "Evidencia subida correctamente.", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Toast.makeText(HomeEspecialista.this, "Error al subir evidencia. Guardando localmente.", Toast.LENGTH_SHORT).show();
                    String coordenadas = latEnvio + "," + lonEnvio + "|" + GlobalData.latActual + "," + GlobalData.lonActual + "_sinconexion";
                    OfflineStorageManager.saveImageOffline(HomeEspecialista.this, colegio, docente, rol, idcolegio, bitmapEvidencia, coordenadas);
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("colegio", colegio);
                params.put("docente", docente);
                params.put("turno", rol);
                params.put("FK_idcolegio", idcolegio);
                return params;
            }
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmapEvidencia.compress(Bitmap.CompressFormat.JPEG, 80, bos);
                byte[] evidenciaBytes = bos.toByteArray();
                String fileName = System.currentTimeMillis() + ".jpg";
                params.put("evidencia", new DataPart(fileName, evidenciaBytes, "image/jpeg"));
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(multipartRequest);
    }

    /**
     * Envía las coordenadas actuales a la URL correspondiente.
     */
    private void enviarCoordenadas() {
        Log.d("SEND_COORDS", "En enviarCoordenadas()");
        String urlCoords = "https://ugelcorongo.pe/ugelasistencias_docente/model/rastreo/actualizar-coordenadas.php";
        JSONObject params = new JSONObject();
        try {
            params.put("latitude", GlobalData.latActual);
            params.put("longitude", GlobalData.lonActual);
            params.put("usuario", docente);
            params.put("rol", rol);
            Log.d("SEND_COORDS", "Parametros: " + params.toString());
        } catch (Exception e) {
            Log.e("SEND_COORDS", "Error armando JSON", e);
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, urlCoords, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("SEND_COORDS", "Respuesta: " + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("SEND_COORDS", "Error: ", error);
                    }
                });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}