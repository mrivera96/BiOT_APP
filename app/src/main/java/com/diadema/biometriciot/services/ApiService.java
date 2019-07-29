package com.diadema.biometriciot.services;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

import com.diadema.biometriciot.entities.AccessToken;
import com.diadema.biometriciot.entities.DataResponse;
import com.diadema.biometriciot.entities.DoorResponse;
import com.diadema.biometriciot.entities.ReporteResponse;

/**
 * Created by César Andrade on 29/04/2019.
 */

public interface ApiService {

    @POST("login")
    @FormUrlEncoded
    Call<AccessToken> login(@Field("email") String email, @Field("password") String password);

    @POST("refresh")
    @FormUrlEncoded
    Call<AccessToken> refresh(@Field("refresh_token") String refreshToken);

    @POST("logout")
    Call<DoorResponse> logout();

    @GET("departamentos")
    Call<ResponseBody> departamentos();

    @GET("dispositivos")
    Call<DoorResponse> dispositivos();

    @POST("abrirpuerta")
    @FormUrlEncoded
    Call<DataResponse> abrirpuerta(@Field("Type") String Type, @Field("IP") String IP);

    @POST("reportes")
    @FormUrlEncoded
    Call<ResponseBody> reportes(@Field("fecha") String fecha, @Field("DayId") int DayId,
                                @Field("IdDepartment") int IdDepartment);

    @POST("reportes")
    @FormUrlEncoded
    Call<ResponseBody> reportes(@Field("fecha") String fecha, @Field("DayId") int DayId,
                                   @Field("IdDepartment") String IdDepartment);

}