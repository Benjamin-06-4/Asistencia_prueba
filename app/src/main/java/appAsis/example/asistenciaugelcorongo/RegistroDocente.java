package appAsis.example.asistenciaugelcorongo;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;

public class RegistroDocente extends AppCompatActivity {

    // Declaración de vistas
    private EditText etDni, etApellidoPaterno, etApellidoMaterno, etNombre,
            etJornada, etCodigoPlaza, etFechaInicio, etFechaFinal;
    private Spinner spNivelEducativo, spCargoLaboral, spSituacionLaboral, spSexo;
    private Button btnBuscar, btnEliminar, btnGuardar;

    // Datos estáticos o que se pueden recibir vía extras
    private String idcolegio;
    private String colegio;
    private String docente;
    private String rol;

    // URLs
    private final String URL_BUSCAR = "https://ugelcorongo.pe/ugelasistencias_docente/model/personal/buscarDni.php";
    private final String URL_GUARDAR = "https://ugelcorongo.pe/ugelasistencias_docente/model/personal/guardarDocente.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_docente);

        // Inicialización de vistas
        etDni = findViewById(R.id.etDni);
        etApellidoPaterno = findViewById(R.id.etApellidoPaterno);
        etApellidoMaterno = findViewById(R.id.etApellidoMaterno);
        etNombre = findViewById(R.id.etNombre);
        etJornada = findViewById(R.id.etJornada);
        etCodigoPlaza = findViewById(R.id.etCodigoPlaza);
        etFechaInicio = findViewById(R.id.etFechaInicio);
        etFechaFinal = findViewById(R.id.etFechaFinal);
        spNivelEducativo = findViewById(R.id.spNivelEducativo);
        spCargoLaboral = findViewById(R.id.spCargoLaboral);
        spSituacionLaboral = findViewById(R.id.spSituacionLaboral);
        spSexo = findViewById(R.id.spSexo);
        btnBuscar = findViewById(R.id.btnBuscar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnGuardar = findViewById(R.id.btnGuardar);

        colegio = getIntent().getStringExtra("colegio");
        idcolegio = getIntent().getStringExtra("idcolegio");
        docente = getIntent().getStringExtra("docente");
        rol = getIntent().getStringExtra("turnos"); // "Docente" o "Director"

        // Botón eliminar y guardar deshabilitados por defecto
        btnEliminar.setEnabled(false);
        btnGuardar.setEnabled(false);

        // Listener para el botón Buscar (lupa)
        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInternetAvailable()) {
                    String dni = etDni.getText().toString().trim();
                    if (!dni.isEmpty()) {
                        buscarDocente(dni);
                    } else {
                        Toast.makeText(RegistroDocente.this, "Ingrese el DNI", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegistroDocente.this, "Se requiere conexión a Internet", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Listener para el botón Eliminar: Limpia campos y deshabilita el botón eliminar
        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eliminarDocente();
                // Tras eliminar, se revalida el formulario (es probable que ya no se cumpla la validación)
                validateFields();
            }
        });

        // Listener para el botón Guardar: Envía los datos vía POST
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInternetAvailable()) {
                    guardarDocente();
                } else {
                    Toast.makeText(RegistroDocente.this, "Se requiere conexión a Internet", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Agregamos TextWatcher a los EditText para validar cuando se modifiquen
        TextWatcher fieldWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s,int start, int count, int after){}
            @Override
            public void onTextChanged(CharSequence s,int start, int before, int count){}
            @Override
            public void afterTextChanged(Editable s) {
                validateFields();
            }
        };

        // EditText requeridos
        etDni.addTextChangedListener(fieldWatcher);
        etApellidoPaterno.addTextChangedListener(fieldWatcher);
        etApellidoMaterno.addTextChangedListener(fieldWatcher);
        etNombre.addTextChangedListener(fieldWatcher);
        etJornada.addTextChangedListener(fieldWatcher);
        etCodigoPlaza.addTextChangedListener(fieldWatcher);
        etFechaInicio.addTextChangedListener(fieldWatcher);
        etFechaFinal.addTextChangedListener(fieldWatcher);

        // Para los spinners, se agrega un OnItemSelectedListener
        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                validateFields();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        };

        etFechaInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crea y muestra un DatePickerDialog
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(RegistroDocente.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                // Ajuste el mes puesto que en DatePickerDialog se cuenta de 0 a 11
                                etFechaInicio.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                validateFields();
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

        etFechaFinal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crea y muestra un DatePickerDialog
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(RegistroDocente.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                // Ajuste el mes puesto que en DatePickerDialog se cuenta de 0 a 11
                                etFechaFinal.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                validateFields();
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });


        spNivelEducativo.setOnItemSelectedListener(spinnerListener);
        spCargoLaboral.setOnItemSelectedListener(spinnerListener);
        spSituacionLaboral.setOnItemSelectedListener(spinnerListener);
        spSexo.setOnItemSelectedListener(spinnerListener);
    }

    // Método para visitar la URL de búsqueda
    @SuppressLint("StaticFieldLeak")
    private void buscarDocente(final String dni) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String result = "";
                try {
                    String urlString = URL_BUSCAR + "?dni=" + dni;
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                        result = sb.toString();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    boolean encontrado = jsonObject.getBoolean("encontrado");
                    if (encontrado) {
                        // Rellenar los campos con los datos obtenidos
                        etApellidoPaterno.setText(jsonObject.getString("apellido_paterno"));
                        etApellidoMaterno.setText(jsonObject.getString("apellido_materno"));
                        etNombre.setText(jsonObject.getString("nombre"));

                        String nivel = jsonObject.getString("nivel_educativo");
                        String cargo = jsonObject.getString("cargo_laboral");
                        String situacion = jsonObject.getString("situacion_laboral");
                        String sexo = jsonObject.getString("sexo");

                        setSpinnerSelection(spNivelEducativo, nivel);
                        setSpinnerSelection(spCargoLaboral, cargo);
                        setSpinnerSelection(spSituacionLaboral, situacion);
                        setSpinnerSelection(spSexo, sexo);

                        etJornada.setText(jsonObject.getString("jornada_laboral"));
                        etCodigoPlaza.setText(jsonObject.getString("codigo_plaza"));
                        etFechaInicio.setText(jsonObject.getString("fecha_inicio_contrato"));
                        etFechaFinal.setText(jsonObject.getString("fecha_final_contrato"));

                        // Construir el identificador del docente (puedes modificar este formato si lo requieres)
                        docente = etDni.getText().toString().trim() + " - " + jsonObject.getString("nombre");

                        // Habilitar el botón de eliminar
                        btnEliminar.setEnabled(true);
                        // Revalida ya que se llenaron campos adicionales mediante la búsqueda
                        validateFields();
                    } else {
                        Toast.makeText(RegistroDocente.this, "Docente no encontrado", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(RegistroDocente.this, "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    // Método para limpiar todos los campos y resetear los spinners
    private void eliminarDocente() {
        etApellidoPaterno.setText("");
        etApellidoMaterno.setText("");
        etNombre.setText("");
        etJornada.setText("");
        etCodigoPlaza.setText("");
        etFechaInicio.setText("");
        etFechaFinal.setText("");
        spNivelEducativo.setSelection(0);
        spCargoLaboral.setSelection(0);
        spSituacionLaboral.setSelection(0);
        spSexo.setSelection(0);
        btnEliminar.setEnabled(false);
        Toast.makeText(RegistroDocente.this, "Datos eliminados", Toast.LENGTH_SHORT).show();
    }

    // Método utilitario para establecer la selección correcta en un Spinner
    private void setSpinnerSelection(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    // Valida que todos los campos requeridos estén llenos
    private void validateFields() {
        boolean allFilled =
                !etDni.getText().toString().trim().isEmpty() &&
                        !etApellidoPaterno.getText().toString().trim().isEmpty() &&
                        !etApellidoMaterno.getText().toString().trim().isEmpty() &&
                        !etNombre.getText().toString().trim().isEmpty() &&
                        !etJornada.getText().toString().trim().isEmpty() &&
                        !etCodigoPlaza.getText().toString().trim().isEmpty() &&
                        !etFechaInicio.getText().toString().trim().isEmpty() &&
                        !etFechaFinal.getText().toString().trim().isEmpty() &&
                        spinnerHasSelection(spNivelEducativo) &&
                        spinnerHasSelection(spCargoLaboral) &&
                        spinnerHasSelection(spSituacionLaboral) &&
                        spinnerHasSelection(spSexo);

        btnGuardar.setEnabled(allFilled);
    }

    // Asume que la primera posición (índice 0) es el item "Seleccione..." o valor no válido
    private boolean spinnerHasSelection(Spinner spinner) {
        return spinner.getSelectedItemPosition() != 0;
    }

    // Envía los datos del formulario mediante una petición POST a la URL de guardado
    @SuppressLint("StaticFieldLeak")
    private void guardarDocente() {
        final String dni = etDni.getText().toString().trim();
        final String apellidoPaterno = etApellidoPaterno.getText().toString().trim();
        final String apellidoMaterno = etApellidoMaterno.getText().toString().trim();
        final String nombre = etNombre.getText().toString().trim();
        final String nivelEducativo = spNivelEducativo.getSelectedItem().toString();
        final String cargoLaboral = spCargoLaboral.getSelectedItem().toString();
        final String situacionLaboral = spSituacionLaboral.getSelectedItem().toString();
        final String jornada = etJornada.getText().toString().trim();
        final String codigoPlaza = etCodigoPlaza.getText().toString().trim();
        final String fechaInicio = etFechaInicio.getText().toString().trim();
        final String fechaFinal = etFechaFinal.getText().toString().trim();
        final String sexo = spSexo.getSelectedItem().toString();

        // Validación adicional (ya debería estar cubierto en validateFields)
        if(dni.isEmpty() || nombre.isEmpty()){
            Toast.makeText(RegistroDocente.this, "Complete los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String response = "";
                try {
                    URL url = new URL(URL_GUARDAR);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(5000);
                    conn.setConnectTimeout(5000);
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    // Codificar los parámetros a enviar
                    String postData = encodePostData(new String[][]{
                            {"dni", dni},
                            {"apellido_paterno", apellidoPaterno},
                            {"apellido_materno", apellidoMaterno},
                            {"nombre", nombre},
                            {"nivel_educativo", nivelEducativo},
                            {"cargo_laboral", cargoLaboral},
                            {"situacion_laboral", situacionLaboral},
                            {"jornada_laboral", jornada},
                            {"codigo_plaza", codigoPlaza},
                            {"fecha_inicio_contrato", fechaInicio},
                            {"fecha_final_contrato", fechaFinal},
                            {"sexo", sexo},
                            {"idcolegio", idcolegio},
                            {"colegio", colegio},
                            {"docente", docente},
                            {"rol", rol}
                    });

                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(postData);
                    writer.flush();
                    writer.close();
                    os.close();

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String inputLine;
                        StringBuilder sb = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            sb.append(inputLine);
                        }
                        in.close();
                        response = sb.toString();
                    } else {
                        response = "Error: " + responseCode;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    response = "Excepción: " + e.getMessage();
                }
                return response;
            }

            @Override
            protected void onPostExecute(String result) {
                Toast.makeText(RegistroDocente.this, "Docente guardado correctamente", Toast.LENGTH_LONG).show();
            }
        }.execute();
    }

    // Método para codificar los parámetros del POST según el formato application/x-www-form-urlencoded
    private String encodePostData(String[][] params) throws Exception {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (String[] param : params) {
            if (!first) {
                result.append("&");
            }
            result.append(URLEncoder.encode(param[0], "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(param[1], "UTF-8"));
            first = false;
        }
        return result.toString();
    }

    // Método para comprobar la conectividad a Internet
    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null) {
            return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
        }
        return false;
    }
}