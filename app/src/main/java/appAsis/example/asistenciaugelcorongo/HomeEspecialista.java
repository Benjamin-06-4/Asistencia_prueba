package appAsis.example.asistenciaugelcorongo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.DialogInterface;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

public class HomeEspecialista extends AppCompatActivity {
    // Botón para registrar asistencia (se usará tanto por docente como director)
    private TextView txt_director;

    // Datos que pueden venir por intent (por ejemplo, desde el login)
    private String rol;       // "Docente" o "Director"
    private String colegio;   // Nombre del colegio
    private String idcolegio; // ID de la I.E.
    private String docente;   // Nombre o identificador del docente

    // Otros atributos y variables (por ejemplo, para datos del usuario) se obtienen en onCreate
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
        // Establecer el nombre del colegio en el TextView
        txt_director.setText(docente);
    }

    /**
     * Este metodo se llama al pulsar el botón "btc_asistencias_director" (fichas)
     */
    public void asistencias(android.view.View view) {
        showFichasPopup();
    }

    /**
     * Metodo para construir y mostrar el popup con la lista de fichas.
     */
    private void showFichasPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Inflar el layout del popup
        android.view.View popupView = getLayoutInflater().inflate(R.layout.dialog_fichas, null);
        builder.setView(popupView);
        builder.setTitle("Fichas");

        // Agregar un botón para cerrar el popup
        builder.setNegativeButton("Cerrar", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Referencia al ListView definido en dialog_fichas.xml
        ListView listViewFichas = popupView.findViewById(R.id.listFichas);

        // Lista estática de títulos de fichas
        List<String> fichasList = new ArrayList<>();
        fichasList.add("1. FICHA DE MONITOREO ESTRATEGIA REFUERZO ESCOLAR 2025 UGEL CORONGO");
        fichasList.add("2. FICHA DE REPORTE DE PROCESO DE AVANCE DEL PLAN LECTOR EN LAS IIEE");
        fichasList.add("3. FICHA DE MONITOREO DEL MANEJO DEL SIAGIE 2025");
        fichasList.add("4. FICHA DE MONITOREO AL SERVICIO DE TUTORÍA Y ORIENTACIÓN EDUCATIVA");
        fichasList.add("5. MONITOREO Y ACOMPAÑAMIENTO AL DESEMPEÑO DOCENTE – MADD 5C 2024");
        fichasList.add("6. FICHA DE MONITOREO - GESTIÓN DE LA CONVIVVENCIA ESCOLAR");
        fichasList.add("7. FICHA DE MONITOREO DE LA ESTRATEGIA VIDA ACTIVA, CREATIVA Y SALUDABLE - VAS");
        fichasList.add("8. FICHA DE MONITOREO DEL TRABAJO COLEGIADO - TC");
        fichasList.add("9. MONITOREO Y ACOMPAÑAMIENTO AL DESEMPEÑO PEDAGÓGICO DEL DIRECTOR");

        // Crear el adapter y asignarlo al ListView
        FichasAdapter adapter = new FichasAdapter(this, fichasList);
        listViewFichas.setAdapter(adapter);

        // Mostrar el popup
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}