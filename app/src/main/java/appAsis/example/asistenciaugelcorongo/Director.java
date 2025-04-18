package appAsis.example.asistenciaugelcorongo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class Director extends AppCompatActivity {
    // Botón para registrar asistencia (se usará tanto por docente como director)
    private ImageButton btc_asistencia;
    private TextView txt_director;

    // Datos que pueden venir por intent (por ejemplo, desde el login)
    private String rol;       // "Docente" o "Director"
    private String colegio;   // Nombre del colegio
    private String idcolegio; // ID de la I.E.
    private String docente;   // Nombre o identificador del docente

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Forzar modo claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_director);

        // Inicializar el TextView
        txt_director = findViewById(R.id.lbl_director); // Asegúrate de que el ID coincide con el XML

        // Obtener datos enviados por intent
        colegio = getIntent().getStringExtra("colegio");
        idcolegio = getIntent().getStringExtra("idcolegio");
        docente = getIntent().getStringExtra("docente");
        rol = getIntent().getStringExtra("turnos"); // "Docente" o "Director"

        // Establecer el nombre del colegio en el TextView
        txt_director.setText(docente);
    }


    public void asistencias(View view) {
        Intent intent = new Intent(this, Docentes_Director.class);

        intent.putExtra("idcolegio",idcolegio);
        intent.putExtra("colegio",colegio);
        intent.putExtra("docente",docente);
        intent.putExtra("rol",rol);

        startActivity(intent);
    }

    public void horarios(View view) {
        Intent intent = new Intent(this, Horarios_Docentes.class);

        intent.putExtra("idcolegio",idcolegio);
        intent.putExtra("colegio",colegio);
        intent.putExtra("docente",docente);
        intent.putExtra("rol",rol);
        startActivity(intent);
    }

    public void docentes(View view) {
        //Intent intent = new Intent(this, DocentesActivity.class);
        //startActivity(intent);
    }
}