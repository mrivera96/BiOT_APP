package com.diadema.biometriciot.entities;

import java.util.List;
import java.util.Map;

/**
 * Created by César Andrade on 29/04/2019.
 */

public class ApiError {
    private String message;
    private Map<String, List<String>> errors;

    public String getMessage() {
        return message;
    }

    public Map<String, List<String>> getErrors() {
        return errors;
    }
}