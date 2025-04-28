package appAsis.example.asistenciaugelcorongo;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatDelegate;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

// Importa otras librerías necesarias (Volley, etc.)
public class HomeEspecialista extends BaseActivity {

    // Botones de la interfaz
    private ImageButton btc_asistencia;
    private ImageButton btc_evidencia;

    // Variables para determinar el registro de asistencia (Entrada/Salida)
    private boolean llegadaRegistrada = false;
    private boolean salidaRegistrada = false;
    private String horaLlegada = "";
    private String horaSalida = "";

    // Variables para la institución educativa de referencia, a partir de datacolegiofichas.txt
    // Se almacenará la latitud y longitud de la institución que se encuentre dentro del rango (≤ 50 m)
    private double refLatitud = 0.0;
    private double refLongitud = 0.0;
    // Opcional: nombre de la institución encontrada
    private String institucionEncontrada = "";

    // Handler para actualizar coordenadas cada 5 segundos cuando la app esté en primer plano
    private Handler coordHandler = new Handler();
    private Runnable coordRunnable = new Runnable() {
        @Override
        public void run() {
            if (isOnline()) {
                Log.d("COORD_RUN", "Especialista: CoordRunnable ejecutado");
                obtenerCoordenadasActual();
                enviarCoordenadas();
            }
            coordHandler.postDelayed(this, 5000);
        }
    };

    // Variable para manejar evidencia (foto)
    private Bitmap evidenciaBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Forzar modo claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_especialista);

        // Se recuperan las variables heredadas en BaseActivity: colegio, docente, rol, idcolegio.
        btc_asistencia = findViewById(R.id.btc_asistencias_especialista);
        btc_evidencia = findViewById(R.id.btc_evidencias_especialistas);

        // Configuración del botón de asistencia
        btc_asistencia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 1) Actualizar las coordenadas de la ficha base y obtener las actuales
                actualizarCoordenadasIEBase();
                loadRefCoordinates();
                obtenerCoordenadasActual();

                // Calcular la distancia desde la posición actual hacia la institución educativa encontrada
                double currentLat = GlobalData.latActual;
                double currentLon = GlobalData.lonActual;
                double distance = calcularDistancia(currentLat, currentLon, refLatitud, refLongitud);

                // Para el especialista la regla es que la distancia debe ser ≤ 50 metros
                if (distance > 50) {
                    Toast.makeText(HomeEspecialista.this,
                            "Estás lejos de la I.E. (" + institucionEncontrada + "): " + distance + " metros",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // Mostrar popup para registrar la asistencia si se cumple la condición
                mostrarPopupAsistencia();
            }
        });

        // Configuración del botón de evidencia
        btc_evidencia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarPopupEvidencia();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // La gestión de la inactividad y de la sesión se maneja en BaseActivity.
        if ("Especialista".equalsIgnoreCase(rol)) {
            coordHandler.post(coordRunnable);
            actualizarCoordenadasIEBase();
            obtenerCoordenadasActual();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        coordHandler.removeCallbacks(coordRunnable);
    }

    // ---------------------------------------------------------------
    // Métodos de georreferenciación y cálculo de distancia
    // ---------------------------------------------------------------

    /**
     * Calcula la distancia (en metros) entre dos pares de coordenadas usando la fórmula de Haversine.
     */
    public double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        double radioTierra = 6371.0;  // Radio de la Tierra en kilómetros
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return radioTierra * c * 1000;  // Convertir a metros
    }

    /**
     * Lee el archivo "datacolegiofichas.txt", que contiene varias instituciones educativas,
     * y determina en cuál se encuentra el especialista (regla: distancia ≤ 50 m).
     * Se selecciona la institución que cumpla con la condición y tenga la menor distancia.
     */
    private void loadRefCoordinates() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(openFileInput("datacolegiofichas.txt")));
            String line;
            double minDistance = Double.MAX_VALUE;
            boolean found = false;
            while ((line = reader.readLine()) != null) {
                String[] partes = line.split(";");
                if (partes.length >= 4) { // Se aseguran los campos: [2]=latitud, [3]=longitud
                    double lat = Double.parseDouble(partes[2].trim());
                    double lon = Double.parseDouble(partes[3].trim());
                    double distance = calcularDistancia(GlobalData.latActual, GlobalData.lonActual, lat, lon);
                    // Si la distancia es ≤ 50 m y es la más cercana encontrada hasta ahora
                    if (distance <= 50 && distance < minDistance) {
                        minDistance = distance;
                        refLatitud = lat;
                        refLongitud = lon;
                        institucionEncontrada = partes[0].trim();  // El nombre (índice 0)
                        found = true;
                    }
                }
            }
            reader.close();
            if (!found) {
                // Si ninguna institución se encuentra dentro de 50 m, se asigna por defecto la posición actual
                refLatitud = GlobalData.latActual;
                refLongitud = GlobalData.lonActual;
                institucionEncontrada = "Desconocida";
            }
        } catch (Exception e) {
            e.printStackTrace();
            // En caso de error, se asigna la posición actual como referencia
            refLatitud = GlobalData.latActual;
            refLongitud = GlobalData.lonActual;
            institucionEncontrada = "Desconocida";
        }
    }

    // ---------------------------------------------------------------
    // Métodos de registro de asistencia
    // ---------------------------------------------------------------

    /**
     * Muestra un popup para registrar la asistencia.
     * Para el especialista, si aún no se ha registrado la "Entrada", se toma como Entrada; si ya se registró, se toma como Salida.
     */
    private void mostrarPopupAsistencia() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_asistencia, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        TextView tvTitulo = dialogView.findViewById(R.id.tv_asistencia_titulo);
        final EditText etComentario = dialogView.findViewById(R.id.et_comentario);
        Button btnSi = dialogView.findViewById(R.id.btn_si);
        Button btnNo = dialogView.findViewById(R.id.btn_no);

        String tipoRegistro = "";
        int tardanzaMinutos = 0; // Para el especialista se omite el cálculo de tardanza

        if (!llegadaRegistrada) {
            tipoRegistro = "Entrada";
        } else if (!salidaRegistrada) {
            tipoRegistro = "Salida";
        }

        if (tipoRegistro.equals("Entrada")) {
            tvTitulo.setText("Registrar su hora de ingreso");
        } else if (tipoRegistro.equals("Salida")) {
            tvTitulo.setText("Registrar su hora de salida");
        }

        final String finalTipoRegistro = tipoRegistro;
        final int finalTardanzaMinutos = tardanzaMinutos;

        btnSi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comentario = etComentario.getText().toString().trim();
                String horaRegistro = obtenerFechaHoraActual(); // Método heredado de BaseActivity
                enviarAsistencia(comentario, horaRegistro, finalTipoRegistro, String.valueOf(finalTardanzaMinutos));
                if (finalTipoRegistro.equals("Entrada")) {
                    llegadaRegistrada = true;
                    horaLlegada = horaRegistro;
                } else if (finalTipoRegistro.equals("Salida")) {
                    salidaRegistrada = true;
                    horaSalida = horaRegistro;
                }
                Toast.makeText(HomeEspecialista.this,
                        finalTipoRegistro + " registrada a las " + horaRegistro,
                        Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { dialog.dismiss(); }
        });

        dialog.show();
    }

    /**
     * Envía los datos de asistencia al servidor usando Volley.
     * En caso de error (por ejemplo, sin conexión), se guarda el registro localmente
     * llamando al método correspondiente de OfflineStorageManager.
     */
    private void enviarAsistencia(String comentario, String horaRegistro, String tipoRegistro, String tardanza) {
        String url = "https://ugelcorongo.pe/ugelasistencias_docente/sesion.php";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Registro enviado correctamente.
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(HomeEspecialista.this,
                                "Sin conexión. Guardando asistencia localmente.",
                                Toast.LENGTH_LONG).show();
                        String coordenadas = GlobalData.latActual + "," + GlobalData.lonActual + "|" +
                                GlobalData.latActual + "," + GlobalData.lonActual + "_sinconexion";
                        OfflineStorageManager.saveAssistanceRecord(
                                HomeEspecialista.this,
                                colegio,
                                docente,
                                comentario,
                                horaRegistro,
                                tipoRegistro,
                                tardanza,
                                rol,
                                coordenadas
                        );
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("colegio", colegio);
                params.put("docente", docente);
                params.put("horaRegistro", horaRegistro);
                params.put("tipoRegistro", tipoRegistro);
                params.put("tardanza", tardanza);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(postRequest);
    }

    // ---------------------------------------------------------------
    // Métodos para la evidencia (foto)
    // ---------------------------------------------------------------

    /**
     * Muestra un popup para que el especialista capture y suba evidencia (foto).
     */
    private void mostrarPopupEvidencia() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_evidencia, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        final ImageView ivEvidenciaPreview = dialogView.findViewById(R.id.ivEvidenciaPreview);
        Button btnTomarFoto = dialogView.findViewById(R.id.btnTomarFoto);
        Button btnSubirEvidencia = dialogView.findViewById(R.id.btnSubirEvidencia);
        Button btnCancelarEvidencia = dialogView.findViewById(R.id.btnCancelarEvidencia);

        btnTomarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, 102);
                } else {
                    Toast.makeText(HomeEspecialista.this, "No se encontró una cámara", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSubirEvidencia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (evidenciaBitmap == null) {
                    Toast.makeText(HomeEspecialista.this, "Toma una foto antes de subir", Toast.LENGTH_SHORT).show();
                    return;
                }
                enviarEvidencia(evidenciaBitmap);
                dialog.dismiss();
            }
        });

        btnCancelarEvidencia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { dialog.dismiss(); }
        });

        dialog.show();
    }

    /**
     * Envía la evidencia (imagen) al servidor.
     * Si no hay conexión, se guarda la imagen localmente llamando a OfflineStorageManager.
     */
    private void enviarEvidencia(final Bitmap bitmapEvidencia) {
        if (!isOnline()) {
            String coordenadas = GlobalData.latActual + "," + GlobalData.lonActual + "|" +
                    GlobalData.latActual + "," + GlobalData.lonActual + "_sinconexion";
            OfflineStorageManager.saveImageOffline(HomeEspecialista.this, colegio, docente, rol, idcolegio, bitmapEvidencia, coordenadas);
            Toast.makeText(HomeEspecialista.this, "Sin conexión. Evidencia guardada localmente.", Toast.LENGTH_SHORT).show();
            return;
        }
        String urlEvidencia = "https://ugelcorongo.pe/ugelasistencias_docente/model/file/img/uploadEvidencia.php";
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(
                Request.Method.POST,
                urlEvidencia,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        Toast.makeText(HomeEspecialista.this, "Evidencia subida correctamente.", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(HomeEspecialista.this, "Error al subir evidencia. Guardando localmente.", Toast.LENGTH_SHORT).show();
                        String coordenadas = GlobalData.latActual + "," + GlobalData.lonActual + "|" +
                                GlobalData.latActual + "," + GlobalData.lonActual + "_sinconexion";
                        OfflineStorageManager.saveImageOffline(HomeEspecialista.this, colegio, docente, rol, idcolegio, bitmapEvidencia, coordenadas);
                    }
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

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(multipartRequest);
    }

    // ---------------------------------------------------------------
    // Otros métodos
    // ---------------------------------------------------------------

    /**
     * Envía las coordenadas actuales del especialista al servidor.
     */
    private void enviarCoordenadas() {
        String urlCoords = "https://ugelcorongo.pe/ugelasistencias_docente/model/rastreo/actualizar-coordenadas.php";
        JSONObject params = new JSONObject();
        try {
            params.put("latitude", GlobalData.latActual);
            params.put("longitude", GlobalData.lonActual);
            params.put("usuario", docente);
            params.put("rol", rol);
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

    /**
     * Devuelve la fecha y hora actual en formato "yyyy-MM-dd HH:mm:ss"
     * usando la zona horaria America/Lima.
     */
    private String obtenerFechaHoraActual() {
        TimeZone tz = TimeZone.getTimeZone("America/Lima");
        Calendar calendar = Calendar.getInstance(tz);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(tz);
        return sdf.format(calendar.getTime());
    }

    /**
     * Procesa el resultado de la actividad para la captura de imagen.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 102 && resultCode == RESULT_OK && data != null) {
            evidenciaBitmap = (Bitmap) data.getExtras().get("data");
        }
    }
}