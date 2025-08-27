package appAsis.example.asistenciaugelcorongo.repository;

import android.content.Context;
import android.graphics.Bitmap;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import appAsis.example.asistenciaugelcorongo.OfflineStorageManager;
import appAsis.example.asistenciaugelcorongo.domain.models.AttendanceRecord;
import appAsis.example.asistenciaugelcorongo.remote.ApiService;
import appAsis.example.asistenciaugelcorongo.utils.NetworkHelper;
import appAsis.example.asistenciaugelcorongo.utils.RawFileReader;
import appAsis.example.asistenciaugelcorongo.utils.URLPostHelper;

public class DataRepository {

    private static DataRepository instance;
    private final ApiService api;
    private final Context context;
    private final RequestQueue requestQueue;
    private static final String FICHAS_FILE = "datafichas.txt";

    private DataRepository(Context ctx) {
        this.context       = ctx.getApplicationContext();
        this.api           = ApiService.getInstance(this.context);
        this.requestQueue  = Volley.newRequestQueue(this.context);
    }

    public static synchronized DataRepository getInstance(Context ctx) {
        if (instance == null) {
            instance = new DataRepository(ctx);
        }
        return instance;
    }

    /**
     * Lee y parsea las líneas de datacolegio.txt en raw/raw.datacolegio.
     */
    public List<String[]> getColegiosLocal() throws IOException {
        return RawFileReader.readRawDatacolegio(context);
    }

    /**
     * Descarga y almacena el catálogo de colegios en "datacolegio.txt".
     */
    public void updateDatacolegioRemote(String url,
                                        ApiService.ApiCallback<String> callback) {
        api.fetchDatacolegio(url, new ApiService.ApiCallback<String>() {
            @Override
            public void onSuccess(String rawData) {
                try {
                    context.openFileOutput("datacolegio.txt", Context.MODE_PRIVATE)
                            .write(rawData.getBytes());
                } catch (IOException ignored) { }
                callback.onSuccess(rawData);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    /**
     * Envía un registro de asistencia remoto o lo guarda offline si no hay red.
     */
    public void sendAttendance(String url,
                               AttendanceRecord record,
                               ApiService.ApiCallback<Boolean> callback) {
        if (NetworkHelper.isOnline(context)) {
            api.sendAttendance(url, record, new ApiService.ApiCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean ok) {
                    callback.onSuccess(true);
                }

                @Override
                public void onError(Exception e) {
                    saveAttendanceOffline(record);
                    callback.onError(e);
                }
            });
        } else {
            saveAttendanceOffline(record);
            callback.onError(new IllegalStateException("Offline"));
        }
    }

    /**
     * Envía evidencia (imagen) o la guarda offline si no hay red.
     */
    public void sendEvidence(
            String url,
            String colegio,
            String docente,
            String rol,
            String idColegio,
            Bitmap evidencia,
            ApiService.ApiCallback<Boolean> callback
    ) {
        if (NetworkHelper.isOnline(context)) {
            api.sendEvidencia(
                    url,
                    colegio,
                    docente,
                    rol,
                    idColegio,
                    evidencia,
                    new ApiService.ApiCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean ok) {
                            callback.onSuccess(true);
                        }
                        @Override
                        public void onError(Exception e) {
                            saveEvidenceOffline(colegio, docente, rol, idColegio, evidencia);
                            callback.onError(e);
                        }
                    }
            );
        } else {
            saveEvidenceOffline(colegio, docente, rol, idColegio, evidencia);
            callback.onError(new IllegalStateException("Offline"));
        }
    }

    /**
     * Guarda un registro de asistencia en disco usando OfflineStorageManager.
     */
    private void saveAttendanceOffline(AttendanceRecord r) {
        String coords = r.getLatitude() + "," + r.getLongitude() + "_sinconexion";
        OfflineStorageManager.saveAssistanceRecord(
                context,
                r.getColegio(),
                r.getDocente(),
                r.getComentario(),
                r.getHoraRegistro(),
                r.getTipoRegistro(),
                r.getTardanza(),
                r.getRol(),
                coords
        );
    }

    /**
     * Obtiene el listado de fichas:
     * - Online: descarga, guarda todo en datafichas.txt y parsea el JSONArray.
     * - Offline: lee datafichas.txt, filtra las activas según rango de fechas.
     */
    public void fetchFichas(String url, ApiService.ApiCallback<JSONArray> callback) {
        if (NetworkHelper.isOnline(context)) {
            api.fetchFichas(url, new ApiService.ApiCallback<String>() {
                @Override
                public void onSuccess(String rawJson) {
                    saveFichasRaw(rawJson);
                    try {
                        JSONArray arr = new JSONArray(rawJson);
                        callback.onSuccess(arr);
                    } catch (Exception e) {
                        callback.onError(e);
                    }
                }
                @Override
                public void onError(Exception e) {
                    // Caída en remoto → lectua offline
                    try {
                        JSONArray arr = readFichasFromFile();
                        callback.onSuccess(arr);
                    } catch (Exception ex) {
                        callback.onError(ex);
                    }
                }
            });
        } else {
            // Sin red → offline directo
            try {
                JSONArray arr = readFichasFromFile();
                callback.onSuccess(arr);
            } catch (Exception e) {
                callback.onError(e);
            }
        }
    }

    /** Lee datafichas.txt, parsea y filtra solo las activas según fechaInicio/fechaFin */
    private JSONArray readFichasFromFile() throws Exception {
        FileInputStream fis = context.openFileInput(FICHAS_FILE);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        fis.close();

        JSONArray raw = new JSONArray(sb.toString());
        // Filtrar activas (estado==“activo” y rango fechas)
        JSONArray result = new JSONArray();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date now = new Date();
        for (int i = 0; i < raw.length(); i++) {
            JSONObject obj = raw.getJSONObject(i);
            if (!"activo".equalsIgnoreCase(obj.optString("estado"))) continue;
            Date inicio = sdf.parse(obj.optString("fecha_inicio"));
            Date fin    = sdf.parse(obj.optString("fecha_termino"));
            if (now.compareTo(inicio) >= 0 && now.compareTo(fin) <= 0) {
                result.put(obj);
            }
        }
        return result;
    }

    private void saveFichasRaw(String raw) {
        try (FileOutputStream fos = context.openFileOutput(FICHAS_FILE, Context.MODE_PRIVATE)) {
            fos.write(raw.getBytes());
        } catch (IOException ignored) {}
    }

    public void sendCoordinates(String url,
                                String usuario,
                                String rol,
                                double lat,
                                double lon,
                                ApiService.ApiCallback<Boolean> callback) {
        api.sendCoordinates(url, usuario, rol, lat, lon, callback);
    }


    /**
     * Envía un registro de asistencia de forma síncrona (RequestFuture).
     */
    public boolean sendAttendanceSync(AttendanceRecord record) {
        try {
            RequestFuture<String> future = RequestFuture.newFuture();
            //String url = "https://ugelcorongo.pe/ugelasistencias_docente/sesion.php";
            String url_registrarasistencia = URLPostHelper.Asistencia.REGISTRAR;
            StringRequest req = new StringRequest(
                    Request.Method.POST,
                    url_registrarasistencia,
                    future,
                    future
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String,String> params = new HashMap<>();
                    params.put("colegio",      record.getColegio());
                    params.put("docente",      record.getDocente());
                    params.put("comentario",   record.getComentario());
                    params.put("horaRegistro", record.getHoraRegistro());
                    params.put("tipoRegistro", record.getTipoRegistro());
                    params.put("tardanza",     record.getTardanza());
                    params.put("rol",          record.getRol());
                    params.put("coordenadas",  record.getLatitude() + "," + record.getLongitude());
                    return params;
                }
            };
            requestQueue.add(req);
            future.get(30, TimeUnit.SECONDS);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Lanza la sincronización de todos los registros pendientes en disco.
     */
    public void syncOfflineRecords() {
        OfflineStorageManager.syncOfflineFiles(context);
    }

    /**
     * Comprime el Bitmap a JPEG, lo convierte a Base64 y delega
     * al OfflineStorageManager para persistirlo.
     */
    private void saveEvidenceOffline(
            String colegio,
            String docente,
            String rol,
            String idColegio,
            Bitmap evidenciaBitmap
    ) {
        // recupera coordenadas si hace falta...
        String coords = "0.00000";
        OfflineStorageManager.saveImageOffline(
                context,
                colegio,
                docente,
                rol,
                idColegio,
                evidenciaBitmap,
                coords
        );
    }
}