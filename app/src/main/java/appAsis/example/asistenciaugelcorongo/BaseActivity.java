package appAsis.example.asistenciaugelcorongo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    public void getCoordenada() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            return;

        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(BaseActivity.this)
                                .removeLocationUpdates(this);
                        if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                            int latestIndex = locationResult.getLocations().size() - 1;
                            GlobalData.latActual = locationResult.getLocations().get(latestIndex).getLatitude();
                            GlobalData.lonActual = locationResult.getLocations().get(latestIndex).getLongitude();

                            double metros = calcularDistancia(GlobalData.latActual, GlobalData.lonActual,
                                    GlobalData.dataLat, GlobalData.dataLon);
                            if (metros <= 150) {
                                GlobalData.finalUbicacionEnvio = "DENTRO DE LA I.E.";
                            } else {
                                GlobalData.finalUbicacionEnvio = "FUERA DE LA I.E.";
                            }
                        }
                    }
                }, Looper.myLooper());
    }

    public double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        double metros_mostrar = 0.0;
        double radioTierra = 6371.0; // en kilómetros
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        metros_mostrar = radioTierra * c * 1000;
        return metros_mostrar;
    }

    // METODO NUEVO PARA LA V5
    protected void actualizarCoordenadasIE() {
        try {
            InputStream isUbicacion = getResources().openRawResource(R.raw.datacolegio);
            BufferedReader reader = new BufferedReader(new InputStreamReader(isUbicacion));
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] partes = linea.split(";");
                // Se asume que partes[0] es el nombre del colegio
                if (colegio != null && colegio.equalsIgnoreCase(partes[0].trim())) {
                    GlobalData.dataLat = Double.parseDouble(partes[5].trim());
                    GlobalData.dataLon = Double.parseDouble(partes[6].trim());
                }
            }
            isUbicacion.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void verificarAsistencias(final VerificacionCallback callback) {
        final int[] count = {0};
        final boolean[] entradaExiste = {false};
        final boolean[] salidaExiste = {false};
        String fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        String urlEntrada = "https://ugelcorongo.pe/ugelasistencias_docente/model/auxasistencia/verAsistenciaDocentesDirector.php" +
                "?colegio=" + colegio +
                "&periodo=" + fecha +
                "&docente=" + docente +
                "&turno=ENTRADA";

        String urlSalida = "https://ugelcorongo.pe/ugelasistencias_docente/model/auxasistencia/verAsistenciaDocentesDirector.php" +
                "?colegio=" + colegio +
                "&periodo=" + fecha +
                "&docente=" + docente +
                "&turno=SALIDA";

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
}