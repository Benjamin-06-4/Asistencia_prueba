package appAsis.example.asistenciaugelcorongo;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

//
public class BaseActivity extends AppCompatActivity {

    protected SessionManager sessionManager;
    // Timeout de inactividad: 1 hora
    private static final long INACTIVITY_TIMEOUT = 3600000;
    // Intervalo de sincronización: 30 minutos (en ms)
    private static final long SYNC_INTERVAL = 1800000;

    private Handler syncHandler = new Handler();
    private Runnable syncRunnable = new Runnable() {
        @Override
        public void run() {
            OfflineStorageManager.syncOfflineFiles(BaseActivity.this);
            syncHandler.postDelayed(this, SYNC_INTERVAL);
        }
    };

    // Variables para datos del usuario
    protected String rol = "";
    protected String colegio;
    protected String docente;
    protected String idcolegio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            sessionManager.logout();
        }
        OfflineStorageManager.syncOfflineFiles(this);
        checkWiFiAndUploadFiles();

        if (getIntent() != null) {
            if (getIntent().hasExtra("turnos")) {
                rol = getIntent().getStringExtra("turnos");
            }
            if (getIntent().hasExtra("colegio")) {
                colegio = getIntent().getStringExtra("colegio");
            }
            if (getIntent().hasExtra("docente")) {
                docente = getIntent().getStringExtra("docente");
            }
            if (getIntent().hasExtra("idcolegio")) {
                idcolegio = getIntent().getStringExtra("idcolegio");
            }
        }
    }

    @Override
    public void onUserInteraction(){
        super.onUserInteraction();
        if (sessionManager != null) {
            sessionManager.updateLastActiveTime();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(sessionManager.isSessionExpired(INACTIVITY_TIMEOUT)){
            sessionManager.logout();
        }
        syncHandler.post(syncRunnable);
        OfflineStorageManager.syncOfflineFiles(this);

        if("Especialista".equalsIgnoreCase(rol)){
            obtenerCoordenadasActual();
        }
        actualizarCoordenadasIEBase();
    }

    @Override
    protected void onPause(){
        super.onPause();
        syncHandler.removeCallbacks(syncRunnable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_logout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.action_logout){
            sessionManager.logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void checkWiFiAndUploadFiles(){
        if(isOnline()){
            OfflineStorageManager.syncOfflineFiles(this);
        }
    }

    protected boolean isOnline(){
        android.net.ConnectivityManager cm = (android.net.ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm != null){
            android.net.NetworkInfo net = cm.getActiveNetworkInfo();
            return (net != null && net.isConnected());
        }
        return false;
    }

    // ===============================
    // Métodos para actualización de coordenadas (solo para Especialistas)
    // ===============================

    protected void actualizarCoordenadasIEBase(){
        try {
            // Se supone que el recurso R.raw.datacolegio contiene líneas con el siguiente formato:
            // [0]: nombre; [1]: DNI; [2]: usuario; [3]: rol; [4]: idColegio; [5]: latitud; [6]: longitud; [7]: docente; [8]: nivel
            String colegioValue = colegio;
            InputStream isUbicacion = getResources().openRawResource(R.raw.datacolegio);
            BufferedReader reader = new BufferedReader(new InputStreamReader(isUbicacion));
            String linea;
            boolean encontrado = false;
            while ((linea = reader.readLine()) != null) {
                String[] partes = linea.split(";");
                if(colegioValue != null && colegioValue.equalsIgnoreCase(partes[0].trim())){
                    // Se encontró el colegio; se actualizarán las coordenadas (digamos que se extraen de la URL obtener-colegios.php)
                    // Para este ejemplo, simularemos que las nuevas coordenadas son partes[2] y partes[3] (ajusta según necesidad)
                    String nuevaLatitud = partes[2].trim();  // Simulación: el valor actualizado de latitud
                    String nuevaLongitud = partes[3].trim(); // Simulación: el valor actualizado de longitud

                    // Actualiza los valores globales (o de la aplicación). Por ejemplo:
                    // GlobalData.dataLatitud = nuevaLatitud;
                    // GlobalData.dataLongitud = nuevaLongitud;

                    // Ahora, para actualizar, se sobrescribe (o se agrega) la línea en el archivo interno "datacolegio.txt".
                    // En este ejemplo, se realizará una actualización: se creará una nueva línea con los datos actualizados.
                    String nuevaLinea = colegioValue + ";" +
                            partes[1].trim() + ";" +
                            partes[2].trim() + ";" + // Usuario (puede cambiar según el JSON real)
                            partes[3].trim() + ";" + // Rol o turno
                            partes[4].trim() + ";" + // ID colegio
                            nuevaLatitud + ";" +     // NUEVA LATITUD
                            nuevaLongitud + ";" +    // NUEVA LONGITUD
                            docente + ";" +          // DOCENTE (del Intent)
                            partes[8].trim();        // NIVEL
                    escribirEnArchivo("datacolegio.txt", nuevaLinea, true);

                    encontrado = true;
                    break;
                }
            }
            reader.close();
            isUbicacion.close();
            // Si no se encontró el colegio en el recurso, agregarlo como nueva línea.
            if(!encontrado) {
                // Suponiendo que los datos nuevos se obtienen de la URL (ejemplo):
                // Se establecen valores nuevos (esto normalmente vendría del JSON de obtener-colegios.php)
                String nuevaLinea = colegioValue + ";"
                        + "DNI_NUEVO" + ";"  // Puedes establecer valores predeterminados
                        + "USUARIO_NUEVO" + ";"
                        + "ROL_NUEVO" + ";"
                        + idcolegio + ";"  // ID colegio ya conocido
                        + "LATITUD_NUEVA" + ";"  // Estos deben obtenerse
                        + "LONGITUD_NUEVA" + ";" +
                        docente + ";" +
                        "NIVEL_NUEVO";
                escribirEnArchivo("datacolegio.txt", nuevaLinea, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Método auxiliar para escribir o actualizar el archivo "datacolegio.txt".
     * Si actualizar==true, se actualiza (se agrega al final una línea modificada);
     * de lo contrario, se añade una nueva línea.
     */
    protected void escribirEnArchivo(String fileName, String linea, boolean actualizar) {
        try {
            // Usar MODE_APPEND para añadir la línea
            FileOutputStream fos = openFileOutput(fileName, Context.MODE_APPEND);
            // En este ejemplo, se escribe la línea seguida de un salto de línea.
            fos.write((linea + "\n").getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Obtiene la ubicación actual; se usa para registrar la posición actual del dispositivo.
     */
    protected void obtenerCoordenadasActual(){
        if(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != android.content.pm.PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            getCoordenada();
        }
    }

    /**
     * Solicita actualizaciones de ubicación mediante FusedLocationProviderClient.
     */
    protected void getCoordenada(){
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != android.content.pm.PackageManager.PERMISSION_GRANTED)
            return;

        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest, new LocationCallback(){
                    @Override
                    public void onLocationResult(LocationResult locationResult){
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(BaseActivity.this)
                                .removeLocationUpdates(this);
                        if(locationResult != null && !locationResult.getLocations().isEmpty()){
                            int latestIndex = locationResult.getLocations().size() - 1;
                            double lat = locationResult.getLocations().get(latestIndex).getLatitude();
                            double lon = locationResult.getLocations().get(latestIndex).getLongitude();
                            // Se asignan los valores a GlobalData
                            GlobalData.latActual = lat;
                            GlobalData.lonActual = lon;
                        }
                    }
                }, android.os.Looper.myLooper());
    }
}