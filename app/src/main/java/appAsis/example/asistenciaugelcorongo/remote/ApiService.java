package appAsis.example.asistenciaugelcorongo.remote;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Base64;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import appAsis.example.asistenciaugelcorongo.BuildConfig;
import appAsis.example.asistenciaugelcorongo.domain.models.AttendanceRecord;

public class ApiService {
    private static ApiService instance;
    private final RequestQueue queue;

    /** Callback genérico para respuestas asincrónicas */
    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    private ApiService(Context ctx) {
        this.queue = Volley.newRequestQueue(ctx.getApplicationContext());
    }

    /** Singleton */
    public static synchronized ApiService getInstance(Context ctx) {
        if (instance == null) {
            instance = new ApiService(ctx);
        }
        return instance;
    }

    /**
     * Descarga el contenido de datacolegio.php o similar y devuelve
     * el JSON (o CSV) como String.
     */
    public void fetchDatacolegio(String url, final ApiCallback<String> callback) {
        StringRequest req = new StringRequest(
                Request.Method.GET,
                url,
                response -> callback.onSuccess(response),
                error -> callback.onError(error)
        );
        queue.add(req);
    }

    /**
     * Envía un registro de asistencia al servidor vía POST.
     * Parséa un AttendanceRecord y notifica con true/false.
     */
    public void sendAttendance(
            String url,
            AttendanceRecord record,
            final ApiCallback<Boolean> callback
    ) {
        StringRequest post = new StringRequest(
                Request.Method.POST,
                url,
                response -> callback.onSuccess(true),
                error -> callback.onError(error)
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("colegio",      record.getColegio());
                p.put("docente",      record.getDocente());
                p.put("turno",        record.getTipoRegistro());
                p.put("horaRegistro", record.getHoraRegistro());
                p.put("tardanza",     record.getTardanza());
                p.put("comentario",   record.getComentario());

                // — Información del dispositivo —
                p.put("ModelInfodevice",     Build.MODEL);
                p.put("IdInfodevice",        Build.ID);
                p.put("ManufactInfodevice",  Build.MANUFACTURER);
                p.put("BrandInfodevice",     Build.BRAND);
                p.put("TypeInfodevice",      Build.TYPE);
                p.put("UserInfodevice",      Build.USER);
                p.put("BaseInfodevice",      Build.VERSION.BASE_OS == null
                        ? Build.VERSION.RELEASE
                        : Build.VERSION.BASE_OS);
                p.put("SdkInfodevice",       String.valueOf(Build.VERSION.SDK_INT));
                p.put("BoardInfodevice",     Build.BOARD);
                p.put("HostInfodevice",      Build.HOST);
                p.put("FingeprintInfodevice",Build.FINGERPRINT);

                // — Versión de la app (versionCode) —
                int vCode = BuildConfig.VERSION_CODE;
                p.put("VCodeInfodevice", String.valueOf(vCode));
                return p;
            }
        };
        queue.add(post);
    }

    /**
     * Sube la evidencia al servidor codificada en Base64.
     */
    public void sendEvidencia(
            String url,
            String colegio,
            String docente,
            String rol,
            String idColegio,
            Bitmap evidenciaBitmap,
            final ApiCallback<Boolean> callback
    ) {
        StringRequest post = new StringRequest(
                Request.Method.POST,
                url,
                response -> callback.onSuccess(true),
                error    -> callback.onError(error)
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("colegio",  colegio);
                params.put("docente",  docente);
                params.put("rol",      rol);
                params.put("idColegio",idColegio);

                // Convertir Bitmap a Base64
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                evidenciaBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                String encoded = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
                params.put("evidencia", encoded);

                return params;
            }
        };
        queue.add(post);
    }

    /**
     * Descarga el listado de fichas (array JSON) y lo devuelve como String.
     */
    public void fetchFichas(String url, final ApiCallback<String> callback) {
        StringRequest req = new StringRequest(
                Request.Method.GET,
                url,
                response -> callback.onSuccess(response),
                error    -> callback.onError(error)
        );
        queue.add(req);
    }

    /**
     * Actualiza coordenadas en el servidor.
     */
    public void sendCoordinates(
            String url,
            String usuario,
            String rol,
            double lat,
            double lon,
            final ApiCallback<Boolean> callback
    ) {
        try {
            JSONObject body = new JSONObject();
            body.put("usuario",   usuario);
            body.put("rol",       rol);
            body.put("latitude",  lat);
            body.put("longitude", lon);

            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    response -> callback.onSuccess(true),
                    error    -> callback.onError(error)
            );
            queue.add(req);
        } catch (Exception e) {
            callback.onError(e);
        }
    }
}