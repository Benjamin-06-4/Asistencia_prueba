package appAsis.example.asistenciaugelcorongo;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class linkUrlClass  extends AsyncTask<String,String,String> {
    @Override
    protected String doInBackground(String... strings) {
        HttpURLConnection httpURLConnection = null;
        URL urllink = null;
        try {
            urllink = new URL(strings[0]);
        }catch (MalformedURLException e){
            e.printStackTrace();
        }

        try{
            httpURLConnection = (HttpURLConnection) urllink.openConnection();
            httpURLConnection.connect();
            int code = httpURLConnection.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK){
                InputStream in = new BufferedInputStream(httpURLConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line = "";
                StringBuffer buffer = new StringBuffer();
                while((line=reader.readLine()) != null){
                    buffer.append(line);
                }
                if ((line=reader.readLine()) == null){
                    return "empty";
                }
                return buffer.toString();
            }else{
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "empty";
    }
}
