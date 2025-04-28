package appAsis.example.asistenciaugelcorongo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREFS_NAME = "session_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_LAST_ACTIVE_TIME = "last_active_time";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Llama a este método al iniciar sesión exitosamente.
    public void createLoginSession() {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        updateLastActiveTime();
        editor.apply();
    }

    // Actualiza el tiempo de última interacción
    public void updateLastActiveTime() {
        editor.putLong(KEY_LAST_ACTIVE_TIME, System.currentTimeMillis());
        editor.apply();
    }

    // Retorna si hay una sesión activa o no
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Comprueba si han pasado más de timeoutMillis (p.ej. 1 hora) desde la última interacción
    public boolean isSessionExpired(long timeoutMillis) {
        long lastActive = prefs.getLong(KEY_LAST_ACTIVE_TIME, 0);
        return (System.currentTimeMillis() - lastActive) > timeoutMillis;
    }

    // Cierra la sesión: borra datos y redirige al login. Puedes detener aquí también servicios globales.
    public void logout() {
        editor.clear();
        editor.apply();
        // Por ejemplo, detén el LocationUpdateService si se está ejecutando
        Intent intent = new Intent(context, LocationUpdateService.class);
        context.stopService(intent);
        // Lanza el Login (MainActivity en este ejemplo) borrando la pila de actividades
        Intent loginIntent = new Intent(context, MainActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(loginIntent);
    }
}