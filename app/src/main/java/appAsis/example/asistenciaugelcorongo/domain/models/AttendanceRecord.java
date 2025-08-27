package appAsis.example.asistenciaugelcorongo.domain.models;

public class AttendanceRecord {
    private final String colegio;
    private final String docente;
    private final String rol;
    private final String tipoRegistro;   // "Entrada" o "Salida"
    private final String horaRegistro;   // "yyyy-MM-dd HH:mm:ss"
    private final String tardanza;       // en minutos (para roles que apliquen)
    private final String comentario;
    private final double latitude;
    private final double longitude;

    public AttendanceRecord(
            String colegio,
            String docente,
            String rol,
            String tipoRegistro,
            String horaRegistro,
            String tardanza,
            String comentario,
            double latitude,
            double longitude
    ) {
        this.colegio     = colegio;
        this.docente     = docente;
        this.rol         = rol;
        this.tipoRegistro= tipoRegistro;
        this.horaRegistro= horaRegistro;
        this.tardanza    = tardanza;
        this.comentario  = comentario;
        this.latitude    = latitude;
        this.longitude   = longitude;
    }

    public String getColegio()      { return colegio; }
    public String getDocente()      { return docente; }
    public String getRol()          { return rol; }
    public String getTipoRegistro() { return tipoRegistro; }
    public String getHoraRegistro() { return horaRegistro; }
    public String getTardanza()     { return tardanza; }
    public String getComentario()   { return comentario; }
    public double getLatitude()     { return latitude; }
    public double getLongitude()    { return longitude; }
}