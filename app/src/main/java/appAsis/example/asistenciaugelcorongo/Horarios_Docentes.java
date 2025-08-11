package appAsis.example.asistenciaugelcorongo;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class Horarios_Docentes extends AppCompatActivity {

    // Datos recibidos por Intent
    private String colegio, idcolegio, docente, rol;
    private LinearLayout container;
    private List<String> listColegios = new ArrayList<>();
    private List<String> listDocentes = new ArrayList<>();
    private List<String> listIdDocentes = new ArrayList<>();

    // Para validar tiempos 00–23:00–59
    private static final Pattern TIME24 =
            Pattern.compile("^([01]\\d|2[0-3]):([0-5]\\d)$");

    private ScheduleManager mgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horarios_docentes);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        container = findViewById(R.id.layout_list);

        colegio   = getIntent().getStringExtra("colegio");
        idcolegio = getIntent().getStringExtra("idcolegio");
        docente   = getIntent().getStringExtra("docente");
        rol       = getIntent().getStringExtra("rol");

        if (colegio == null || rol == null) {
            Toast.makeText(this,
                    "Falta información esencial", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mgr = ScheduleManager.get(this);

        leerDocentesDelArchivo();
        mostrarDocentes();
    }

    private void leerDocentesDelArchivo() {
        try {
            java.io.InputStream isStream = getResources()
                    .openRawResource(R.raw.datadocentes);
            java.io.BufferedReader reader   = new java.io.BufferedReader(
                    new java.io.InputStreamReader(isStream));
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] p = linea.split(";");
                if (p.length < 7) continue;
                if (colegio.equals(p[0])) {
                    String nombre = p[2]+" "+p[3]+" "+p[4];
                    listColegios.add(p[0]);
                    listDocentes.add(nombre);
                    listIdDocentes.add(p[5]);
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarDocentes() {
        boolean noExiste = true;
        for (int i = 0; i < listDocentes.size(); i++) {
            if (!"Director".equals(rol)) break;
            noExiste = false;

            View row = getLayoutInflater()
                    .inflate(R.layout.row_add_docentes_horarios,
                            container, false);

            EditText editName = row.findViewById(R.id.edit_docente_name);
            EditText txtId    = row.findViewById(R.id.txtiddocente);
            ImageButton btnH  = row.findViewById(R.id.btn_horario);

            editName.setText(listDocentes.get(i));
            txtId   .setText(listIdDocentes.get(i));

            btnH.setOnClickListener(v -> {
                String nom = editName.getText().toString();
                String id  = txtId.getText().toString();
                showScheduleDialog(nom, id);
            });

            container.addView(row);
        }

        if (noExiste) {
            TextView tv = new TextView(this);
            tv.setText("No se encontraron docentes para este colegio.");
            container.addView(tv);
        }
    }

    /** Comprueba Internet */
    private boolean isInternet() {
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm != null ? cm.getActiveNetworkInfo() : null;
        return ni != null && ni.isConnected();
    }

    /** Diálogo completo de asignación */
    private void showScheduleDialog(String nom, String idDoc) {
        LayoutInflater inf = LayoutInflater.from(this);
        View v = inf.inflate(R.layout.dialog_horario, null);
        AlertDialog dlg = new AlertDialog.Builder(this)
                .setView(v).create();

        TextView tvNom = v.findViewById(R.id.tv_docente_nombre);
        tvNom.setText(nom);

        EditText etIn = v.findViewById(R.id.et_horaIngreso);
        EditText etOut= v.findViewById(R.id.et_horaSalida);

        // CheckBoxes
        CheckBox cbL = v.findViewById(R.id.cb_lun);
        CheckBox cbM = v.findViewById(R.id.cb_mar);
        CheckBox cbX = v.findViewById(R.id.cb_mie);
        CheckBox cbJ = v.findViewById(R.id.cb_jue);
        CheckBox cbV = v.findViewById(R.id.cb_vie);
        List<CheckBox> dias = List.of(cbL,cbM,cbX,cbJ,cbV);

        // TimePicker listeners
        etIn .setInputType(0);
        etIn .setFocusable(false);
        etIn .setOnClickListener( x -> pickTime(etIn) );
        etOut.setInputType(0);
        etOut.setFocusable(false);
        etOut.setOnClickListener(x -> pickTime(etOut));

        // Carga inicial (online→local)
        new Thread(() -> {
            if (isInternet()) {
                try {
                    mgr.loadRemote(
                            "https://tu.server/datahorarios.txt"
                    );
                } catch (Exception ignored) {}
            }
            mgr.loadLocal();
            new Handler(Looper.getMainLooper())
                    .post(() -> preloadUI(nom, dias, etIn, etOut));
        }).start();

        Button btnSave   = v.findViewById(R.id.btn_save);
        Button btnDelete = v.findViewById(R.id.btn_delete);
        Button btnNo     = v.findViewById(R.id.btn_cancel);

        btnSave.setOnClickListener(x -> {
            String ing = etIn .getText().toString().trim();
            String out = etOut.getText().toString().trim();

            if (!TIME24.matcher(ing).matches() ||
                    !TIME24.matcher(out).matches()) {
                Toast.makeText(this,
                        "Formato inválido. Usa HH:mm (00–23,00–59)",
                        Toast.LENGTH_LONG).show();
                return;
            }
            if (!isAfter(ing,out)) {
                Toast.makeText(this,
                        "Hora de salida debe ser posterior",
                        Toast.LENGTH_LONG).show();
                return;
            }

            List<String> sel = new ArrayList<>();
            for (CheckBox cb : dias)
                if (cb.isChecked()) sel.add(cb.getText().toString());
            if (sel.isEmpty()) {
                Toast.makeText(this,
                        "Selecciona al menos un día",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                try {
                    for (String d : sel) {
                        mgr.upsert(
                                new ScheduleManager.Schedule(
                                        colegio, nom, d, ing, out
                                )
                        );
                    }
                    runOnUiThread(() -> {
                        updateColoring(dias);
                        for (String d : sel) {
                            enviarHorario(nom, idDoc, d, ing, out);
                        }
                        //Toast.makeText(this,"Guardado", Toast.LENGTH_SHORT).show();
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(
                            this, "Error: "+e.getMessage(),
                            Toast.LENGTH_LONG).show());
                }
            }).start();

            dlg.dismiss();
        });

        btnDelete.setOnClickListener(x -> {
            List<String> sel = new ArrayList<>();
            for (CheckBox cb : dias)
                if (cb.isChecked()) sel.add(cb.getText().toString());
            if (sel.isEmpty()) {
                Toast.makeText(this,
                        "Marca día(s) para eliminar",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            new Thread(() -> {
                try {
                    mgr.delete(colegio, nom, sel);
                    runOnUiThread(() -> {
                        for (CheckBox cb : dias)
                            cb.setChecked(false);
                        etIn .setText("");
                        etOut.setText("");
                        updateColoring(dias);
                        Toast.makeText(this,
                                "Eliminado", Toast.LENGTH_SHORT
                        ).show();
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(
                            this, "Error: "+e.getMessage(),
                            Toast.LENGTH_LONG).show());
                }
            }).start();
        });

        btnNo.setOnClickListener(x -> dlg.dismiss());
        dlg.show();
    }

    /**
     * Envía el horario asignado al servidor mediante POST.
     */
    private void enviarHorario(String docenteNombre, String idDocente, String dia, String horaIngreso, String horaSalida) {
        String url = "https://ugelcorongo.pe/ugelasistencias_docente/model/auxasistencia/asignarHorario.php";

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> Toast.makeText(Horarios_Docentes.this, "Horario asignado correctamente", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(Horarios_Docentes.this, "Horario Guardado de manera Local", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("colegio", colegio);
                params.put("idcolegio", idcolegio);
                params.put("docente", docenteNombre);
                params.put("iddocente", idDocente);
                params.put("dia", dia);
                params.put("horaIngreso", horaIngreso);
                params.put("horaSalida", horaSalida);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(postRequest);
    }

    /** Pre-carga horas y estado de casillas */
    private void preloadUI(String nom,
                           List<CheckBox> dias,
                           EditText etIn, EditText etOut) {

        for (CheckBox cb : dias) {
            String d = cb.getText().toString();
            List<ScheduleManager.Schedule> lst =
                    mgr.filter(colegio, nom, d);
            if (!lst.isEmpty()) {
                cb.setChecked(true);
                etIn .setText(lst.get(0).ingreso);
                etOut.setText(lst.get(0).salida);
            } else {
                cb.setChecked(false);
            }
        }
        updateColoring(dias);
    }

    /** Forzar redraw para bg selector */
    private void updateColoring(List<CheckBox> dias) {
        for (CheckBox cb : dias)
            cb.refreshDrawableState();
    }

    /** Muestra un TimePickerDialog */
    private void pickTime(EditText target) {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(
                this,
                (TimePicker tp,int h,int m) -> {
                    target.setText(
                            String.format(Locale.getDefault(),
                                    "%02d:%02d", h, m)
                    );
                },
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
        ).show();
    }

    /** Comprueba salida > ingreso */
    private boolean isAfter(String in, String out) {
        try {
            SimpleDateFormat df =
                    new SimpleDateFormat("HH:mm",Locale.getDefault());
            Date d1 = df.parse(in);
            Date d2 = df.parse(out);
            return d2.after(d1);
        } catch (ParseException e) {
            return false;
        }
    }
}