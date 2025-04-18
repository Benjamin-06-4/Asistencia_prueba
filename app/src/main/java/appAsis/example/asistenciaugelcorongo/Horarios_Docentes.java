package appAsis.example.asistenciaugelcorongo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class Horarios_Docentes extends AppCompatActivity {

    // Datos recibidos por Intent
    private String colegio;
    private String idcolegio;
    private String docente;
    private String rol;  // Esperamos "Director" para esta actividad

    // Elementos de la interfaz
    // En este caso se usará el contenedor definido en el ScrollView
    private LinearLayout container;   // Debe tener el id "layout_list"
    private EditText txtSearch;       // Opcional, para búsqueda
    private TextView txtCricketer;    // Por ejemplo, para mostrar información en la cabecera
    private TextView txt_director;    // Otro TextView en la cabecera

    // Listas para almacenar datos obtenidos de R.raw.datadocentes
    private List<String> listColegios = new ArrayList<>();
    private List<String> listDocentes = new ArrayList<>();
    private List<String> listIdDocentes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horarios_docentes);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Asignar los elementos de la cabecera y el contenedor
        txtCricketer = findViewById(R.id.txtCricketer);
        txt_director = findViewById(R.id.txt_director);
        container = findViewById(R.id.layout_list); // Usamos el contenedor del ScrollView

        // Verificar que el contenedor se encuentre
        if (container == null) {
            Toast.makeText(this, "Contenedor no encontrado. Revisa el ID en el XML.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Obtener datos enviados en el Intent y comprobar que no sean nulos
        colegio = getIntent().getStringExtra("colegio");
        idcolegio = getIntent().getStringExtra("idcolegio");
        docente = getIntent().getStringExtra("docente");
        rol = getIntent().getStringExtra("rol");

        if (colegio == null || rol == null) {
            Toast.makeText(this, "Falta información esencial en el Intent", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Actualiza la cabecera (opcional)
        txtCricketer.setText(colegio);
        txt_director.setText(rol);

        // Cargar la información de los docentes
        leerDocentesDelArchivo();
        mostrarDocentes();
    }

    /**
     * Lee el archivo R.raw.datadocentes y guarda la información de docentes en las listas.
     */
    private void leerDocentesDelArchivo() {
        try {
            InputStream is = getResources().openRawResource(R.raw.datadocentes);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] parts = linea.split(";");
                if (parts.length < 7) continue;
                // Se asume que parts[0] es el nombre del colegio,
                // parts[2-4] conforman el nombre completo,
                // y parts[5] es el id del docente.
                String docenteNombre = parts[2] + " " + parts[3] + " " + parts[4];
                // Usar "parts[0].equals(colegio)" es más seguro si colegio puede ser nulo, pero ya lo comprobamos
                if (colegio.equals(parts[0])) {
                    listColegios.add(parts[0]);
                    listDocentes.add(docenteNombre);
                    listIdDocentes.add(parts[5]);
                }
            }
            is.close();
        } catch (Exception e) {
            Log.e("Horarios_Docentes", "Error leyendo docentes: ", e);
        }
    }

    /**
     * Infla el layout para cada docente (row_add_docentes_horarios.xml) y configura el botón para asignar horarios.
     */
    private void mostrarDocentes() {
        boolean noExiste = true;
        for (int i = 0; i < listColegios.size(); i++) {
            if (listColegios.get(i).equals(colegio) && rol.equals("Director")) {
                noExiste = false;
                View docenteView = getLayoutInflater().inflate(R.layout.row_add_docentes_horarios, container, false);

                // Asignar el nombre del docente y su identificador
                EditText editName = docenteView.findViewById(R.id.edit_docente_name);
                editName.setText(listDocentes.get(i));

                EditText txtIdDocente = docenteView.findViewById(R.id.txtiddocente);
                txtIdDocente.setText(listIdDocentes.get(i));

                // Configurar el botón btn_horario
                ImageButton btnHorario = docenteView.findViewById(R.id.btn_horario);
                btnHorario.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String currentDocente = editName.getText().toString();
                        String idDocente = txtIdDocente.getText().toString();
                        mostrarPopupHorario(currentDocente, idDocente);
                    }
                });

                container.addView(docenteView);
            }
        }
        if (noExiste) {
            TextView tv = new TextView(this);
            tv.setText("No se encontraron docentes para este colegio.");
            container.addView(tv);
        }
    }

    /**
     * Verifica si hay conexión a Internet.
     */
    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }

    /**
     * Muestra el diálogo para asignar horario (ingreso y salida) para el docente.
     */
    private void mostrarPopupHorario(String docenteNombre, String idDocente) {
        if (!isInternetAvailable()) {
            Toast.makeText(this, "Sin conexión a Internet. Inténtalo más tarde.", Toast.LENGTH_SHORT).show();
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(Horarios_Docentes.this);
        // Inflar el layout del diálogo (dialog_horario.xml)
        View dialogView = inflater.inflate(R.layout.dialog_horario, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(Horarios_Docentes.this);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        // Asegurarse de que los elementos existan en dialog_horario.xml
        TextView tvDocenteNombre = dialogView.findViewById(R.id.tv_docente_nombre);
        if (tvDocenteNombre == null) {
            Toast.makeText(this, "Error: Falta el TextView tv_docente_nombre en dialog_horario.xml", Toast.LENGTH_LONG).show();
            return;
        }
        tvDocenteNombre.setText(docenteNombre);

        EditText etHoraIngreso = dialogView.findViewById(R.id.et_horaIngreso);
        EditText etHoraSalida = dialogView.findViewById(R.id.et_horaSalida);

        Button btnSi = dialogView.findViewById(R.id.btn_si);
        Button btnNo = dialogView.findViewById(R.id.btn_no);

        btnSi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String horaIngreso = etHoraIngreso.getText().toString().trim();
                String horaSalida = etHoraSalida.getText().toString().trim();
                if (TextUtils.isEmpty(horaIngreso) || TextUtils.isEmpty(horaSalida)) {
                    Toast.makeText(Horarios_Docentes.this, "Ingresa ambos horarios", Toast.LENGTH_SHORT).show();
                    return;
                }
                enviarHorario(docenteNombre, idDocente, horaIngreso, horaSalida);
                dialog.dismiss();
            }
        });

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /**
     * Envía el horario asignado al servidor mediante POST.
     */
    private void enviarHorario(String docenteNombre, String idDocente, String horaIngreso, String horaSalida) {
        String url = "https://ugelcorongo.pe/ugelasistencias_docente/model/auxasistencia/asignarHorario.php";

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> Toast.makeText(Horarios_Docentes.this, "Horario asignado correctamente.", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(Horarios_Docentes.this, "Error al asignar horario.", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("colegio", colegio);
                params.put("idcolegio", idcolegio);
                params.put("docente", docenteNombre);
                params.put("iddocente", idDocente);
                params.put("horaIngreso", horaIngreso);
                params.put("horaSalida", horaSalida);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(postRequest);
    }
}