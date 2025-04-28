package appAsis.example.asistenciaugelcorongo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.graphics.Bitmap;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

/**
 * Esta clase agrupa métodos para:
 * 1) Guardar registros de asistencia en TXT,
 * 2) Guardar archivos PDF y sus metadatos en JSON,
 * 3) Guardar imágenes de evidencia y sus metadatos en JSON.
 * 4) Sincronizar (cada 30 minutos) los archivos pendientes (TXT, PDF, IMG) cuando hay conexión.
 *
 * Los archivos (y metadatos) se guardan en el almacenamiento interno (con openFileOutput()).
 */
public class OfflineStorageManager {

    // ========= Métodos de guardado local =========

    /**
     * Guarda un registro de asistencia en formato TXT.
     * Formato: colegio;docente;horaRegistro;tipoRegistro;tardanza;rol;coordenadas;dateCaptured\n
     */
    public static void saveAssistanceRecord(Context context,
                                            String colegio,
                                            String docente,
                                            String comentario,
                                            String horaRegistro,
                                            String tipoRegistro,
                                            String tardanza,
                                            String rol,
                                            String coordenadas) {
        String dateCaptured = obtenerFechaHoraActual();
        String registro = colegio + ";" + docente + ";" + comentario + ";" + horaRegistro + ";" + tipoRegistro + ";" +
                tardanza + ";" + rol + ";" + coordenadas + ";" + dateCaptured + "\n";
        // Usamos un nombre dinámico que inicie con "data_" para identificarlo en la sincronización.
        String fileName = "data_" + System.currentTimeMillis() + ".txt";
        try {
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_APPEND);
            fos.write(registro.getBytes());
            fos.close();
            Toast.makeText(context, "Registro guardado localmente.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error al guardar el registro de asistencia.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Guarda un archivo PDF offline y genera un archivo de metadata (JSON).
     * Se guardan con nombres: "pdf_[timestamp]_[rol].pdf" y su metadata "pdf_[timestamp]_[rol].json"
     */
    public static void savePdfOffline(Context context,
                                      String colegio,
                                      String docente,
                                      String rol,
                                      String idcolegio,
                                      String comentario,
                                      Uri pdfUri,
                                      String coordenadas) {
        byte[] pdfBytes = getFileDataFromUri(context, pdfUri);
        if (pdfBytes == null || pdfBytes.length == 0) {
            Toast.makeText(context, "Error: PDF vacío o no leído.", Toast.LENGTH_SHORT).show();
            return;
        }
        String timestamp = String.valueOf(System.currentTimeMillis());
        String pdfFileName = "pdf_" + timestamp + "_" + rol + ".pdf";
        saveFile(context, pdfFileName, pdfBytes);
        // Crear metadata en formato JSON.
        JSONObject meta = new JSONObject();
        try {
            meta.put("colegio", colegio);
            meta.put("docente", docente);
            meta.put("turno", rol);
            meta.put("FK_idcolegio", idcolegio);
            meta.put("comentario", comentario);
            meta.put("coordenadas", coordenadas);
            meta.put("dateCaptured", obtenerFechaHoraActual());
            meta.put("dateUploaded", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        String metaFileName = pdfFileName.replace(".pdf", ".json");
        saveFile(context, metaFileName, meta.toString().getBytes());
        Toast.makeText(context, "PDF guardado localmente.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Guarda una imagen (Bitmap de evidencia) offline y genera metadata en JSON.
     * Se guardan con nombres: "img_[timestamp]_[rol].jpg" y "img_[timestamp]_[rol].json"
     */
    public static void saveImageOffline(Context context,
                                        String colegio,
                                        String docente,
                                        String rol,
                                        String idcolegio,
                                        Bitmap bitmap,
                                        String coordenadas) {
        if (bitmap == null) {
            Toast.makeText(context, "Error: imagen no válida.", Toast.LENGTH_SHORT).show();
            return;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        byte[] imageBytes = bos.toByteArray();
        String timestamp = String.valueOf(System.currentTimeMillis());
        String imgFileName = "img_" + timestamp + "_" + rol + ".jpg";
        saveFile(context, imgFileName, imageBytes);
        JSONObject meta = new JSONObject();
        try {
            meta.put("colegio", colegio);
            meta.put("docente", docente);
            meta.put("turno", rol);
            meta.put("FK_idcolegio", idcolegio);
            meta.put("coordenadas", coordenadas);
            meta.put("dateCaptured", obtenerFechaHoraActual());
            meta.put("dateUploaded", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        String metaFileName = imgFileName.replace(".jpg", ".json");
        saveFile(context, metaFileName, meta.toString().getBytes());
        Toast.makeText(context, "Imagen guardada localmente.", Toast.LENGTH_SHORT).show();
    }

    // ---------- Métodos auxiliares para guardar/leer archivos ----------

    /**
     * Guarda un arreglo de bytes en un archivo privado interno.
     */
    public static void saveFile(Context context, String fileName, byte[] data) {
        try {
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(data);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lee y retorna los bytes de un archivo referenciado por un Uri.
     */
    public static byte[] getFileDataFromUri(Context context, Uri uri) {
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            is.close();
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retorna la fecha y hora actual en formato "yyyy-MM-dd HH:mm:ss" con zona horaria America/Lima.
     */
    public static String obtenerFechaHoraActual() {
        TimeZone tz = TimeZone.getTimeZone("America/Lima");
        Calendar calendar = Calendar.getInstance(tz);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(tz);
        return sdf.format(calendar.getTime());
    }

    /**
     * Retorna la fecha actual en formato "yyyy-MM-dd".
     */
    public static String obtenerFechaActual() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new java.util.Date());
    }

    // ---------- Sincronización de archivos offline ----------

    /**
     * Recorre el directorio interno en busca de archivos pendientes (archivos cuyo nombre
     * comience con "data_", "pdf_" o "img_") y los intenta subir a sus respectivos endpoints.
     * Si la subida es exitosa, se eliminan el archivo y (si existe) su archivo de metadata.
     * Este método debe llamarse cada 30 minutos (o cuando la app se inicia/reanuda) si hay Internet.
     */
    public static void syncOfflineFiles(final Context context) {
        if (!isOnline(context)) return;
        File dir = context.getFilesDir();
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            String fileName = file.getName();
            if (fileName.startsWith("data_") || fileName.startsWith("pdf_") || fileName.startsWith("img_")) {
                uploadOfflineFile(context, file);
            }
        }
    }

    /**
     * Verifica la conectividad a Internet.
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return (netInfo != null && netInfo.isConnected());
        }
        return false;
    }

    /**
     * Sube un archivo offline usando VolleyMultipartRequest según su tipo.
     * Si la subida es exitosa, elimina el archivo y su archivo de metadata (para PDF e IMG).
     */
    private static void uploadOfflineFile(final Context context, final File file) {
        String urlUpload = "";
        if (file.getName().startsWith("pdf_")) {
            urlUpload = "https://ugelcorongo.pe/ugelasistencias_docente/model/file/archivos/uploadFile.php";
        } else if (file.getName().startsWith("img_")) {
            urlUpload = "https://ugelcorongo.pe/ugelasistencias_docente/model/file/img/uploadEvidencia.php";
        } else if (file.getName().startsWith("data_")) {
            urlUpload = "https://ugelcorongo.pe/ugelasistencias_docente/sesion.php";
        }

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(
                Request.Method.POST,
                urlUpload,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        // Eliminamos el archivo si se subió correctamente.
                        file.delete();
                        // Para PDF o IMG, eliminamos también la metadata.
                        if (file.getName().startsWith("pdf_")) {
                            String metaFileName = file.getName().replace(".pdf", ".json");
                            File metaFile = new File(context.getFilesDir(), metaFileName);
                            if (metaFile.exists()) metaFile.delete();
                        } else if (file.getName().startsWith("img_")) {
                            String metaFileName = file.getName().replace(".jpg", ".json");
                            File metaFile = new File(context.getFilesDir(), metaFileName);
                            if (metaFile.exists()) metaFile.delete();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Si falla la subida, se conserva el archivo para reintentar.
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // Para el archivo "data_" (asistencia), se extraen los parámetros del contenido del archivo.
                if (file.getName().startsWith("data_")) {
                    String content = readFileAsString(context, file);
                    // Se espera un contenido con formato: colegio;docente;horaRegistro;tipoRegistro;tardanza;rol;coordenadas;dateCaptured
                    String[] parts = content.split(";");
                    if (parts.length >= 8) {
                        params.put("colegio", parts[0]);
                        params.put("docente", parts[1]);
                        params.put("comentario", parts[2]);
                        params.put("horaRegistro", parts[3]);
                        params.put("tipoRegistro", parts[4]);
                        params.put("tardanza", parts[5]);
                        // Se pueden enviar otros parámetros según lo requiera el endpoint.
                    }
                } else {
                    // Para PDF e IMG se usan los metadatos del archivo JSON correspondiente.
                    String metaFileName = "";
                    if (file.getName().startsWith("pdf_"))
                        metaFileName = file.getName().replace(".pdf", ".json");
                    else if (file.getName().startsWith("img_"))
                        metaFileName = file.getName().replace(".jpg", ".json");
                    File metaFile = new File(context.getFilesDir(), metaFileName);
                    if (metaFile.exists()) {
                        try {
                            String metaContent = readFileAsString(context, metaFile);
                            JSONObject meta = new JSONObject(metaContent);
                            params.put("colegio", meta.optString("colegio"));
                            params.put("docente", meta.optString("docente"));
                            params.put("turno", meta.optString("turno"));
                            if (file.getName().startsWith("pdf_")) {
                                params.put("FK_idcolegio", meta.optString("FK_idcolegio"));
                                params.put("comentario", meta.optString("comentario"));
                            } else if (file.getName().startsWith("img_")) {
                                params.put("FK_idcolegio", meta.optString("FK_idcolegio"));
                            }
                            // También es posible enviar coordenadas y fechaCaptured si fuese necesario.
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                try {
                    byte[] fileBytes = getFileBytes(file);
                    String mimeType = "application/octet-stream";
                    if (file.getName().startsWith("pdf_"))
                        mimeType = "application/pdf";
                    else if (file.getName().startsWith("img_"))
                        mimeType = "image/jpeg";
                    else if (file.getName().startsWith("data_"))
                        mimeType = "text/plain";
                    params.put("files", new DataPart(file.getName(), fileBytes, mimeType));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(multipartRequest);
    }

    // Helper para leer un archivo completo a String.
    private static String readFileAsString(Context context, File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            fis.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    // Helper para obtener bytes de un archivo.
    private static byte[] getFileBytes(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        while ((read = fis.read(buffer)) != -1) {
            bos.write(buffer, 0, read);
        }
        fis.close();
        return bos.toByteArray();
    }
}