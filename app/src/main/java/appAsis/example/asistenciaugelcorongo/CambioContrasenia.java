package appAsis.example.asistenciaugelcorongo;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class CambioContrasenia extends AppCompatActivity {

    // Constante para la llave del extra que se envía desde MainActivity
    public static final String EXTRA_USUARIO = "usuario_actual";

    // Componentes de la interfaz
    TextView mensaje;    // Para mostrar información o alertas (lblMensaje)
    EditText t_codmod, t_password;  // txtcodmod se usará para el usuario; txtpassword para la nueva contraseña
    Button b_cambio;     // Botón para solicitar el cambio (btnSolicitacambio)

    // Variable para almacenar el valor recibido del MainActivity
    String usuarioRecibido;

    // Variables para manejo de fecha
    Calendar calendar;
    SimpleDateFormat simpleDateFormatnow;
    TimeZone myTimeZone = TimeZone.getTimeZone("America/Lima");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambio_contrasenia);

        // Vincular componentes del layout
        mensaje = findViewById(R.id.lblMensaje);
        t_codmod = findViewById(R.id.txtcodmod);
        t_password = findViewById(R.id.txtpassword);
        b_cambio = findViewById(R.id.btnSolicitacambio);

        // Recibir el valor del usuario proveniente desde MainActivity
        usuarioRecibido = getIntent().getStringExtra(EXTRA_USUARIO);
        if (usuarioRecibido != null && !usuarioRecibido.isEmpty()) {
            t_codmod.setText(usuarioRecibido);
            // Para evitar que el usuario modifique este campo, se deshabilita.
            t_codmod.setEnabled(false);
        }

        // Mensaje de información
        String texto = "\u2022 Debe contar con conexión a internet\n\n" +
                "\u2022 La contraseña no debe ser igual que el usuario";
        mensaje.setText(texto);
    }

    /**
     * Metodo que se invoca al darle clic al botón "SOLICITAR CAMBIO"
     * Se verifica:
     *  - Conexión a Internet (obligatorio para el proceso de cambio).
     *  - Que los campos no estén vacíos.
     *  - Que la nueva contraseña no sea igual al usuario (evitar credenciales por defecto).
     */
    public void solicitarCambio(View view) {
        String codmod = t_codmod.getText().toString().trim();
        String password = t_password.getText().toString().trim();

        // Verificar conexión a Internet
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        boolean auxConexion = connectivityManager.getActiveNetwork() != null &&
                connectivityManager.getActiveNetworkInfo().isConnected();
        if (!auxConexion) {
            Toast.makeText(this, "Debe tener conexión a Internet para cambiar la contraseña.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar que los campos no estén vacíos
        if (codmod.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Los campos no deben estar vacíos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar que la nueva contraseña no sea igual al usuario
        if (password.equals(codmod)) {
            Toast.makeText(this, "La nueva contraseña no puede ser igual al usuario.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener la hora actual para registrar el cambio
        calendar = Calendar.getInstance();
        simpleDateFormatnow = new SimpleDateFormat("HH:mm");
        simpleDateFormatnow.setTimeZone(myTimeZone);
        String fechaEnvio = simpleDateFormatnow.format(calendar.getTime());

        // Realizar la solicitud al servidor para el cambio de contraseña vía POST
        String url = "https://ugelcorongo.pe/ugelasistencias_docente/model/password/cambiospass.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getApplicationContext(), "Contraseña actualizada correctamente.", Toast.LENGTH_SHORT).show();

                        // Guardar en SharedPreferences el estado del cambio para este usuario.
                        SharedPreferences sharedPreferences = getSharedPreferences("CambioPassword", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("contraseñaCambiada_" + codmod, true);
                        editor.apply();

                        // Regresar a la pantalla principal
                        Intent home = new Intent(CambioContrasenia.this, MainActivity.class);
                        startActivity(home);
                        finish();
                    }
                },
                error -> Toast.makeText(getApplicationContext(), "Error al actualizar contraseña.", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parametros = new HashMap<>();
                parametros.put("codmod", codmod);
                parametros.put("password", password);
                parametros.put("fecha_envio", fechaEnvio);
                return parametros;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    /**
     * Metodo estático para ser utilizado en MainActivity (o en otra parte) para verificar
     * si el usuario ya realizó el cambio de contraseña.
     * Se usa SharedPreferences para guardar el estado.
     */
    public static boolean confirmarCambio(String dniUsuario, AppCompatActivity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("CambioPassword", MODE_PRIVATE);
        return sharedPreferences.getBoolean("contraseñaCambiada_" + dniUsuario, false);
    }
}