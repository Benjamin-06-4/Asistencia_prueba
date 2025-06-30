package appAsis.example.asistenciaugelcorongo;

import java.util.HashMap;
public class Teacher {
    private String colegio;
    private String dni;
    private String name;
    private String cargo;
    private String condicion;
    private String nivelEducativo;
    private String jorLab;
    private String contratoInicio;
    private HashMap<Integer, String> asistenciaPorDia = new HashMap<>();
    private String horarioLlegada;
    private String apellido_paterno;
    private String apellido_materno;
    private String nombres;
    private String id;

    public String getDni() {
        return dni;
    }
    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getCargo() {
        return cargo;
    }
    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getCondicion() {
        return condicion;
    }
    public void setCondicion(String condicion) {
        this.condicion = condicion;
    }

    public String getNivelEducativo() {
        return nivelEducativo;
    }
    public void setNivelEducativo(String nivelEducativo) {
        this.nivelEducativo = nivelEducativo;
    }

    public String getJorLab() {
        return jorLab;
    }
    public void setJorLab(String jorLab) {
        this.jorLab = jorLab;
    }

    public String getContratoInicio() {
        return contratoInicio;
    }
    public void setContratoInicio(String contratoInicio) {
        this.contratoInicio = contratoInicio;
    }

    public HashMap<Integer, String> getAsistenciaPorDia() {
        return asistenciaPorDia;
    }
    public void setAsistenciaPorDia(HashMap<Integer, String> asistenciaPorDia) {
        this.asistenciaPorDia = asistenciaPorDia;
    }

    public String getHorarioLlegada() {
        return horarioLlegada;
    }
    public void setHorarioLlegada(String horarioLlegada) {
        this.horarioLlegada = horarioLlegada;
    }

    public String getColegio() {
        return colegio;
    }

    public void setColegio(String colegio) {
        this.colegio = colegio;
    }

    public String getApellido_materno() {
        return apellido_materno;
    }

    public void setApellido_materno(String apellido_materno) {
        this.apellido_materno = apellido_materno;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellido_paterno() {
        return apellido_paterno;
    }

    public void setApellido_paterno(String apellido_paterno) {
        this.apellido_paterno = apellido_paterno;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}