package appAsis.example.asistenciaugelcorongo;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Metodos extends DialogFragment {
    private AlertDialog.Builder builder;
    private Dialog dialog;
    private Context context;
    private String[] data;
    public String select = "Injustificada", colegio, idcolegio, turno, conn = "", horanow, fechaVeri, url, t_justificacioninputaux = "SIN ARGUMENTO";
    public String latitud, longitud, ubicacion;
    ImageButton entrada, salida, inasistencia;
    TextView t_nombreDocente, t_iddocente, hora, t_inputjustificacion;
    View docenteView;
    boolean exists = false;

    String ModelInfodevice = Build.SERIAL;
    String IdInfodevice = Build.ID;
    String ManufactInfodevice = Build.MANUFACTURER;
    String BrandInfodevice = Build.BRAND;
    String TypeInfodevice = Build.TYPE;
    String UserInfodevice = Build.USER;
    String BaseInfodevice = String.valueOf(Build.VERSION_CODES.BASE);
    String SdkInfodevice = Build.VERSION.SDK;
    String BoardInfodevice = Build.BOARD;
    String HostInfodevice = Build.HOST;
    String FingeprintInfodevice = Build.FINGERPRINT;
    String VCodeInfodevice = Build.VERSION.RELEASE;
    public Metodos(Context context,String[] data, String turno, ImageButton entrada, ImageButton salida, ImageButton inasistencia,
                   TextView t_nombreDocente, String colegio, String idcolegio, TextView t_iddocente,
                   TextView hora, String horanow, String fechaVeri,View docenteView, String url,
                   String latitud, String longitud, String ubicacion){
        super();
        this.context = context;
        this.data = data;
        this.turno = turno;
        this.entrada = docenteView.findViewById(R.id.btnReloj);
        this.salida = docenteView.findViewById(R.id.btnSalida);
        this.inasistencia = docenteView.findViewById(R.id.btnInasistencia);

        this.t_nombreDocente = t_nombreDocente;
        this.colegio = colegio;
        this.idcolegio = idcolegio;
        this.t_iddocente = t_iddocente;
        this.hora = docenteView.findViewById(R.id.txtHora);
        this.horanow = horanow;;
        this.fechaVeri = fechaVeri;
        this.docenteView = docenteView;
        this.url = url;

        this.latitud = latitud;
        this.longitud = longitud;
        this.ubicacion = ubicacion;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState){
        builder = new AlertDialog.Builder(context);
        builder.setTitle("JUSTIFICACION");
        builder.setSingleChoiceItems(data, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                select = data[i];
            }
        });
        builder.setPositiveButton("Enviar",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                t_nombreDocente.setBackgroundColor(Color.GRAY);
                hora.setText("Reporte");

                String t_nombreDocenteb = t_nombreDocente.getText().toString();
                String t_iddocenteb = t_iddocente.getText().toString();

                ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
                boolean auxConexion = connectivityManager.getActiveNetwork() != null && connectivityManager.getActiveNetworkInfo().isConnected();

                if (auxConexion != true) {
                    conn = "SIN CONEXION";
                }else {
                    conn = "CON CONEXION";
                    //String inasisJustificaurl = "https://ugelpomabamba.gob.pe/ugel/ugelasistencias_docente/sesionespecialista.php";
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            //Toast.makeText(getApplicationContext(),"OPERACIÓN EXITOSA", Toast.LENGTH_SHORT).show();
                        }
                    }, error -> Toast.makeText(context.getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show()) {
                        @Nullable
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> parametros = new HashMap<String, String>();

                            parametros.put("colegio", colegio);
                            parametros.put("docente", t_nombreDocenteb);
                            parametros.put("t_llegada", horanow);
                            parametros.put("fecha_registro", fechaVeri);
                            parametros.put("turno", turno);
                            parametros.put("asunto", "REPORTE INASISTENCIA " + conn);
                            parametros.put("FK_idcolegio", idcolegio);
                            parametros.put("FK_iddocente", t_iddocenteb);
                            parametros.put("problema", select);
                            parametros.put("argumento", t_inputjustificacion.getText().toString());

                            parametros.put("latitud", latitud);
                            parametros.put("longitud", longitud);
                            parametros.put("ubicacion", ubicacion);

                            parametros.put("ModelInfodevice", ModelInfodevice);
                            parametros.put("IdInfodevice", IdInfodevice);
                            parametros.put("ManufactInfodevice", ManufactInfodevice);
                            parametros.put("BrandInfodevice", BrandInfodevice);
                            parametros.put("TypeInfodevice", TypeInfodevice);
                            parametros.put("UserInfodevice", UserInfodevice);
                            parametros.put("BaseInfodevice", BaseInfodevice);
                            parametros.put("SdkInfodevice", SdkInfodevice);
                            parametros.put("BoardInfodevice", BoardInfodevice);
                            parametros.put("HostInfodevice", HostInfodevice);
                            parametros.put("FingeprintInfodevice", FingeprintInfodevice);
                            parametros.put("VCodeInfodevice", VCodeInfodevice);
                            return parametros;
                        }
                    };
                    RequestQueue requestQueue = Volley.newRequestQueue(context);
                    requestQueue.add(stringRequest);
                }
                String retornar = leerArchivoJustificar();

                String nomAsunto = "REPORTE INASISTENCIA " + conn;
                retornar = retornar + t_nombreDocenteb + ";" + colegio + ";" + horanow + ";" + idcolegio + ";" + t_iddocenteb + ";" + nomAsunto + ";"  + turno + ";" + select + ";" + fechaVeri + ";" +  t_inputjustificacion.getText().toString() + ";" + latitud + ";" + longitud + ";" + ubicacion;
                retornar = retornar + ";" + ModelInfodevice + ";" + IdInfodevice + ";" + ManufactInfodevice + ";" + BrandInfodevice;
                retornar = retornar + ";" + TypeInfodevice + ";" + UserInfodevice + ";" + BaseInfodevice + ";" + SdkInfodevice;
                retornar = retornar + ";" + BoardInfodevice + ";" + HostInfodevice + ";" + FingeprintInfodevice + ";" + VCodeInfodevice + "\n";
                guardarJustificar(retornar);
            }
        });
        builder.setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        View inputsJustificacionView = getLayoutInflater().inflate(R.layout.row_inputjustificacion,null,false);
        EditText editText = (EditText) inputsJustificacionView.findViewById(R.id.txtinputjustificacion);
        t_inputjustificacion = inputsJustificacionView.findViewById(R.id.txtinputjustificacion);
        if (t_inputjustificacion.getText().toString().equals("")){
            t_inputjustificacion.setText("SIN ARGUMENTO");
        }else{
            t_justificacioninputaux = t_inputjustificacion.getText().toString();
        }

        builder.setView(inputsJustificacionView);

        return builder.create();
    }

    public String leerArchivoJustificar(){
        String retomarAux = "";
        String auxArchivo = "dataInternalJustificacion" + colegio + ".txt";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Path path = Files.createTempFile("dataInternalJustificacion", ".txt");
                exists = Files.exists(path);     //true
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        String vasovacio = "";
        if(exists) {
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = context.openFileInput(auxArchivo);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String lineaTexto;
                StringBuilder stringBuilder = new StringBuilder();
                while ((lineaTexto = bufferedReader.readLine()) != null) {
                    stringBuilder.append(lineaTexto).append("\n");

                    String[] parts = lineaTexto.split(";");
                    String part1 = parts[0]; // DOCENTE
                    String part2 = parts[1]; // COLEGIO
                    String part3 = parts[2]; // ACTUAL
                    String part4 = parts[3]; // IDCOLEGIO
                    String part5 = parts[4]; // IDDOCENTE
                    String part6 = parts[5]; // ASUNTO
                    String part7 = parts[6]; // TURNO
                    String part8 = parts[7]; // PROBLEMA
                    String part9 = parts[8]; // FECHA
                    String part10 = parts[9]; // ARGUMENTO

                    String part11 = parts[10]; // LATITUD
                    String part12 = parts[11]; // LONGITUD
                    String part13 = parts[12]; // UBICAION

                    String part14 = parts[13]; // ModelInfodevice
                    String part15 = parts[14]; // IdInfodevice
                    String part16 = parts[15]; // ManufactInfodevice
                    String part17 = parts[16]; // BrandInfodevice
                    String part18 = parts[17]; // TypeInfodevice
                    String part19 = parts[18]; // UserInfodevice
                    String part20 = parts[19]; // BaseInfodevice
                    String part21 = parts[20]; // SdkInfodevice
                    String part22 = parts[21]; // BoardInfodevice
                    String part23 = parts[22]; // HostInfodevice
                    String part24 = parts[23]; // FingeprintInfodevice
                    String part25 = parts[24]; // VCodeInfodevice

                    vasovacio = vasovacio + part1 + ";" + part2 + ";" + part3 + ";" + part4 + ";" + part5 + ";" + part6 + ";" + part7 + ";" + part8 + ";" + part9 + ";" + part10 + ";" + part11 + ";" + part12 + ";" + part13;
                    vasovacio = vasovacio + ";" + part14 + ";" + part15 + ";" + part16 + ";" + part17 + ";" + part18 + ";" + part19 + ";" + part20 + ";" + part21 + ";" + part22 + ";" + part23 + ";" + part24 + ";" + part25 + "\n";
                    retomarAux = vasovacio;
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
        return retomarAux;
    }

    public void guardarJustificar(String retornar){
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = context.openFileOutput("dataInternalJustificacion" + colegio + ".txt", MODE_PRIVATE);
            fileOutputStream.write(retornar.getBytes());
        } catch (Exception ex) {
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}