package com.diadema.biometriciot.entities;

import com.squareup.moshi.Json;

/**
 * Created by César Andrade on 29/04/2019.
 */

public class AccessToken {

    @Json(name = "token_type")
    private String tokenType;
    @Json(name = "expires_in")
    private int expiresIn;
    @Json(name = "access_token")
    private String accessToken;
    @Json(name = "refresh_token")
    private String refreshToken;
    @Json(name = "respuesta")
    private int respuesta;

    public String getTokenType() {
        return tokenType;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public int getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(int respuesta) {
        this.respuesta = respuesta;
    }
}