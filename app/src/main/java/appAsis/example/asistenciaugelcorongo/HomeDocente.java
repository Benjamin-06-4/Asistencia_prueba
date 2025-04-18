package appAsis.example.asistenciaugelcorongo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import android.database.Cursor;
import android.provider.OpenableColumns;

public class HomeDocente extends AppCompatActivity {

    // Botón para registrar asistencia (se usará tanto por docente como director)
    private ImageButton btc_asistencia;

    // Parámetros de localización
    private double latitudActual, longitudActual;
    // Variables que se actualizan desde datacolegio (coordenadas de la I.E.)
    private String dataLatitud = "-8.81907000";  // valor de ejemplo, se actualiza desde datacolegio
    private String dataLongitud = "-77.46168000"; // valor de ejemplo, se actualiza desde datacolegio
    // Cadena que indicará si el docente está "DENTRO DE LA I.E." o "FUERA DE LA I.E."
    private String finalUbicacionEnvio = "DESCONOCIDO";

    // Control de registro (para docentes)
    private boolean llegadaRegistrada = false;
    private boolean salidaRegistrada = false;
    private Double metros_mostrar = 0.0;

    // Datos que pueden venir por intent (por ejemplo, desde el login)
    private String rol;       // "Docente" o "Director"
    private String colegio;   // Nombre del colegio
    private String idcolegio; // ID de la I.E.
    private String docente;   // Nombre o identificador del docente

    // Declarar constantes y variable para el PDF
    private static final int PICK_PDF_FILE = 101;
    private Uri pdfUri = null;
    private TextView tvNombreArchivoGlobal;

    // Variable para evidencia en foto
    private static final int REQUEST_IMAGE_EVIDENCIA = 102;
    private Bitmap evidenciaBitmap = null;  // Aquí guardaremos la imagen capturada
    private ImageView ivEvidenciaPreview; // Referencia al ImageView del popup



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Forzar modo claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_docente);

        // Obtener datos enviados por intent (si aplica)
        colegio = getIntent().getStringExtra("colegio");
        idcolegio = getIntent().getStringExtra("idcolegio");
        docente = getIntent().getStringExtra("docente");
        rol = getIntent().getStringExtra("turnos"); // "Docente" o "Director"

        btc_asistencia = findViewById(R.id.btc_asistencia);

        // Configurar el clic del botón "Asistencia"
        btc_asistencia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Se actualizan las coordenadas de la I.E.
                actualizarCoordenadasIE();

                // Se obtiene la ubicación actual (ver metodo getCoordenada)
                obtenerCoordenadasActual();

                // Una vez obtenida la ubicación, se muestra el popup
                // (La lógica dentro del popup verificará si la distancia es ≤ 100 metros)
                mostrarPopupAsistencia();
            }
        });

        ImageButton btc_sesionpdf = findViewById(R.id.btc_sesionpdf);
        btc_sesionpdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarPopupPdf();
            }
        });

        ImageButton btc_evidencia = findViewById(R.id.btc_evidencia);
        btc_evidencia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarPopupEvidencia();
            }
        });
    }

    // Metodo que actualiza las coordenadas de la I.E. leyendo del archivo de datacolegio
    private void actualizarCoordenadasIE() {
        try {
            InputStream isUbicacion = this.getResources().openRawResource(R.raw.datacolegio);
            BufferedReader reader = new BufferedReader(new InputStreamReader(isUbicacion));
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] partes = linea.split(";");
                // Se asume que:
                // partes[0] -> Nombre del colegio
                // partes[5] -> Latitud
                // partes[6] -> Longitud
                // partes[7] -> Nombre del docente (opcional para validación adicional)
                if (colegio != null && colegio.equalsIgnoreCase(partes[0].trim())) {
                    dataLatitud = partes[5].trim();
                    dataLongitud = partes[6].trim();
                    // Opcional: si deseas también validar que el docente sea el asignado
                    // if (docente.equalsIgnoreCase(partes[7].trim())) { ... }
                }
            }
            isUbicacion.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Metodo para obtener la ubicación actual utilizando LocationServices
    public void obtenerCoordenadasActual() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(HomeDocente.this,
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
                != PackageManager.PERMISSION_GRANTED) return;

        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        // Detener actualizaciones una vez obtenida la localización
                        LocationServices.getFusedLocationProviderClient(HomeDocente.this)
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

    /**
     * Calcula la distancia entre dos coordenadas utilizando la fórmula de Haversine.
     */
    public double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        double radioTierra = 6371.0; // en kilómetros
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        metros_mostrar = radioTierra * c * 1000;
        return radioTierra * c * 1000; // Convertir a metros
    }

    /**
     * Metodo para obtener la fecha y hora actual en la zona America/Lima.
     */
    private String obtenerFechaHoraActual() {
        TimeZone tz = TimeZone.getTimeZone("America/Lima");
        Calendar calendar = Calendar.getInstance(tz);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(tz);
        return sdf.format(calendar.getTime());
    }

    /**
     * Muestra el popup para registrar asistencia.
     * Se valida que la ubicación (finalUbicacionEnvio) sea "DENTRO DE LA I.E."
     * y se comprueba si se permite registrar la asistencia según la lógica de horarios y registros.
     * Para docentes: se permite marcar primero la llegada y luego la salida, dentro de ventanas horarias permitidas.
     * Para directores: se permite enviar o modificar la asistencia de sus docentes (se mostrará la opción de editar con una "tuerca").
     */
    private void mostrarPopupAsistencia() {
        // Verifica que la ubicación esté dentro de la I.E. (o muestra error si no lo está)
        if (!finalUbicacionEnvio.equals("DENTRO DE LA I.E.")) {
            Toast.makeText(this, "Estás lejos de la I.E: " + metros_mostrar + " ms", Toast.LENGTH_SHORT).show();
            return;
        }

        // Consulta el estado actual de los registros (entrada y salida)
        verificarAsistencias(new VerificacionCallback() {
            @Override
            public void onVerificacion(boolean entradaExiste, boolean salidaExiste) {
                // Actualiza las variables de instancia con el resultado de la verificación
                HomeDocente.this.llegadaRegistrada = entradaExiste;
                HomeDocente.this.salidaRegistrada = salidaExiste;

                LayoutInflater inflater = LayoutInflater.from(HomeDocente.this);
                View dialogView = inflater.inflate(R.layout.dialog_asistencia, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeDocente.this);
                builder.setView(dialogView);
                final AlertDialog dialog = builder.create();

                // Referencias a los componentes del diálogo
                TextView tvTitulo = dialogView.findViewById(R.id.tv_asistencia_titulo);
                final EditText etComentario = dialogView.findViewById(R.id.et_comentario);
                Button btnSi = dialogView.findViewById(R.id.btn_si);
                Button btnNo = dialogView.findViewById(R.id.btn_no);

                // Configura el título del diálogo según la verificación
                if (entradaExiste && salidaExiste) {
                    tvTitulo.setText("Registro completo por hoy");
                    etComentario.setEnabled(false);
                    btnSi.setEnabled(false);
                } else {
                    if (!entradaExiste) {
                        tvTitulo.setText("Registrar su hora de ingreso");
                    } else if (entradaExiste && !salidaExiste) {
                        tvTitulo.setText("Registrar su hora de salida");
                    }
                }

                // Configurar el clic del botón "Sí"
                btnSi.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String comentario = etComentario.getText().toString().trim();
                        String horaRegistro = obtenerFechaHoraActual();
                        String tipoRegistro = "";

                        // Se evalúa con base en las variables de instancia actualizadas
                        if (!HomeDocente.this.llegadaRegistrada) {
                            tipoRegistro = "Entrada";
                            HomeDocente.this.llegadaRegistrada = true;
                            Toast.makeText(HomeDocente.this, "Llegada registrada a las " + horaRegistro, Toast.LENGTH_LONG).show();
                        } else if (HomeDocente.this.llegadaRegistrada && !HomeDocente.this.salidaRegistrada) {
                            tipoRegistro = "Salida";
                            HomeDocente.this.salidaRegistrada = true;
                            Toast.makeText(HomeDocente.this, "Salida registrada a las " + horaRegistro, Toast.LENGTH_LONG).show();
                        }
                        // Enviar el registro si se determinó correctamente el tipo de registro
                        if (!tipoRegistro.isEmpty()) {
                            enviarAsistencia(comentario, horaRegistro, tipoRegistro);
                        }
                        dialog.dismiss();
                    }
                });

                // Botón "No": cierra el diálogo
                btnNo.setOnClickListener(v -> dialog.dismiss());

                dialog.show();
            }
        });
    }

    /**
     * Registra la asistencia del docente enviando los datos al servidor y
     * guardando localmente (por ejemplo, la hora, comentario, coordenadas, etc.).
     */
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
                    Toast.makeText(HomeDocente.this, "Error al enviar registro.", Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // Datos de la asistencia
                params.put("colegio", colegio);
                params.put("docente", docente);
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

    private void verificarAsistencias(final VerificacionCallback callback) {
        final int[] count = {0};
        final boolean[] entradaExiste = {false};
        final boolean[] salidaExiste = {false};
        String fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // URL para verificar la Entrada
        String urlEntrada = "https://ugelcorongo.pe/ugelasistencias_docente/model/auxasistencia/verAsistencia.php" +
                "?colegio=" + colegio +
                "&periodo=" + fecha +
                "&turno=ENTRADA";

        // URL para verificar la Salida
        String urlSalida = "https://ugelcorongo.pe/ugelasistencias_docente/model/auxasistencia/verAsistencia.php" +
                "?colegio=" + colegio +
                "&periodo=" + fecha +
                "&turno=SALIDA";

        // Solicitud para Entrada
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

        // Solicitud para Salida
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


    public interface VerificacionCallback {
        void onVerificacion(boolean entradaRegistrada, boolean salidaRegistrada);
    }

    private void mostrarPopupPdf() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_pdf, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        // Asigna la referencia al TextView global
        tvNombreArchivoGlobal = dialogView.findViewById(R.id.tvNombreArchivo);

        Button btnElegirArchivo = dialogView.findViewById(R.id.btnElegirArchivo);
        final EditText etComentario = dialogView.findViewById(R.id.etComentarioPdf);
        Button btnSubirPdf = dialogView.findViewById(R.id.btnSubirPdf);
        Button btnCancelarPdf = dialogView.findViewById(R.id.btnCancelarPdf);

        btnElegirArchivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                startActivityForResult(intent, PICK_PDF_FILE);
            }
        });

        btnSubirPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pdfUri == null) {
                    Toast.makeText(HomeDocente.this, "Seleccione un archivo PDF", Toast.LENGTH_SHORT).show();
                    return;
                }
                String comentario = etComentario.getText().toString().trim();
                subirPdf(pdfUri, comentario);
                dialog.dismiss();
            }
        });

        btnCancelarPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PDF_FILE && resultCode == RESULT_OK && data != null) {
            pdfUri = data.getData();
            byte[] pdfBytes = getFileDataFromUri(pdfUri);
            if (pdfBytes == null || pdfBytes.length == 0) {
                Log.e("UPLOAD_DATA", "pdfBytes is null or empty");
                Toast.makeText(this, "Error: archivo vacío o no leído", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("UPLOAD_DATA", "pdfBytes length: " + pdfBytes.length);
            }
            // Si tienes un TextView para mostrar el nombre, actualízalo también
            String displayName = getFileName(pdfUri);
            if (tvNombreArchivoGlobal != null) {
                tvNombreArchivoGlobal.setText(displayName);
            }
        }
        if (requestCode == REQUEST_IMAGE_EVIDENCIA && resultCode == RESULT_OK && data != null) {
            // Se obtiene el Bitmap de la foto (thumbnail)
            evidenciaBitmap = (Bitmap) data.getExtras().get("data");
            // Actualiza la vista previa en el ImageView del popup
            if (ivEvidenciaPreview != null) {
                ivEvidenciaPreview.setImageBitmap(evidenciaBitmap);
            }
        }
    }


    private void subirPdf(Uri pdfUri, final String comentario) {
        // Verificar conectividad a internet (opcional, pero recomendado)
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo == null || !netInfo.isConnected()) {
            Toast.makeText(this, "Sin conexión a internet", Toast.LENGTH_SHORT).show();
            return;
        }

        String urlUpload = "https://ugelcorongo.pe/ugelasistencias_docente/model/file/archivos/uploadFile.php";

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(
                Request.Method.POST,
                urlUpload,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        Toast.makeText(HomeDocente.this, "PDF subido correctamente", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(HomeDocente.this, "Error al subir PDF", Toast.LENGTH_SHORT).show();
                        Log.e("UPLOAD", "Error de Volley: " + error.toString());
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
                params.put("comentario", comentario);
                Log.d("UPLOAD", "Params: " + params.toString());
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                byte[] pdfBytes = getFileDataFromUri(pdfUri);
                String fileName = System.currentTimeMillis() + ".pdf";
                params.put("files", new DataPart(fileName, pdfBytes, "application/pdf"));
                Log.d("UPLOAD", "Byte Data: " + params.toString());
                return params;
            }
        };


        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(multipartRequest);
    }

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private byte[] getFileDataFromUri(Uri uri) {
        try {
            InputStream iStream = getContentResolver().openInputStream(uri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = iStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            return byteBuffer.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void mostrarPopupEvidencia() {
        // Infla el layout del popup (dialog_evidencia.xml)
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_evidencia, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        // Asigna la referencia del ImageView al variable global
        ivEvidenciaPreview = dialogView.findViewById(R.id.ivEvidenciaPreview);
        Button btnTomarFoto = dialogView.findViewById(R.id.btnTomarFoto);
        Button btnSubirEvidencia = dialogView.findViewById(R.id.btnSubirEvidencia);
        Button btnCancelarEvidencia = dialogView.findViewById(R.id.btnCancelarEvidencia);

        // Al pulsar "Tomar Foto", se lanza la cámara
        btnTomarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_EVIDENCIA);
                } else {
                    Toast.makeText(HomeDocente.this, "No se encontró una cámara", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Al pulsar "Subir Evidencia", se envía la foto al servidor (metodo enviarEvidencia)
        btnSubirEvidencia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (evidenciaBitmap == null) {
                    Toast.makeText(HomeDocente.this, "Toma una foto antes de subir", Toast.LENGTH_SHORT).show();
                    return;
                }
                enviarEvidencia(evidenciaBitmap);
                dialog.dismiss();
            }
        });

        // Cerrar el diálogo
        btnCancelarEvidencia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void enviarEvidencia(final Bitmap bitmapEvidencia) {
        // Verifica conexión a Internet si lo deseas
        String urlEvidencia = "https://ugelcorongo.pe/ugelasistencias_docente/model/file/img/uploadEvidencia.php";

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(
                Request.Method.POST,
                urlEvidencia,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        Toast.makeText(HomeDocente.this, "Evidencia subida correctamente", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(HomeDocente.this, "Error al subir evidencia", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // Envía los parámetros que necesites, por ejemplo:
                params.put("colegio", colegio);
                params.put("docente", docente);
                params.put("turno", rol);
                params.put("FK_idcolegio", idcolegio);
                // Puedes enviar otros datos opcionales, por ejemplo un comentario (si lo hubieses incluido en el diálogo)
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // Convierte el Bitmap a un arreglo de bytes (JPEG, calidad 80%)
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmapEvidencia.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
                byte[] evidenciaBytes = byteArrayOutputStream.toByteArray();
                // Crea un nombre de archivo único para la evidencia
                String fileName = System.currentTimeMillis() + ".jpg";
                // La clave "evidencia" (puedes usar "files" si lo prefieres, pero a diferencia del PDF, en este endpoint la usamos para evidencia)
                params.put("evidencia", new DataPart(fileName, evidenciaBytes, "image/jpeg"));
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(multipartRequest);
    }
}