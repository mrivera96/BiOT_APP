package com.diadema.biometriciot.entities;

import com.google.gson.annotations.SerializedName;

/**
 * Created by César Andrade on 29/04/2019.
 */

public class Reporte {

    @SerializedName("departamento")
    private String departamento;

    @SerializedName("dia")
    private String dia;

    @SerializedName("fecha")
    private String fecha;

    @SerializedName("fecha_y_hora_marco_max")
    private String fecha_y_hora_marco_max;

    @SerializedName("fecha_y_hora_marco_min")
    private String fecha_y_hora_marco_min;

    @SerializedName("hora_entrada")
    private String hora_entrada;

    @SerializedName("hora_salida")
    private String hora_salida;

    @SerializedName("horasrealestrabajadas")
    private String horasrealestrabajadas;

    @SerializedName("horastrabajadas")
    private String horastrabajadas;

    @SerializedName("minutos_entrada")
    private String minutos_entrada;

    @SerializedName("minutos_salida")
    private String minutos_salida;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("nombre_horario")
    private String nombre_horario;

    @SerializedName("cantidadempleadosdepto")
    private  int cantidadempleadosdepto;

    public Reporte(String departamento, String dia, String fecha, String fecha_y_hora_marco_max,
                   String fecha_y_hora_marco_min, String hora_entrada, String hora_salida,
                   String horasrealestrabajadas, String horastrabajadas, String minutos_entrada,
                   String minutos_salida, String nombre, String nombre_horario) {
        this.departamento = departamento;
        this.dia = dia;
        this.fecha = fecha;
        this.fecha_y_hora_marco_max = fecha_y_hora_marco_max;
        this.fecha_y_hora_marco_min = fecha_y_hora_marco_min;
        this.hora_entrada = hora_entrada;
        this.hora_salida = hora_salida;
        this.horasrealestrabajadas = horasrealestrabajadas;
        this.horastrabajadas = horastrabajadas;
        this.minutos_entrada = minutos_entrada;
        this.minutos_salida = minutos_salida;
        this.nombre = nombre;
        this.nombre_horario = nombre_horario;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getFecha_y_hora_marco_max() {
        return fecha_y_hora_marco_max;
    }

    public void setFecha_y_hora_marco_max(String fecha_y_hora_marco_max) {
        this.fecha_y_hora_marco_max = fecha_y_hora_marco_max;
    }

    public String getFecha_y_hora_marco_min() {
        return fecha_y_hora_marco_min;
    }

    public void setFecha_y_hora_marco_min(String fecha_y_hora_marco_min) {
        this.fecha_y_hora_marco_min = fecha_y_hora_marco_min;
    }

    public String getHora_entrada() {
        return hora_entrada;
    }

    public void setHora_entrada(String hora_entrada) {
        this.hora_entrada = hora_entrada;
    }

    public String getHora_salida() {
        return hora_salida;
    }

    public void setHora_salida(String hora_salida) {
        this.hora_salida = hora_salida;
    }

    public String getHorasrealestrabajadas() {
        return horasrealestrabajadas;
    }

    public void setHorasrealestrabajadas(String horasrealestrabajadas) {
        this.horasrealestrabajadas = horasrealestrabajadas;
    }

    public String getHorastrabajadas() {
        return horastrabajadas;
    }

    public void setHorastrabajadas(String horastrabajadas) {
        this.horastrabajadas = horastrabajadas;
    }

    public String getMinutos_entrada() {
        return minutos_entrada;
    }

    public void setMinutos_entrada(String minutos_entrada) {
        this.minutos_entrada = minutos_entrada;
    }

    public String getMinutos_salida() {
        return minutos_salida;
    }

    public void setMinutos_salida(String minutos_salida) {
        this.minutos_salida = minutos_salida;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre_horario() {
        return nombre_horario;
    }

    public void setNombre_horario(String nombre_horario) {
        this.nombre_horario = nombre_horario;
    }

    public int getCantidadempleadosdepto() {
        return cantidadempleadosdepto;
    }

    public void setCantidadempleadosdepto(int cantidadempleadosdepto) {
        this.cantidadempleadosdepto = cantidadempleadosdepto;
    }
}
