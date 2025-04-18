package appAsis.example.asistenciaugelcorongo;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

public class login extends AppCompatActivity {

    public TextView l_Colegio, l_Notifica;
    public String idcolegio, mensajeNotificacion="", retomarAux="", retomarAuxInasistencia = "", notificacionesquery = "", auxArrayExcelUbicaciones = "";
    public String pruebaauax = "", colegio = "", guardaInfoExtExcel = "", queryConcat = "", getPutFiltrar, putDocente, putNivelColegio, putUbicaciones, putDocentesExcel, putCodmod;
    List<String>  dataExcelcargada = new ArrayList<String>(), dataExcelEstado = new ArrayList<String>(), dataExcelColegio = new ArrayList<String>(), dataExcelDocente = new ArrayList<String>();
    List<String>  arrayExcelUbicacion = new ArrayList<String>();
    RequestQueue requestQueue;
    List<String> dataMensajes = new ArrayList<String>(); boolean exists = false;
    Button b_login;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //l_Notifica = (TextView) findViewById(R.id.lblMensajeNotifica);
        l_Colegio = (TextView) findViewById(R.id.lblColegio);

        //t_password = findViewById(R.id.txtPassword);
        colegio = getIntent().getStringExtra("pass");
        String turnos = getIntent().getStringExtra("turnos");
        String notificacion = getIntent().getStringExtra("notificacion");
        idcolegio = getIntent().getStringExtra("idcolegio");
        getPutFiltrar = getIntent().getStringExtra("filtro");
        putDocente = getIntent().getStringExtra("docente");
        putNivelColegio = getIntent().getStringExtra("nivelcolegio");
        putUbicaciones = getIntent().getStringExtra("ubicaciones");
        putDocentesExcel = getIntent().getStringExtra("docentesexcel");
        putCodmod = getIntent().getStringExtra("codmodput");

        l_Colegio.setText(colegio);

        if (turnos.equals("Maniana")){
            findViewById(R.id.btcTarde).setVisibility(View.INVISIBLE);
            findViewById(R.id.textView3).setVisibility(View.INVISIBLE);
            findViewById(R.id.btcNoche).setVisibility(View.INVISIBLE);
            findViewById(R.id.textView4).setVisibility(View.INVISIBLE);
        }
        if (turnos.equals("Tarde")){
            findViewById(R.id.btcNoche).setVisibility(View.INVISIBLE);
            findViewById(R.id.textView4).setVisibility(View.INVISIBLE);
        }

        ConnectivityManager connectivityManager = (ConnectivityManager)getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        boolean auxConexion = connectivityManager.getActiveNetwork()!=null && connectivityManager.getActiveNetworkInfo().isConnected();
        if (auxConexion!=true){
        }else{
            enviarContraseniasaved();
            asistenciaVerificart("dataInternalTemp","MAÑANA");
            asistenciaVerificart("dataInternalTempSalida","MAÑANA");
            asistenciaVerificart("dataInternalTempTardellegada","TARDE");
            asistenciaVerificart("dataInternalTempTardeSalida","TARDE");
            justificacionVerificar("dataInternalJustificacionMañana","MAÑANA");
            justificacionVerificar("dataInternalJustificacionTarde","TARDE");
            //justificacionVerificar("dataInternalJustificacionTarde","TARDE");
            datosDocentesDirector("dataAsisColeDoce", "MAÑANA");
            datosDocentesDirector("dataAsisColeDoce", "TARDE");
            dataExcelCargado("dataExtExcel","");

            if (!notificacion.equals("")){
                AlertDialog.Builder alerta = new AlertDialog.Builder(login.this);
                alerta.setMessage(notificacion)
                        .setCancelable(false)
                        .setPositiveButton("ENTENDIDO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                AlertDialog titulo = alerta.create();
                titulo.setTitle("NOTIFICACIÓN NUEVA!");
                titulo.show();
            }

        };

        //l_Colegio.setText(notificacion);
    }

    public void TurnoManiana(View view){
        Intent i = new Intent(this,listmaniana.class);
        i.putExtra("pass",l_Colegio.getText().toString());
        i.putExtra("idcolegio",idcolegio);
        i.putExtra("filtro",getPutFiltrar);
        i.putExtra("docente",putDocente);
        i.putExtra("nivelcolegio",putNivelColegio);
        i.putExtra("codmodput",putCodmod);

        startActivity(i);

        //Intent home = new Intent(this, login.class);
        //finish();
    }
    public void TurnoTarde(View view){
        Intent i = new Intent(this,tarde.class);
        i.putExtra("pass",l_Colegio.getText().toString());
        i.putExtra("idcolegio",idcolegio);
        i.putExtra("filtro",getPutFiltrar);
        i.putExtra("docente",putDocente);
        i.putExtra("nivelcolegio",putNivelColegio);
        i.putExtra("codmodput",putCodmod);
        startActivity(i);

        //Intent home = new Intent(this, login.class);
        //finish();
    }
    public void asistenciaVerificart(String nombrearc, String turno){
        boolean exists = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                Path path = Files.createTempFile(nombrearc, ".txt");
                exists = Files.exists(path);     //true
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(exists){
            String auxArchivo = nombrearc + l_Colegio.getText().toString() + ".txt";
            FileInputStream fileInputStream = null;
            try {
                String vasovacio = "";
                fileInputStream = openFileInput(auxArchivo);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String lineaTexto;
                StringBuilder stringBuilder = new StringBuilder();
                while ((lineaTexto = bufferedReader.readLine())!=null){
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

                    vasovacio = vasovacio + part1 + ";" + part2 + ";" + part3 + ";" + part4 + ";" + part5 + ";" + "SIN CONEXION" + ";" + part7 + ";" + part8 + ";" + part9 + ";" + part10  + ";" + part11 + ";" + part12 + ";" + part13;
                    vasovacio = vasovacio + ";" + part14 + ";" + part15 + ";" + part16 + ";" + part17 + ";" + part18 + ";" + part19 + ";" + part20 + ";" + part21 + ";" + part22 + ";" + part23 + ";" + part24 + ";" + part25 + "\n";
                    retomarAux = vasovacio;

                    if (part6.equals("SIN CONEXION")){
                        final String[] posast = {new String()};
                        String url = "https://ugelpomabamba.gob.pe/ugel/ugelasistencias_docente/sesion.php";

                        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                //Toast.makeText(getApplicationContext(),"OPERACIÓN EXITOSA", Toast.LENGTH_SHORT).show();
                            }
                        }, error -> Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show()){
                            @Nullable
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String,String> parametros = new HashMap<String,String>();

                                parametros.put("colegio", part2);
                                parametros.put("docente", part1);
                                parametros.put("t_llegada", part3);
                                parametros.put("turno", turno);
                                parametros.put("asunto", "REGISTRO ASISTENCIA DOCENTE " + turno + " SIN CONEXION");
                                parametros.put("FK_idcolegio", part4);
                                parametros.put("FK_iddocente", part5);
                                parametros.put("fecha_registro", part9);

                                parametros.put("latitud", String.valueOf(part11));
                                parametros.put("longitud", String.valueOf(part12));
                                parametros.put("ubicacion", part13);

                                parametros.put("ModelInfodevice", part14);
                                parametros.put("IdInfodevice", part15);
                                parametros.put("ManufactInfodevice", part16);
                                parametros.put("BrandInfodevice", part17);
                                parametros.put("TypeInfodevice", part18);
                                parametros.put("UserInfodevice", part19);
                                parametros.put("BaseInfodevice", part20);
                                parametros.put("SdkInfodevice", part21);
                                parametros.put("BoardInfodevice", part22);
                                parametros.put("HostInfodevice", part23);
                                parametros.put("FingeprintInfodevice", part24);
                                parametros.put("VCodeInfodevice", part25);

                                posast[0] = String.valueOf(parametros);
                                return parametros;
                            }
                        };
                        RequestQueue requestQueue = Volley.newRequestQueue(login.this);
                        requestQueue.add(stringRequest);

                        url = "https://ugelpomabamba.gob.pe/ugel/ugelasistencias_docente/createBitacora.php";
                        stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {}
                        }, error -> Toast.makeText(getApplicationContext(),error.toString(), Toast.LENGTH_SHORT).show()){
                            @Nullable
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String,String> parametros = new HashMap<String,String>();

                                parametros.put("colegio", part2);
                                parametros.put("asunto", "REGISTRO DE ASISTENCIA " + turno);
                                parametros.put("latitud", String.valueOf(part11));
                                parametros.put("longitud", String.valueOf(part12));
                                parametros.put("ubicacion", part13);
                                parametros.put("FK_idcolegio", part4);
                                parametros.put("fecha_envio", part3);
                                parametros.put("fecha_appsave", part7);
                                parametros.put("FK_iddocente", part5);

                                parametros.put("ModelInfodevice", part14);
                                parametros.put("IdInfodevice", part15);
                                parametros.put("ManufactInfodevice", part16);
                                parametros.put("BrandInfodevice", part17);
                                parametros.put("TypeInfodevice", part18);
                                parametros.put("UserInfodevice", part19);
                                parametros.put("BaseInfodevice", part20);
                                parametros.put("SdkInfodevice", part21);
                                parametros.put("BoardInfodevice", part22);
                                parametros.put("HostInfodevice", part23);
                                parametros.put("FingeprintInfodevice", part24);
                                parametros.put("VCodeInfodevice", part25);

                                return parametros;
                            }
                        };
                        requestQueue = Volley.newRequestQueue(login.this);
                        requestQueue.add(stringRequest);
                    }
                }

                //File dir = getFilesDir();
                //File fileDelete = new File(dir, nombrearc + l_Colegio.getText().toString().trim() + ".txt");
                //boolean deleted = fileDelete.delete();

                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = openFileOutput(nombrearc + l_Colegio.getText().toString() + ".txt", MODE_PRIVATE);
                    fileOutputStream.write(retomarAux.getBytes());
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
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if (fileInputStream!=null){
                    try {
                        fileInputStream.close();
                    }catch (Exception e){

                    }
                }
            }
        }
    }
    public void enviarContraseniasaved(){
        String auxArchivo = "dataCambiosTemp" + l_Colegio.getText().toString() + ".txt";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                Path path = Files.createTempFile("dataCambiosTemp", ".txt");
                exists = Files.exists(path);     //true
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(exists) {
            FileInputStream fileInputStream = null;
            try {
                String url = "https://ugelpomabamba.gob.pe/ugel/ugelasistencias_docente/model/password/cambiospass.php";

                fileInputStream = openFileInput(auxArchivo);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String lineaTexto;
                StringBuilder stringBuilder = new StringBuilder();
                while ((lineaTexto = bufferedReader.readLine()) != null) {
                    stringBuilder.append(lineaTexto).append("\n");

                    String[] parts = lineaTexto.split(";");
                    String part1 = parts[0]; // colegio
                    String part2 = parts[1]; // codmod
                    String part3 = parts[2]; // password
                    String part4 = parts[3]; // idcolegio
                    String part5 = parts[4]; // fecha_envio
                    String part6 = parts[5]; // conexion

                    if (!part6.equals("CON CONEXION")){
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                //Toast.makeText(getApplicationContext(),"OPERACIÓN EXITOSA", Toast.LENGTH_SHORT).show();
                            }
                        }, error -> Toast.makeText(getApplicationContext(),error.toString(), Toast.LENGTH_SHORT).show()){
                            @Nullable
                            @Override
                            protected Map<String, String> getParams() {
                                Map<String,String> parametros = new HashMap<String,String>();

                                parametros.put("colegio",part1);
                                parametros.put("codmod",part2);
                                parametros.put("password",part3);
                                parametros.put("idcolegio",part4);
                                parametros.put("fecha_envio",part5);
                                parametros.put("conexion","SIN CONEXION");

                                return parametros;
                            }
                        };
                        RequestQueue requestQueue = Volley.newRequestQueue(this);
                        requestQueue.add(stringRequest);

                    }
                }
                cambiarEstadopass();
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
    }
    public void cambiarEstadopass(){
        String auxArchivo = "dataCambiosTemp" + l_Colegio.getText().toString() + ".txt";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                Path path = Files.createTempFile("dataCambiosTemp", ".txt");
                exists = Files.exists(path);     //true
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        String vasovacio = "";
        if(exists) {
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = openFileInput(auxArchivo);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String lineaTexto;
                StringBuilder stringBuilder = new StringBuilder();
                while ((lineaTexto = bufferedReader.readLine()) != null) {
                    stringBuilder.append(lineaTexto).append("\n");

                    String[] parts = lineaTexto.split(";");
                    String part1 = parts[0]; // colegio
                    String part2 = parts[1]; // codmod
                    String part3 = parts[2]; // password
                    String part4 = parts[3]; // idcolegio
                    String part5 = parts[4]; // fecha_envio
                    String part6 = parts[5]; // conexion
                    String part7 = parts[6]; // indice

                    vasovacio = vasovacio + part1 + ";" + part2 + ";" + part3 + ";" + part4 + ";" + part5 + ";" + "CON CONEXION" + ";" + part7 + "\n";
                    pruebaauax = vasovacio;
                }
                //tView.setText(stringBuilder);
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

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = openFileOutput("dataCambiosTemp" + l_Colegio.getText().toString() + ".txt", MODE_PRIVATE);
            fileOutputStream.write(0);

            fileOutputStream = openFileOutput("dataCambiosTemp" + l_Colegio.getText().toString() + ".txt", MODE_PRIVATE);
            fileOutputStream.write(pruebaauax.getBytes());
        } catch (Exception e) {
        }
    }
    public void justificacionVerificar(String nombrearc, String turno){
        boolean exists = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                Path path = Files.createTempFile(nombrearc, ".txt");
                exists = Files.exists(path);     //true
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(exists){
            String auxArchivo = nombrearc + l_Colegio.getText().toString() + ".txt";
            FileInputStream fileInputStream = null;
            try {
                String vasovacio = "";
                fileInputStream = openFileInput(auxArchivo);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String lineaTexto;
                StringBuilder stringBuilder = new StringBuilder();
                while ((lineaTexto = bufferedReader.readLine())!=null){
                    stringBuilder.append(lineaTexto).append("\n");

                    String[] parts = lineaTexto.split(";");
                    String part1 = parts[0]; // DOCENTE
                    String part2 = parts[1]; // COLEGIO
                    String part3 = parts[2]; // ACTUAL
                    String part4 = parts[3]; // IDCOLEGIO
                    String part5 = parts[4]; // IDDOCENTE
                    String part6 = parts[5]; // CONEXION
                    String part7 = parts[6]; // TURNO
                    String part8 = parts[7]; // PROBLEMA
                    String part9 = parts[8]; // FECHA
                    String part10 = parts[9]; // FECHA

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

                    vasovacio = vasovacio + part1 + ";" + part2 + ";" + part3 + ";" + part4 + ";" + part5 + ";" + "REPORTE INASISTENCIA CON CONEXION" + ";" + part7 + ";" + part8 + ";" + part9 + ";" + part10  + ";" + part11 + ";" + part12 + ";" + part13;
                    vasovacio = vasovacio + ";" + part14 + ";" + part15 + ";" + part16 + ";" + part17 + ";" + part18 + ";" + part19 + ";" + part20 + ";" + part21 + ";" + part22 + ";" + part23 + ";" + part24 + ";" + part25 + "\n";
                    retomarAuxInasistencia = vasovacio;

                    if (part6.equals("REPORTE INASISTENCIA SIN CONEXION")){
                        String url = "https://ugelpomabamba.gob.pe/ugel/ugelasistencias_docente/sesion.php";
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                //Toast.makeText(getApplicationContext(),"OPERACIÓN EXITOSA", Toast.LENGTH_SHORT).show();
                            }
                        }, error -> Toast.makeText(getApplicationContext(),error.toString(), Toast.LENGTH_SHORT).show()){
                            @Nullable
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String,String> parametros = new HashMap<String,String>();

                                parametros.put("colegio", part2);
                                parametros.put("docente", part1);
                                parametros.put("t_llegada", part3);
                                parametros.put("turno", turno);
                                parametros.put("asunto", "REPORTE INASISTENCIA SIN CONEXION");
                                parametros.put("FK_idcolegio", part4);
                                parametros.put("FK_iddocente", part5);
                                parametros.put("problema", part8);
                                parametros.put("argumento", part10);

                                parametros.put("latitud", part11);
                                parametros.put("longitud", part12);
                                parametros.put("ubicacion", part13);

                                parametros.put("ModelInfodevice", part14);
                                parametros.put("IdInfodevice", part15);
                                parametros.put("ManufactInfodevice", part16);
                                parametros.put("BrandInfodevice", part17);
                                parametros.put("TypeInfodevice", part18);
                                parametros.put("UserInfodevice", part19);
                                parametros.put("BaseInfodevice", part20);
                                parametros.put("SdkInfodevice", part21);
                                parametros.put("BoardInfodevice", part22);
                                parametros.put("HostInfodevice", part23);
                                parametros.put("FingeprintInfodevice", part24);
                                parametros.put("VCodeInfodevice", part25);
                                return parametros;
                            }
                        };
                        RequestQueue requestQueue = Volley.newRequestQueue(this);
                        requestQueue.add(stringRequest);
                    }
                }

                //File dir = getFilesDir();
                //File fileDelete = new File(dir, nombrearc + l_Colegio.getText().toString().trim() + ".txt");
                //boolean deleted = fileDelete.delete();

                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = openFileOutput(nombrearc + l_Colegio.getText().toString() + ".txt", MODE_PRIVATE);
                    fileOutputStream.write(retomarAuxInasistencia.getBytes());
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
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if (fileInputStream!=null){
                    try {
                        fileInputStream.close();
                    }catch (Exception e){

                    }
                }
            }
        }
    }

    public void dataExcelCargado(String nombrearc, String turno){
        String URL = "https://ugelpomabamba.gob.pe/ugel/ugelasistencias_docente/model/excel/verExcelcargado.php?colegio=" + colegio;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObject = null;
                for (int i = 0; i < response.length(); i++) {
                    try {
                        jsonObject = response.getJSONObject(i);
                        String nombre_institucion = jsonObject.getString("nombre_institucion");
                        String apellido_paterno = jsonObject.getString("apellido_paterno");
                        String apellido_materno = jsonObject.getString("apellido_materno");
                        String nombres = jsonObject.getString("nombres");
                        String documento_identidad = jsonObject.getString("documento_identidad");
                        String estado = jsonObject.getString("estado");

                        String docente = apellido_paterno + ";" + apellido_materno + ";" + nombres;
                        //concatenarExcelcargado = concatenarExcelcargado + nombre_institucion + ";" + documento_identidad + ";" + apellido_paterno + ";" + apellido_materno + ";" + nombres + ";" + idcolegio + "\n";
                        if (nombre_institucion.equals(colegio)){
                            dataExcelColegio.add(nombre_institucion);
                            dataExcelcargada.add(documento_identidad);
                            dataExcelEstado.add(estado);
                            dataExcelDocente.add(docente);
                        }
                    } catch (JSONException e) {

                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }
        );
        requestQueue = Volley.newRequestQueue(login.this);
        requestQueue.add(jsonArrayRequest);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                Path path = Files.createTempFile(nombrearc, ".txt");
                exists = Files.exists(path);     //true
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(exists) {
            FileOutputStream fileOutputStream = null;

            if (dataExcelcargada.size() > 0){
                for (int i = 0; i < dataExcelcargada.size(); i++) {
                    guardaInfoExtExcel = guardaInfoExtExcel + colegio + ";" + dataExcelcargada.get(i) + ";" + dataExcelDocente.get(i) + ";" + idcolegio + ";" + dataExcelEstado.get(i) + "\n";
                }
            }
            try {
                String auxArchivo = nombrearc + l_Colegio.getText().toString() + ".txt";
                fileOutputStream = openFileOutput(auxArchivo, MODE_PRIVATE);
                fileOutputStream.write(guardaInfoExtExcel.getBytes());
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_general,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                logout();
                return  true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void logout(){
        Intent home = new Intent(this, MainActivity.class);
        home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(home);
        finish();
    }

    public void datosDocentesDirector(String nombrearc, String turno){
        String datosDocenteDirector = "";
        TimeZone myTimeZoneaux = TimeZone.getTimeZone("America/Lima");
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormatnow = new SimpleDateFormat("yyyy-MM-dd");
        simpleDateFormatnow.setTimeZone(myTimeZoneaux);

        String periodo = simpleDateFormatnow.format(calendar.getTime());

        requestQueue = Volley.newRequestQueue(login.this);

        String linkURL = "https://ugelpomabamba.gob.pe/ugel/ugelasistencias_docente/model/auxasistencia/verAsistenciaColegioDocente.php?colegio=" + colegio + "&periodo=" + periodo + "&turno=" + turno;

        linkUrlClass linkUrlClassObj = new linkUrlClass();
        try {
            String response = linkUrlClassObj.execute(linkURL).get();
            if(!response.equals("empty")){
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    String queryDocente = jsonObject.getString("docente");
                    String queryColegio = jsonObject.getString("colegio");
                    String t_llegada = jsonObject.getString("t_llegada");
                    String FK_idcolegio = jsonObject.getString("FK_idcolegio");
                    String FK_iddocente = jsonObject.getString("FK_iddocente");
                    String fecha_registro = jsonObject.getString("fecha_registro");
                    String latitud = jsonObject.getString("latitud");
                    String longitud = jsonObject.getString("longitud");
                    String ubicacion = jsonObject.getString("ubicacion");
                    String ModelInfodevice = jsonObject.getString("ModelInfodevice");
                    String IdInfodevice = jsonObject.getString("IdInfodevice");
                    String ManufactInfodevice = jsonObject.getString("ManufactInfodevice");
                    String BrandInfodevice = jsonObject.getString("BrandInfodevice");
                    String TypeInfodevice = jsonObject.getString("TypeInfodevice");
                    String UserInfodevice = jsonObject.getString("UserInfodevice");
                    String BaseInfodevice = jsonObject.getString("BaseInfodevice");
                    String SdkInfodevice = jsonObject.getString("SdkInfodevice");
                    String BoardInfodevice = jsonObject.getString("BoardInfodevice");
                    String HostInfodevice = jsonObject.getString("HostInfodevice");
                    String FingeprintInfodevice = jsonObject.getString("FingeprintInfodevice");
                    String VCodeInfodevice = jsonObject.getString("VCodeInfodevice");

                    String asunto = jsonObject.getString("asunto");
                    queryConcat = queryConcat + queryDocente + ";" + queryColegio + ";" + t_llegada + ";" + FK_idcolegio + ";" + FK_iddocente + ";CON CONEXION" + ";" + fecha_registro + ";" + latitud + ";" + longitud + ";" + ubicacion;
                    queryConcat = queryConcat + ";" + ModelInfodevice + ";" + IdInfodevice + ";" + ManufactInfodevice + ";" + BrandInfodevice;
                    queryConcat = queryConcat + ";" + TypeInfodevice + ";" + UserInfodevice + ";" + BaseInfodevice + ";" + SdkInfodevice;
                    queryConcat = queryConcat + ";" + BoardInfodevice + ";" + HostInfodevice + ";" + FingeprintInfodevice + ";" + VCodeInfodevice;
                    queryConcat = queryConcat + ";" + asunto + "\n";
                }

            }
        }catch (InterruptedException e){
            e.printStackTrace();
        }catch (ExecutionException e){
            e.printStackTrace();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = openFileOutput(nombrearc + l_Colegio.getText().toString() + turno + ".txt", MODE_PRIVATE);
            fileOutputStream.write(queryConcat.getBytes());
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