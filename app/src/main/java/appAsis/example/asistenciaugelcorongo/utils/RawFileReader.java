package appAsis.example.asistenciaugelcorongo.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import appAsis.example.asistenciaugelcorongo.R;

/**
 * Lee el recurso raw/datacolegio.txt y parsea cada línea separada por ';'.
 */
public final class RawFileReader {
    private RawFileReader() { /* No instanciable */ }

    /**
     * Lee R.raw.datacolegio y devuelve una lista de arreglos:
     * [0]=colegio, [1]=usuario, [2]=password, [3]=rol,
     * [4]=idColegio, [5]=latitud, [6]=longitud.
     */
    public static List<String[]> readRawDatacolegio(Context ctx) throws IOException {
        InputStream is = ctx.getResources().openRawResource(R.raw.datacolegio);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        List<String[]> rows = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            // Ignorar líneas vacías
            if (line.trim().isEmpty()) continue;
            String[] parts = line.split(";");
            // Solo agregar si tiene al menos 7 columnas
            if (parts.length >= 7) {
                rows.add(parts);
            }
        }
        br.close();
        return rows;
    }
}