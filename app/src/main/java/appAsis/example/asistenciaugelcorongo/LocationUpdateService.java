package appAsis.example.asistenciaugelcorongo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.*;

import org.json.JSONObject;

public class LocationUpdateService extends Service {

    private static final long UPDATE_INTERVAL = 5000; // 5 segundos
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private RequestQueue requestQueue;
    private String urlCoordenadas = "https://ugelcorongo.pe/ugelasistencias_docente/model/rastreo/actualizar-coordenadas.php";

    // Datos que recibiremos como extra
    private String usuario;  // se usará, por ejemplo, el valor "docente" de MainActivity / HomeEspecialista
    private String rol;      // se usará el valor de "turnos" que debería ser "Especialista" en este caso

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestQueue = Volley.newRequestQueue(this);
    }

    /**
     * Se obtienen los datos enviados en el Intent que arranca el Service.
     * Se inicia la actualización de ubicación.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        usuario = intent.getStringExtra("docente");
        rol = intent.getStringExtra("turnos");

        // Iniciamos la obtención de actualizaciones de ubicación
        startLocationUpdates();

        // START_STICKY para que el Service se mantenga en ejecución
        return START_STICKY;
    }

    /**
     * Configura y solicita actualizaciones periódicas de ubicación.
     */
    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null)
                    return;

                // Se obtiene la última ubicación disponible
                double latitude = locationResult.getLastLocation().getLatitude();
                double longitude = locationResult.getLastLocation().getLongitude();
                // Enviar la información al servidor
                sendCoordinates(latitude, longitude);
            }
        };

        try {
            // Es necesario tener los permisos de ubicación (verifica que se hayan solicitado en runtime)
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            Log.e("LocationUpdateService", "Falta permiso de ubicación", e);
        }
    }

    /**
     * Envía las coordenadas junto al usuario y rol mediante una petición POST.
     */
    private void sendCoordinates(double latitude, double longitude) {
        JSONObject params = new JSONObject();
        try {
            params.put("latitude", latitude);
            params.put("longitude", longitude);
            params.put("usuario", usuario);
            params.put("rol", rol);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                urlCoordenadas,
                params,
                response -> {
                    // Aquí podrías procesar la respuesta si es necesario
                    Log.d("LocationUpdateService", "Coordenadas enviadas: " + response.toString());
                },
                error -> {
                    // Manejo de error (opcional)
                    Log.e("LocationUpdateService", "Error al enviar coordenadas: " + error.toString());
                }
        );
        requestQueue.add(request);
    }

    @Override
    public void onDestroy() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;  // No se utiliza binding para este Service
    }
}