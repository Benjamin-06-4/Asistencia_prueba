package appAsis.example.asistenciaugelpomabamba2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ListUsuario extends AppCompatActivity implements View.OnClickListener {

    public TextView l_Colegio, bienvenida;
    public TextView t_nombreUsuario, t_nombreDocente, t_turno, t_idcolegio, t_dniPutExtra, t_colegioPutExtra;
    String idcolegio, colegio, linea, putDocente, turnoPutExtra, dniPutExtra, docentePutExtra, colegioPutExtra, getPutFiltrar, getPutDni;
    ImageButton imagenRelojButton;
    LinearLayout layoutList;
    List<String> listDocentes = new ArrayList<String>(), listColegios = new ArrayList<String>(), listIdcolegio = new ArrayList<String>();
    List<String> listIdDocentes = new ArrayList<String>(), listTurnos = new ArrayList<String>(), listDnidocente = new ArrayList<String>();
    List<String> listExcelDocenteNomCompleto = new ArrayList<String>();

    boolean exists = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_usuario);

        //l_Notifica = (TextView) findViewById(R.id.lblMensajeNotifica);
        l_Colegio = (TextView) findViewById(R.id.lblColegio);
        bienvenida = (TextView) findViewById(R.id.l_colegio);
        bienvenida.setText("Docente: ");

        //t_password = findViewById(R.id.txtPassword);
        colegio = getIntent().getStringExtra("pass");
        String turnos = getIntent().getStringExtra("turnos");
        String notificacion = getIntent().getStringExtra("notificacion");
        idcolegio = getIntent().getStringExtra("idcolegio");
        putDocente = getIntent().getStringExtra("docente");
        getPutFiltrar = getIntent().getStringExtra("filtro");
        getPutDni = getIntent().getStringExtra("dni");

        layoutList = findViewById(R.id.layout_list);
        l_Colegio.setText(putDocente);

        InputStream is = this.getResources().openRawResource(R.raw.datadocentexcole);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            if(is!=null){
                while((linea = reader.readLine())!=null){
                    listColegios.add(linea.split(";")[0]);
                    listDocentes.add(linea.split(";")[2] + ' ' + linea.split(";")[3] + ' ' + linea.split(";")[4]);
                    listIdDocentes.add(linea.split(";")[5]);
                    listTurnos.add(linea.split(";")[6]);
                    listIdcolegio.add(linea.split(";")[7]);
                    listDnidocente.add(linea.split(";")[1]);
                }
            }
            is.close();
        }catch (Exception e){}

        //32600070
        //ELIMINAR

        boolean excelDocente = false; String excelDocenteNomCompleto = "";
        String guardaInfotextfile = "";
        String auxExcelUbicaciones = "dataExcelDocentes.txt";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Path path = Files.createTempFile("dataExcelDocentes", ".txt");
                exists = Files.exists(path);     //true
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(exists) {
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = openFileInput(auxExcelUbicaciones);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String lineaTexto;
                StringBuilder stringBuilder = new StringBuilder();
                while ((lineaTexto = bufferedReader.readLine()) != null) {
                    stringBuilder.append(lineaTexto).append("\n");

                    String[] parts = lineaTexto.split(";");
                    String part1 = parts[7]; // DOCENTE

                    listExcelDocenteNomCompleto.add(part1);
                    if(part1.equals(putDocente)){
                        excelDocente = true;
                        excelDocenteNomCompleto = part1;
                    }
                }
            } catch (Exception ex) {
            } finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (Exception e) {

                    }
                }
            }
        }
        //ELIMINAR

        boolean noexiste = true;
        Class claseCargo = login.class;;
        for (int i = 0; i < listColegios.size(); i++) {
            if(listDocentes.get(i).equals(putDocente)){
                noexiste = false;
                if(excelDocente){
                    putDocente = excelDocenteNomCompleto;
                }

                String idforDocente = listIdDocentes.get(i);
                View docenteView = getLayoutInflater().inflate(R.layout.row_add_usuario,null,false);
                EditText editText = (EditText) docenteView.findViewById(R.id.txt_usuario);
                t_nombreUsuario = docenteView.findViewById(R.id.txt_nombreUsuario);
                t_turno = docenteView.findViewById(R.id.txt_turno);
                t_idcolegio = docenteView.findViewById(R.id.txt_idcolegio);
                t_dniPutExtra = docenteView.findViewById(R.id.txt_dniPutExtra);
                t_nombreDocente = docenteView.findViewById(R.id.txt_nombreDocente);
                t_colegioPutExtra = docenteView.findViewById(R.id.txt_colegioPutExtra);

                editText.setText(listColegios.get(i));
                t_nombreUsuario.setText(listColegios.get(i));

                turnoPutExtra =  listTurnos.get(i).toString();
                t_turno.setText(listTurnos.get(i).toString());

                idcolegio = listIdcolegio.get(i).toString();
                t_idcolegio.setText(listIdcolegio.get(i).toString());

                dniPutExtra = listDnidocente.get(i).toString();
                t_dniPutExtra.setText(listDnidocente.get(i).toString());

                docentePutExtra = listDocentes.get(i).toString();
                t_nombreDocente.setText(listDocentes.get(i).toString());

                colegioPutExtra = listColegios.get(i).toString();
                t_colegioPutExtra.setText("t: " + t_turno.getText().toString() + listColegios.get(i).toString());

                imagenRelojButton =  docenteView.findViewById(R.id.btnReloj);
                imagenRelojButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        t_nombreUsuario = docenteView.findViewById(R.id.txt_nombreUsuario);
                        t_turno = docenteView.findViewById(R.id.txt_turno);
                        t_idcolegio = docenteView.findViewById(R.id.txt_idcolegio);
                        t_dniPutExtra = docenteView.findViewById(R.id.txt_dniPutExtra);
                        t_nombreDocente = docenteView.findViewById(R.id.txt_nombreDocente);
                        t_colegioPutExtra = docenteView.findViewById(R.id.txt_colegioPutExtra);
                        Intent home = new Intent(ListUsuario.this, claseCargo);

                        home.putExtra("pass", t_nombreUsuario.getText().toString());
                        home.putExtra("dni", t_dniPutExtra.getText().toString());
                        home.putExtra("turnos", t_turno.getText().toString());
                        home.putExtra("idcolegio", t_idcolegio.getText().toString());
                        home.putExtra("docente", putDocente);
                        home.putExtra("notificacion",notificacion);
                        home.putExtra("filtro", getPutFiltrar);
                        startActivity(home);
                    }
                });
                layoutList.addView(docenteView);
            }
        }

        if(noexiste){
            String noTurno = "", noIdcolegio = "";
            for (int i = 0; i < listColegios.size(); i++) {
                if (listColegios.get(i).equals(colegio)) {
                    noTurno = listTurnos.get(i);
                    noIdcolegio = listIdcolegio.get(i);
                }
            }

            View docenteView = getLayoutInflater().inflate(R.layout.row_add_usuario, null, false);
            EditText editText = (EditText) docenteView.findViewById(R.id.txt_usuario);
            t_nombreUsuario = docenteView.findViewById(R.id.txt_nombreUsuario);
            t_turno = docenteView.findViewById(R.id.txt_turno);
            t_idcolegio = docenteView.findViewById(R.id.txt_idcolegio);
            t_dniPutExtra = docenteView.findViewById(R.id.txt_dniPutExtra);
            t_nombreDocente = docenteView.findViewById(R.id.txt_nombreDocente);
            t_colegioPutExtra = docenteView.findViewById(R.id.txt_colegioPutExtra);

            editText.setText(colegio);
            t_nombreUsuario.setText(colegio);

            turnoPutExtra = noTurno;
            t_turno.setText(noTurno);

            idcolegio = noIdcolegio;
            t_idcolegio.setText(noIdcolegio);

            dniPutExtra = getPutDni;
            t_dniPutExtra.setText(getPutDni);

            docentePutExtra = putDocente;
            t_nombreDocente.setText(putDocente);

            colegioPutExtra = colegio;
            t_colegioPutExtra.setText("t: " + t_turno.getText().toString() + colegio);

            imagenRelojButton = docenteView.findViewById(R.id.btnReloj);
            imagenRelojButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    t_nombreUsuario = docenteView.findViewById(R.id.txt_nombreUsuario);
                    t_turno = docenteView.findViewById(R.id.txt_turno);
                    t_idcolegio = docenteView.findViewById(R.id.txt_idcolegio);
                    t_dniPutExtra = docenteView.findViewById(R.id.txt_dniPutExtra);
                    t_nombreDocente = docenteView.findViewById(R.id.txt_nombreDocente);
                    t_colegioPutExtra = docenteView.findViewById(R.id.txt_colegioPutExtra);
                    Intent home = new Intent(ListUsuario.this, claseCargo);

                    home.putExtra("pass", t_nombreUsuario.getText().toString());
                    home.putExtra("dni", t_dniPutExtra.getText().toString());
                    home.putExtra("turnos", t_turno.getText().toString());
                    home.putExtra("idcolegio", t_idcolegio.getText().toString());
                    home.putExtra("docente", putDocente);
                    home.putExtra("notificacion", notificacion);
                    home.putExtra("filtro", getPutFiltrar);
                    startActivity(home);
                }
            });
            layoutList.addView(docenteView);
        }
    }
    private void addView() {
        View docenteView = getLayoutInflater().inflate(R.layout.row_add_usuario,null,false);
        EditText editText = (EditText) docenteView.findViewById(R.id.txt_usuario);
    }

    @Override
    public void onClick(View view) {
        addView();
    }
}