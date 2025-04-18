package appAsis.example.asistenciaugelcorongo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDialogFragment;

public class Archivos extends AppCompatDialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        if (activity == null) {
            throw new NullPointerException("La actividad es nula");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("INFORMACIÓN")
                .setMessage("Mensaje de prueba") // Agrega un mensaje no nulo
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Aquí puedes agregar la lógica del botón "ok"
                    }
                });
        return builder.create();
    }

}
