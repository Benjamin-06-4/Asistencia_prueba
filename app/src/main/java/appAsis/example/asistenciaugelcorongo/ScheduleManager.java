package appAsis.example.asistenciaugelcorongo;

import android.content.Context;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class ScheduleManager {
    private static final String FILENAME = "datahorarios.txt";
    private static ScheduleManager instance;
    private Context ctx;
    private List<Schedule> cache = new ArrayList<>();

    private ScheduleManager(Context c) {
        ctx = c.getApplicationContext();
    }

    public static ScheduleManager get(Context c) {
        if (instance == null) instance = new ScheduleManager(c);
        return instance;
    }

    public static class Schedule {
        public String colegio;
        public String docente;
        public String dia;      // "L", "M", "X", "J", "V"
        public String ingreso;  // "HH:mm"
        public String salida;   // "HH:mm"
        public Schedule(String colegio, String docente,
                        String dia, String ing, String out){
            this.colegio  = colegio;
            this.docente  = docente;
            this.dia      = dia;
            this.ingreso  = ing;
            this.salida   = out;
        }
        @Override
        public String toString() {
            // registro: colegio;docente;dia;ingreso;salida
            return colegio+";"+docente+";"+dia+";"+ingreso+";"+salida;
        }
    }

    /** Lee todo el archivo local en cache */
    public List<Schedule> loadLocal() {
        cache.clear();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        ctx.openFileInput(FILENAME)))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] f = line.split(";");
                if (f.length == 5) {
                    cache.add(new Schedule(
                            f[0], f[1], f[2], f[3], f[4]
                    ));
                }
            }
        } catch (IOException ignored) {}
        return cache;
    }

    /** Persiste en interno el contenido de cache */
    private void persistAll() throws IOException {
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(
                        ctx.openFileOutput(FILENAME, Context.MODE_PRIVATE)))) {
            for (Schedule s : cache) {
                bw.write(s.toString());
                bw.newLine();
            }
        }
    }

    /** Inserta o actualiza (sin duplicados por colegio+docente+dia) */
    public void upsert(Schedule s) throws Exception {
        Iterator<Schedule> it = cache.iterator();
        while (it.hasNext()) {
            Schedule old = it.next();
            if (old.colegio.equals(s.colegio)
                    && old.docente.equals(s.docente)
                    && old.dia.equals(s.dia)) {
                it.remove();
            }
        }
        cache.add(s);
        persistAll();
    }

    /** Elimina todos los registros de un docente en días dados */
    public void delete(String colegio, String docente, List<String> dias)
            throws IOException {
        cache.removeIf(s ->
                s.colegio.equals(colegio) &&
                        s.docente.equals(docente) &&
                        dias.contains(s.dia)
        );
        persistAll();
    }

    /** Filtra en cache */
    public List<Schedule> filter(String colegio, String docente, String dia) {
        List<Schedule> out = new ArrayList<>();

        // Fecha/hora actual
        Calendar ahora = Calendar.getInstance();
        // Formato de hora en tu Schedule (ajusta si es distinto)
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        for (Schedule s: cache) {
            if (s.colegio.equals(colegio) && s.docente.equals(docente) && s.dia.equals(dia)) {
                try {
                    // Parsear hora de ingreso
                    Date horaIngreso = sdf.parse(s.ingreso);
                    Calendar calIngreso = Calendar.getInstance();
                    calIngreso.setTime(horaIngreso);

                    // Ajustar fecha al día de hoy
                    calIngreso.set(Calendar.YEAR, ahora.get(Calendar.YEAR));
                    calIngreso.set(Calendar.MONTH, ahora.get(Calendar.MONTH));
                    calIngreso.set(Calendar.DAY_OF_MONTH, ahora.get(Calendar.DAY_OF_MONTH));

                    long diffMs = ahora.getTimeInMillis() - calIngreso.getTimeInMillis();
                    long quinceMin = 15 * 60 * 1000;

                    if (diffMs >= -quinceMin && diffMs <= quinceMin) {
                        out.add(s);
                    }
                    // Si diffMs > quinceMin, se descarta
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return out;
    }

    /** Carga remota de la URL (mismo formato: ; y \n) */
    public List<Schedule> loadRemote(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setConnectTimeout(5000);
        c.setReadTimeout(5000);
        if (c.getResponseCode() == 200) {
            List<Schedule> remote = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(c.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] f = line.split(";");
                    if (f.length == 5) {
                        remote.add(new Schedule(
                                f[0], f[1], f[2], f[3], f[4]
                        ));
                    }
                }
            }
            return remote;
        } else {
            throw new IOException("HTTP error " + c.getResponseCode());
        }
    }
}