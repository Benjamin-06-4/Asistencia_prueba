package appAsis.example.asistenciaugelcorongo;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class FichasAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> fichas;

    public FichasAdapter(@NonNull Context context, @NonNull List<String> fichas) {
        super(context, 0, fichas);
        this.context = context;
        this.fichas = fichas;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.row_add_fichas_especialistas, parent, false);
        }

        // Recuperar la fila (row) y sus componentes
        EditText txtFichaTitle = convertView.findViewById(R.id.edit_docente_name);
        ImageButton btnHorario = convertView.findViewById(R.id.btn_horario);

        String fichaTitle = fichas.get(position);
        txtFichaTitle.setText(fichaTitle);
        // Hacemos que el EditText actúe como etiqueta (no editable)
        txtFichaTitle.setKeyListener(null);

        // Listener para el ImageButton de cada fila
        btnHorario.setOnClickListener(v -> {
            // Puedes iniciar la actividad donde se muestren las preguntas
            // para la ficha seleccionada (ej.: FichaQuestionsActivity)
            Intent intent = new Intent(context, FichaQuestionsActivity.class);
            intent.putExtra("fichaTitle", fichaTitle);
            context.startActivity(intent);
        });

        // También se puede asignar el listener a toda la fila
        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FichaQuestionsActivity.class);
            intent.putExtra("fichaTitle", fichaTitle);
            context.startActivity(intent);
        });

        return convertView;
    }
}