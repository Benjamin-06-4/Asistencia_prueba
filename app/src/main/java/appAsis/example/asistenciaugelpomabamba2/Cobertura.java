package appAsis.example.asistenciaugelpomabamba2;

import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public interface Cobertura extends LocationListener, GpsStatus.Listener {
    public void onLocationChanged(Location location);
    public void onProviderDiseabled(String provider);
    public void onProviderEnabled(String provider);
    public void onStatusChanged(String provider, int status, Bundle extras);
    public void onGpsStatusChanged(int event);

}
