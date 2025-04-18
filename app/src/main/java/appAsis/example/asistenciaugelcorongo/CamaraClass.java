package appAsis.example.asistenciaugelcorongo;

import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

public class CamaraClass {
    Context context;
    TextView docente;
    String horanow;
    String fechaVeri;
    String rutaImagen;
    View docenteView;

    public CamaraClass(Context context, TextView docente, String horanow, String fechaVeri, String rutaImagen,View docenteView){
        super();
        this.context = context;
        this.docente = docente;
        this.horanow = horanow;
        this.fechaVeri = fechaVeri;
        this.rutaImagen = rutaImagen;
        this.docenteView = docenteView;
    }
    public File crearImagen(Context context, TextView docente, String horanow, String fechaVeri, String rutaImagen) throws IOException {
        String nombreImagen = "foto_" + docente.getText().toString() + "_" + horanow;
        File directorio = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imagen = File.createTempFile(nombreImagen,".jpg",directorio);

        this.rutaImagen = imagen.getAbsolutePath();
        return imagen;
    }
}
