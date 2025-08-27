package appAsis.example.asistenciaugelcorongo.utils;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public final class LocationHelper {
    private static final long TIMEOUT_MS = 10_000;

    private LocationHelper() { /* No instanciable */ }

    public interface LocationResultCallback {
        void onLocationResult(Location location);
        void onError(Exception e);
    }

    /**
     * Solicita una única ubicación de alta precisión.
     * Si no hay permiso, llama a onError con SecurityException.
     */
    public static void requestSingleLocation(@NonNull Context ctx,
                                             @NonNull LocationResultCallback callback) {
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            callback.onError(new SecurityException("ACCESS_FINE_LOCATION permission not granted"));
            return;
        }

        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient(ctx.getApplicationContext());
        CancellationTokenSource cts = new CancellationTokenSource();

        client.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                cts.getToken()
        ).addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    callback.onLocationResult(location);
                } else {
                    callback.onError(new Exception("Location returned null"));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onError(e);
            }
        });

        // Cancelar solicitud tras TIMEOUT_MS
        new Handler(Looper.getMainLooper()).postDelayed(cts::cancel, TIMEOUT_MS);
    }

    /**
     * Calcula la distancia en metros entre dos coordenadas
     * usando la fórmula de Haversine.
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double metros_mostrar = 0.0;
        double earthRadius = 6371.0; // en kilómetros
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        metros_mostrar = earthRadius * c * 1000;
        return metros_mostrar;
    }
}