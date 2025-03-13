package appAsis.example.asistenciaugelpomabamba2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import appAsis.example.asistenciaugelpomabamba2.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    List<String> listColegios = new ArrayList<String>();
    List<String> listPasswords = new ArrayList<String>();
    List<String> listCodmods = new ArrayList<String>();
    List<String> listTurnos = new ArrayList<String>();
    List<String> listIdColegios = new ArrayList<String>();
    List<String> listDocente = new ArrayList<String>(), listNivelesColegio = new ArrayList<String>();
    List<String> dataNotificaciones = new ArrayList<String>();

    List<String> excelDocenteColegio = new ArrayList<String>(), excelDocenteUser = new ArrayList<String>(), excelDocentePass = new ArrayList<String>(), excelDocenteTipo = new ArrayList<String>(), excelDocenteGeo = new ArrayList<String>(), excelAbreviado = new ArrayList<String>();
    List<String> excelCodmod_ie = new ArrayList<String>();
    List<String> excelDocenteApeP = new ArrayList<String>(), excelDocenteApeM = new ArrayList<String>(), excelDocenteNom = new ArrayList<String>(), excelDocenteUni = new ArrayList<String>();
    List<String> excelDocenteNomCompleto = new ArrayList<String>(), excelDocenteTurno  = new ArrayList<String>();

    TextView textView;
    //dd
    EditText t_password, t_codmod;
    String str_password, str_codmod, str_turno = "n", str_colegio, str_idcolegio, mensajeNotificacion = "", fechaQueryNotifica, lblcolegio = "", str_docente;
    String aux111 = null, aux112 = null, aux113 = null, aux114 = null, aux115 = null, aux116 = null, aux117 = null, aux118 = null, aux120 = "n";
    Button b_login, b_ViewCambio;
    RequestQueue requestQueue;
    String contraseniaCambiada = "", queryConcat = "", notificarbusquedaquery = "", auxArrayExcelUbicaciones = "", auxArrayExcelDocentes = "";

    String tipobusqueda = "", colegiobuscaqueda = "", docentebusqueda = "", fechaactual = "", str_nivelcolegio;
    double diff_inicial = 0;
    boolean exists = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {
            Thread.sleep(1000);
            setTheme(R.style.Theme_AsistenciaUgelCorongo);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String linea;
        InputStream is = this.getResources().openRawResource(R.raw.datacolegio);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            if(is!=null){
                while((linea = reader.readLine())!=null){
                    listColegios.add(linea.split(";")[0]);
                    listPasswords.add(linea.split(";")[1]);
                    listCodmods.add(linea.split(";")[2]);
                    listTurnos.add(linea.split(";")[3]);
                    listIdColegios.add(linea.split(";")[4]);
                    listDocente.add(linea.split(";")[7]);
                    listNivelesColegio.add(linea.split(";")[8]);
                    //listado.add(linea.split(";")[1]);
                }
            }
            is.close();

        }catch (Exception e){}


        t_password = findViewById(R.id.txtPassword);
        t_codmod = findViewById(R.id.txtCodmodular);
        b_login = findViewById(R.id.btcLogin);
        b_ViewCambio = findViewById(R.id.btnCambiopass);
        textView = findViewById(R.id.textView);



        TimeZone myTimeZoneaux = TimeZone.getTimeZone("America/Lima");
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormatnow = new SimpleDateFormat("yyyy-MM-dd");
        simpleDateFormatnow.setTimeZone(myTimeZoneaux);

        fechaactual = simpleDateFormatnow.format(calendar.getTime());

        b_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mensajeNotificacion = "";
                ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
                boolean auxConexion = connectivityManager.getActiveNetwork() != null && connectivityManager.getActiveNetworkInfo().isConnected();

                String URL = "https://ugelpomabamba.gob.pe/ugel/ugelasistencias_docente/verNotificaciones.php";
                //String URL = "https://ugelpomabamba.gob.pe/ugel/ugelasistencias_docente/model/auxasistencia/verNotificaciones.php?colegio=" + colegiobuscaqueda + "&filtro=" + tipobusqueda + "&docente=" + docentebusqueda + "&fecha=" + fechaactual;
                if (auxConexion != true) {
                }else {
                    JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            JSONObject jsonObject = null;
                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    jsonObject = response.getJSONObject(i);
                                    //mensajeNotificacion = jsonObject.getString("mensaje");
                                    //fechaQueryNotifica = jsonObject.getString("fechainicio");

                                    String querymensaje = jsonObject.getString("mensaje");
                                    String queryfechainicio = jsonObject.getString("fechainicio");
                                    String queryfechafinal = jsonObject.getString("fechafinal");

                                    fechaTextfile (queryfechainicio);
                                    double auxinicial = diff_inicial;

                                    fechaTextfile (queryfechafinal);
                                    double auxfinal = diff_inicial;

                                    if(auxinicial >= 0 && auxfinal <= 0){
                                        mensajeNotificacion = mensajeNotificacion + "SMS: " + querymensaje + "\n" + "FINALIZA: " + queryfechafinal + "\n \n";
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
                    ;
                    requestQueue = Volley.newRequestQueue(MainActivity.this);
                    requestQueue.add(jsonArrayRequest);

                    dataExcelUbicaciones();
                    dataExcelDocente();
                }
                try {
                    login(textView);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });


    }

    public void login(View view) throws IOException{
        buscar();
        Toast.makeText(MainActivity.this,"Esperando respuesta...", Toast.LENGTH_SHORT).show();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //EXCEL DOCENTE
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = openFileOutput("dataExcelDocentes.txt", MODE_PRIVATE);
                    fileOutputStream.write(auxArrayExcelDocentes.getBytes());
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

                String status = "false";
                if(t_password.getText().toString().equals("") && t_codmod.getText().toString().equals("")){
                    Toast.makeText(MainActivity.this,"Los campos no deben estar vacíos", Toast.LENGTH_SHORT).show();
                }else{
                    str_password = t_password.getText().toString();
                    str_codmod = t_codmod.getText().toString();

                    for (int i = 0; i < listColegios.size(); i++) {
                        if((str_password.equals(listPasswords.get(i).toString()) || contraseniaCambiada.equals(str_password))
                                && str_codmod.equals(listCodmods.get(i).toString())){
                            str_colegio = listColegios.get(i).toString();
                            str_turno = listTurnos.get(i).toString().trim();
                            str_idcolegio = listIdColegios.get(i).toString().trim();
                            str_nivelcolegio = listNivelesColegio.get(i).toString().trim();

                            str_docente = listDocente.get(i).toString().trim();

                            status = "true";
                        }
                        for (int j = 0; j < excelDocenteUser.size(); j++) {
                            if (str_password.equals(excelDocentePass.get(j)) && str_turno != "n"){
                                if(excelAbreviado.get(j).equals(str_codmod)){
                                    //DETERMINA SI ES ESPECIALISTA
                                    str_colegio = excelDocenteColegio.get(j);
                                    str_turno = "Especialista";
                                    str_idcolegio = listIdColegios.get(i).toString().trim();
                                    str_nivelcolegio = listNivelesColegio.get(i).toString().trim();

                                    str_docente = excelDocenteNomCompleto.get(j);

                                    status = "true";
                                    break;
                                }
                                //Toast.makeText(MainActivity.this, "Contains: " + excelDocenteTipo.get(j), Toast.LENGTH_SHORT).show();
                                if(excelDocenteTipo.get(j).contains("Tarde") || excelDocenteTipo.get(j).contains("Maniana")){
                                    //DETERMINA SI ES DIRECTOR
                                    str_colegio = excelDocenteColegio.get(j);
                                    str_turno = excelDocenteTurno.get(j);
                                    str_idcolegio = listIdColegios.get(i).toString().trim();
                                    str_nivelcolegio = listNivelesColegio.get(i).toString().trim();

                                    str_docente = excelDocenteNomCompleto.get(j);

                                    status = "true";
                                    break;
                                }else{
                                    //DETERMINA SI ES DOCENTE
                                    str_colegio = excelDocenteColegio.get(j);
                                    str_turno = excelDocenteTurno.get(j);
                                    str_idcolegio = listIdColegios.get(i).toString().trim();
                                    str_nivelcolegio = listNivelesColegio.get(i).toString().trim();

                                    str_docente = excelDocenteNomCompleto.get(j);

                                    status = "true";
                                    break;
                                }
                            }
                            if (excelCodmod_ie.get(j).equals(str_codmod) && str_password.equals(excelDocentePass.get(j))){
                                str_colegio = excelDocenteColegio.get(j);
                                str_turno = excelDocenteTurno.get(j);
                                str_idcolegio = listIdColegios.get(i).toString().trim();
                                str_nivelcolegio = listNivelesColegio.get(i).toString().trim();

                                str_docente = excelDocenteNomCompleto.get(j);

                                aux114 = excelDocenteColegio.get(j); aux115 = excelDocenteTurno.get(j);
                                aux116 = listIdColegios.get(i).toString().trim(); aux117 = listNivelesColegio.get(i).toString().trim();
                                aux118 = excelDocenteNomCompleto.get(j);
                                status = "true";
                                aux111 = excelCodmod_ie.get(j); aux112 = excelDocentePass.get(j); aux113 = excelDocenteTurno.get(j);
                                aux120 = "p";
                            }
                            //excelCodmod_ie
                        }
                    }
                    if (str_codmod.equals(str_password)){
                        str_turno = "Docente";
                    }

                    //!aux111.isEmpty() && !aux112.isEmpty() && !aux113.isEmpty()
                    if(aux120.equals("p")){
                        str_colegio = aux114;
                        str_turno = aux115;
                        str_idcolegio = aux116;
                        str_nivelcolegio = aux117;
                        str_docente = aux118;
                    }

                    //Toast.makeText(this,"Contraseñabd: " + contraseniabd + " txt: " + str_password + " Colegiobd: " + colegiobd + " txC: "+ comboxColegio.getSelectedItem().toString() + " estado: " + status, Toast.LENGTH_SHORT).show();
                    //Toast.makeText(this,"Contraseña: " + contraseniabd + "Colegio: " + colegiobd, Toast.LENGTH_SHORT).show();
                    //Toast.makeText(this,"Colegio: " + colegiobd, Toast.LENGTH_SHORT).show();
                    //Toast.makeText(MainActivity.this,"Estado: " + status, Toast.LENGTH_SHORT).show();
                    if(status == "true"){
                        str_password = t_password.getText().toString();

                        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                        progressDialog.setMessage("Ingresando...");
                        progressDialog.show();

                        String filtrar = "";
                        Class claseCargo = null;
                        if (str_turno.equals("Especialista")){
                            claseCargo = Especialistas.class;
                            filtrar = "Especialista";
                            tipobusqueda = "DOCENTE";
                            colegiobuscaqueda = "DOCENTE";
                            docentebusqueda = str_docente;
                        }

                        if (str_turno.equals("Maniana") || str_turno.equals("Tarde")){
                            claseCargo = login.class;
                            filtrar = "Turno";
                            tipobusqueda = "COLEGIO";
                            colegiobuscaqueda = str_colegio;
                            docentebusqueda = "NO";
                        }

                        if (str_turno.equals("Docente")){
                            claseCargo = ListUsuario.class;
                            filtrar = "Docente";
                            tipobusqueda = "DOCENTE";
                            colegiobuscaqueda = "DOCENTE";
                            docentebusqueda = str_docente;
                        }
                        String nombrearcNotifica = "dataNotificaciones";

                        //String notificaquery = datosNotificacion(nombrearcNotifica, tipobusqueda, str_colegio, docentebusqueda, fechaactual);
                        //pruebaQuery(tipobusqueda, colegiobuscaqueda, docentebusqueda, fechaactual);
                        Intent home = new Intent(MainActivity.this, claseCargo);
                        home.putExtra("pass", str_colegio);
                        home.putExtra("dni", str_password);
                        home.putExtra("turnos", str_turno);
                        home.putExtra("idcolegio", str_idcolegio);
                        home.putExtra("docente", str_docente);
                        home.putExtra("notificacion",mensajeNotificacion);
                        home.putExtra("filtro", filtrar);
                        home.putExtra("nivelcolegio", str_nivelcolegio);

                        home.putExtra("ubicaciones", auxArrayExcelUbicaciones);
                        home.putExtra("docentesexcel", auxArrayExcelDocentes);
                        home.putExtra("codmodput", str_codmod);
                        fileOutputStream = null;
                        try {
                            fileOutputStream = openFileOutput("dataExcelUbicaciones.txt", MODE_PRIVATE);
                            fileOutputStream.write(auxArrayExcelUbicaciones.getBytes());
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


                        startActivity(home);
                    }else{
                        Toast.makeText(MainActivity.this,"DATOS INCORRECTOS, INTENTELO NUEVAMENTE", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        }, 2500);
    }

    public void cambioContra(View view) throws IOException{
        Intent home = new Intent(this, CambioContrasenia.class);
        startActivity(home);
    }

    public void buscar (){
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        boolean auxConexion = connectivityManager.getActiveNetwork() != null && connectivityManager.getActiveNetworkInfo().isConnected();

        String URL = "https://ugelpomabamba.gob.pe/ugel/ugelasistencias_docente/model/password/verCambiospass.php?codmod=" + t_codmod.getText().toString();

        if (auxConexion != true) {
            for (int i = 0; i < listColegios.size(); i++) {
                if(t_codmod.getText().toString().equals(listCodmods.get(i).toString())){
                    lblcolegio = listColegios.get(i).toString();
                }
            }

            String auxArchivo = "dataCambiosTemp" + lblcolegio + ".txt";
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
                        String part3 = parts[2]; // ACTUAL
                        String part5 = parts[5]; // ACTUAL
                        contraseniaCambiada = part3;
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
                //auxGuardarBorrarSalida = vasovacio;
            }
        }else {
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    JSONObject jsonObject = null;
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            jsonObject = response.getJSONObject(i);
                            contraseniaCambiada = jsonObject.getString("password");
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
            ;
            requestQueue = Volley.newRequestQueue(MainActivity.this);
            requestQueue.add(jsonArrayRequest);
        }
    }

    public void fechaTextfile (String fechaVerificar){
        String pattern = "yyyy-MM-dd";
        TimeZone myTimeZone = TimeZone.getTimeZone("America/Lima");
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormatnowFecha = new SimpleDateFormat("yyyy-MM-dd");
        simpleDateFormatnowFecha.setTimeZone(myTimeZone);
        String horanowVerificar = simpleDateFormatnowFecha.format(calendar.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        try {
            Date date1 = sdf.parse(fechaVerificar);
            Date date2 = sdf.parse(horanowVerificar);
            long elapsedms = date1.getTime() - date2.getTime();
            diff_inicial = TimeUnit.DAYS.convert(elapsedms, TimeUnit.MILLISECONDS);
        }
        catch (ParseException exception) {
            exception.printStackTrace();
        }

        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        Date fechaDesde = null;
        Date fechaHasta = null;
        try {
            fechaDesde = date.parse(fechaVerificar);
            fechaHasta = date.parse(horanowVerificar);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        long startTime = fechaDesde.getTime() ;
        long endTime = fechaHasta.getTime();
        long diasDesde = (long) Math.floor(startTime / (1000*60*60*24)); // convertimos a dias, para que no afecten cambios de hora
        long diasHasta = (long) Math.floor(endTime / (1000*60*60*24)); // convertimos a dias, para que no afecten cambios de hora
        diff_inicial = diasHasta - diasDesde;
    }

    public void dataExcelUbicaciones(){
        auxArrayExcelUbicaciones = "";
        String URL = "https://ugelpomabamba.gob.pe/ugel/ugelasistencias_docente/model/excel/verExcelUbicacion.php";
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObject = null;
                for (int i = 0; i < response.length(); i++) {
                    try {
                        jsonObject = response.getJSONObject(i);
                        String nombre_institucion = jsonObject.getString("colegio");
                        String latitud = jsonObject.getString("latitud");
                        String longitud = jsonObject.getString("longitud");
                        String altura = jsonObject.getString("altura");

                        auxArrayExcelUbicaciones += nombre_institucion + ";" + latitud + ";" + longitud + ";" + altura + "\n";

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
        ;
        requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(jsonArrayRequest);
    }

    public void dataExcelDocente(){
        auxArrayExcelDocentes = "";
        String URL = "https://ugelpomabamba.gob.pe/ugel/ugelasistencias_docente/model/excel/verExcelcargado.php";
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObject = null;
                for (int i = 0; i < response.length(); i++) {
                    try {
                        jsonObject = response.getJSONObject(i);
                        String nombre_institucion = jsonObject.getString("nombre_institucion");
                        String documento_identidad = jsonObject.getString("documento_identidad");
                        String tipo_trabajador = jsonObject.getString("tipo_trabajador");
                        String latitud = "-8.81907000";
                        String longitud = "-77.46168000";
                        String iddocenteexcel = jsonObject.getString("id");
                        String apellido_paterno = jsonObject.getString("apellido_paterno");
                        String apellido_materno = jsonObject.getString("apellido_materno");
                        String nombres = jsonObject.getString("nombres");
                        String estado = jsonObject.getString("estado");
                        String codmod_ie = jsonObject.getString("codmod_ie");
                        String docente = apellido_paterno + " " + apellido_materno + " " + nombres;
                        String Unidirector = "Unidirector";

                        String abreviado = "AFFSADFAWEFQEWFASVSDVAS";
                        try {
                            String firstLetra = nombres.substring(0,1);
                            abreviado = firstLetra + "" + apellido_paterno;
                        }catch (Exception e){}

                        String userExcel = "";
                        if(tipo_trabajador.equals("Director Maniana") || tipo_trabajador.equals("Director Tarde")){
                            userExcel = jsonObject.getString("codmod_ie");
                            tipo_trabajador = tipo_trabajador.replaceAll("Director ", "");
                        }
                        if(tipo_trabajador.equals("Docente")){
                            userExcel = documento_identidad;
                        }
                        if(tipo_trabajador.equals("Especialista")){
                            userExcel = String.valueOf(nombres.toUpperCase().charAt(0));
                            userExcel += apellido_paterno.replace(" ","");
                        }

                        auxArrayExcelDocentes += nombre_institucion + ";" + documento_identidad + ";" + userExcel + ";" + tipo_trabajador + ";" + iddocenteexcel + ";";
                        auxArrayExcelDocentes += latitud + ";" + longitud + ";" + docente + ";" + Unidirector + ";" + estado + ";" + codmod_ie + "\n";

                        excelDocenteUser.add(userExcel);
                        excelDocenteColegio.add(nombre_institucion);
                        excelDocentePass.add(documento_identidad);
                        excelDocenteTipo.add(tipo_trabajador);
                        excelDocenteNomCompleto.add(docente);
                        excelDocenteTurno.add(tipo_trabajador);
                        excelAbreviado.add(abreviado);
                        excelCodmod_ie.add(codmod_ie);

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
        ;
        requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(jsonArrayRequest);


    }

    public void txtExcelDocente(){
        //EXCEL DOCENTE
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = openFileOutput("dataExcelDocentes.txt", MODE_PRIVATE);
            fileOutputStream.write(auxArrayExcelDocentes.getBytes());
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