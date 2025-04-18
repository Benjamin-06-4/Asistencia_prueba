package appAsis.example.asistenciaugelcorongo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    // Datos cargados desde el archivo "datacolegio"
    List<String> listColegios           = new ArrayList<>();
    List<String> listPasswords          = new ArrayList<>();
    List<String> listUsers            = new ArrayList<>();
    List<String> listTipoUsuario             = new ArrayList<>();
    List<String> listIdColegios         = new ArrayList<>();
    List<String> listDocente            = new ArrayList<>();
    List<String> listNivelesColegio     = new ArrayList<>();

    // Datos cargados desde Excel con información de docentes, directores y especialistas
    List<String> excelDocenteColegio    = new ArrayList<>();
    List<String> excelDocenteUser       = new ArrayList<>();
    List<String> excelDocentePass       = new ArrayList<>();
    List<String> excelDocenteTipo       = new ArrayList<>();
    List<String> excelAbreviado         = new ArrayList<>();
    List<String> excelCodmod_ie         = new ArrayList<>();
    List<String> excelDocenteNomCompleto = new ArrayList<>();
    List<String> excelDocenteTurno      = new ArrayList<>();

    // Componentes de la Interfaz (xml)
    TextView   textView;
    EditText   t_codmod;
    EditText   t_password;
    Button     b_login;
    Button     b_ViewCambio;

    // Variables auxiliares para control interno y procesamiento
    String auxArrayExcelDocentes      = "";
    String str_idcolegio      = ""; // Variable almacena el id de la I.E.
    String str_colegio      = ""; // Variable almacena el id de la I.E.
    String str_nivelcolegio      = ""; // Variable almacena el nivel de la I.E.
    String str_usuario      = "";      // Variable almacena el nombre del usuario (Nombre del docente/director/especialista/colegio)
    int index = -1;

    // Variables para roles, búsquedas y fechas
    String fechaactual        = "";

    // Componentes de Red y Peticiones
    RequestQueue requestQueue;

    // Constantes y URL de Servicio
    // Constante para URL de verificación de cambio de contraseña
    public static final String URL_VER_CAMBIO = "https://ugelcorongo.pe/ugelasistencias_docente/model/password/verCambiospass.php?codmod=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Forzar modo claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar componentes de la interfaz (según el XML)
        t_codmod = findViewById(R.id.txtCodmodular);
        t_password = findViewById(R.id.txtPassword);
        b_login = findViewById(R.id.btcLogin);
        b_ViewCambio = findViewById(R.id.btnCambiopass);
        textView = findViewById(R.id.textView);

        // Inicializar la cola de peticiones de Volley
        requestQueue = Volley.newRequestQueue(MainActivity.this);

        // Cargar datos del archivo RAW "datacolegio" en las listas correspondientes
        try {
            InputStream is = getResources().openRawResource(R.raw.datacolegio);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] parts = linea.split(";");
                // Verificar que la línea contenga al menos la cantidad de datos requerida
                if (parts.length >= 9) {
                    listColegios.add(parts[0].trim());
                    listPasswords.add(parts[1].trim());
                    listUsers.add(parts[2].trim());
                    listTipoUsuario.add(parts[3].trim());
                    listIdColegios.add(parts[4].trim());
                    listDocente.add(parts[7].trim());
                    listNivelesColegio.add(parts[8].trim());
                }
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Configurar la fecha actual en formato "yyyy-MM-dd"
        TimeZone timeZone = TimeZone.getTimeZone("America/Lima");
        Calendar calendar = Calendar.getInstance(timeZone);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(timeZone);
        fechaactual = sdf.format(calendar.getTime());

        // Establecer el listener del botón de login
        b_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Aquí se invoca el metodo login, el cual se encargará de
                // 1. Realizar la validación online forzada el primer inicio de sesión.
                // 2. En caso de que ya se haya cambiado la contraseña, hacer la validación offline leyendo
                //    el archivo interno que almacena la contraseña ya actualizada.
                try {
                    login(view);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // Establecer el listener del botón para cambio de contraseña y también al botón ("¿Olvidaste tu contraseña?")
        b_ViewCambio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cambioContra(view);
            }
        });

        // Cargar datos de docentes nuevos desde el servidor
        dataExcelDocente();

        // Opcional: En futuras fases, se pueden cargar también otros datos,
        // como ubicaciones, notificaciones, etc.
        // Ejemplo:
        // dataExcelUbicaciones();
    }

    public void login(View view) throws IOException {
        // Obtener los datos ingresados en los campos de usuario y contraseña
        final String dniUsuario = t_codmod.getText().toString().trim();
        final String passwordIngresada = t_password.getText().toString().trim();
        final String auxArchivo = "dataCambiosTemp_" + dniUsuario + ".txt";

        // Validar que los campos no estén vacíos
        if (TextUtils.isEmpty(dniUsuario) || TextUtils.isEmpty(passwordIngresada)) {
            Toast.makeText(MainActivity.this, "Los campos no deben estar vacíos", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Nueva validación contra datacolegio.txt ---
        // Buscar el índice correspondiente al usuario ingresado en la lista precargada (listUsers)
        index = -1;
        for (int i = 0; i < listPasswords.size(); i++) {
            if (listPasswords.get(i).equals(passwordIngresada)) {
                index = i;
                break;
            }
        }

        // Verificar conectividad a internet
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
                .getSystemService(CONNECTIVITY_SERVICE);
        boolean isConnected = (cm.getActiveNetwork() != null &&
                cm.getActiveNetworkInfo().isConnected());

        if (isConnected) {
            // Si hay conexión, se valida en línea para forzar el cambio de contraseña en el primer inicio
            validarCambioDeContraseñaEnServidor(dniUsuario, new ResultadoValidacion() {
                @Override
                public void onResultado(boolean cambioRealizado, String nuevaContraseña) {
                    if (!cambioRealizado) {
                        // Si aún no se realizó el cambio, obligar al usuario a cambiar la contraseña
                        Toast.makeText(MainActivity.this, "Por seguridad debes cambiar tu contraseña.", Toast.LENGTH_SHORT).show();
                        cambioContra(view);
                    } else {
                        // Guardar la contraseña actualizada localmente (nuevaContraseña)
                        guardarContraseñaLocal(auxArchivo, nuevaContraseña);
                        try {
                            String passLocal = cargarContraseñaLocal(auxArchivo);
                            if (passLocal.equals(passwordIngresada)) {
                                // Continuar con el proceso de login (determinar rol y redirigir)

                                // Si se encontró el registro, obtener la contraseña por defecto para ese usuario
                                if (index == -1) {
                                    continuarLogin(dniUsuario, passwordIngresada);
                                } else {
                                    Toast.makeText(MainActivity.this, "Las credenciales para este usuario ya fueron cambiadas, datos incorrectos.", Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                Toast.makeText(MainActivity.this, "Usuario o contraseña incorrectos.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Error al acceder a los datos locales.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        } else {
            // Modo offline: se valida leyendo la contraseña almacenada en el archivo interno
            try {
                String passLocal = cargarContraseñaLocal(auxArchivo);
                if (passLocal.equals(passwordIngresada)) {
                    continuarLogin(dniUsuario, passwordIngresada);
                } else {
                    Toast.makeText(MainActivity.this, "Usuario o contraseña incorrectos.", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException ex) {
                Toast.makeText(MainActivity.this, "Error al acceder a los datos locales.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void cambioContra(View view) {
        // Redirige a la actividad de cambio de contraseña.
        // "¿Olvidaste tu contraseña?" o forzar cambio
        String dniUsuario = t_codmod.getText().toString().trim();

        Intent intent = new Intent(MainActivity.this, CambioContrasenia.class);
        intent.putExtra(CambioContrasenia.EXTRA_USUARIO, dniUsuario);
        startActivity(intent);
    }

    public void dataExcelDocente() {
        // Reiniciar el contenido local de la información
        auxArrayExcelDocentes = "";

        // URL para obtener los docentes nuevos registrados
        String url = "https://ugelcorongo.pe/ugelasistencias_docente/model/excel/verExcelcargado.php";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = response.getJSONObject(i);
                                String nombreInstitucion = jsonObject.getString("nombre_institucion");
                                String documentoIdentidad = jsonObject.getString("documento_identidad");
                                String apellidoPaterno = jsonObject.getString("apellido_paterno");
                                String apellidoMaterno = jsonObject.getString("apellido_materno");
                                String nombres = jsonObject.getString("nombres");
                                String codmod_ie = jsonObject.getString("codmod_ie");
                                String tipoTrabajador = jsonObject.getString("tipo_trabajador");
                                String idDocenteExcel = jsonObject.getString("id");

                                // Construir el nombre completo del docente
                                String docente = apellidoPaterno + " " + apellidoMaterno + " " + nombres;

                                // Concatenar información en la variable auxiliar
                                auxArrayExcelDocentes += nombreInstitucion + ";" +
                                        documentoIdentidad + ";" +
                                        documentoIdentidad + ";" +
                                        tipoTrabajador + ";" +
                                        idDocenteExcel + ";" +
                                        "-8.81907000;-77.46168000;" + docente + "\n";

                                // Actualizar las listas para que luego se puedan determinar roles y otros datos
                                excelDocenteUser.add(documentoIdentidad);
                                excelDocenteColegio.add(nombreInstitucion);
                                excelDocentePass.add(documentoIdentidad);
                                excelDocenteTipo.add(tipoTrabajador);
                                excelDocenteNomCompleto.add(docente);
                                excelDocenteTurno.add(tipoTrabajador);
                                // Para los especialistas se usa una abreviación generada (Primera letra del nombre + apellido)
                                excelAbreviado.add((nombres.charAt(0) + apellidoPaterno).toUpperCase());
                                excelCodmod_ie.add(codmod_ie);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Error al cargar datos de docentes", Toast.LENGTH_SHORT).show();
                    }
                });

        // Agregar la solicitud a la cola de Volley
        requestQueue.add(jsonArrayRequest);
    }

    public interface ResultadoValidacion {
        /**
         * @param cambioRealizado Indica si la contraseña ya fue cambiada (true) o aún es la precargada (false).
         * @param nuevaContraseña La contraseña que se encuentra en el servidor (o vacía si no se obtuvo un valor válido).
         */
        void onResultado(boolean cambioRealizado, String nuevaContraseña);
    }

    public void validarCambioDeContraseñaEnServidor(final String dniUsuario, final ResultadoValidacion callback) {
        String url = URL_VER_CAMBIO + dniUsuario;

        JsonArrayRequest request = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            if (response.length() > 0) {
                                JSONObject jsonObject = response.getJSONObject(0);
                                // Obtiene el "password" del servidor en la respuesta
                                String contrasenaServidor = jsonObject.getString("password").trim();

                                // Si la contraseña en el servidor es igual al DNI, se considera que no se ha cambiado.
                                if (contrasenaServidor.equals(dniUsuario)) {
                                    callback.onResultado(false, contrasenaServidor);
                                } else {
                                    callback.onResultado(true, contrasenaServidor);
                                }
                            } else {
                                // Si no hay resultados, asumimos que no se realizó el cambio de la contraseña.
                                callback.onResultado(false, "");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callback.onResultado(false, "");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Borrar porque los datos se van a guardar en la base de datos, y no estará pre guardada
                        Toast.makeText(MainActivity.this, "Usuario no encontrado en la base de datos.", Toast.LENGTH_SHORT).show();
                        callback.onResultado(false, "");
                    }
                });

        requestQueue.add(request);
    }

    private void guardarContraseñaLocal(String auxArchivo, String contrasenia) {
        try (FileOutputStream fos = openFileOutput(auxArchivo, MODE_PRIVATE)) {
            fos.write(contrasenia.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String cargarContraseñaLocal(String auxArchivo) throws IOException {
        try (FileInputStream fis = openFileInput(auxArchivo);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {
            String savedPassword = br.readLine();
            return (savedPassword != null) ? savedPassword : "";
        }
    }

    private void continuarLogin(String dniUsuario, String passwordIngresada) {
        // Determinar el rol (Especialista, Director o Docente)
        String rol = determinarRol(dniUsuario, passwordIngresada);
        Intent home;

        switch (rol) {
            case "Especialista":
                //home = new Intent(MainActivity.this, Especialistas.class);
                home = new Intent(MainActivity.this, HomeEspecialista.class);
                home.putExtra("filtro", "Especialista");
                break;
            case "Director":
                //home = new Intent(MainActivity.this, login.class);
                home = new Intent(MainActivity.this, Director.class);
                home.putExtra("filtro", "Director");
                break;
            case "Docente":
                //home = new Intent(MainActivity.this, ListUsuario.class);
                home = new Intent(MainActivity.this, HomeDocente.class);
                home.putExtra("filtro", "Docente");
                break;
            default:
                Toast.makeText(MainActivity.this, "No se pudo determinar el rol del usuario.", Toast.LENGTH_SHORT).show();
                return;
        }

        // Extras que necesitan los activity's siguientes (Especialistas, login, ListUsuario)
        home.putExtra("codmod", dniUsuario);
        home.putExtra("password", passwordIngresada);

        home.putExtra("pass", passwordIngresada);
        home.putExtra("dni", dniUsuario);
        home.putExtra("turnos", rol);
        home.putExtra("idcolegio", str_idcolegio);
        home.putExtra("colegio", str_colegio);
        home.putExtra("docente", str_usuario);
        home.putExtra("notificacion","");
        home.putExtra("nivelcolegio", str_nivelcolegio);

        home.putExtra("ubicaciones", "");
        home.putExtra("docentesexcel", "");
        home.putExtra("codmodput", dniUsuario);

        // Inicia la actividad
        startActivity(home);
    }

    /**
     * - Si el usuario ingresado se encuentra en la lista de códigos modulares (listUsers), es Director.
     * - Si el usuario ingresado coincide con alguna de las abreviaciones en excelAbreviado, es Especialista.
     * - Si el usuario es numérico y tiene 8 dígitos, es Docente.
     * - Por defecto, se retorna "Docente".
     *
     * @param userInput     El valor ingresado en el campo de usuario (puede ser DNI, codmod o abreviación).
     * @param passwordInput El valor ingresado en el campo de contraseña (siempre se espera que sea el DNI).
     * @return Un String que indica el rol: "Director", "Especialista" o "Docente".
     */
    /**
     * Determina el rol del usuario según:
     * - Si el usuario ingresado se encuentra en listUsers (datos del txt), se toma el valor en listTipoUsuario.
     * - Si no se encuentra, se intenta buscar en los datos de Excel (excelDocenteUser y excelDocenteTipo).
     * - Si el input es numérico (entre 6 y 8 dígitos), se asume "Docente".
     * - Si coincide con alguna abreviación en excelAbreviado, se asume "Especialista".
     * Por defecto, se retorna "Docente".
     */
    private String determinarRol(String userInput, String passwordInput) {
        // Buscar primero en los registros precargados del txt
        for (int i = 0; i < listUsers.size(); i++) {
            if (listUsers.get(i).equals(userInput)) {
                String tipo = listTipoUsuario.get(i); // "Director", "Especialista" o "Docente"
                // Guardar datos adicionales para usar en el Intent de login
                str_idcolegio = listIdColegios.get(i);
                str_colegio = listColegios.get(i);
                str_nivelcolegio = listNivelesColegio.get(i);
                str_usuario = listDocente.get(i);
                if (tipo.equalsIgnoreCase("Director"))
                    return "Director";
                else if (tipo.equalsIgnoreCase("Especialista"))
                    return "Especialista";
                else if (tipo.equalsIgnoreCase("Docente"))
                    return "Docente";
                return "Docente";
            }
        }

        // Buscar en los datos cargados desde Excel
        for (int j = 0; j < excelDocenteUser.size(); j++) {
            if (excelDocenteUser.get(j).equals(userInput)) {
                String tipo = excelDocenteTipo.get(j); // Puede ser "Director Maniania", "Director Tarde", "Especialista" o "Docente"
                if (tipo.toLowerCase().contains("director maniana") || tipo.toLowerCase().contains("airector tarde"))
                    return "Director";
                else if (tipo.equalsIgnoreCase("especialista"))
                    return "Especialista";
                else
                    return "Docente";
            }
        }
        // Si no se encontró, si el input es numérico (entre 6 y 8 dígitos) se asume "Docente"
        if (userInput.matches("\\d{6,8}")) {
            return "Docente";
        }
        // Si el input coincide con alguna abreviación de especialistas, se asume "Especialista"
        if (excelAbreviado.contains(userInput.toUpperCase())) {
            return "Especialista";
        }
        return "Docente";
    }
}