package appAsis.example.asistenciaugelpomabamba2;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SaveFile extends AsyncTask<String,String,String> {
    String colegio;
    TextView t_search;

    public SaveFile(String colegio, TextView t_search) {
        this.colegio = colegio;
        this.t_search=t_search;
    }

    public boolean save(String path) {
        File file = new File(path);

        TimeZone myTimeZone = TimeZone.getTimeZone("America/Lima");
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormatnowFecha = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        simpleDateFormatnowFecha.setTimeZone(myTimeZone);
        String fecha_registro = simpleDateFormatnowFecha.format(calendar.getTime());

        String UPLOAD_URL = "https://ugelpomabamba.gob.pe/ugel/ugelasistencias_docente/model/file/archivos/uploadFile.php";


        // Create an HTTP client to execute the request
        try {
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("colegio", colegio)
                    .addFormDataPart("fecha_registro", fecha_registro)
                    .addFormDataPart("files", file.getName(),
                            RequestBody.create(MediaType.parse("application/pdf"), file))
                    .build();

            // Create a POST request to send the data to UPLOAD_URL
            Request request = new Request.Builder()
                    .url(UPLOAD_URL)
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    t_search.setText("Unable to upload to server.");
                    e.printStackTrace();
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    t_search.setText("Upload was successful.");
                }
            });
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        // Execute the request and get the response from the server
        //Response response = null;
        //response = client.newCall(request).execute();
    }

    @Override
    protected String doInBackground(String... strings) {
        if(save(strings[0])){
            return "true";
        }
        else{
            return "failed";
        }
    }
}
