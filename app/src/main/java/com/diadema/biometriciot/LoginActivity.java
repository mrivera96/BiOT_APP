package com.diadema.biometriciot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.transition.TransitionManager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.diadema.biometriciot.entities.AccessToken;
import com.diadema.biometriciot.entities.ApiError;
import com.diadema.biometriciot.services.ApiService;
import com.diadema.biometriciot.services.NetworkStatusManager;
import com.diadema.biometriciot.services.RetrofitBuilder;
import com.diadema.biometriciot.services.TokenManager;
import com.diadema.biometriciot.services.Utils;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by César Andrade on 29/04/2019.
 */
/**
 * Updated by Melvin Rivera on 14/05/2019.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int PERMISION_FINE_LOCATION=100;
    protected static int fallos = 0;
    private Button btnLogin;
    @BindView(R.id.til_email)
    TextInputLayout tilEmail;

    @BindView(R.id.til_password)
    TextInputLayout tilPassword;

    @BindView(R.id.container)
    RelativeLayout container;

    @BindView(R.id.lblICT)
    TextView lblIct;

    @BindView(R.id.form_container)
    LinearLayout formContainer;

    @BindView(R.id.loader)
    ProgressBar loader;

    @BindView(R.id.contenedorPadreLogin)
    ConstraintLayout contenedorPadreLogin;

    @BindView(R.id.contenedorMensajeLogin)
    LinearLayout contenedorMensajeLogin;

    @BindView(R.id.contenedorLogin)
    ScrollView contenedorLogin;

    @BindView(R.id.imagenMensajeLogin)
    ImageView imagenMensaje;

    @BindView(R.id.tvMensajeLogin)
    TextView tvMensaje;

    @BindView(R.id.salir)
    TextView btnSalir;

    ApiService service, serviceWithAuth;
    TokenManager tokenManager;
    AwesomeValidation validator;
    Call<AccessToken> call;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);


        btnLogin = findViewById(R.id.btn_login);
        service = RetrofitBuilder.createService(ApiService.class);
        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));
        SharedPreferences prefs_Info=getSharedPreferences("prefsInfo", MODE_PRIVATE);
        validator = new AwesomeValidation(ValidationStyle.TEXT_INPUT_LAYOUT);
        checkNetworkState();
        /***
         *ACTUALIZADO
         * EN LAS NUEVAS VERSIONES DE ANDROID ES NECESARIO OBTENER PERMISO ACCESS_FINE_LOCATION Y ADEMÁS
         * TENER LOS SERVICIOS DE LOCALIZACIÓN ACTIVADOS PARA PODER OBTENER DETALLES DE LA RED
         * POSTERIORMENTE LA EMPRESA AGREGÓ UNA NUEVA RED INALAÁMBRICA Y SE DECIDIÓ NO VALIDAR LA RED A
         * LA CUAL ESTÁ CONECTADO, YA QUE 2 DE ELLAS PUEDEN ACCEDER AL SERVIDOR QUE ALOJA LOS DATOS
         ***/
        /*LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Check permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISION_FINE_LOCATION);
                recreate();
            }

        }else{
            showMensaje(getResources().getDrawable(R.drawable.gps),"Debe activar los servicios de localización");
        }*/


        serviceWithAuth = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);
        setupRules();
    }

    @OnClick(R.id.btn_login)
    void login(){

        String email = tilEmail.getEditText().getText().toString();
        String password = tilPassword.getEditText().getText().toString();

        tilEmail.setError(null);
        tilPassword.setError(null);

        validator.clear();

        if (validator.validate()) {
            showLoading();
            call = service.login(email, password);
            call.enqueue(new Callback<AccessToken>() {
                @Override
                public void onResponse(@NonNull Call<AccessToken> call, @NonNull Response<AccessToken> response) {

                    Log.w(TAG, "onResponse: " + response);

                    if(response.isSuccessful()){
                        tokenManager.saveToken(response.body());
                        Intent home = new Intent(getApplicationContext(), MainActivity.class);
                        access(home);
                    } else {
                        fallos = fallos + 1;
                        if(fallos == 3){
                            Toast.makeText(getApplicationContext(), R.string.bloqueo, Toast.LENGTH_LONG).show();
                            btnLogin.setText(R.string.deshabilitado);
                            btnLogin.setEnabled(false);
                            fallos = 0;
                            esperarDesbloqueo();
                        }

                        if(response.code() == 422){
                            handleErrors(response.errorBody());
                        }

                        if(response.code() == 401){
                            ApiError apiError = Utils.converErrors(response.errorBody());
                            Toast.makeText(LoginActivity.this, apiError.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        showForm();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<AccessToken> call, @NonNull Throwable t) {
                    Log.w(TAG, "onFailure: " + t.getMessage());
                    showForm();
                }
            });

        }
    }

    public void access(Intent intent){
        startActivity(intent);
        finish();
    }

    private void showLoading(){
        TransitionManager.beginDelayedTransition(container);
        contenedorMensajeLogin.setVisibility(View.GONE);
        formContainer.setVisibility(View.GONE);
        lblIct.setVisibility(View.GONE);
        loader.setVisibility(View.VISIBLE);
    }

    private void showForm(){
        TransitionManager.beginDelayedTransition(container);
        contenedorMensajeLogin.setVisibility(View.GONE);
        formContainer.setVisibility(View.VISIBLE);
        loader.setVisibility(View.GONE);
    }

    private void showMensaje(Drawable imagMensaje, String mensaje){
        imagenMensaje.setImageDrawable(imagMensaje);
        tvMensaje.setText(mensaje);
        TransitionManager.beginDelayedTransition(contenedorPadreLogin);
        contenedorMensajeLogin.setVisibility(View.VISIBLE);
        contenedorLogin.setVisibility(View.GONE);
        loader.setVisibility(View.GONE);
    }

    public void setupRules() {
        validator.addValidation(this, R.id.til_email, Patterns.EMAIL_ADDRESS, R.string.err_email);
        validator.addValidation(this, R.id.til_password, "[a-zA-Z0-9.]{5,50}", R.string.err_password);
    }

    private void handleErrors(ResponseBody response) {
        ApiError apiError = Utils.converErrors(response);
        for (Map.Entry<String, List<String>> error : apiError.getErrors().entrySet()) {
            if (error.getKey().equals("email")) {
                tilEmail.setError(error.getValue().get(0));
            }
            if (error.getKey().equals("password")) {
                tilPassword.setError(error.getValue().get(0));
            }
        }
    }

    @OnClick(R.id.salir)
    void salir(){
        finish();
    }


    private void esperarDesbloqueo(){
        new CountDownTimer(5000, 1000){

            @Override
            public void onTick(long l) {}

            @Override
            public void onFinish() {
                btnLogin.setText(R.string.ingresar);
                btnLogin.setEnabled(true);
            }
        }.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (call != null) {
            call.cancel();
            call = null;
        }
    }


   /* @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISION_FINE_LOCATION) {
            if(grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                // Permission was denied or request was cancelled
            }
        }
    }*/

    private void checkNetworkState(){
        switch (NetworkStatusManager.status(Objects.requireNonNull(getApplicationContext()))) {
            case "Offline":
                showMensaje(getResources().getDrawable(R.drawable.no_wifi_blanco), getString(R.string.sin_internet));
                break;
            case "Mobile data":
                showMensaje(getResources().getDrawable(R.drawable.no_wifi_blanco), getString(R.string.no_wifi));
                break;

            case "SSID Correct":
                try {
                    if (tokenManager.getToken().getAccessToken() != null) {
                        Intent intentRH = new Intent(getApplicationContext(), MainActivity.class);
                        access(intentRH);
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), getString(R.string.no_te_emociones), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

}
