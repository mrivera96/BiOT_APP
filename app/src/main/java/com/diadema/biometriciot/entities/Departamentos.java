package com.diadema.biometriciot.entities;

import com.google.gson.annotations.SerializedName;

/**
 * Created by César Andrade on 29/04/2019.
 */

public class Departamentos {
    @SerializedName("IdDepartment")
    private int IdDepartment;

    @SerializedName("Description")
    private String Description;

    @SerializedName("data")
    private String[] respuesta;

    public Departamentos(int idDepartment, String description) {
        IdDepartment = idDepartment;
        Description = description;
    }

    public int getIdDepartment() {
        return IdDepartment;
    }

    public void setIdDepartment(int IdDepartment) {
        this.IdDepartment = IdDepartment;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String Description) {
        this.Description = Description;
    }

    public String[] getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String[] respuesta) {
        this.respuesta = respuesta;
    }
}
