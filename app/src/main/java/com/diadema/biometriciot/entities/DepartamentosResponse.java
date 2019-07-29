package com.diadema.biometriciot.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by César Andrade on 29/04/2019.
 */

public class DepartamentosResponse {
    private List<Departamentos> data;
    public List<Object> searchListing = new ArrayList<>();

    public List<Object> getRespuesta() {
        return searchListing;
    }
}
