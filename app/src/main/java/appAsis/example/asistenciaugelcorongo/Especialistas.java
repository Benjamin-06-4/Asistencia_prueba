package appAsis.example.asistenciaugelcorongo;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class Especialistas extends AppCompatActivity implements View.OnClickListener, Cobertura {

    private static final int REQUEST_CODE = 1, REQUEST_CODE_IMG = 100, CHOOSE_PDF_FROM_DEVICE = 1001;
    private String selectedFilePath;
    private String SERVER_URL = "http://coderefer.com/extras/UploadToServer.php";
    ProgressDialog dialog;
    LinearLayout layoutList;
    Button buttonAsistencia, b_openfile, b_uploadImg;
    ImageView alertBtnDelete, camaraView, i_PruebaBorrar;
    CircleImageView imgUpload; Bitmap bitmapUploadImg; String encodedimage;
    public static final String apirul = "https://ugelpomabamba.gob.pe/ugel/ugelasistencias_docente/model/file/img/uploadImg.php";
    List<String> listDocentes = new ArrayList<String>(), listColegios = new ArrayList<String>(), listIdDocentes = new ArrayList<String>();
    List<String> dataDocente = new ArrayList<String>(), dataHora = new ArrayList<String>(), dataIdDocentes = new ArrayList<String>();
    List<String> dataTemporalDocenteLlegadaArray = new ArrayList<String>(), dataTemporalHoraLlegadaArray = new ArrayList<String>();
    List<String>  dataTemporalHoraSalidaArray = new ArrayList<String>(), dataTemporalDocenteSalidaArray = new ArrayList<String>();
    List<String>  dataTemporalDocenteJustificacion = new ArrayList<String>(), listDocenteColegio = new ArrayList<String>();
    List<String>  dataUbicacionesLatitud = new ArrayList<String>(), dataUbicacionesLongitud = new ArrayList<String>(), dataUbicacionesColegio = new ArrayList<String>();
    List<String>  dataExcelcargada = new ArrayList<String>(), dataExcelEstado = new ArrayList<String>(), dataExcelColegio = new ArrayList<String>(), dataExcelDocente = new ArrayList<String>();
    List<LinearLayout> listLayout = new ArrayList<LinearLayout>(), listLayoutAux = new ArrayList<LinearLayout>();
    List<View> listViews = new ArrayList<View>(), listViewsAux = new ArrayList<View>();
    List<String> listExcelUbicacionesColegio = new ArrayList<String>(), listExcelUbicacionesLatitud = new ArrayList<String>(), listExcelUbicacionesLongitud = new ArrayList<String>();
    String dataLatitud, dataLongitud, fechaVerificar = "", concatenarExcelcargado = "";
    String linea, auxText = "", colegio, datoIdDocenteaux, guardarIdDocente, horanow, horanowVerificar;
    Double longitud = 0.0, latitud = 0.0;
    TextView tView, t_iddocente, t_latitud, t_longitud, t_ubiDocente, t_ubiFecha, t_ubicacionreal, t_ubicaLatitud, t_ubicaLongitud, t_docenteUploadImg;
    int hora, minuto;
    ImageButton imagenRelojButton,imagenRelojButtonSalida, imagenButtonInasistencia, b_search, btnCamara;
    TextView t_selecthora, t_nombreDocente, lbl_search;
    EditText t_search;
    static String NOM_ARCHIVO = "dataSaveManiana",auxAtribArchivo ="dataSaveManiana", idcolegio, retomarAux="", guardarBorrar = "", auxGuardarBorrar ="";
    static String retomarAuxSalida ="", auxGuardarBorrarSalida ="", retomarAuxInasistencia ="";
    private static final int PERMISSION_LOCATION = 1000;
    Boolean enviadoArc = false;
    StringBuilder stringBuilder = new StringBuilder();
    Calendar calendar;
    TimeZone myTimeZone = TimeZone.getTimeZone("America/Lima");
    SimpleDateFormat simpleDateFormatnow;
    String[] justificaciones = {"Injustificada","Justificada","Licencia sin goce","Permiso sin goce","Tardanza","Huelga/paro", "Otros"};
    String ubicacionColegioLatitud ="", ubicacionColegioLongitud ="", finalUbicacionEnvio = "", especialista = "", rutaImagen;  boolean exists = false;
    String mensajeColegioUbicacion = "", colegioproximo = "", mensajePickhour = "";
    double diff = 0, latitudActual = 0, longitudActual = 0; OutputStream outputStream;
    ActivityResultLauncher<Intent> resultLauncher;
    Uri filePath;

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
    private static final int STORAGE_PERMISSION_CODE = 123;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_especialistas);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction();

        layoutList = findViewById(R.id.layout_list);
        tView = (TextView)findViewById(R.id.txtCricketer);
        t_selecthora = (TextView) findViewById(R.id.txtHora);
        t_nombreDocente = (TextView) findViewById(R.id.edit_docente_name);
        t_iddocente = findViewById(R.id.txtiddocente);
        t_latitud = findViewById(R.id.txtiddocente);
        t_longitud = findViewById(R.id.txtiddocente);
        t_ubiDocente = findViewById(R.id.txtiddocente);
        t_ubiFecha = findViewById(R.id.txtiddocente);
        t_ubicacionreal = findViewById(R.id.txtUbicacionreal);
        t_ubicaLatitud = findViewById(R.id.txtUbicacionreal);
        t_ubicaLongitud = findViewById(R.id.txtUbicacionreal);
        t_docenteUploadImg = findViewById(R.id.edit_docente_name);

        i_PruebaBorrar = findViewById(R.id.imageUpload);
        b_uploadImg = findViewById(R.id.btnUploadImg);

        t_search = findViewById(R.id.txtSearch);
        lbl_search = findViewById(R.id.lblSearch);
        b_search = findViewById(R.id.btnSearch);
        b_openfile = findViewById(R.id.btnUploadFile);

        imagenRelojButton = findViewById(R.id.btnReloj);
        imagenRelojButtonSalida = (ImageButton) findViewById(R.id.btnSalida);
        imagenButtonInasistencia = (ImageButton) findViewById(R.id.btnInasistencia);
        btnCamara = findViewById(R.id.btnCamara);

        InputStream is = this.getResources().openRawResource(R.raw.datacolegio);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        colegio = getIntent().getStringExtra("pass");
        String dni = getIntent().getStringExtra("dni");
        idcolegio = getIntent().getStringExtra("idcolegio");
        String notificacion = getIntent().getStringExtra("notificacion");
        especialista = getIntent().getStringExtra("docente");
        //tView.setText("Especialista: " + colegio + "     DNI: "+dni);
        tView.setText("Especialista: " + especialista);


        ConnectivityManager connectivityManager = (ConnectivityManager)getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        boolean auxConexion = connectivityManager.getActiveNetwork()!=null && connectivityManager.getActiveNetworkInfo().isConnected();
        if (auxConexion!=true){
        }else{
            enviarContraseniasaved();
            asistenciaVerificart("dataInternalTemp","Mañana");
            asistenciaVerificart("dataInternalTempSalida","Mañana");
            asistenciaVerificart("dataInternalTempTardellegada","Tarde");
            asistenciaVerificart("dataInternalTempTardeSalida","Tarde");
            justificacionVerificar("dataInternalJustificacionMañana","Mañana");
            justificacionVerificar("dataInternalJustificacionTarde","Tarde");
            //justificacionVerificar("dataInternalJustificacionTarde","Tarde");

            if (!notificacion.equals("")){
                AlertDialog.Builder alerta = new AlertDialog.Builder(Especialistas.this);
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

        try {
            if(is!=null){
                while((linea = reader.readLine())!=null){
                    if (linea.split(";")[3].equals("Maniana") || linea.split(";")[3].equals("Tarde")){
                        listColegios.add(linea.split(";")[0]);
                        dataExcelcargada.add(linea.split(";")[1]);
                        listDocentes.add(linea.split(";")[0]);
                        listIdDocentes.add(linea.split(";")[4]);
                    }
                }
            }
            Set<String> hashSet = new HashSet<String>(listColegios);
            listColegios.clear();
            listColegios.addAll(hashSet);

            hashSet = new HashSet<String>(dataExcelcargada);
            dataExcelcargada.clear();
            dataExcelcargada.addAll(hashSet);

            hashSet = new HashSet<String>(listDocentes);
            listDocentes.clear();
            listDocentes.addAll(hashSet);

            hashSet = new HashSet<String>(listIdDocentes);
            listIdDocentes.clear();
            listIdDocentes.addAll(hashSet);

            is.close();

        }catch (Exception e){}

        String lineaUbicacion;
        InputStream isUbicacion = this.getResources().openRawResource(R.raw.dataubicacolegio);
        BufferedReader readerUbicacion = new BufferedReader(new InputStreamReader(isUbicacion));
        try {
            if(isUbicacion!=null){
                while((lineaUbicacion = readerUbicacion.readLine())!=null){
                    //colegio.equals(lineaUbicacion.split(";")[0])
                        dataUbicacionesColegio.add(lineaUbicacion.split(";")[0]);
                        dataUbicacionesLatitud.add(lineaUbicacion.split(";")[2]);
                        dataUbicacionesLongitud.add(lineaUbicacion.split(";")[3]);
                        dataLatitud = lineaUbicacion.split(";")[2];
                        dataLongitud = lineaUbicacion.split(";")[3];
                    //listado.add(linea.split(";")[1]);
                }
            }

            is.close();

        }catch (Exception e){}

        String guardaInfotextfile = "";
        String auxArchivo = "dataInternalTemp" + colegio + ".txt";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Path path = Files.createTempFile("dataInternalTemp", ".txt");
                exists = Files.exists(path);     //true
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
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
                    String part1 = parts[0]; // DOCENTE
                    String part2 = parts[1]; // COLEGIO
                    String part3 = parts[2]; // ACTUAL
                    String part4 = parts[3]; // IDCOLEGIO
                    String part5 = parts[4]; // IDDOCENTE
                    String part6 = parts[5]; // CONEXION
                    String part7 = parts[6]; // VERFICAR FECHA
                    String part8 = parts[7]; // Latitud
                    String part9 = parts[8]; // Longitud
                    String part10 = parts[9]; // Ubicacion

                    fechaTextfile (part7);

                    if (diff == 0 || (!part6.equals("CON CONEXION") && diff <= -1)){
                        guardaInfotextfile = guardaInfotextfile + part1 + ";" + part2 + ";" + part3 + ";" + part4 + ";" + part5 + ";" + part6 + ";" + part7 + ";" + part8 + ";" + part9 + ";" + part10 +"\n";
                        dataTemporalDocenteLlegadaArray.add(part1);
                        dataTemporalHoraLlegadaArray.add(part3);
                    }
                }

                String filePicker = "dataInternalTemp" + colegio + ".txt";
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = openFileOutput(filePicker, MODE_PRIVATE);
                    fileOutputStream.write(guardaInfotextfile.getBytes());
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
        guardaInfotextfile = "";
        String auxExcelUbicaciones = "dataExcelUbicaciones.txt";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Path path = Files.createTempFile("dataExcelUbicaciones", ".txt");
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
                    String part1 = parts[0]; // COLEGIO
                    String part2 = parts[1]; // Latitud
                    String part3 = parts[2]; // Longitud

                    listExcelUbicacionesColegio.add(part1);
                    listExcelUbicacionesLatitud.add(part2);
                    listExcelUbicacionesLongitud.add(part3);
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

        guardaInfotextfile = "";
        String auxArchivoSalida = "dataInternalTempSalida" + colegio + ".txt";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Path path = Files.createTempFile("dataInternalTempSalida", ".txt");
                exists = Files.exists(path);     //true
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(exists) {
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = openFileInput(auxArchivoSalida);
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
                    String part6 = parts[5]; // CONEXION
                    String part7 = parts[6]; // VERFICAR FECHA
                    String part8 = parts[7]; // Latitud
                    String part9 = parts[8]; // Longitud
                    String part10 = parts[9]; // Ubicacion

                    fechaTextfile (part7);

                    if (diff == 0 || (!part6.equals("CON CONEXION") && diff <= -1)){
                        guardaInfotextfile = guardaInfotextfile + part1 + ";" + part2 + ";" + part3 + ";" + part4 + ";" + part5 + ";" + part6 + ";" + part7 + ";" + part8 + ";" + part9 + ";" + part10 +"\n";
                        dataTemporalDocenteSalidaArray.add(part1);
                        dataTemporalHoraSalidaArray.add(part3);
                    }
                }

                String filePicker = "dataInternalTempSalida" + colegio + ".txt";
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = openFileOutput(filePicker, MODE_PRIVATE);
                    fileOutputStream.write(guardaInfotextfile.getBytes());
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

        guardaInfotextfile = "";
        String auxArchivoJustificacion = "dataInternalJustificacion" + colegio + ".txt";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Path path = Files.createTempFile("dataInternalJustificacion", ".txt");
                exists = Files.exists(path);     //true
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(exists) {
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = openFileInput(auxArchivoJustificacion);
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

                    fechaTextfile (part9);
                    if (diff == 0 || (!part6.equals("REPORTE INASISTENCIA CON CONEXION") && diff <= -1)){
                        guardaInfotextfile = guardaInfotextfile + part1 + ";" + part2 + ";" + part3 + ";" + part4 + ";" + part5 + ";" + part6 + ";" + part7 + ";" + part8 + ";" + part9 +"\n";
                        dataTemporalDocenteJustificacion.add(part1);
                    }
                }

                String filePicker = "dataInternalJustificacion" + colegio + ".txt";
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = openFileOutput(filePicker, MODE_PRIVATE);
                    fileOutputStream.write(guardaInfotextfile.getBytes());
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


        for (int i = 0; i < listColegios.size(); i++) {
            listDocenteColegio.add(listDocentes.get(i));
        }

        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Intent data = result.getData();
                        if (data != null){
                            Uri sUri = data.getData();
                            String sPath = sUri.getPath();

                            File file = new File(sUri.toString());
                            int size = (int) file.length();
                            t_search.setText("Uri: " + sUri + " - Path: " +sPath + " - Size: " + size);
                        }
                    }
                }
        );


        b_openfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(Especialistas.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(Especialistas.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                }else{
                    //uploadFile(selectedFilePath);
                    openFilesave();
                }
            }
        });

        ObtenerCoordenadasActual();
        for (int i = 0; i < listColegios.size(); i++) {
            String idforDocente = listIdDocentes.get(i);
            View docenteView = getLayoutInflater().inflate(R.layout.row_add_docente,null,false);
            EditText editText = (EditText) docenteView.findViewById(R.id.edit_docente_name);
            t_nombreDocente = docenteView.findViewById(R.id.edit_docente_name);

            editText.setText(listDocentes.get(i));
            t_selecthora = docenteView.findViewById(R.id.txtHora);
            t_iddocente = docenteView.findViewById(R.id.txtiddocente);
            t_latitud = docenteView.findViewById(R.id.txtiddocente);
            t_longitud = docenteView.findViewById(R.id.txtiddocente);

            t_iddocente.setText(listIdDocentes.get(i));
            t_latitud.setText(listIdDocentes.get(i));
            t_latitud.setText(listIdDocentes.get(i));
            guardarIdDocente = listIdDocentes.get(i);

            imagenRelojButton =  docenteView.findViewById(R.id.btnReloj);
            imagenButtonInasistencia = (ImageButton) docenteView.findViewById(R.id.btnInasistencia);
            imagenRelojButtonSalida = (ImageButton) docenteView.findViewById(R.id.btnSalida);
            imagenRelojButtonSalida.setVisibility(View.INVISIBLE);
            btnCamara = docenteView.findViewById(R.id.btnCamara);

            if (t_selecthora.getText().equals("Sin asignar")){
                imagenRelojButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ObtenerCoordenadasActual();
                        String auxArchivo = "dataInternalTemp" + colegio + ".txt";
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            try {
                                Path path = Files.createTempFile("dataInternalTemp", ".txt");
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
                                    String part1 = parts[0]; // DOCENTE
                                    String part2 = parts[1]; // COLEGIO
                                    String part3 = parts[2]; // ACTUAL
                                    String part4 = parts[3]; // IDCOLEGIO
                                    String part5 = parts[4]; // IDDOCENTE
                                    String part6 = parts[5]; // CONEXION
                                    String part7 = parts[6]; // VERFICAR FECHA
                                    String part8 = parts[7]; // Latitud
                                    String part9 = parts[8]; // Longitud
                                    String part10 = parts[9]; // Ubicacion

                                    vasovacio = vasovacio + part1 + ";" + part2 + ";" + part3 + ";" + part4 + ";" + part5 + ";" + part6 + ";" + part7 + ";" + part8 + ";" + part9 + ";" + part10 +"\n";
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
                            auxGuardarBorrar = vasovacio;
                        }
                        String mensajeAlert = "¿ENVIAR HORA DE ASISTENCIA AL COLEGIO? ";
                        String filePicker = "dataInternalTemp" + colegio + ".txt";
                        String asunto = "REGISTRO ASISTENCIA AL COLEGIO";

                        pickerHour(docenteView, mensajeAlert, filePicker, asunto, imagenRelojButton, imagenRelojButtonSalida, retomarAux, idforDocente);
                    }
                });
            }

            imagenRelojButtonSalida.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ObtenerCoordenadasActual();
                    String auxArchivo = "dataInternalTempSalida" + colegio + ".txt";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        try {
                            Path path = Files.createTempFile("dataInternalTempSalida", ".txt");
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
                                String part1 = parts[0]; // DOCENTE
                                String part2 = parts[1]; // COLEGIO
                                String part3 = parts[2]; // ACTUAL
                                String part4 = parts[3]; // IDCOLEGIO
                                String part5 = parts[4]; // IDDOCENTE
                                String part6 = parts[5]; // CONEXION
                                String part7 = parts[6]; // VERFICAR FECHA
                                String part8 = parts[7]; // Latitud
                                String part9 = parts[8]; // Longitud
                                String part10 = parts[9]; // Ubicacion

                                vasovacio = vasovacio + part1 + ";" + part2 + ";" + part3 + ";" + part4 + ";" + part5 + ";" + part6 + ";" + part7 + ";" + part8 + ";" + part9 + ";" + part10 +"\n";
                                retomarAuxSalida = vasovacio;
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
                        auxGuardarBorrarSalida = vasovacio;
                    }
                    String mensajeAlert = "¿ENVIAR HORA DE SALIDA COLEGIO?";
                    String filePicker = "dataInternalTempSalida" + colegio + ".txt";
                    String asunto = "REGISTRO ASISTENCIA SALIDA DEL COLEGIO";

                    pickerHour(docenteView, mensajeAlert, filePicker, asunto, imagenRelojButton, imagenRelojButtonSalida, retomarAuxSalida, idforDocente);
                }
            });

            imagenButtonInasistencia.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //ObtenerCoordenadasActual();
                    getCoordenada();
                    String latitudSave = String.valueOf(latitudActual);
                    String longitudSave = String.valueOf(longitudActual);
                    String finalUbicacionEnvioAux = finalUbicacionEnvio;

                    TimeZone myTimeZone = TimeZone.getTimeZone("America/Lima");
                    calendar = Calendar.getInstance();
                    simpleDateFormatnow = new SimpleDateFormat("HH:mm");
                    simpleDateFormatnow.setTimeZone(myTimeZone);
                    horanow = simpleDateFormatnow.format(calendar.getTime());

                    simpleDateFormatnow = new SimpleDateFormat("yyyy-MM-dd");
                    simpleDateFormatnow.setTimeZone(myTimeZone);
                    String fechaVerificaInasistencia = simpleDateFormatnow.format(calendar.getTime());
                    String inasisJustificaurl = "https://ugelpomabamba.gob.pe/ugel/ugelasistencias_docente/sesionespecialista.php";
                    Metodos alertDialog = new Metodos(Especialistas.this, justificaciones, "Mañana",
                            imagenRelojButton, imagenRelojButtonSalida, imagenButtonInasistencia, editText, colegio,
                            idcolegio, t_iddocente, t_selecthora, horanow, fechaVerificaInasistencia, docenteView, inasisJustificaurl,
                            latitudSave,longitudSave,finalUbicacionEnvioAux);
                    alertDialog.show(getSupportFragmentManager(),"dialog: ");
                    alertDialog.setCancelable(false);
                }
            });

            t_docenteUploadImg = docenteView.findViewById(R.id.edit_docente_name);
            t_docenteUploadImg.setText(listDocentes.get(i));
            btnCamara.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ContextCompat.checkSelfPermission(Especialistas.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        i_PruebaBorrar.setVisibility(View.VISIBLE);
                        abrirCamara(docenteView);
                    }else{
                        askPermission();
                    }
                }
            });

            layoutList.addView(docenteView);

            listLayout.add(layoutList);
            listLayoutAux.add(layoutList);
            listViews.add(docenteView);
            listViewsAux.add(docenteView);

            dataDocente.add(listDocentes.get(i));
            dataIdDocentes.add(listIdDocentes.get(i));
            if (dataTemporalHoraLlegadaArray.size() > 0){
                for (int j = 0; j < dataTemporalHoraLlegadaArray.size(); j++) {
                    if (dataTemporalDocenteLlegadaArray.get(j).equals(listDocentes.get(i))){
                        t_nombreDocente.setBackgroundColor(Color.GRAY);
                        docenteView.findViewById(R.id.btnReloj).setVisibility(View.INVISIBLE);
                        docenteView.findViewById(R.id.btnSalida).setVisibility(View.VISIBLE);
                        imagenRelojButton.setVisibility(View.INVISIBLE);
                        t_selecthora.setText(dataTemporalHoraLlegadaArray.get(j));
                        dataHora.add(dataTemporalHoraLlegadaArray.get(j));
                    }else{
                        dataHora.add("Sin asignar");
                    }
                }
            }else{
                dataHora.add("Sin asignar");
            }

            if (dataTemporalHoraSalidaArray.size() > 0){
                for (int j = 0; j < dataTemporalHoraSalidaArray.size(); j++) {
                    if (dataTemporalDocenteSalidaArray.get(j).equals(listDocentes.get(i))){
                        t_nombreDocente.setBackgroundColor(Color.GRAY);
                        docenteView.findViewById(R.id.btnReloj).setVisibility(View.INVISIBLE);
                        docenteView.findViewById(R.id.btnSalida).setVisibility(View.VISIBLE);
                        t_selecthora.setText("COMPLETO");
                        dataHora.add("COMPLETO");
                    }else{
                        dataHora.add("Sin asignar");
                    }
                }
            }else{
                dataHora.add("Sin asignar");
            }

            if (dataTemporalDocenteJustificacion.size() > 0){
                for (int j = 0; j < dataTemporalDocenteJustificacion.size(); j++) {
                    if (dataTemporalDocenteJustificacion.get(j).equals(listDocentes.get(i))){
                        t_nombreDocente.setBackgroundColor(Color.GRAY);
                        //docenteView.findViewById(R.id.btnReloj).setVisibility(View.INVISIBLE);
                        //docenteView.findViewById(R.id.btnSalida).setVisibility(View.INVISIBLE);
                        //docenteView.findViewById(R.id.btnInasistencia).setVisibility(View.INVISIBLE);
                        t_selecthora.setText("Reporte");
                        dataHora.add("Reporte");
                    }else{
                        dataHora.add("Sin asignar");
                    }
                }
            }else{
                dataHora.add("Sin asignar");
            }
        };

        b_uploadImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //uploadFile(selectedFilePath);
                uploadtoserver();
            }
        });

        b_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listLayout = listLayoutAux;
                listViews = listViewsAux;
                for (int i = 0; i < listLayout.size(); i++) {
                    listLayout.get(i).removeView(listViews.get(i));
                }
                for (int i = 0; i < listLayout.size(); i++) {
                    listLayout.get(i).addView(listViews.get(i));
                }
                for (int i = 0; i < listViews.size(); i++) {
                    if(listDocenteColegio.get(i).matches(".*" + t_search.getText().toString() + ".*")){
                    }else{
                        listLayout.get(i).removeView(listViews.get(i));
                    }

                }
            }
        });

        /**
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
         checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
         != PackageManager.PERMISSION_GRANTED){
         requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_LOCATION);
         }else{
         showLocation();
         }*/
    }

    public void askPermission(){
        ActivityCompat.requestPermissions(Especialistas.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE_IMG);

    }

    public void abrirCamara(View docenteView){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,1);
        t_docenteUploadImg = docenteView.findViewById(R.id.edit_docente_name);
    }

    public void saveImagen() {
        File dir = new File(Environment.getExternalStorageDirectory(),"imagen");
        if (!dir.exists()){
            dir.mkdir();
        }
        File file = new File(dir,System.currentTimeMillis()+".jpg");
        try{
            outputStream = new FileOutputStream(file);
        }catch (FileNotFoundException e){}

        try{
            outputStream.flush();
        }catch (IOException e){

        }
        try {
            outputStream.close();
        }catch (IOException e){

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showLocation();
                getCoordenada();
            }
        }
        /**
         if (requestCode == 1 && grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
         openFilesave();
         }*/
        if (requestCode == STORAGE_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Displaying a toast
                Toast.makeText(this, "PERMISO ACCEDIDO", Toast.LENGTH_LONG).show();
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "PERMISO DENEGADO", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == 1 && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            bitmapUploadImg = (Bitmap) extras.get("data");
            i_PruebaBorrar.setImageBitmap(bitmapUploadImg);
            encodebitmap(bitmapUploadImg);
        }
        if (requestCode == CHOOSE_PDF_FROM_DEVICE && resultCode == RESULT_OK){
            if (data == null) {
                return;
            }
            Uri selectedFileUri = data.getData();

            selectedFilePath = FilePath.getPath(this, selectedFileUri);

            if (selectedFilePath != null && !selectedFilePath.equals("")) {
                //tvFileName.setText(selectedFilePath);
            } else {
                Toast.makeText(this, "NO ES POSIBLE ENVIAR EL ARCHIVO", Toast.LENGTH_SHORT).show();
            }

            /**
             Uri pdfData = data.getData();
             Cursor returnCursor =
             getContentResolver().query(pdfData, null, null, null, null);
             int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
             //byte[] blotget = returnCursor.getBlob(returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
             returnCursor.moveToFirst();
             int size = (int) returnCursor.getLong(sizeIndex);

             ConnectivityManager connectivityManager = (ConnectivityManager)getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
             boolean auxConexion = connectivityManager.getActiveNetwork()!=null && connectivityManager.getActiveNetworkInfo().isConnected();
             if (auxConexion!=true){
             }else{
             }
             SaveFile uploadTask=new SaveFile(colegio,t_search);
             uploadTask.save(pdfData.toString());
             uploadTask.execute(new String[]{pdfData.toString()});
             //SaveFile saveFile = new SaveFile(colegio,t_search);
             try {
             //saveFile.save(pdfData.toString());
             } catch (Exception e) {
             throw new RuntimeException(e);
             }

             //t_search.setText(pdfData.toString() + " - tamaño: " + size );
             */
        }
        if (requestCode==111 && resultCode==RESULT_OK){
            bitmapUploadImg = (Bitmap)data.getExtras().get("data");
            imgUpload.setImageBitmap(bitmapUploadImg);
            encodebitmap(bitmapUploadImg);
        }
    }
    //Requesting permission

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    public int uploadFile(final String selectedFilePath) {
        int serverResponseCode = 0;

        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "–";
        String boundary = "*****";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File selectedFile = new File(selectedFilePath);

        String[] parts = selectedFilePath.split("/");
        final String fileName = parts[parts.length - 1];

        if (!selectedFile.isFile()) {
            dialog.dismiss();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //tvFileName.setText("Source File Doesn't Exist: " + selectedFilePath);
                }
            });
            return 0;
        } else {
            try {
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                URL url = new URL(SERVER_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);//Allow Inputs
                connection.setDoOutput(true);//Allow Outputs
                connection.setUseCaches(false);//Don't use a cached Copy
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("uploaded_file", selectedFilePath);

                //creating new dataoutputstream
                dataOutputStream = new DataOutputStream(connection.getOutputStream());

                //writing bytes to data outputstream
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + selectedFilePath + "\"" + lineEnd);

                dataOutputStream.writeBytes(lineEnd);

                //returns no. of bytes present in fileInputStream
                bytesAvailable = fileInputStream.available();
                //selecting the buffer size as minimum of available bytes or 1 MB
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                //setting the buffer as byte array of size of bufferSize
                buffer = new byte[bufferSize];

                //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                //loop repeats till bytesRead = -1, i.e., no bytes are left to read
                while (bytesRead > 0) {
                    //write the bytes read from inputstream
                    dataOutputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();

                //Log.i(TAG, "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);
                //response code of 200 indicates the server status OK
                if (serverResponseCode == 200) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //tvFileName.setText("File Upload completed.\n\n You can see the uploaded file here: \n\n" + "http://coderefer.com/extras/uploads/" + fileName);
                        }
                    });
                }
                //closing the input and output streams
                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();


            } catch (FileNotFoundException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Especialistas.this, "File Not Found", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(Especialistas.this, "URL error!", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(Especialistas.this, "Cannot Read/Write File!", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
            return serverResponseCode;
        }
    }

    public void encodebitmap(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteofimages = byteArrayOutputStream.toByteArray();
        encodedimage = android.util.Base64.encodeToString(byteofimages, Base64.DEFAULT);
    }

    public static byte[] readFile(String file) throws IOException {

        File f = new File(file);

        // work only for 2GB file, because array index can only upto Integer.MAX

        byte[] buffer = new byte[(int) f.length()];

        FileInputStream is = new FileInputStream(file);

        is.read(buffer);

        is.close();

        return buffer;

    }

    @Override
    public void onClick(View view) {
        addView();
    }

    private void addView() {
        View docenteView = getLayoutInflater().inflate(R.layout.row_add_docente,null,false);
        EditText editText = (EditText) docenteView.findViewById(R.id.edit_docente_name);

        dataDocente.add("");
        dataHora.add("Sin asignar");
        layoutList.addView(docenteView);
    }

    private void pickerHour(View docenteView, String mensajeAlert, String filePicker, String asunto, ImageButton bllegada, ImageButton bsalida, String concatenarAux, String idforDocente) {

        TimeZone myTimeZone = TimeZone.getTimeZone("America/Lima");
        calendar = Calendar.getInstance();
        simpleDateFormatnow = new SimpleDateFormat("HH:mm");
        simpleDateFormatnow.setTimeZone(myTimeZone);
        t_selecthora = (TextView) docenteView.findViewById(R.id.txtHora);
        t_iddocente = docenteView.findViewById(R.id.txtiddocente);
        t_latitud = docenteView.findViewById(R.id.txtiddocente);
        t_longitud = docenteView.findViewById(R.id.txtiddocente);
        t_ubiDocente = docenteView.findViewById(R.id.edit_docente_name);
        t_ubiFecha = docenteView.findViewById(R.id.txtHora);
        imagenRelojButtonSalida = docenteView.findViewById(R.id.btnSalida);
        imagenRelojButton = docenteView.findViewById(R.id.btnReloj);
        TextView savedHora = docenteView.findViewById(R.id.txtHora);

        //String auxDocente = ((TextView) docenteView.findViewById(R.id.edit_docente_name)).getText().toString();
        //String auxDocente = String.valueOf((TextView) docenteView.findViewById(R.id.edit_docente_name));
        t_nombreDocente = docenteView.findViewById(R.id.edit_docente_name);


        mensajePickhour = "";
        for (int j = 0; j < dataUbicacionesLatitud.size(); j++) {
            String colegiocomparar = t_nombreDocente.getText().toString();
            if(colegioproximo.equals(colegiocomparar)){
                mensajePickhour = "DENTRO";
            }
        }


        mensajeAlert = mensajeAlert + "\n \nI.E: " + t_nombreDocente.getText().toString() + "\n \nEspecialista: " + especialista;

        if(mensajePickhour.equals("DENTRO")){
            if (t_selecthora.getText().equals("Sin asignar") || !t_selecthora.getText().equals("COMPLETO")){

                AlertDialog.Builder alerta = new AlertDialog.Builder(Especialistas.this);
                alerta.setMessage(mensajeAlert)
                        .setCancelable(false)
                        .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                TimeZone myTimeZoneaux = TimeZone.getTimeZone("America/Lima");
                                calendar = Calendar.getInstance();
                                simpleDateFormatnow = new SimpleDateFormat("HH:mm");
                                simpleDateFormatnow.setTimeZone(myTimeZoneaux);

                                horanow = simpleDateFormatnow.format(calendar.getTime());
                                String auxDocente = ((TextView) docenteView.findViewById(R.id.edit_docente_name)).getText().toString();
                                datoIdDocenteaux = t_iddocente.getText().toString();
                                String docenteNombre = "";
                                //String pruebaauax = concatenarAux + dataDocente.get(j).toString() + ";" + colegio + ";" + horanow + ";" + idcolegio + ";" + idforDocente + "\n";
                                for (int j = 0; j < dataDocente.size(); j++) {
                                    if (dataDocente.get(j).equals(auxDocente)) {
                                        docenteNombre = dataDocente.get(j).toString();
                                        imagenRelojButtonSalida.setVisibility(View.VISIBLE);
                                        imagenRelojButton.setVisibility(View.INVISIBLE);

                                        dataDocente.remove(j);
                                        dataHora.remove(j);
                                        listIdDocentes.remove(j);
                                        dataDocente.add(auxDocente);
                                        dataHora.add(horanow);
                                        listIdDocentes.add(datoIdDocenteaux);
                                        t_selecthora.setText(horanow);
                                        savedHora.setText(horanow + " -> " + especialista);
                                        if (asunto.equals("REGISTRO ASISTENCIA SALIDA DOCENTE")){
                                            t_selecthora.setText("COMPLETO");
                                        }

                                        FileOutputStream fileOutputStream = null;
                                        try {
                                            fileOutputStream = openFileOutput(filePicker, MODE_PRIVATE);
                                            fileOutputStream.write(0);
                                            stringBuilder.setLength(0);
                                            stringBuilder.delete(0, stringBuilder.length());
                                        } catch (Exception e) {
                                        }
                                    } else if (dataDocente.get(j).equals("")) {
                                        dataDocente.remove(j);
                                        dataHora.remove(j);
                                        dataIdDocentes.remove(j);
                                        dataDocente.add(auxDocente);
                                        dataHora.add(horanow);
                                        dataIdDocentes.add(datoIdDocenteaux);
                                        t_selecthora.setText(horanow);
                                        savedHora.setText(horanow + " -> " + especialista);
                                    }
                                }
                                String pruebaauax = "";
                                ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
                                boolean auxConexion = connectivityManager.getActiveNetwork() != null && connectivityManager.getActiveNetworkInfo().isConnected();
                                //auxConexion!=true
                                //no.equals("")

                                TimeZone myTimeZone = TimeZone.getTimeZone("America/Lima");
                                SimpleDateFormat simpleDateFormatnowFecha = new SimpleDateFormat("yyyy-MM-dd");
                                simpleDateFormatnowFecha.setTimeZone(myTimeZone);
                                horanowVerificar = simpleDateFormatnowFecha.format(calendar.getTime());

                                getUbicacionRegistro();
                                String latitudSave = String.valueOf(latitudActual);
                                String longitudSave = String.valueOf(longitudActual);

                                horanow = t_selecthora.getText().toString();

                                String horaguardada = savedHora.getText().toString();
                                String cadenaSubstringhora = horaguardada.substring(0,5);

                                if (auxConexion != true) {
                                    pruebaauax = concatenarAux + docenteNombre + ";" + colegio + ";" + horaguardada + ";" + idcolegio + ";" + idforDocente + ";SIN CONEXION" + ";" + horanowVerificar + ";" + latitudSave + ";" + longitudSave + ";" + finalUbicacionEnvio;
                                    pruebaauax = pruebaauax + ";" + ModelInfodevice + ";" + IdInfodevice + ";" + ManufactInfodevice + ";" + BrandInfodevice;
                                    pruebaauax = pruebaauax + ";" + TypeInfodevice + ";" + UserInfodevice + ";" + BaseInfodevice + ";" + SdkInfodevice;
                                    pruebaauax = pruebaauax + ";" + BoardInfodevice + ";" + HostInfodevice + ";" + FingeprintInfodevice + ";" + VCodeInfodevice + "\n";
                                }else {
                                    pruebaauax = concatenarAux + docenteNombre + ";" + colegio + ";" + horaguardada + ";" + idcolegio + ";" + idforDocente + ";CON CONEXION" + ";" + horanowVerificar + ";" + latitudSave + ";" + longitudSave + ";" + finalUbicacionEnvio;
                                    pruebaauax = pruebaauax + ";" + ModelInfodevice + ";" + IdInfodevice + ";" + ManufactInfodevice + ";" + BrandInfodevice;
                                    pruebaauax = pruebaauax + ";" + TypeInfodevice + ";" + UserInfodevice + ";" + BaseInfodevice + ";" + SdkInfodevice;
                                    pruebaauax = pruebaauax + ";" + BoardInfodevice + ";" + HostInfodevice + ";" + FingeprintInfodevice + ";" + VCodeInfodevice + "\n";
                                    String url = "https://ugelpomabamba.gob.pe/ugel/ugelasistencias_docente/sesionespecialista.php";
                                    String finalHoraguardada = cadenaSubstringhora;
                                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            //Toast.makeText(getApplicationContext(),"OPERACIÓN EXITOSA", Toast.LENGTH_SHORT).show();
                                        }
                                    }, error -> Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show()) {
                                        @Nullable
                                        @Override
                                        protected Map<String, String> getParams() throws AuthFailureError {
                                            Map<String, String> parametros = new HashMap<String, String>();

                                            parametros.put("colegio", colegio);
                                            parametros.put("docente", auxDocente);
                                            parametros.put("t_llegada", cadenaSubstringhora);
                                            parametros.put("turno", "MAÑANA");
                                            parametros.put("asunto", asunto);
                                            parametros.put("FK_idcolegio", idcolegio);
                                            parametros.put("FK_iddocente", datoIdDocenteaux);

                                            parametros.put("latitud", latitudSave);
                                            parametros.put("longitud", longitudSave);
                                            parametros.put("ubicacion", finalUbicacionEnvio);

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
                                    RequestQueue requestQueue = Volley.newRequestQueue(Especialistas.this);
                                    requestQueue.add(stringRequest);

                                }
                                FileOutputStream fileOutputStream = null;
                                try {
                                    fileOutputStream = openFileOutput(filePicker, MODE_PRIVATE);
                                    fileOutputStream.write(pruebaauax.getBytes());
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

                                t_nombreDocente.setBackgroundColor(Color.GRAY);
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                AlertDialog titulo = alerta.create();
                titulo.setTitle("REGISTRAR ASISTENCIA");
                titulo.show();
            }
        }
        else{
            AlertDialog.Builder alerta = new AlertDialog.Builder(Especialistas.this);
            alerta.setMessage("UBICACION DE LA I.E. FUERA DE RANGO O GPS DESHABILITADO")
                    .setCancelable(false)
                    .setPositiveButton("CERRAR", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
            AlertDialog titulo = alerta.create();
            titulo.setTitle("GEOFERENCIA NO RECONOCIBLE");
            titulo.show();
        }
    }

    public String verificaAsistencia(String fecha){
        String auxArchivo = NOM_ARCHIVO;
        FileInputStream fileInputStream = null;
        String retornar = "";
        try {
            fileInputStream = openFileInput(auxArchivo);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String lineaTexto;
            while ((lineaTexto = bufferedReader.readLine())!=null){
                stringBuilder.append(lineaTexto).append("\n");
            }
            retornar = stringBuilder.substring(stringBuilder.length() - 18, stringBuilder.length()-10).toString().trim();
            //tView.setText(stringBuilder.toString());
            //tView.setText(stringBuilder.substring(stringBuilder.length() - 18).toString().trim());
        }catch (Exception ex){

        }finally {
            if (fileInputStream!=null){
                try {
                    fileInputStream.close();
                }catch (Exception e){

                }
            }
        }
        NOM_ARCHIVO = auxAtribArchivo + colegio.toString().trim() + ".txt";
        return retornar;
    }

    public void getUbicacionRegistro(){
        TimeZone myTimeZone = TimeZone.getTimeZone("America/Lima");
        calendar = Calendar.getInstance();
        simpleDateFormatnow = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        horanow = simpleDateFormatnow.format(calendar.getTime());
        RequestQueue requestQueue;
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        boolean auxConexion = connectivityManager.getActiveNetwork() != null && connectivityManager.getActiveNetworkInfo().isConnected();

        String URL = "https://ugelpomabamba.gob.pe/ugel/ugelasistencias_docente/verGeoreferencia.php?colegio=" + colegio;
        if (auxConexion != true) {
        }else {
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    JSONObject jsonObject = null;
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            jsonObject = response.getJSONObject(i);
                            ubicacionColegioLatitud = jsonObject.getString("latitud");
                            ubicacionColegioLongitud = jsonObject.getString("longitud");
                            t_latitud.setText(ubicacionColegioLatitud);
                            t_longitud.setText(ubicacionColegioLongitud);
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
            requestQueue = Volley.newRequestQueue(Especialistas.this);
            requestQueue.add(jsonArrayRequest);
        }

        if (auxConexion != true) {
        }else {
            String url = "https://ugelpomabamba.gob.pe/ugel/ugelasistencias_docente/createBitacora.php";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    //Toast.makeText(getApplicationContext(),"OPERACIÓN EXITOSA", Toast.LENGTH_SHORT).show();
                }
            }, error -> Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show()) {
                @Nullable
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> parametros = new HashMap<String, String>();

                    parametros.put("colegio", colegio);
                    parametros.put("asunto", "REGISTRO DE LLEGADA AL COLEGIO");
                    parametros.put("latitud", String.valueOf(latitudActual));
                    parametros.put("longitud", String.valueOf(longitudActual));
                    parametros.put("ubicacion", finalUbicacionEnvio);
                    parametros.put("FK_idcolegio", idcolegio);

                    horanow = horanow.substring(0,5);
                    parametros.put("fecha_envio", horanow);
                    parametros.put("FK_iddocente", datoIdDocenteaux);

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
            requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);
        }

        myTimeZone = TimeZone.getTimeZone("America/Lima");
        simpleDateFormatnow = new SimpleDateFormat("HH:mm");
        horanow = simpleDateFormatnow.format(calendar.getTime());
    }


    public void openFilesave(){

        /**
         Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
         intent.addCategory(Intent.CATEGORY_OPENABLE);
         intent.setType("application/pdf");
         startActivityForResult(intent, CHOOSE_PDF_FROM_DEVICE);

         Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
         intent.setType("application/pdf");
         resultLauncher.launch(intent);
         Intent intent = new Intent();
         intent.setType("application/pdf");
         intent.setAction(Intent.ACTION_GET_CONTENT);
         startActivityForResult(Intent.createChooser(intent,"Select pdf file"),CHOOSE_PDF_FROM_DEVICE);*/

        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Choose File to Upload.."), CHOOSE_PDF_FROM_DEVICE);
    }


    public void uploadtoserver(){
        final String docenteUploadImgString = t_docenteUploadImg.getText().toString();
        StringRequest request = new StringRequest(Request.Method.POST, apirul, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                i_PruebaBorrar.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), "Foto Subida", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "PRIMERO TOMAR FOTO", Toast.LENGTH_SHORT).show();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError{
                Map<String, String> map = new HashMap<String, String>();
                map.put("colegio",colegio);
                map.put("docente",docenteUploadImgString);
                map.put("turno","MAÑANA");
                map.put("FK_idcolegio",idcolegio);
                map.put("uploadImg",encodedimage);
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(Especialistas.this);
        queue.add(request);
    }

    public void fechaTextfile (String fechaVerificar){
        String pattern = "yyyy-MM-dd";
        TimeZone myTimeZone = TimeZone.getTimeZone("America/Lima");
        calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormatnowFecha = new SimpleDateFormat("yyyy-MM-dd");
        simpleDateFormatnowFecha.setTimeZone(myTimeZone);
        horanowVerificar = simpleDateFormatnowFecha.format(calendar.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        try {
            Date date1 = sdf.parse(fechaVerificar);
            Date date2 = sdf.parse(horanowVerificar);
            long elapsedms = date1.getTime() - date2.getTime();
            diff = TimeUnit.DAYS.convert(elapsedms, TimeUnit.MILLISECONDS);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
    }
    public void logout(){
        Intent home = new Intent(Especialistas.this, MainActivity.class);
        home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(home);
        finish();
    }
    public void ObtenerCoordenadasActual(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(Especialistas.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
        }else{
            getCoordenada();
        }
    }

    public void getCoordenada() {
        try{
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(3000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                return;
            }
            LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest,new LocationCallback(){
                public void onLocationResult(LocationResult locationResult){
                    super.onLocationResult(locationResult);
                    LocationServices.getFusedLocationProviderClient(Especialistas.this).removeLocationUpdates(this);
                    if (locationResult != null && locationResult.getLocations().size() > 0){
                        int latestLocationIndex = locationResult.getLocations().size() - 1;
                        latitudActual = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                        longitudActual = locationResult.getLocations().get(latestLocationIndex).getLongitude();
                        //latitudActual = -8.8194667;
                        //longitudActual = -77.4590151;
                        String ubicacionEnvio = "DESCONOCIDO";

                        double distanciaaux = 9999999;
                        double metros = 9999999;
                        for (int i = 0; i < dataUbicacionesLatitud.size(); i++) {
                            Double ubicacionColegioLatitud1 = Double.valueOf(dataUbicacionesLatitud.get(i));
                            Double ubicacionColegioLongitud1 = Double.valueOf(dataUbicacionesLongitud.get(i));
                            String nuevaCoordenadaExcel = "";

                            for (int j = 0; j < listExcelUbicacionesColegio.size(); j++) {
                                if(dataUbicacionesColegio.get(i).equals(listExcelUbicacionesColegio.get(j))){
                                    ubicacionColegioLatitud1 = Double.valueOf(listExcelUbicacionesLatitud.get(j));
                                    ubicacionColegioLongitud1 = Double.valueOf(listExcelUbicacionesLongitud.get(j));
                                    nuevaCoordenadaExcel = listExcelUbicacionesColegio.get(j).toString();
                                }
                            }

                            Double auxLatitudActual = Double.valueOf(latitudActual);
                            Double auxLongitudActual = Double.valueOf(longitudActual);

                            double radioTierra = 6371;//en kilómetros
                            double dLat = Math.toRadians(ubicacionColegioLatitud1 - auxLatitudActual);
                            double dLng = Math.toRadians(ubicacionColegioLongitud1 - auxLongitudActual);
                            double sindLat = Math.sin(dLat / 2);
                            double sindLng = Math.sin(dLng / 2);
                            double va1 = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                                    * Math.cos(Math.toRadians(auxLatitudActual)) * Math.cos(Math.toRadians(ubicacionColegioLatitud1));
                            double va2 = 2 * Math.atan2(Math.sqrt(va1), Math.sqrt(1 - va1));
                            double distancia = radioTierra * va2;
                            metros = distancia * 1000;
                            if (metros <= distanciaaux){
                                distanciaaux = metros;
                                colegioproximo = dataUbicacionesColegio.get(i).toString();
                                if(!nuevaCoordenadaExcel.equals("")){
                                    colegioproximo = nuevaCoordenadaExcel;
                                }
                            }
                        }
                        double redondearDistancia = Math.round(distanciaaux * 100.0) / 100.0;
                        if (distanciaaux <= 120){
                            finalUbicacionEnvio = "DENTRO DE LA I.E. " + colegioproximo;
                            t_ubicacionreal.setText("DENTRO DE LA I.E: " + colegioproximo);
                        }else{
                            finalUbicacionEnvio = "DESCONOCIDO";
                            t_ubicacionreal.setText("UBICACION DESCONOCIDA" + "\nDistancia: " + redondearDistancia + "m");
                        }
                    }
                }
            }, Looper.myLooper());
        }catch (Exception e){

        }
    }

    @Override
    public void onProviderDiseabled(String provider) {}
    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onGpsStatusChanged(int event) {}

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

    @SuppressLint("MissingPermission")
    private void showLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER,0,0,this);
        }else{
            Toast.makeText(this, "GPS ACTIVADO", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        latitud = location.getLatitude();
        longitud = location.getLongitude();

        //t_ubicaLatitud.setText(latitud.toString());
        //t_ubicaLongitud.setText(longitud.toString());
        t_ubicaLatitud.setText(latitud.toString());
        t_ubicaLongitud.setText(longitud.toString());


        String ubicacionEnvio = "DESCONOCIDO";


        Double ubicacionColegioLatitud1 = Double.valueOf(dataLatitud);
        Double ubicacionColegioLongitud1 = Double.valueOf(dataLongitud);

        Double auxLatitudActual = Double.valueOf(t_ubicaLatitud.getText().toString());
        Double auxLongitudActual = Double.valueOf(t_ubicaLongitud.getText().toString());

        double radioTierra = 6371;//en kilómetros
        double dLat = Math.toRadians(ubicacionColegioLatitud1 - auxLatitudActual);
        double dLng = Math.toRadians(ubicacionColegioLongitud1 - auxLongitudActual);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double va1 = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(auxLatitudActual)) * Math.cos(Math.toRadians(ubicacionColegioLatitud1));
        double va2 = 2 * Math.atan2(Math.sqrt(va1), Math.sqrt(1 - va1));
        double distancia = radioTierra * va2;
        double metros = distancia * 1000;

        if (metros <= 100){
            t_ubicacionreal.setText("DENTRO DE I.E.");
        }else{
            t_ubicacionreal.setText("UBICACIÓN DESCONOCIDA");
        }
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
            String auxArchivo = nombrearc + colegio + ".txt";
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
                    String part7 = parts[6]; // FECHA
                    String part8 = parts[7]; // LATITUD
                    String part9 = parts[8]; // LONGITUD
                    String part10 = parts[9]; // UBICACION

                    String subtringpart3 = part3.substring(0,5);

                    vasovacio = vasovacio + part1 + ";" + part2 + ";" + part3 + ";" + part4 + ";" + part5 + ";" + "CON CONEXION" + ";" + part7 + ";" + part8 + ";" + part9 + ";" + part10 +"\n";
                    retomarAux = vasovacio;

                    if (part6.equals("SIN CONEXION")){
                        String url = "https://ugelpomabamba.gob.pe/ugel/ugelasistencias_docente/sesionespecialista.php";
                        String finalPart = subtringpart3;
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
                                parametros.put("t_llegada", finalPart);
                                parametros.put("turno", turno);
                                parametros.put("asunto", "REGISTRO ASISTENCIA DOCENTE " + turno + " SIN CONEXION");
                                parametros.put("FK_idcolegio", part4);
                                parametros.put("FK_iddocente", part5);

                                parametros.put("latitud", part8);
                                parametros.put("longitud", part9);
                                parametros.put("ubicacion", part10);

                                return parametros;
                            }
                        };
                        RequestQueue requestQueue = Volley.newRequestQueue(this);
                        requestQueue.add(stringRequest);

                        url = "https://ugelpomabamba.gob.pe/ugel/ugelasistencias_docente/createBitacora.php";

                        String finalPart1 = subtringpart3;
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
                                parametros.put("latitud", part8);
                                parametros.put("longitud", part9);
                                parametros.put("ubicacion", part10);
                                parametros.put("FK_idcolegio", part4);
                                parametros.put("fecha_envio", finalPart1);

                                return parametros;
                            }
                        };
                        requestQueue = Volley.newRequestQueue(this);
                        requestQueue.add(stringRequest);
                    }
                }

                //File dir = getFilesDir();
                //File fileDelete = new File(dir, nombrearc + l_Colegio.getText().toString().trim() + ".txt");
                //boolean deleted = fileDelete.delete();

                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = openFileOutput(nombrearc + colegio + ".txt", MODE_PRIVATE);
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
            String auxArchivo = nombrearc + colegio + ".txt";
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

                    vasovacio = vasovacio + part1 + ";" + part2 + ";" + part3 + ";" + part4 + ";" + part5 + ";" + "REPORTE INASISTENCIA CON CONEXION" + ";" + part7 + ";" + part8 + ";" + part9 + ";" + part10  + ";" + part11 + ";" + part12 + ";" + part13 + "\n";
                    retomarAuxInasistencia = vasovacio;

                    if (part6.equals("REPORTE INASISTENCIA SIN CONEXION")){
                        String url = "https://ugelpomabamba.gob.pe/ugel/ugelasistencias_docente/sesionespecialista.php";
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
                    fileOutputStream = openFileOutput(nombrearc + colegio + ".txt", MODE_PRIVATE);
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

    public void enviarContraseniasaved(){
        String auxArchivo = "dataCambiosTemp" + colegio + ".txt";
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
                            protected Map<String, String> getParams() throws AuthFailureError {
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
        String auxArchivo = "dataCambiosTemp" + colegio + ".txt";
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
            fileOutputStream = openFileOutput("dataCambiosTemp" + colegio + ".txt", MODE_PRIVATE);
            fileOutputStream.write(0);

            fileOutputStream = openFileOutput("dataCambiosTemp" + colegio + ".txt", MODE_PRIVATE);
            fileOutputStream.write(vasovacio.getBytes());
        } catch (Exception e) {
        }
    }
}