package appAsis.example.asistenciaugelcorongo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class FichaQuestionsActivity extends AppCompatActivity {

    // Código de solicitud para la cámara
    private static final int REQUEST_IMAGE_EVIDENCIA = 101;
    // Variable para identificar cuál botón de "Enviar foto" fue presionado:
    // 1 = Equipo, 2 = Plan Refuerzo, 3 = Diagnóstico, 4 = Reuniones.
    private int currentPhotoQuestion = 0;

    // Campos de datos generales
    private EditText etInstitucionEducativa, etCodigoModular, etNivelEducativo, etFechaMonitoreo, etResponsable;
    // RadioGroups para las respuestas (tipo Sí/No)
    private RadioGroup rgEquipoResponsable, rgPlanRefuerzo, rgDiagnostico, rgReuniones;
    // Botones para "Enviar foto" y el botón para enviar toda la ficha
    private Button btnFotoEquipo, btnFotoPlanRefuerzo, btnFotoDiagnostico, btnFotoReuniones, btnEnviarFicha;
    // Bitmaps que almacenarán las imágenes capturadas para cada pregunta
    private Bitmap evidenciaEquipo, evidenciaPlanRefuerzo, evidenciaDiagnostico, evidenciaReuniones;

    // URL del endpoint para enviar la ficha
    private String fichaEspecialistaUrl = "https://ugelcorongo.pe/ugelasistencias_docente/model/especialista/endpoint.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Forzar modo claro (opcional)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ficha_questions);

        // Enlazar los campos de "Datos generales"
        etInstitucionEducativa = findViewById(R.id.etInstitucionEducativa);
        etCodigoModular = findViewById(R.id.etCodigoModular);
        etNivelEducativo = findViewById(R.id.etNivelEducativo);
        etFechaMonitoreo = findViewById(R.id.etFechaMonitoreo);
        etResponsable = findViewById(R.id.etResponsable);

        // Enlazar los RadioGroups
        rgEquipoResponsable = findViewById(R.id.rgEquipoResponsable);
        rgPlanRefuerzo = findViewById(R.id.rgPlanRefuerzo);
        rgDiagnostico = findViewById(R.id.rgDiagnostico);
        rgReuniones = findViewById(R.id.rgReuniones);

        // Enlazar los botones para enviar la foto
        btnFotoEquipo = findViewById(R.id.btnFotoEquipo);
        btnFotoPlanRefuerzo = findViewById(R.id.btnFotoPlanRefuerzo);
        btnFotoDiagnostico = findViewById(R.id.btnFotoDiagnostico);
        btnFotoReuniones = findViewById(R.id.btnFotoReuniones);
        // Botón para enviar la ficha completa al servidor
        btnEnviarFicha = findViewById(R.id.btnEnviarFicha);

        // Asignar la fecha actual y ejemplo de responsable (puedes obtener el usuario de sesión)
        etFechaMonitoreo.setText(getCurrentDate());
        etResponsable.setText("Responsable Ejemplo");

        // Configuración de "Enviar foto" para cada pregunta
        btnFotoEquipo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentPhotoQuestion = 1;
                openCamera();
            }
        });

        btnFotoPlanRefuerzo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentPhotoQuestion = 2;
                openCamera();
            }
        });

        btnFotoDiagnostico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentPhotoQuestion = 3;
                openCamera();
            }
        });

        btnFotoReuniones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentPhotoQuestion = 4;
                openCamera();
            }
        });

        // Botón para enviar la ficha
        btnEnviarFicha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enviarFicha();
            }
        });
    }

    /**
     * Abre la aplicación de cámara para capturar la imagen.
     */
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_EVIDENCIA);
        } else {
            Toast.makeText(this, "No hay aplicación de cámara disponible", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Recupera la imagen capturada y la asigna al bitmap correspondiente.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_EVIDENCIA && resultCode == RESULT_OK && data != null) {
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            if (imageBitmap != null) {
                switch (currentPhotoQuestion) {
                    case 1:
                        evidenciaEquipo = imageBitmap;
                        Toast.makeText(this, "Foto para Equipo capturada", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        evidenciaPlanRefuerzo = imageBitmap;
                        Toast.makeText(this, "Foto para Plan Refuerzo capturada", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        evidenciaDiagnostico = imageBitmap;
                        Toast.makeText(this, "Foto para Diagnóstico capturada", Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        evidenciaReuniones = imageBitmap;
                        Toast.makeText(this, "Foto para Reuniones capturada", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(this, "No se asignó la foto", Toast.LENGTH_SHORT).show();
                        break;
                }
            } else {
                Toast.makeText(this, "No se capturó imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Retorna la fecha actual en formato dd/MM/yyyy.
     */
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Metodo invocado cuando se presiona el botón "Enviar Ficha".
     */
    public void enviarFicha() {
        Toast.makeText(this, "Enviando ficha...", Toast.LENGTH_SHORT).show();
        enviarFichaServer();
    }

    /**
     * Envía la ficha junto con los datos generales, respuestas y evidencias (imágenes) al servidor
     * utilizando una petición POST multipart con VolleyMultipartRequest.
     */
    private void enviarFichaServer() {
        String horaRegistro = obtenerFechaHoraActual();
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(
                Request.Method.POST,
                fichaEspecialistaUrl,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        String responseData = new String(response.data);
                        Toast.makeText(FichaQuestionsActivity.this, "Envío exitoso: " + responseData, Toast.LENGTH_LONG).show();
                        Log.d("VOLLEY_SUCCESS", "Respuesta: " + responseData);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(FichaQuestionsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("VOLLEY_ERROR", "Error: " + error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                // Parámetros de texto (datos generales y respuestas)
                Map<String, String> params = new HashMap<>();
                params.put("institucion", etInstitucionEducativa.getText().toString().trim());
                params.put("codigo_modular", etCodigoModular.getText().toString().trim());
                params.put("nivel_educativo", etNivelEducativo.getText().toString().trim());
                params.put("fecha_monitoreo", horaRegistro);
                params.put("responsable", etResponsable.getText().toString().trim());

                params.put("equipo_responsable", getSelectedRadioText(rgEquipoResponsable));
                params.put("plan_refuerzo", getSelectedRadioText(rgPlanRefuerzo));
                params.put("diagnostico", getSelectedRadioText(rgDiagnostico));
                params.put("reuniones", getSelectedRadioText(rgReuniones));
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                // Parámetros de imagen (si se capturó cada evidencia)
                Map<String, DataPart> params = new HashMap<>();
                if (evidenciaEquipo != null) {
                    params.put("foto_equipo", new DataPart("equipo.jpg", getFileDataFromBitmap(evidenciaEquipo), "image/jpeg"));
                }
                if (evidenciaPlanRefuerzo != null) {
                    params.put("foto_plan", new DataPart("plan.jpg", getFileDataFromBitmap(evidenciaPlanRefuerzo), "image/jpeg"));
                }
                if (evidenciaDiagnostico != null) {
                    params.put("foto_diagnostico", new DataPart("diagnostico.jpg", getFileDataFromBitmap(evidenciaDiagnostico), "image/jpeg"));
                }
                if (evidenciaReuniones != null) {
                    params.put("foto_reuniones", new DataPart("reuniones.jpg", getFileDataFromBitmap(evidenciaReuniones), "image/jpeg"));
                }
                return params;
            }
        };

        // Agregar la petición a la cola de Volley para ejecutarla
        Volley.newRequestQueue(this).add(multipartRequest);
    }

    /**
     * Convierte un Bitmap a un arreglo de bytes, útil para enviar imágenes en la petición multipart.
     */
    private byte[] getFileDataFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // Comprime la imagen a JPEG, calidad 80 (ajustable)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Recupera el texto del RadioButton seleccionado en un RadioGroup.
     */
    private String getSelectedRadioText(RadioGroup radioGroup) {
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton radioButton = findViewById(selectedId);
            return radioButton.getText().toString();
        }
        return "";
    }

    private String obtenerFechaHoraActual() {
        TimeZone tz = TimeZone.getTimeZone("America/Lima");
        Calendar calendar = Calendar.getInstance(tz);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(tz);
        return sdf.format(calendar.getTime());
    }
}