package appAsis.example.asistenciaugelcorongo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class Director extends AppCompatActivity {
    // Botón para registrar asistencia (se usará tanto por docente como director)
    private ImageButton btc_asistencia;
    private TextView txt_director;

    // Datos que pueden venir por intent (por ejemplo, desde el login)
    private String rol;       // "Docente" o "Director"
    private String colegio;   // Nombre del colegio
    private String idcolegio; // ID de la I.E.
    private String docente;   // Nombre o identificador del docente

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Forzar modo claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_director);

        // Inicializar el TextView
        txt_director = findViewById(R.id.lbl_director); // Asegúrate de que el ID coincide con el XML

        // Obtener datos enviados por intent
        colegio = getIntent().getStringExtra("colegio");
        idcolegio = getIntent().getStringExtra("idcolegio");
        docente = getIntent().getStringExtra("docente");
        rol = getIntent().getStringExtra("turnos"); // "Docente" o "Director"

        // Establecer el nombre del colegio en el TextView
        txt_director.setText(docente);
    }

    public void asistencias(View view) {
        Intent intent = new Intent(this, Docentes_Director.class);

        intent.putExtra("idcolegio",idcolegio);
        intent.putExtra("colegio",colegio);
        intent.putExtra("docente",docente);
        intent.putExtra("rol",rol);

        startActivity(intent);
    }

    public void horarios(View view) {
        Intent intent = new Intent(this, Horarios_Docentes.class);

        intent.putExtra("idcolegio",idcolegio);
        intent.putExtra("colegio",colegio);
        intent.putExtra("docente",docente);
        intent.putExtra("rol",rol);
        startActivity(intent);
    }

    public void docentes(View view) {
        //Intent intent = new Intent(this, DocentesActivity.class);
        //startActivity(intent);
    }

    public void reporte_asistencia(View view) {
        showPeriodoDialog();
    }

    private void showPeriodoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_periodo, null);
        builder.setView(dialogView);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) final Spinner spinnerYear = dialogView.findViewById(R.id.spinnerYear);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) final Spinner spinnerMonth = dialogView.findViewById(R.id.spinnerMonth);

        // Rellenar Spinner de años: se muestran, por ejemplo, los últimos 5 años hasta el actual
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        ArrayList<Integer> years = new ArrayList<>();
        for (int y = currentYear; y <= currentYear; y++) {
            years.add(y);
        }
        ArrayAdapter<Integer> adapterYears = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        adapterYears.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(adapterYears);

        // Rellenar Spinner de meses (1 a 12)
        ArrayList<Integer> months = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            months.add(m);
        }
        ArrayAdapter<Integer> adapterMonths = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        adapterMonths.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapterMonths);

        builder.setTitle("Seleccione Año y Mes")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int selectedYear = (Integer) spinnerYear.getSelectedItem();
                        int selectedMonth = (Integer) spinnerMonth.getSelectedItem();

                        // Obtener el año y mes actual
                        Calendar cal = Calendar.getInstance();
                        int currentY = cal.get(Calendar.YEAR);
                        int currentM = cal.get(Calendar.MONTH); // MONTH es 0-based establecer en la fase de lanzamiento + 1

                        // Validación: si el periodo seleccionado es mayor o igual al actual
                        if (selectedYear > currentY || (selectedYear == currentY && selectedMonth >= currentM)) {
                            Toast.makeText(Director.this, "Este periodo no está habilitado para descargar reporte", Toast.LENGTH_LONG).show();
                        } else {
                            // Formatear el período, por ejemplo "2022-03"
                            String period = selectedYear + "-" + (selectedMonth < 10 ? "0" + selectedMonth : selectedMonth);
                            generateReporteExcel(period);
                        }
                    }
                })
                .setNegativeButton("Cancelar", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Metodo para obtener los docentes y generar el Excel
    private void generateReporteExcel(final String period) {
        // URL para obtener los docentes (asegúrate de que los parámetros son los correctos)
        String urlDocentes = "https://ugelcorongo.pe/ugelasistencias_docente/model/auxasistencia/verDocentesColegios.php"
                + "?colegio=" + colegio + "&estado=ACTIVO";

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, urlDocentes, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        ArrayList<Teacher> teachers = new ArrayList<>();
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject obj = response.getJSONObject(i);
                                Teacher teacher = new Teacher();
                                teacher.setDni(obj.getString("dni"));
                                teacher.setName(obj.getString("apellidos_nombres"));
                                teacher.setCargo(obj.getString("cargo"));
                                teacher.setCondicion(obj.getString("condicion"));
                                teacher.setNivelEducativo(obj.getString("nivel_educativo"));
                                teacher.setJorLab(obj.getString("jor_lab"));
                                teacher.setContratoInicio(obj.getString("contrato_inicio"));
                                teachers.add(teacher);
                            }
                            exportExcelReport(teachers, period);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(Director.this, "Error al procesar datos", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(Director.this, "Error en la conexión", Toast.LENGTH_LONG).show();
                    }
                });
        queue.add(jsonArrayRequest);
    }

    // Metodo para generar y guardar el reporte Excel con la plantilla
    private void exportExcelReport(ArrayList<Teacher> teachers, String period) {
        try {
            // Crear el Workbook y la hoja
            Workbook workbook = new HSSFWorkbook();
            Sheet sheet = workbook.createSheet("Reporte " + period);
            int rowIndex = 0;

            // Encabezado: datos fijos + columnas para los días (1 al 31)
            Row rowHeader = sheet.createRow(rowIndex++);
            int cellIndex = 0;
            rowHeader.createCell(cellIndex++).setCellValue("N°");
            rowHeader.createCell(cellIndex++).setCellValue("DNI");
            rowHeader.createCell(cellIndex++).setCellValue("Apellidos y Nombres");
            rowHeader.createCell(cellIndex++).setCellValue("Cargo");
            rowHeader.createCell(cellIndex++).setCellValue("Condición");
            rowHeader.createCell(cellIndex++).setCellValue("Nivel Educativo");
            rowHeader.createCell(cellIndex++).setCellValue("Jor.Lab");
            for (int d = 1; d <= 31; d++) {
                rowHeader.createCell(cellIndex++).setCellValue(String.valueOf(d));
            }

            // Llenado de filas para cada docente
            int teacherCounter = 1;
            for (Teacher teacher : teachers) {
                Row row = sheet.createRow(rowIndex++);
                cellIndex = 0;
                row.createCell(cellIndex++).setCellValue(teacherCounter++);
                row.createCell(cellIndex++).setCellValue(teacher.getDni());
                row.createCell(cellIndex++).setCellValue(teacher.getName());
                row.createCell(cellIndex++).setCellValue(teacher.getCargo());
                row.createCell(cellIndex++).setCellValue(teacher.getCondicion());
                row.createCell(cellIndex++).setCellValue(teacher.getNivelEducativo());
                row.createCell(cellIndex++).setCellValue(teacher.getJorLab());
                for (int d = 1; d <= 31; d++) {
                    row.createCell(cellIndex++).setCellValue("");
                }
            }

            // Se omite el autoSizeColumn ya que provoca crash en Android.

            // Guardar el archivo en el directorio de la app.
            String fileName = colegio + " reporte de asistencia " + period + ".xls";
            File file = new File(getExternalFilesDir(null), fileName);
            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();

            // Notificar al usuario para abrir el archivo
            showNotificationWithFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(Director.this, "Error al generar reporte: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Metodo para solicitar permiso de notificación (para Android 13+)
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(Director.this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(Director.this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }
    }

    private void showNotificationWithFile(File file) {
        // Primero, si aún no lo has hecho, solicita el permiso de notificaciones
        requestNotificationPermission();

        // Obtener el URI seguro usando FileProvider
        Uri fileUri = FileProvider.getUriForFile(Director.this,
                Director.this.getApplicationContext().getPackageName() + ".provider",
                file);

        // Crear un Intent para abrir el archivo Excel
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/vnd.ms-excel");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Crear el PendingIntent
        PendingIntent pendingIntent = PendingIntent.getActivity(
                Director.this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Definir el canal y construir la notificación
        String channelId = "reporte_channel";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(Director.this, channelId)
                .setSmallIcon(R.drawable.logoapp) // Asegúrate que este ícono exista y sea visible.
                .setContentTitle("Reporte descargado")
                .setContentText("Toca para abrir el reporte")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // Obtener NotificationManager
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Para Android O+ crea el canal de notificaciones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Reporte";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(channel);
        }

        // Mostrar la notificación (se usa un ID único, por ejemplo 1)
        notificationManager.notify(1, builder.build());
    }
}