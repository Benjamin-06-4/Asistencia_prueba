package appAsis.example.asistenciaugelpomabamba2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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

import appAsis.example.asistenciaugelpomabamba2.R;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class tarde extends AppCompatActivity implements View.OnClickListener, Cobertura {

    private static final int REQUEST_CODE = 1, REQUEST_CODE_IMG = 100;;
    LinearLayout layoutList;
    Button buttonAdd, buttonAsistencia, b_uploadImg;
    ImageView alertBtnDelete, camaraView,i_PruebaBorrar;
    CircleImageView imgUpload; Bitmap bitmapUploadImg; String encodedimage;
    public static final String apirul = "https://ugelpomabamba.gob.pe/ugel/ugelasistencias_docente/model/file/img/uploadImg.php";
    String file_Path = null; private static final int PERMISSION_REQUEST_CODE = 1111; String file_path=null; Button b_subirfile;
    public static final String apirulfile = "https://ugelpomabamba.gob.pe/ugel/ugelasistencias_docente/model/file/archivos/uploadFile.php";
    private static final int REQUEST_GALLERY = 200;
    Button b_openfile;

    List<String> listDocentes = new ArrayList<String>(), listColegios = new ArrayList<String>(), listIdDocentes = new ArrayList<String>();
    List<String> dataDocente = new ArrayList<String>(), dataHora = new ArrayList<String>(), dataIdDocentes = new ArrayList<String>();
    List<String> dataTemporalDocenteLlegadaArray = new ArrayList<String>(), dataTemporalHoraLlegadaArray = new ArrayList<String>();
    List<String>  dataTemporalHoraSalidaArray = new ArrayList<String>(), dataTemporalDocenteSalidaArray = new ArrayList<String>();
    List<String>  dataTemporalDocenteJustificacion = new ArrayList<String>(), listDocenteColegio = new ArrayList<String>();
    List<String>  dataExcelcargada = new ArrayList<String>(), dataExcelEstado = new ArrayList<String>(), dataExcelColegio = new ArrayList<String>(), dataExcelDocente = new ArrayList<String>();
    List<String> dataAsisColeDoceLlegada = new ArrayList<String>(), dataAsisColeDoceHoraLlegada = new ArrayList<String>();
    List<String> dataAsisColeDoceSalida = new ArrayList<String>(), dataAsisColeDoceHoraSalida = new ArrayList<String>();
    List<String> dataAsisColeDoceJusti = new ArrayList<String>(), dataAsisColeDoceAsuntoJusti = new ArrayList<String>();
    List<String> listExcelUbicacionesColegio = new ArrayList<String>(), listExcelUbicacionesLatitud = new ArrayList<String>(), listExcelUbicacionesLongitud = new ArrayList<String>();
    List<String> dataExcelDocentesInactivos = new ArrayList<String>();
    List<LinearLayout> listLayout = new ArrayList<LinearLayout>(), listLayoutAux = new ArrayList<LinearLayout>();
    List<View> listViews = new ArrayList<View>(),listViewsAux = new ArrayList<View>();
    String dataLatitud, dataLongitud, fechaVerificar = "";
    String linea, auxText = "", colegio, datoIdDocenteaux, guardarIdDocente, horanow, horanowVerificar, getPutFiltrar, putDocente, putNivelColegio, putCodmod;
    Double longitud = 0.0, latitud = 0.0;
    TextView tView, t_iddocente, t_latitud, t_longitud, t_ubiDocente, t_ubiFecha, t_ubicacionreal, t_ubicaLatitud, t_ubicaLongitud, t_docenteUploadImg;
    int hora, minuto;
    ImageButton imagenRelojButton,imagenRelojButtonSalida, imagenButtonInasistencia, b_search, btnCamara;
    TextView t_selecthora, t_nombreDocente, lbl_search;
    EditText t_search;
    static String NOM_ARCHIVO = "dataSaveTarde",auxAtribArchivo ="dataSaveTarde", idcolegio, retomarAux="", guardarBorrar = "", auxGuardarBorrar ="";
    static String retomarAuxSalida ="", auxGuardarBorrarSalida ="";
    private static final int PERMISSION_LOCATION = 1000; OutputStream outputStream;
    Boolean enviadoArc = false;

    StringBuilder stringBuilder = new StringBuilder();

    Calendar calendar;
    TimeZone myTimeZone = TimeZone.getTimeZone("America/Lima");
    SimpleDateFormat simpleDateFormatnow;
    String dataAuxTemporal = "", ubicacionColegioLatitud ="", ubicacionColegioLongitud ="", finalUbicacionEnvio = "";
    boolean exists = false;
    double diff = 0, latitudActual = 0, longitudActual = 0;
    String[] justificaciones = {"Injustificada","Justificada","Licencia sin goce","Permiso sin goce","Tardanza","Huelga/paro", "Otros"};

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listmaniana);

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

        imagenRelojButton = (ImageButton) findViewById(R.id.btnReloj);
        imagenRelojButtonSalida = (ImageButton) findViewById(R.id.btnSalida);
        imagenButtonInasistencia = (ImageButton) findViewById(R.id.btnInasistencia);

        InputStream is = this.getResources().openRawResource(R.raw.datadocentes);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        colegio = getIntent().getStringExtra("pass");
        idcolegio = getIntent().getStringExtra("idcolegio");
        getPutFiltrar = getIntent().getStringExtra("filtro");
        putDocente = getIntent().getStringExtra("docente");
        putNivelColegio = getIntent().getStringExtra("nivelcolegio");
        putCodmod = getIntent().getStringExtra("codmodput");

        btnCamara = findViewById(R.id.btnCamara);
        //camaraView = findViewById(R.id.imageView2);
        tView.setText(colegio);

        try {
            if(is!=null){
                while((linea = reader.readLine())!=null){
                    String auxDocenteLine = linea.split(";")[2] + ' ' + linea.split(";")[3] + ' ' + linea.split(";")[4];
                    String nivelDatanivel = linea.split(";")[6];
                    if (getPutFiltrar.equals("Docente")){
                        if (auxDocenteLine.equals(putDocente) && colegio.equals(linea.split(";")[0])){
                            listColegios.add(linea.split(";")[0]);
                            dataExcelcargada.add(linea.split(";")[1]);
                            listDocentes.add(linea.split(";")[2] + ' ' + linea.split(";")[3] + ' ' + linea.split(";")[4]);
                            listIdDocentes.add(linea.split(";")[5]);
                        }
                    }else{
                        if ((nivelDatanivel.equals("Unidirector") || putNivelColegio.equals(nivelDatanivel)) && colegio.equals(linea.split(";")[0]) && putCodmod.equals(linea.split(";")[8])) {
                            listColegios.add(linea.split(";")[0]);
                            dataExcelcargada.add(linea.split(";")[1]);
                            listDocentes.add(linea.split(";")[2] + ' ' + linea.split(";")[3] + ' ' + linea.split(";")[4]);
                            listIdDocentes.add(linea.split(";")[5]);
                        }
                    }
                }
            }
            is.close();

        }catch (Exception e){}

        String auxArchivoExcel = "dataExcelDocentes.txt";
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
                fileInputStream = openFileInput(auxArchivoExcel);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String lineaTexto;
                StringBuilder stringBuilder = new StringBuilder();
                while ((lineaTexto = bufferedReader.readLine()) != null) {
                    stringBuilder.append(lineaTexto).append("\n");

                    String[] parts = lineaTexto.split(";");
                    String part1 = parts[0]; // COLEGIO
                    String part2 = parts[1]; // DNI
                    String part3 = parts[3]; // TIPO Docente
                    String part4 = parts[4]; // ID
                    String part5 = parts[7]; // DOCENTE
                    String part6 = parts[9]; // ESTADO
                    String part7 = parts[10]; // ESTADO
                    boolean comparar = false;

                    if(!part6.equals("ACTIVO")){
                        dataExcelDocentesInactivos.add(part5);
                    }

                    if (putCodmod.equals(part7)) {
                        if(dataExcelcargada.size() > 0){
                            for (int i = 0; i < dataExcelcargada.size(); i++) {
                                if (part2.equals(dataExcelcargada.get(i)) && part6.equals("ACTIVO") && part3.equals("Tarde") && colegio.equals(part1) ){
                                    //if (part3.equals("Docente") || part3.equals("Tarde")){}
                                    listColegios.set(i,part1);
                                    listDocentes.set(i,part5);
                                    listIdDocentes.set(i,part4);
                                    comparar = true;
                                }
                            }
                            if(!comparar){
                                listColegios.add(part1);
                                listDocentes.add(part5);
                                listIdDocentes.add(part4);
                            }
                        }else{
                            if (part6.equals("ACTIVO") && (part3.equals("Docente") || part3.equals("Tarde")) && colegio.equals(part1)){
                                listColegios.add(part1);
                                listDocentes.add(part5);
                                listIdDocentes.add(part4);
                            }
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

        String lineaUbicacion;
        InputStream isUbicacion = this.getResources().openRawResource(R.raw.datacolegio);
        BufferedReader readerUbicacion = new BufferedReader(new InputStreamReader(isUbicacion));
        try {
            if(isUbicacion!=null){
                while((lineaUbicacion = readerUbicacion.readLine())!=null){
                    if (colegio.equals(lineaUbicacion.split(";")[0])){
                        dataLatitud = lineaUbicacion.split(";")[5];
                        dataLongitud = lineaUbicacion.split(";")[6];
                    }

                    //listado.add(linea.split(";")[1]);
                }
            }
            is.close();

        }catch (Exception e){}

        String guardaInfotextfile = "";
        String auxArchivo = "dataInternalTempTardellegada" + colegio + ".txt";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Path path = Files.createTempFile("dataInternalTempTardellegada", ".txt");
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

                String filePicker = "dataInternalTempTardellegada" + colegio + ".txt";
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

        String auxArchivoQueryAsis = "dataAsisColeDoce" + colegio + "TARDE" + ".txt";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Path path = Files.createTempFile("dataAsisColeDoce", ".txt");
                exists = Files.exists(path);     //true
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(exists) {
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = openFileInput(auxArchivoQueryAsis);
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

                    String part7 = parts[6]; // FECHA
                    String part8 = parts[7]; // LATITUD
                    String part9 = parts[8]; // LONGITUD
                    String part10 = parts[9]; // UBICAION

                    String part11 = parts[10]; // ModelInfodevice
                    String part12 = parts[11]; // IdInfodevice
                    String part13 = parts[12]; // ManufactInfodevice

                    String part14 = parts[13]; // BrandInfodevice
                    String part15 = parts[14]; // TypeInfodevice
                    String part16 = parts[15]; // UserInfodevice
                    String part17 = parts[16]; // BaseInfodevice
                    String part18 = parts[17]; // SdkInfodevice
                    String part19 = parts[18]; // BoardInfodevice
                    String part20 = parts[19]; // HostInfodevice
                    String part21 = parts[20]; // FingeprintInfodevice
                    String part22 = parts[21]; // VCodeInfodevice
                    String part23 = parts[22]; // asunto

                    part7 = part7.substring(0,10);
                    fechaTextfile (part7);

                    if (diff == 0 || (!part6.equals("CON CONEXION") && diff <= -1)){
                        if (part23.equals("REGISTRO ASISTENCIA LLEGADA DOCENTE TURNO TARDE")){
                            dataAsisColeDoceLlegada.add(part1);
                            dataAsisColeDoceHoraLlegada.add(part3);
                        }
                        if (part23.equals("REGISTRO ASISTENCIA SALIDA DOCENTE TURNO TARDE")){
                            dataAsisColeDoceSalida.add(part1);
                            dataAsisColeDoceHoraSalida.add(part3);
                        }
                        if (part23.equals("REPORTE INASISTENCIA CON CONEXION")){
                            dataAsisColeDoceJusti.add(part1);
                            dataAsisColeDoceAsuntoJusti.add(part3);
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
        String auxArchivoSalida = "dataInternalTempTardeSalida" + colegio + ".txt";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Path path = Files.createTempFile("dataInternalTempTardeSalida", ".txt");
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

                String filePicker = "dataInternalTempTardeSalida" + colegio + ".txt";
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
            if(listColegios.get(i).equals(colegio)){
                listDocenteColegio.add(listDocentes.get(i));
            }
        }

        ObtenerCoordenadasActual();
        for (int i = 0; i < listColegios.size(); i++) {
            if(listColegios.get(i).equals(colegio)){
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

                imagenRelojButton = docenteView.findViewById(R.id.btnReloj);
                imagenButtonInasistencia = (ImageButton) docenteView.findViewById(R.id.btnInasistencia);
                imagenRelojButtonSalida = (ImageButton) docenteView.findViewById(R.id.btnSalida);
                imagenRelojButtonSalida.setVisibility(View.INVISIBLE);
                btnCamara = docenteView.findViewById(R.id.btnCamara);

                if (t_selecthora.getText().equals("Sin asignar")) {
                    imagenRelojButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ObtenerCoordenadasActual();
                            String auxArchivo = "dataInternalTempTardellegada" + colegio + ".txt";
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                try {
                                    Path path = Files.createTempFile("dataInternalTempTardellegada", ".txt");
                                    exists = Files.exists(path);     //true
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            String vasovacio = "";
                            if (exists) {
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
                            String mensajeAlert = "¿ENVIAR HORA DE LLEGADA DOCENTE?";
                            String filePicker = "dataInternalTempTardellegada" + colegio + ".txt";
                            String asunto = "REGISTRO ASISTENCIA LLEGADA DOCENTE TURNO TARDE";
                            pickerHour(docenteView, mensajeAlert, filePicker, asunto, imagenRelojButton, imagenRelojButtonSalida, retomarAux, idforDocente);
                        }
                    });
                }

                imagenRelojButtonSalida.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ObtenerCoordenadasActual();
                        String auxArchivo = "dataInternalTempTardeSalida" + colegio + ".txt";
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            try {
                                Path path = Files.createTempFile("dataInternalTempTardeSalida", ".txt");
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
                            auxGuardarBorrarSalida = vasovacio;
                        }
                        String mensajeAlert = "¿ENVIAR HORA DE SALIDA DOCENTE?";
                        String filePicker = "dataInternalTempTardeSalida" + colegio + ".txt";
                        String asunto = "REGISTRO ASISTENCIA SALIDA DOCENTE TURNO TARDE";

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

                        String inasisJustificaurl = "https://ugelpomabamba.gob.pe/ugel/ugelasistencias_docente/sesion.php";
                        Metodos alertDialog = new Metodos(tarde.this, justificaciones, "Tarde",
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

                        if (ContextCompat.checkSelfPermission(tarde.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
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

                //ELIMINAR A LOS INACTIVOS
                if (dataExcelDocentesInactivos.size() > 0){
                    for (int j = 0; j < dataExcelDocentesInactivos.size(); j++) {
                        if (dataExcelDocentesInactivos.get(j).equals(listDocentes.get(i))){
                            t_nombreDocente.setBackgroundColor(Color.GRAY);
                            docenteView.findViewById(R.id.btnReloj).setVisibility(View.INVISIBLE);
                            docenteView.findViewById(R.id.btnSalida).setVisibility(View.INVISIBLE);
                            imagenRelojButton.setVisibility(View.INVISIBLE);
                            imagenButtonInasistencia.setVisibility(View.INVISIBLE);
                            btnCamara.setVisibility(View.INVISIBLE);
                            t_selecthora.setText("INACTIVO");
                        }
                    }
                }

                if (dataTemporalHoraLlegadaArray.size() > 0){
                    for (int j = 0; j < dataTemporalHoraLlegadaArray.size(); j++) {
                        if (dataTemporalDocenteLlegadaArray.get(j).equals(listDocentes.get(i))){
                            if (diff == 0){
                                t_nombreDocente.setBackgroundColor(Color.GRAY);
                                docenteView.findViewById(R.id.btnReloj).setVisibility(View.INVISIBLE);
                                docenteView.findViewById(R.id.btnSalida).setVisibility(View.VISIBLE);
                                imagenRelojButton.setVisibility(View.INVISIBLE);
                                t_selecthora.setText(dataTemporalHoraLlegadaArray.get(j));
                                dataHora.add(dataTemporalHoraLlegadaArray.get(j));
                            }
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
                            if (diff == 0) {
                                t_nombreDocente.setBackgroundColor(Color.GRAY);
                                docenteView.findViewById(R.id.btnReloj).setVisibility(View.INVISIBLE);
                                docenteView.findViewById(R.id.btnSalida).setVisibility(View.VISIBLE);
                                t_selecthora.setText("COMPLETO");
                                dataHora.add("COMPLETO");
                            }
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
                            if (diff == 0){
                                //t_nombreDocente.setBackgroundColor(Color.GRAY);
                                //docenteView.findViewById(R.id.btnReloj).setVisibility(View.INVISIBLE);
                                //docenteView.findViewById(R.id.btnSalida).setVisibility(View.INVISIBLE);
                                //docenteView.findViewById(R.id.btnInasistencia).setVisibility(View.INVISIBLE);
                                t_selecthora.setText("Reporte");
                                dataHora.add("Reporte");
                            }
                        }else{
                            dataHora.add("Sin asignar");
                        }
                    }
                }else{
                    dataHora.add("Sin asignar");
                }

                //dataAsisColeDoceHoraLlegada, dataAsisColeDoceLlegada
                if (dataAsisColeDoceHoraLlegada.size() > 0){
                    for (int j = 0; j < dataAsisColeDoceHoraLlegada.size(); j++) {
                        if (dataAsisColeDoceLlegada.get(j).equals(listDocentes.get(i))){
                            t_nombreDocente.setBackgroundColor(Color.GRAY);
                            docenteView.findViewById(R.id.btnReloj).setVisibility(View.INVISIBLE);
                            docenteView.findViewById(R.id.btnSalida).setVisibility(View.VISIBLE);
                            imagenRelojButton.setVisibility(View.INVISIBLE);
                            t_selecthora.setText(dataAsisColeDoceHoraLlegada.get(j));
                            dataHora.add(dataAsisColeDoceHoraLlegada.get(j));
                        }else{
                            dataHora.add("Sin asignar");
                        }
                    }
                }else{
                    dataHora.add("Sin asignar");
                }
                //dataAsisColeDoceSalida, dataAsisColeDoceHoraSalida
                if (dataAsisColeDoceHoraSalida.size() > 0){
                    for (int j = 0; j < dataAsisColeDoceHoraSalida.size(); j++) {
                        if (dataAsisColeDoceSalida.get(j).equals(listDocentes.get(i))){
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
                //dataAsisColeDoceJusti, dataAsisColeDoceAsuntoJusti
                if (dataAsisColeDoceJusti.size() > 0){
                    for (int j = 0; j < dataAsisColeDoceJusti.size(); j++) {
                        if (dataAsisColeDoceJusti.get(j).equals(listDocentes.get(i))){
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
            }
        };

        b_uploadImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

        b_openfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 if (ActivityCompat.checkSelfPermission(listmaniana.this,
                 Manifest.permission.READ_EXTERNAL_STORAGE) !=
                 PackageManager.PERMISSION_GRANTED){
                 ActivityCompat.requestPermissions(listmaniana.this,
                 new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                 }else{
                 //uploadFile(selectedFilePath);
                 openFilesave();
                 }*/

                //check permission greater than equal to marshmeellow we used run time permission
                if(Build.VERSION.SDK_INT>=23){
                    if(checkPermission()){
                        filePicker();
                    }
                    else{
                        requestPermission();
                    }
                }
                else{
                    filePicker();
                }
            }
        });
        b_subirfile = findViewById(R.id.btnUpload);
        b_subirfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(file_path!=null){
                    UploadFile();
                }
                else{
                    Toast.makeText(tarde.this, "Seleccione un archivo", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_LOCATION);
        }else{
            showLocation();
        }
    }
    //nuevo metodo upload borrar
    private boolean checkPermission(){
        int result= ContextCompat.checkSelfPermission(tarde.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(result== PackageManager.PERMISSION_GRANTED){
            return true;
        }
        else{
            return false;
        }
    }
    private void filePicker(){
        //.Now Permission Working
        Toast.makeText(tarde.this, "SELECTOR DE ARCHIVOS", Toast.LENGTH_SHORT).show();
        //Let's Pick File
        Intent opengallery=new Intent(Intent.ACTION_PICK);
        opengallery.setType("image/*|application/pdf|audio/*");
        startActivityForResult(opengallery,REQUEST_GALLERY);
    }
    private void requestPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(tarde.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            Toast.makeText(tarde.this, "Permitir el acceso a gestor de archivos", Toast.LENGTH_SHORT).show();
        }
        else{
            ActivityCompat.requestPermissions(tarde.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);
        }
    }
    private void UploadFile() {
        tarde.UploadTask uploadTask = new tarde.UploadTask();
        uploadTask.execute(new String[]{file_path});
    }
    public class UploadTask extends AsyncTask<String,String,String> {
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(s.equalsIgnoreCase("true")){
                Toast.makeText(tarde.this, "Archivo Subido", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(tarde.this, "Error envio", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            if(uploadFile(strings[0])){
                return "true";
            }
            else{
                return "failed";
            }
        }

        private boolean uploadFile(String path){
            File file=new File(path);
            try{
                RequestBody requestBody=new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("files",file.getName(),RequestBody.create(MediaType.parse("image/*|application/pdf|audio/*"),file))
                        .addFormDataPart("some_key","some_value")
                        .addFormDataPart("submit","submit")
                        .build();

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(apirulfile)
                        .post(requestBody)
                        .build();

                OkHttpClient client = new OkHttpClient();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, okhttp3.Response response) throws IOException {

                    }
                });
                file_path = "";
                return true;
            }
            catch (Exception e){
                e.printStackTrace();
                file_path = "";
                return false;
            }
        }
    }

    public void askPermission(){
        ActivityCompat.requestPermissions(tarde.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE_IMG);
    }

    public void abrirCamara(View docenteView){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,1);
        t_docenteUploadImg = docenteView.findViewById(R.id.edit_docente_name);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == 1 && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            bitmapUploadImg = (Bitmap) extras.get("data");
            i_PruebaBorrar.setImageBitmap(bitmapUploadImg);
            encodebitmap(bitmapUploadImg);
        }
        if(requestCode==REQUEST_GALLERY && resultCode== Activity.RESULT_OK){
            String filePath=getRealPathFromUri(data.getData(),tarde.this);
            Log.d("File Path : "," "+filePath);
            //now we will upload the file
            //lets import okhttp first
            this.file_path=filePath;

            File file=new File(filePath);
            //tView.setText(file.getName());
            //file_name.setText(file.getName());

        }

    }
    public String getRealPathFromUri(Uri uri, Activity activity){
        Cursor cursor = activity.getContentResolver().query(uri,null,null,null,null);
        if(cursor == null){
            return uri.getPath();
        }else{
            cursor.moveToFirst();
            int id = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
            return cursor.getString(id);
        }

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
        if (t_selecthora.getText().equals("Sin asignar") || !t_selecthora.getText().equals("COMPLETO")){

            AlertDialog.Builder alerta = new AlertDialog.Builder(tarde.this);
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
                                    savedHora.setText(horanow);
                                    if (asunto.equals("REGISTRO ASISTENCIA SALIDA DOCENTE TURNO TARDE")){
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
                                    savedHora.setText(horanow);
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

                            simpleDateFormatnowFecha = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                            simpleDateFormatnowFecha.setTimeZone(myTimeZone);
                            String fecha_registro = simpleDateFormatnowFecha.format(calendar.getTime());

                            getUbicacionRegistro();
                            String latitudSave = String.valueOf(latitudActual);
                            String longitudSave = String.valueOf(longitudActual);

                            horanow = t_selecthora.getText().toString();

                            String horaguardada = savedHora.getText().toString();
                            if (auxConexion != true) {
                                pruebaauax = concatenarAux + docenteNombre + ";" + colegio + ";" + horaguardada + ";" + idcolegio + ";" + idforDocente + ";SIN CONEXION" + ";" + fecha_registro + ";" + latitudSave + ";" + longitudSave + ";" + finalUbicacionEnvio +"\n";
                            }else {
                                pruebaauax = concatenarAux + docenteNombre + ";" + colegio + ";" + horaguardada + ";" + idcolegio + ";" + idforDocente + ";CON CONEXION" + ";" + fecha_registro + ";" + latitudSave + ";" + longitudSave + ";" + finalUbicacionEnvio +"\n";
                                String url = "https://ugelpomabamba.gob.pe/ugel/ugelasistencias_docente/sesion.php";
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
                                        parametros.put("t_llegada", horaguardada);
                                        parametros.put("turno", "TARDE");
                                        parametros.put("asunto", asunto);
                                        parametros.put("FK_idcolegio", idcolegio);
                                        parametros.put("FK_iddocente", datoIdDocenteaux);
                                        parametros.put("fecha_appsave", fecha_registro);

                                        parametros.put("latitud", latitudSave);
                                        parametros.put("longitud", longitudSave);
                                        parametros.put("ubicacion", finalUbicacionEnvio);
                                        return parametros;
                                    }
                                };
                                RequestQueue requestQueue = Volley.newRequestQueue(tarde.this);
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

        SimpleDateFormat simpleDateFormatnowFecha = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        simpleDateFormatnowFecha.setTimeZone(myTimeZone);
        String fecha_registro = simpleDateFormatnowFecha.format(calendar.getTime());

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
            requestQueue = Volley.newRequestQueue(tarde.this);
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
                    parametros.put("asunto", "REGISTRO DE LLEGADA DOCENTE");
                    parametros.put("latitud", latitud.toString());
                    parametros.put("longitud", longitud.toString());
                    parametros.put("ubicacion", finalUbicacionEnvio);
                    parametros.put("FK_idcolegio", idcolegio);
                    parametros.put("fecha_envio", horanow);
                    parametros.put("fecha_appsave", fecha_registro);

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

    public void encodebitmap(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteofimages = byteArrayOutputStream.toByteArray();
        encodedimage = android.util.Base64.encodeToString(byteofimages, Base64.DEFAULT);
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
        RequestQueue queue = Volley.newRequestQueue(tarde.this);
        queue.add(request);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showLocation();
            }
        } else {
            Toast.makeText(this, "PERMISO NO ACEPTADO", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private String hereLocation(Location location){
        latitud = location.getLatitude();
        longitud = location.getLongitude();

        return "Lat. " + location.getLatitude() + "\nLon: " + location.getLongitude();
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

        //tView.setText(hereLocation(location));
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

    @Override
    public void onProviderDiseabled(String provider) {
    }
    @Override
    public void onProviderEnabled(String provider) {
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
    @Override
    public void onGpsStatusChanged(int event) {

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

    public void ObtenerCoordenadasActual(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(tarde.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
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
                    LocationServices.getFusedLocationProviderClient(tarde.this).removeLocationUpdates(this);
                    if (locationResult != null && locationResult.getLocations().size() > 0){
                        int latestLocationIndex = locationResult.getLocations().size() - 1;
                        latitudActual = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                        longitudActual = locationResult.getLocations().get(latestLocationIndex).getLongitude();

                        String ubicacionEnvio = "DESCONOCIDO";

                        Double ubicacionColegioLatitud1 = Double.valueOf(dataLatitud);
                        Double ubicacionColegioLongitud1 = Double.valueOf(dataLongitud);
                        String nuevaCoordenadaExcel = "";

                        for (int j = 0; j < listExcelUbicacionesColegio.size(); j++) {
                            if(colegio.equals(listExcelUbicacionesColegio.get(j))){
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
                        double metros = distancia * 1000;

                        if (metros <= 120){
                            if(!nuevaCoordenadaExcel.equals("")){
                                finalUbicacionEnvio = "DENTRO DE LA I.E. " + nuevaCoordenadaExcel;
                                t_ubicacionreal.setText("DENTRO DE LA I.E: " + nuevaCoordenadaExcel);
                            }else{
                                finalUbicacionEnvio = "DENTRO DE LA I.E. " + colegio;
                                t_ubicacionreal.setText("DENTRO DE I.E: " + colegio);
                            }
                        }else{
                            finalUbicacionEnvio = "DESCONOCIDO";
                            t_ubicacionreal.setText("UBICACIÓN DESCONOCIDA");
                        }
                    }
                }
            }, Looper.myLooper());
        }catch (Exception e){

        }
    }
}