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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import android.database.Cursor;
import android.provider.OpenableColumns;

public class HomeDocente extends AppCompatActivity {

    // Vistas y botones
    private ImageButton btc_asistencia;

    // Variables de georreferencia y localización
    private double latitudActual, longitudActual;
    private String dataLatitud = "-8.81907000";  // Se actualizará con la lectura de datacolegio
    private String dataLongitud = "-77.46168000"; // Se actualizará con la lectura de datacolegio
    private String finalUbicacionEnvio = "DESCONOCIDO"; // Se actualizará a "DENTRO DE LA I.E." o "FUERA DE LA I.E."
    private Double metros_mostrar = 0.0; // Distancia calculada en metros entre la ubicación actual y la I.E.

    // Datos del docente que se reciben (por ejemplo, desde el login)
    private String idcolegio; // ID de la I.E.
    private String colegio;   // Ejemplo: "UNIDAD DE GESTION EDUCATIVA LOCAL CORONGO"
    private String docente;   // Ejemplo: "ITURRIA HUAMAN ROBERT ALBERTO"
    private String rol;       // "Docente" o "Director"

    // Lista global de horarios para el docente, obtenida de la URL o localmente
    private ArrayList<HorarioDocente> horariosDocente = new ArrayList<>();

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_docente);

        btc_asistencia = findViewById(R.id.btc_asistencia);
        // Los valores de "colegio" y "docente" se reciben, por ejemplo, desde el Intent:
        colegio = getIntent().getStringExtra("colegio");
        idcolegio = getIntent().getStringExtra("idcolegio");
        docente = getIntent().getStringExtra("docente");
        rol = getIntent().getStringExtra("turnos"); // "Docente" o "Director"

        // Primero se intenta obtener los horarios vía conexión (verHorarios.php),
        // sino se recurre a cargarlos desde el archivo local (assets/datahorarios.txt)
        obtenerHorarios();

        // Al hacer clic en el botón de asistencia, se actualizan las coordenadas de la I.E.,
        // se obtiene la ubicación actual y, posteriormente, se muestra el popup de registro.
        btc_asistencia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actualizarCoordenadasIE();
                obtenerCoordenadasActual();
                // Se llama al popup; en escenarios reales se podría invocar desde el callback
                // de ubicación para asegurarse de que se haya obtenido la localización.
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

    /**
     * Intenta obtener los horarios del docente desde el servidor.
     * Si falla (por ej., sin conexión), se cargan desde assets/datahorarios.txt.
     */
    private void obtenerHorarios() {
        String urlHorarios = "https://ugelcorongo.pe/ugelasistencias_docente/model/auxasistencia/verHorarios.php" +
                "?colegio=" + colegio +
                "&docente=" + docente;
        StringRequest request = new StringRequest(Request.Method.GET, urlHorarios,
                response -> parseHorarios(response),
                error -> cargarHorariosLocales());
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    /**
     * Parsea la respuesta (con líneas separadas por "\n" y campos separados por ";")
     * y carga la lista de horarios.
     */
    private void parseHorarios(String response) {
        horariosDocente.clear();
        String[] lines = response.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.split(";");
            if (parts.length >= 6) {
                // Filtra por colegio y docente
                if (parts[0].trim().equalsIgnoreCase(colegio.trim()) &&
                        parts[1].trim().equalsIgnoreCase(docente.trim())) {
                    HorarioDocente h = new HorarioDocente();
                    h.colegio = parts[0].trim();
                    h.docente = parts[1].trim();
                    h.llegadaPermitida = parts[2].trim();
                    h.llegada = parts[3].trim();
                    h.salida = parts[4].trim();
                    h.salidaPermitida = parts[5].trim();
                    horariosDocente.add(h);
                }
            }
        }
    }

    /**
     * Carga los horarios desde el archivo local assets/datahorarios.txt.
     */
    private void cargarHorariosLocales() {
        horariosDocente.clear();
        try {
            InputStream is = getAssets().open("datahorarios.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(";");
                if (parts.length >= 6) {
                    if (parts[0].trim().equalsIgnoreCase(colegio.trim()) &&
                            parts[1].trim().equalsIgnoreCase(docente.trim())) {
                        HorarioDocente h = new HorarioDocente();
                        h.colegio = parts[0].trim();
                        h.docente = parts[1].trim();
                        h.llegadaPermitida = parts[2].trim();
                        h.llegada = parts[3].trim();
                        h.salida = parts[4].trim();
                        h.salidaPermitida = parts[5].trim();
                        horariosDocente.add(h);
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Muestra el diálogo para registrar la asistencia.
     * Se recorre la lista de horarios para determinar en qué turno se debe registrar
     * (Entrada o Salida) según la hora actual.
     */
    private void mostrarPopupAsistencia() {
        // Verifica que la ubicación esté dentro de la I.E.
        if (!finalUbicacionEnvio.equals("DENTRO DE LA I.E.")) {
            Toast.makeText(this, "Estás lejos de la I.E: " + metros_mostrar + " ms", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener la hora actual en formato "HH:mm"
        String currentTimeStr = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Date currentTime;
        try {
            currentTime = sdf.parse(currentTimeStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        // Recorremos la lista de horarios (asumidos ordenados cronológicamente)
        HorarioDocente horarioSeleccionado = null;
        String tipoRegistro = "";
        int tardanzaMinutos = 0;

        for (HorarioDocente h : horariosDocente) {
            try {
                Date llegadaPermitida = sdf.parse(h.llegadaPermitida);
                Date horaLlegada = sdf.parse(h.llegada);
                Date horaSalida = sdf.parse(h.salida);
                Date salidaPermitida = sdf.parse(h.salidaPermitida);

                // Si aún no se ha registrado la entrada en este turno:
                if (!h.llegadaRegistrada) {
                    if (currentTime.compareTo(llegadaPermitida) >= 0 && currentTime.before(horaSalida)) {
                        tardanzaMinutos = currentTime.before(horaLlegada) ? 0 : calcularTardanza(currentTimeStr, h.llegada);
                        tipoRegistro = "Entrada";
                        horarioSeleccionado = h;
                        break;
                    }
                }
                // Si ya se registró la entrada y falta la salida:
                if (h.llegadaRegistrada && !h.salidaRegistrada) {
                    if (currentTime.compareTo(horaSalida) >= 0 && currentTime.compareTo(salidaPermitida) <= 0) {
                        tipoRegistro = "Salida";
                        horarioSeleccionado = h;
                        break;
                    } else if (currentTime.after(salidaPermitida)) {
                        enviarMiselanea("No registra salida para el turno que inicia a las " + h.llegada +
                                ". El docente el día " + obtenerFechaActual() + " no marcó su salida a la hora establecida.");
                        h.salidaRegistrada = true;
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // Si no se encontró ningún turno adecuado:
        if (horarioSeleccionado == null) {
            if (horariosDocente.isEmpty()) {
                Toast.makeText(this, "No se han configurado horarios para su cuenta.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Fuera de horario permitido en este momento.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Configurar y mostrar el diálogo de asistencia
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_asistencia, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        TextView tvTitulo = dialogView.findViewById(R.id.tv_asistencia_titulo);
        final EditText etComentario = dialogView.findViewById(R.id.et_comentario);
        Button btnSi = dialogView.findViewById(R.id.btn_si);
        Button btnNo = dialogView.findViewById(R.id.btn_no);

        if (tipoRegistro.equals("Entrada")) {
            tvTitulo.setText("Registrar su hora de ingreso");
        } else if (tipoRegistro.equals("Salida")) {
            tvTitulo.setText("Registrar su hora de salida");
        }

        // Variables finales para usar en el lambda del botón
        final String finalTipoRegistro = tipoRegistro;
        final int finalTardanzaMinutos = tardanzaMinutos;
        HorarioDocente finalHorarioSeleccionado = horarioSeleccionado;

        btnSi.setOnClickListener(v -> {
            String comentario = etComentario.getText().toString().trim();
            String horaRegistro = obtenerFechaHoraActual(); // Formato "yyyy-MM-dd HH:mm:ss" en America/Lima
            enviarAsistencia(comentario, horaRegistro, finalTipoRegistro, String.valueOf(finalTardanzaMinutos));

            // Actualizar el estado del turno seleccionado
            if (finalTipoRegistro.equals("Entrada")) {
                finalHorarioSeleccionado.llegadaRegistrada = true;
            } else if (finalTipoRegistro.equals("Salida")) {
                finalHorarioSeleccionado.salidaRegistrada = true;
            }

            if (finalTipoRegistro.equals("Entrada") && finalTardanzaMinutos > 0) {
                Toast.makeText(HomeDocente.this,
                        "Llegada registrada a las " + horaRegistro + " con " + finalTardanzaMinutos + " minuto(s) de tardanza",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(HomeDocente.this,
                        finalTipoRegistro + " registrada a las " + horaRegistro,
                        Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();
        });

        btnNo.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Calcula la tardanza en minutos entre la hora de registro y la hora oficial.
     * @param horaRegistro en formato "HH:mm"
     * @param horaOficial en formato "HH:mm"
     * @return minutos de tardanza (0 si puntual)
     */
    private int calcularTardanza(String horaRegistro, String horaOficial) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            Date regDate = sdf.parse(horaRegistro);
            Date oficDate = sdf.parse(horaOficial);
            long diffMillis = regDate.getTime() - oficDate.getTime();
            int minutos = (int) (diffMillis / (60 * 1000));
            return minutos > 0 ? minutos : 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Envía los datos de asistencia al servidor.
     * En caso de error (por ejemplo, sin conexión), guarda el registro localmente.
     */
    private void enviarAsistencia(String comentario, String horaRegistro, String tipoRegistro, String tardanza) {
        String url = "https://ugelcorongo.pe/ugelasistencias_docente/sesion.php";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    // Registro enviado correctamente.
                },
                error -> {
                    Toast.makeText(HomeDocente.this, "Sin conexión. Se guardará el registro localmente.", Toast.LENGTH_LONG).show();
                    guardarRegistroLocal(comentario, horaRegistro, tipoRegistro, tardanza);
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

    /**
     * Guarda localmente el registro de asistencia (por ejemplo, en dataregistros.txt)
     * para sincronizar después cuando haya conexión.
     */
    private void guardarRegistroLocal(String comentario, String horaRegistro, String tipoRegistro, String tardanza) {
        String registro = colegio + ";" + docente + ";" + horaRegistro + ";" + tipoRegistro + ";" +
                tardanza + ";" + comentario + ";" + obtenerFechaActual() + "\n";
        try {
            FileOutputStream fos = openFileOutput("dataregistros.txt", Context.MODE_APPEND);
            fos.write(registro.getBytes());
            fos.close();
            Toast.makeText(this, "Registro guardado localmente.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "No se pudo guardar el registro localmente.", Toast.LENGTH_SHORT).show();
        }
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
     * Devuelve la fecha actual en formato "yyyy-MM-dd"
     */
    private String obtenerFechaActual() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    /**
     * Envía una "miselánea" en caso de que no se registre la salida dentro del intervalo permitido.
     */
    private void enviarMiselanea(String observacion) {
        String url = "https://ugelcorongo.pe/ugelasistencias_docente/enviar_miselaneas.php";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> Toast.makeText(HomeDocente.this, "Miselánea enviada correctamente.", Toast.LENGTH_LONG).show(),
                error -> Toast.makeText(HomeDocente.this, "Error al enviar miselánea.", Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("colegio", colegio);
                params.put("docente", docente);
                params.put("observacion", observacion);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(postRequest);
    }

    // ***************** Métodos de Georreferencia *****************

    /**
     * Actualiza las coordenadas de la I.E. leyendo el archivo raw (datacolegio).
     * Se asume que el archivo tiene líneas con campos separados por ";" y que:
     *   - partes[0] = Nombre del colegio.
     *   - partes[5] = Latitud.
     *   - partes[6] = Longitud.
     */
    private void actualizarCoordenadasIE() {
        try {
            InputStream isUbicacion = getResources().openRawResource(R.raw.datacolegio);
            BufferedReader reader = new BufferedReader(new InputStreamReader(isUbicacion));
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] partes = linea.split(";");
                if (colegio != null && colegio.equalsIgnoreCase(partes[0].trim())) {
                    dataLatitud = partes[5].trim();
                    dataLongitud = partes[6].trim();
                    // Opcional: se puede validar el docente si se requiere.
                }
            }
            isUbicacion.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Solicita los permisos de ubicación y llama a getCoordenada()
     * para obtener la localización actual.
     */
    public void obtenerCoordenadasActual() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(HomeDocente.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            getCoordenada();
        }
    }

    /**
     * Solicita actualizaciones de ubicación usando la API de Fused Location.
     * Una vez obtenida la ubicación, se calcula la distancia a la I.E.
     * Si la distancia es ≤ 150 metros, se marca como "DENTRO DE LA I.E.".
     */
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
                        // Detener actualizaciones para ahorrar batería
                        LocationServices.getFusedLocationProviderClient(HomeDocente.this)
                                .removeLocationUpdates(this);
                        if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                            int latestIndex = locationResult.getLocations().size() - 1;
                            latitudActual = locationResult.getLocations().get(latestIndex).getLatitude();
                            longitudActual = locationResult.getLocations().get(latestIndex).getLongitude();

                            // Calcular la distancia en metros entre la ubicación actual y la I.E.
                            double metros = calcularDistancia(
                                    latitudActual, longitudActual,
                                    Double.parseDouble(dataLatitud), Double.parseDouble(dataLongitud)
                            );
                            metros_mostrar = metros;
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
     * Calcula la distancia (en metros) entre dos pares de coordenadas utilizando la fórmula de Haversine.
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

        // Al pulsar "Subir Evidencia", se envía la foto al servidor (método enviarEvidencia)
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
}