package com.diadema.biometriciot.entities;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by César Andrade on 29/04/2019.
 */

public class ReporteResponse {
    @SerializedName("data")
    private List<Reporte> data;

    public List<Reporte> getData() {
        return data;
    }
}
