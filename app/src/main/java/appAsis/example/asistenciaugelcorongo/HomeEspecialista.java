package appAsis.example.asistenciaugelcorongo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.DialogInterface;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeEspecialista extends AppCompatActivity {
    // Botón para registrar asistencia (se usará tanto por docente como director)
    private TextView txt_director;

    // Datos que pueden venir por intent
    private String rol;       // "Docente" o "Director"
    private String colegio;   // Nombre del colegio
    private String idcolegio; // ID de la I.E.
    private String docente;   // Nombre o identificador del docente

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Forzar modo claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_especialista);

        // Recuperar datos enviados por Intent
        colegio = getIntent().getStringExtra("colegio");
        idcolegio = getIntent().getStringExtra("idcolegio");
        docente = getIntent().getStringExtra("docente");
        rol = getIntent().getStringExtra("turnos");

        txt_director = findViewById(R.id.lbl_especialista);
        txt_director.setText(docente);
    }

    /**
     * Este método se invoca al pulsar el botón para mostrar las fichas.
     */
    public void fichas_especialistas(View view) {
        fetchFichas();
    }

    /**
     * Verifica si hay conexión a Internet.
     */
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return (netInfo != null && netInfo.isConnected());
        }
        return false;
    }

    /**
     * Método para obtener las fichas:
     * - Con conexión: consulta la URL, guarda TODOS los registros (activos e inactivos) en el archivo interno,
     *   y luego muestra solo las fichas activas.
     * - Sin conexión: lee el archivo y filtra las fichas activas comprobando también el rango de fechas.
     */
    private void fetchFichas() {
        if (isOnline()) {
            String urlFichas = "https://ugelcorongo.pe/ugelasistencias_docente/model/especialista/verFichas.php";
            RequestQueue queue = Volley.newRequestQueue(HomeEspecialista.this);
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                    Request.Method.GET,
                    urlFichas,
                    null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            // Guarda TODAS las fichas en datafichas.txt (activos e inactivos)
                            guardarFichasEnArchivo(response.toString());
                            // Muestra el diálogo filtrando solo las fichas activas (según el campo estado)
                            mostrarDialogoFichas(response, false);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Si hay error, se recurre a leer el archivo local
                            readFichasFromFile();
                        }
                    }
            );
            queue.add(jsonArrayRequest);
        } else {
            // Sin conexión: leer la información almacenada en datafichas.txt y filtrar usando el rango de fechas.
            readFichasFromFile();
        }
    }

    /**
     * Guarda la cadena recibida en un archivo interno.
     */
    private void guardarFichasEnArchivo(String data) {
        try {
            FileOutputStream fos = openFileOutput("datafichas.txt", Context.MODE_PRIVATE);
            fos.write(data.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lee el archivo datafichas.txt y muestra la información.
     */
    private void readFichasFromFile() {
        try {
            FileInputStream fis = openFileInput("datafichas.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            fis.close();
            String data = sb.toString();
            if (data.trim().startsWith("[")) {
                JSONArray jsonArray = new JSONArray(data);
                // En modo offline se filtra también comprobando que la fecha actual esté dentro del rango
                jsonArray = filtrarFichasActivasOffline(jsonArray);
                mostrarDialogoFichas(jsonArray, true);
            } else {
                // Caso de archivo en formato de texto plano (línea por línea)
                ArrayList<JSONObject> fichasList = new ArrayList<>();
                String[] lines = data.split("\n");
                for (String l : lines) {
                    if (l.trim().isEmpty()) continue;
                    // Se espera formato: nombre ficha; fecha_inicio; fecha_termino; estado
                    String[] parts = l.split(";");
                    if (parts.length >= 4) {
                        JSONObject obj = new JSONObject();
                        obj.put("nombre", parts[0].trim());
                        obj.put("fecha_inicio", parts[1].trim());
                        obj.put("fecha_termino", parts[2].trim());
                        obj.put("estado", parts[3].trim());
                        fichasList.add(obj);
                    }
                }
                JSONArray jsonArray = new JSONArray();
                for (JSONObject obj : fichasList) {
                    jsonArray.put(obj);
                }
                // Se filtra en modo offline
                jsonArray = filtrarFichasActivasOffline(jsonArray);
                mostrarDialogoFichas(jsonArray, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // En caso de error se muestra el diálogo sin registros
            mostrarDialogoFichas(new JSONArray(), true);
        }
    }

    /**
     * En modo offline, filtra las fichas comprobando que:
     * 1) El estado sea "activo"
     * 2) La fecha actual se encuentre entre fecha_inicio y fecha_termino
     */
    private JSONArray filtrarFichasActivasOffline(JSONArray fichas) {
        JSONArray result = new JSONArray();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date now = new Date();

            // Recorrer cada ficha y actualizar su estado según el rango de fechas
            for (int i = 0; i < fichas.length(); i++) {
                JSONObject obj = fichas.getJSONObject(i);
                Date inicio = sdf.parse(obj.getString("fecha_inicio"));
                Date fin = sdf.parse(obj.getString("fecha_termino"));

                // Si la fecha actual se encuentra entre inicio y fin, la ficha es activa
                if (now.compareTo(inicio) >= 0 && now.compareTo(fin) <= 0) {
                    obj.put("estado", "activo");
                    result.put(obj);
                } else {
                    obj.put("estado", "inactivo");
                }
            }

            // Actualizamos el archivo interno con la versión modificada del JSONArray
            guardarFichasEnArchivo(fichas.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Construye y muestra el diálogo con la lista de fichas.
     * Si el parámetro offline es true, se asume que ya se filtró usando el rango de fechas.
     * En caso contrario, se filtra solo por el campo "estado".
     */
    private void mostrarDialogoFichas(JSONArray fichas, boolean offline) {
        final List<String> fichasList = new ArrayList<>();
        try {
            for (int i = 0; i < fichas.length(); i++) {
                JSONObject obj = fichas.getJSONObject(i);
                // En modo online se filtra por estado activo,
                // y en modo offline ya se pasó por la validación del rango de fechas.
                if (offline) {
                    fichasList.add(obj.getString("nombre"));
                } else {
                    if (obj.getString("estado").equalsIgnoreCase("activo")) {
                        fichasList.add(obj.getString("nombre"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View popupView = getLayoutInflater().inflate(R.layout.dialog_fichas, null);
        builder.setView(popupView);
        builder.setTitle("Fichas");

        builder.setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        ListView listViewFichas = popupView.findViewById(R.id.listFichas);
        FichasAdapter adapter = new FichasAdapter(this, fichasList);
        listViewFichas.setAdapter(adapter);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}