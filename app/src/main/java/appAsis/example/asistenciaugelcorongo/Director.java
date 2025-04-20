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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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

    // Metodo para mostrar el diálogo de selección de período (año y mes)
    private void showPeriodoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Director.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_periodo, null);
        builder.setView(view);

        final Spinner spinnerYear = view.findViewById(R.id.spinnerYear);
        final Spinner spinnerMonth = view.findViewById(R.id.spinnerMonth);

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        ArrayList<Integer> years = new ArrayList<>();
        for (int y = currentYear; y <= 2099; y++) {
            years.add(y);
        }
        ArrayAdapter<Integer> adapterYears = new ArrayAdapter<>(Director.this, android.R.layout.simple_spinner_item, years);
        adapterYears.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(adapterYears);

        ArrayList<Integer> months = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            months.add(m);
        }
        ArrayAdapter<Integer> adapterMonths = new ArrayAdapter<>(Director.this, android.R.layout.simple_spinner_item, months);
        adapterMonths.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapterMonths);

        builder.setTitle("Seleccione Año y Mes")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int selectedYear = (Integer) spinnerYear.getSelectedItem();
                        int selectedMonth = (Integer) spinnerMonth.getSelectedItem();

                        // Evitamos períodos mayores o iguales al mes actual
                        Calendar now = Calendar.getInstance();
                        int nowYear = now.get(Calendar.YEAR);
                        int nowMonth = now.get(Calendar.MONTH) + 2;
                        if (selectedYear > nowYear || (selectedYear == nowYear && selectedMonth >= nowMonth)) {
                            Toast.makeText(Director.this, "Este periodo no está habilitado para descargar reporte", Toast.LENGTH_LONG).show();
                        } else {
                            String monthStr = (selectedMonth < 10) ? "0" + selectedMonth : String.valueOf(selectedMonth);
                            String period = selectedYear + "-" + monthStr;
                            fetchTeachersAndGenerateReport(period);
                        }
                    }
                })
                .setNegativeButton("Cancelar", null);
        builder.create().show();
    }

    // Consulta la lista de docentes y, luego, obtiene feriados y procede a cargar horarios y asistencias
    private void fetchTeachersAndGenerateReport(final String period) {
        String urlDocentes = "https://ugelcorongo.pe/ugelasistencias_docente/model/auxasistencia/verDocentesColegios.php"
                + "?colegio=" + colegio + "&estado=ACTIVO";
        RequestQueue queue = Volley.newRequestQueue(Director.this);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, urlDocentes, null,
                new Response.Listener<JSONArray>(){
                    @Override
                    public void onResponse(JSONArray response) {
                        final ArrayList<Teacher> teachers = new ArrayList<>();
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
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(Director.this, "Error al procesar docentes", Toast.LENGTH_LONG).show();
                            return;
                        }
                        // Primero obtener feriados para el período
                        fetchHolidaysForPeriod(period, new HolidaysCallback(){
                            @Override
                            public void onHolidaysLoaded(Set<String> holidays) {
                                // Luego, para cada docente se obtienen su horario y luego su asistencia
                                fetchSchedulesAndAttendanceAndGenerateReport(teachers, period, holidays);
                            }
                        });
                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Director.this, "Error de conexión docentes", Toast.LENGTH_LONG).show();
            }
        });
        queue.add(request);
    }

    // Callback para devolver el Set de feriados
    private interface HolidaysCallback {
        void onHolidaysLoaded(Set<String> holidays);
    }

    // Consulta la URL verFeriados.php y llena el Set de feriados
    private void fetchHolidaysForPeriod(final String period, final HolidaysCallback callback) {
        String urlFeriados = "https://ugelcorongo.pe/ugelasistencias_docente/model/auxasistencia/verFeriados.php?periodo=" + period;
        RequestQueue queue = Volley.newRequestQueue(Director.this);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, urlFeriados, null,
                new Response.Listener<JSONArray>(){
                    @Override
                    public void onResponse(JSONArray response) {
                        Set<String> holidays = new HashSet<>();
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject obj = response.getJSONObject(i);
                                holidays.add(obj.getString("fecha_feriado"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        callback.onHolidaysLoaded(holidays);
                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                callback.onHolidaysLoaded(new HashSet<String>());
            }
        });
        queue.add(request);
    }

    // Obtiene el horario del docente (campo llegada) a partir de verHorarios.php
    private void fetchScheduleForTeacher(final Teacher teacher, final Runnable onComplete) {
        try {
            String url = "https://ugelcorongo.pe/ugelasistencias_docente/model/auxasistencia/verHorarios.php"
                    + "?colegio=" + URLEncoder.encode(colegio, "UTF-8")
                    + "&docente=" + URLEncoder.encode(teacher.getName(), "UTF-8");
            StringRequest request = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>(){
                        @Override
                        public void onResponse(String response) {
                            String[] lines = response.split("\n");
                            if (lines.length > 0) {
                                String firstLine = lines[0];
                                String[] parts = firstLine.split(";");
                                if (parts.length >= 4) {
                                    String llegada = parts[3].trim();
                                    teacher.setHorarioLlegada(llegada);
                                }
                            }
                            onComplete.run();
                        }
                    },
                    new Response.ErrorListener(){
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            teacher.setHorarioLlegada("08:00"); // Valor por defecto
                            onComplete.run();
                        }
                    });
            RequestQueue queue = Volley.newRequestQueue(Director.this);
            queue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
            teacher.setHorarioLlegada("08:00");
            onComplete.run();
        }
    }

    // Obtiene la asistencia del docente mediante verAsistenciaDocentesDirector.php y llena el mapa asistenciaPorDia
    private void fetchAttendanceForTeacher(final Teacher teacher, final String period, final Runnable onComplete) {
        try {
            String urlAttendance = "https://ugelcorongo.pe/ugelasistencias_docente/model/auxasistencia/verAsistenciaDocentesDirector.php"
                    + "?colegio=" + URLEncoder.encode(colegio, "UTF-8")
                    + "&periodo=" + URLEncoder.encode(period, "UTF-8")
                    + "&docente=" + URLEncoder.encode(teacher.getName(), "UTF-8")
                    + "&turno=ENTRADA";

            RequestQueue queue = Volley.newRequestQueue(Director.this);
            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, urlAttendance, null,
                    new Response.Listener<JSONArray>(){
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject obj = response.getJSONObject(i);
                                    // Se espera que el campo "fecha_registro" tenga el formato "YYYY-MM-DD HH:mm:ss"
                                    String fechaRegistro = obj.getString("t_llegada");
                                    String[] parts = fechaRegistro.split(" ");
                                    String fecha = parts[0]; // "YYYY-MM-DD"
                                    int dia = Integer.parseInt(fecha.split("-")[2]);

                                    teacher.getAsistenciaPorDia().put(dia, fechaRegistro);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            onComplete.run();
                        }
                    },
                    new Response.ErrorListener(){
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            onComplete.run();
                        }
                    });
            queue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
            onComplete.run();
        }
    }

    // Para cada docente, obtiene su horario y asistencia antes de generar el reporte
    private void fetchSchedulesAndAttendanceAndGenerateReport(final ArrayList<Teacher> teachers, final String period, final Set<String> holidays) {
        final AtomicInteger count = new AtomicInteger(0);
        final int total = teachers.size();
        for (final Teacher teacher : teachers) {
            fetchScheduleForTeacher(teacher, new Runnable(){
                @Override
                public void run() {
                    fetchAttendanceForTeacher(teacher, period, new Runnable(){
                        @Override
                        public void run() {
                            if (count.incrementAndGet() == total) {
                                exportExcelReportWithAttendanceSummary(teachers, period, holidays);
                            }
                        }
                    });
                }
            });
        }
    }

    // Genera el Excel con dos hojas: "Detalle ..." y "Resumen ..."
    private void exportExcelReportWithAttendanceSummary(ArrayList<Teacher> teachers, String period, Set<String> holidays) {
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            String[] parts = period.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            Calendar cal = Calendar.getInstance();
            cal.set(year, month - 1, 1);
            int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

            Workbook workbook = new HSSFWorkbook();
            Sheet sheetDiario = workbook.createSheet("Detalle " + period);
            Sheet sheetResumen = workbook.createSheet("Resumen " + period);

            int rowIndexDiario = 0, rowIndexResumen = 0, cellIndex;

            // Encabezado de hoja DIARIO
            Row rowHeaderDiario = sheetDiario.createRow(rowIndexDiario++);
            cellIndex = 0;
            rowHeaderDiario.createCell(cellIndex++).setCellValue("N°");
            rowHeaderDiario.createCell(cellIndex++).setCellValue("DNI");
            rowHeaderDiario.createCell(cellIndex++).setCellValue("Apellidos y Nombres");
            rowHeaderDiario.createCell(cellIndex++).setCellValue("Cargo");
            rowHeaderDiario.createCell(cellIndex++).setCellValue("Condición");
            rowHeaderDiario.createCell(cellIndex++).setCellValue("Nivel Educativo");
            rowHeaderDiario.createCell(cellIndex++).setCellValue("Jor.Lab");
            for (int d = 1; d <= 31; d++) {
                rowHeaderDiario.createCell(cellIndex++).setCellValue(String.valueOf(d));
            }

            // Encabezado de hoja RESUMEN
            Row rowHeaderResumen = sheetResumen.createRow(rowIndexResumen++);
            cellIndex = 0;
            rowHeaderResumen.createCell(cellIndex++).setCellValue("N°");
            rowHeaderResumen.createCell(cellIndex++).setCellValue("DNI");
            rowHeaderResumen.createCell(cellIndex++).setCellValue("Apellidos y Nombres");
            rowHeaderResumen.createCell(cellIndex++).setCellValue("Cargo");
            rowHeaderResumen.createCell(cellIndex++).setCellValue("Condición");
            rowHeaderResumen.createCell(cellIndex++).setCellValue("Nivel Educativo");
            rowHeaderResumen.createCell(cellIndex++).setCellValue("Jor.Lab");
            rowHeaderResumen.createCell(cellIndex++).setCellValue("Inasistencias");
            rowHeaderResumen.createCell(cellIndex++).setCellValue("Tardanzas");

            int teacherCounter = 1;
            for (Teacher teacher : teachers) {
                // Obtener el horario de llegada personalizado; si no, se usa "08:00"
                String scheduleTimeStr = teacher.getHorarioLlegada();
                if (scheduleTimeStr == null || scheduleTimeStr.isEmpty()) {
                    scheduleTimeStr = "08:00";
                }
                Date teacherThreshold = timeFormat.parse(scheduleTimeStr);

                int inasistencias = 0;
                int tardanzaTotal = 0;

                // Fila en hoja DIARIO para el docente
                Row rowDiario = sheetDiario.createRow(rowIndexDiario++);
                cellIndex = 0;
                rowDiario.createCell(cellIndex++).setCellValue(teacherCounter);
                rowDiario.createCell(cellIndex++).setCellValue(teacher.getDni());
                rowDiario.createCell(cellIndex++).setCellValue(teacher.getName());
                rowDiario.createCell(cellIndex++).setCellValue(teacher.getCargo());
                rowDiario.createCell(cellIndex++).setCellValue(teacher.getCondicion());
                rowDiario.createCell(cellIndex++).setCellValue(teacher.getNivelEducativo());
                rowDiario.createCell(cellIndex++).setCellValue(teacher.getJorLab());

                // Recorrido por días 1 a 31
                for (int d = 1; d <= 31; d++) {
                    Cell cell = rowDiario.createCell(cellIndex++);
                    if (d > maxDay) {
                        cell.setCellValue("");
                        continue;
                    }
                    String dayStr = (d < 10) ? "0" + d : String.valueOf(d);
                    String dateStr = period + "-" + dayStr;

                    cal.set(year, month - 1, d);
                    boolean esFinDeSemana = (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                            cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);
                    boolean esFeriado = holidays.contains(dateStr);

                    if (esFinDeSemana || esFeriado) {
                        cell.setCellValue("");
                    } else {
                        // Día laboral: buscar el registro en el mapa (clave: día)
                        String registro = teacher.getAsistenciaPorDia().get(d);
                        if (registro != null && !registro.isEmpty()) {
                            String horaRegistro = registro.substring(registro.indexOf(" ") + 1, registro.indexOf(" ") + 6);
                            Date teacherArrival = timeFormat.parse(horaRegistro);
                            if (teacherArrival.compareTo(teacherThreshold) <= 0) {
                                cell.setCellValue("A");
                            } else {
                                cell.setCellValue("T");
                                long diffMillis = teacherArrival.getTime() - teacherThreshold.getTime();
                                int diffMinutes = (int) (diffMillis / 60000);
                                tardanzaTotal += diffMinutes;
                            }
                        } else {
                            cell.setCellValue("I");
                            inasistencias++;
                        }
                    }
                }

                // Fila en hoja RESUMEN para el docente
                Row rowResumen = sheetResumen.createRow(rowIndexResumen++);
                cellIndex = 0;
                rowResumen.createCell(cellIndex++).setCellValue(teacherCounter);
                rowResumen.createCell(cellIndex++).setCellValue(teacher.getDni());
                rowResumen.createCell(cellIndex++).setCellValue(teacher.getName());
                rowResumen.createCell(cellIndex++).setCellValue(teacher.getCargo());
                rowResumen.createCell(cellIndex++).setCellValue(teacher.getCondicion());
                rowResumen.createCell(cellIndex++).setCellValue(teacher.getNivelEducativo());
                rowResumen.createCell(cellIndex++).setCellValue(teacher.getJorLab());
                rowResumen.createCell(cellIndex++).setCellValue(inasistencias);
                rowResumen.createCell(cellIndex++).setCellValue(tardanzaTotal);

                teacherCounter++;
            }

            // Se omite el autoajuste de columnas en Android para evitar crash
            String fileName = colegio + " reporte de asistencia " + period + ".xls";
            File file = new File(getExternalFilesDir(null), fileName);
            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();

            Toast.makeText(Director.this, "Reporte generado en: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            showNotificationWithFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(Director.this, "Error al generar reporte: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showNotificationWithFile(File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(Director.this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(Director.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }
        Uri fileUri = FileProvider.getUriForFile(Director.this,
                getApplicationContext().getPackageName() + ".provider",
                file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/vnd.ms-excel");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        PendingIntent pendingIntent = PendingIntent.getActivity(Director.this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String channelId = "reporte_channel";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(Director.this, channelId)
                .setSmallIcon(R.drawable.logoapp)
                .setContentTitle("Reporte descargado")
                .setContentText("Toca para abrir el reporte")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Reporte";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(1, builder.build());
    }
}