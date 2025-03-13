package appAsis.example.asistenciaugelpomabamba2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import appAsis.example.asistenciaugelpomabamba2.R;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

public class CambioContrasenia extends AppCompatActivity {
    TextView mensaje;
    EditText t_codmod, t_password;
    Button b_cambio;
    Calendar calendar;
    SimpleDateFormat simpleDateFormatnow;
    TimeZone myTimeZone = TimeZone.getTimeZone("America/Lima");
    List<String> listColegios = new ArrayList<String>(),listCodmods = new ArrayList<String>(),listIdColegios = new ArrayList<String>();
    String colegio = "", idcolegio, retomarAux="", pruebaauax; boolean exists = false;

    List<String> dataColegio = new ArrayList<String>(),dataCodmod = new ArrayList<String>(), dataPassword = new ArrayList<String>(),dataIdcolegio = new ArrayList<String>(), dataFechaEnvio = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambio_contrasenia);
        mensaje = findViewById(R.id.lblMensaje);
        t_codmod = (EditText) findViewById(R.id.txtcodmod);
        t_password = (EditText) findViewById(R.id.txtpassword);
        b_cambio = (Button) findViewById(R.id.btnSolicitacambio);


        String linea;
        InputStream is = this.getResources().openRawResource(R.raw.datacolegio);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            if(is!=null){
                while((linea = reader.readLine())!=null){
                    listColegios.add(linea.split(";")[0]);
                    listCodmods.add(linea.split(";")[2]);
                    listIdColegios.add(linea.split(";")[4]);
                }
            }
            is.close();

        }catch (Exception e){}

        ConnectivityManager connectivityManager = (ConnectivityManager)getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        boolean auxConexion = connectivityManager.getActiveNetwork()!=null && connectivityManager.getActiveNetworkInfo().isConnected();
        if (auxConexion!=true){
            mensaje.setText("DEBIDO A QUE NO CUENTA CON COBERTURA SE GUARDARÁ LA BITÁCORA DE LOS CAMBIOS");
        }else{
            mensaje.setText("EL CAMBIO DE CONTRASEÑA SERÁ PREVIAMENTE AUTORIZADO POR EL ADMINISTRADOR");
        }
    }
    public void solicitarCambio(View view) throws IOException {
        String codmod = t_codmod.getText().toString().trim();
        String password = t_password.getText().toString().trim();

        calendar = Calendar.getInstance();
        simpleDateFormatnow = new SimpleDateFormat("HH:mm");
        simpleDateFormatnow.setTimeZone(myTimeZone);

        String fechaEnvio = simpleDateFormatnow.format(calendar.getTime());

        if(codmod.equals("") && password.equals("")){
            Toast.makeText(this,"Los campos no deben estar vacíos", Toast.LENGTH_SHORT).show();
        }else{

            for (int i = 0; i < listColegios.size(); i++) {
                if(codmod.equals(listCodmods.get(i))){
                    colegio = listColegios.get(i);
                    idcolegio = listIdColegios.get(i);
                }
            }
            if(colegio.equals("")){
                Toast.makeText(this,"EL CÓDIGO MODULAR NO CORRESPONDE", Toast.LENGTH_SHORT).show();
            }else{

                String auxArchivo = "dataCambiosTemp" + colegio + ".txt";
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    try {
                        Path path = Files.createTempFile("dataCambiosTemp", ".txt");
                        exists = Files.exists(path);     //true
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                int cont = 1;
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

                            vasovacio = vasovacio + part1 + ";" + part2 + ";" + part3 + ";" + part4 + ";" + part5 + ";" + part6 + ";" + part7 + "\n";
                            cont++;
                            retomarAux = vasovacio;
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

                ConnectivityManager connectivityManager = (ConnectivityManager)getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
                boolean auxConexion = connectivityManager.getActiveNetwork()!=null && connectivityManager.getActiveNetworkInfo().isConnected();
                if (auxConexion!=true){
                    pruebaauax = retomarAux + colegio + ";" + codmod + ";" + password + ";" + idcolegio + ";" + fechaEnvio + ";" + "SIN CONEXION" + ";" + cont + "\n";
                }else{
                    pruebaauax = retomarAux + colegio + ";" + codmod + ";" + password + ";" + idcolegio + ";" + fechaEnvio + ";" + "CON CONEXION" + ";" + cont + "\n";
                    String url = "https://ugelpomabamba.gob.pe/ugel/ugelasistencias_docente/model/password/cambiospass.php";
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

                            parametros.put("colegio",colegio);
                            parametros.put("codmod",codmod);
                            parametros.put("password",password);
                            parametros.put("idcolegio",idcolegio);
                            parametros.put("fecha_envio",fechaEnvio);
                            parametros.put("conexion","CON CONEXION");

                            return parametros;
                        }
                    };
                    RequestQueue requestQueue = Volley.newRequestQueue(this);
                    requestQueue.add(stringRequest);
                }

                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = openFileOutput("dataCambiosTemp" + colegio + ".txt", MODE_PRIVATE);
                    fileOutputStream.write(0);

                    fileOutputStream = openFileOutput("dataCambiosTemp" + colegio + ".txt", MODE_PRIVATE);
                    fileOutputStream.write(pruebaauax.getBytes());
                } catch (Exception e) {
                }


                Intent home = new Intent(this, MainActivity.class);
                startActivity(home);
                finish();
            }
        }
    }
}