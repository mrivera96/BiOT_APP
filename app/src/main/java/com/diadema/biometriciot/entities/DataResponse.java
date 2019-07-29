package com.diadema.biometriciot.entities;

import com.squareup.moshi.Json;

/**
 * Created by César Andrade on 29/04/2019.
 */

public class DataResponse {

    @Json(name = "data")
    private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}