package appAsis.example.asistenciaugelcorongo;

// Clase que representa el horario de un docente
public class HorarioDocente {
    public String colegio;
    public String docente;
    public String llegadaPermitida;  // Ejemplo: "7:40"
    public String llegada;           // Ejemplo: "8:00"
    public String salida;            // Ejemplo: "13:00"
    public String salidaPermitida;   // Ejemplo: "13:15"

    public boolean llegadaRegistrada = false;
    public boolean salidaRegistrada = false;
}