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

import appAsis.example.asistenciaugelcorongo.utils.URLPostHelper;

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

    // ------------------------------------------------------------------------
    //                     MÉTODOS DE GUARDADO LOCAL
    // ------------------------------------------------------------------------

    /**
     * Guarda un registro de asistencia en formato TXT:
     * colegio;docente;comentario;horaRegistro;tipoRegistro;tardanza;rol;coordenadas;dateCaptured\n
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
        String registro = colegio + ";" +
                docente + ";" +
                comentario + ";" +
                horaRegistro + ";" +
                tipoRegistro + ";" +
                tardanza + ";" +
                rol + ";" +
                coordenadas + ";" +
                dateCaptured + "\n";

        String fileName = "data_" + System.currentTimeMillis() + ".txt";
        try (FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_APPEND)) {
            fos.write(registro.getBytes());
            Toast.makeText(context, "Registro guardado localmente.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error guardando registro.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Guarda un PDF offline y genera metadata JSON.
     * Nombres: pdf_[timestamp]_[rol].pdf + pdf_[timestamp]_[rol].json
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
            Toast.makeText(context, "PDF vacío o no leído.", Toast.LENGTH_SHORT).show();
            return;
        }

        String ts = String.valueOf(System.currentTimeMillis());
        String pdfName = "pdf_" + ts + "_" + rol + ".pdf";
        saveFile(context, pdfName, pdfBytes);

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

        String metaName = pdfName.replace(".pdf", ".json");
        saveFile(context, metaName, meta.toString().getBytes());

        Toast.makeText(context, "PDF guardado localmente.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Guarda una imagen offline y genera metadata JSON.
     * Nombres: img_[timestamp]_[rol].jpg + img_[timestamp]_[rol].json
     */
    public static void saveImageOffline(Context context,
                                        String colegio,
                                        String docente,
                                        String rol,
                                        String idcolegio,
                                        Bitmap bitmap,
                                        String coordenadas) {
        if (bitmap == null) {
            Toast.makeText(context, "Imagen no válida.", Toast.LENGTH_SHORT).show();
            return;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        byte[] imgBytes = bos.toByteArray();

        String ts = String.valueOf(System.currentTimeMillis());
        String imgName = "img_" + ts + "_" + rol + ".jpg";
        saveFile(context, imgName, imgBytes);

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

        String metaName = imgName.replace(".jpg", ".json");
        saveFile(context, metaName, meta.toString().getBytes());

        Toast.makeText(context, "Imagen guardada localmente.", Toast.LENGTH_SHORT).show();
    }

    // ------------------------------------------------------------------------
    //                       MÉTODOS AUXILIARES I/O
    // ------------------------------------------------------------------------

    private static void saveFile(Context context, String fileName, byte[] data) {
        try (FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)) {
            fos.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] getFileDataFromUri(Context context, Uri uri) {
        try (InputStream is = context.getContentResolver().openInputStream(uri);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Lee un archivo completo y devuelve su contenido como String.
     */
    private static String readFileAsString(Context context, File file) {
        try (FileInputStream fis = context.openFileInput(file.getName());
             BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while (((line = br.readLine())) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static byte[] getFileBytes(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = fis.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }
            return bos.toByteArray();
        }
    }

    private static String obtenerFechaHoraActual() {
        TimeZone tz = TimeZone.getTimeZone("America/Lima");
        Calendar cal = Calendar.getInstance(tz);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(tz);
        return sdf.format(cal.getTime());
    }

    private static String obtenerFechaActual() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
    }

    // ------------------------------------------------------------------------
    //                     SINCRONIZACIÓN DE ARCHIVOS OFFLINE
    // ------------------------------------------------------------------------

    /**
     * Itera sobre archivos internos (data_, pdf_, img_) y los sube.
     * En éxitos elimina el principal y su .json creándose, si aplica.
     */
    public static void syncOfflineFiles(final Context context) {
        if (!isOnline(context)) return;
        File[] files = context.getFilesDir().listFiles();
        if (files == null) return;

        for (File file : files) {
            String name = file.getName();
            if (name.startsWith("data_") ||
                    name.startsWith("pdf_") ||
                    name.startsWith("img_")) {
                uploadOfflineFile(context, file);
            }
        }
    }

    private static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }

    /**
     * Construye y envía un VolleyMultipartRequest según el tipo de archivo.
     * Elimina los archivos en caso de éxito.
     */
    private static void uploadOfflineFile(final Context context, final File file) {
        String url = "";
        if (file.getName().startsWith("pdf_")) {
            //url = "https://ugelcorongo.pe/ugelasistencias_docente/model/file/archivos/uploadFile.php";
            url = URLPostHelper.PDF.REGISTRAR;
        } else if (file.getName().startsWith("img_")) {
            //url = "https://ugelcorongo.pe/ugelasistencias_docente/model/file/img/uploadEvidencia.php";
            url = URLPostHelper.Imagen.REGISTRAR;
        } else if (file.getName().startsWith("data_")) {
            //url = "https://ugelcorongo.pe/ugelasistencias_docente/sesion.php";
            url = URLPostHelper.Asistencia.REGISTRAR;
        }

        RequestQueue queue = Volley.newRequestQueue(context);
        VolleyMultipartRequest req = new VolleyMultipartRequest(
                Request.Method.POST,
                url,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        // eliminar principal
                        file.delete();
                        // eliminar metadata .json si aplica
                        if (file.getName().startsWith("pdf_") ||
                                file.getName().startsWith("img_")) {
                            String meta = file.getName().replaceFirst("\\.(pdf|jpg)$", ".json");
                            context.deleteFile(meta);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // dejar para reintento
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                if (file.getName().startsWith("data_")) {
                    String[] parts = readFileAsString(context, file).split(";");
                    if (parts.length >= 8) {
                        p.put("colegio", parts[0]);
                        p.put("docente", parts[1]);
                        p.put("comentario", parts[2]);
                        p.put("horaRegistro", parts[3]);
                        p.put("tipoRegistro", parts[4]);
                        p.put("tardanza", parts[5]);
                        // añade más params si tu endpoint los pide
                    }
                } else {
                    String metaName = file.getName().replaceFirst("\\.(pdf|jpg)$", ".json");
                    File meta = new File(context.getFilesDir(), metaName);
                    if (meta.exists()) {
                        try {
                            JSONObject m = new JSONObject(readFileAsString(context, meta));
                            p.put("colegio", m.optString("colegio"));
                            p.put("docente", m.optString("docente"));
                            p.put("turno", m.optString("turno"));
                            p.put("FK_idcolegio", m.optString("FK_idcolegio"));
                            if (file.getName().startsWith("pdf_"))
                                p.put("comentario", m.optString("comentario"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                return p;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> data = new HashMap<>();
                try {
                    byte[] bytes = getFileBytes(file);
                    String mime = "application/octet-stream";
                    if (file.getName().endsWith(".pdf")) mime = "application/pdf";
                    if (file.getName().endsWith(".jpg")) mime = "image/jpeg";
                    data.put("files", new DataPart(file.getName(), bytes, mime));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return data;
            }
        };

        queue.add(req);
    }
}