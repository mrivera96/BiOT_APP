package com.diadema.biometriciot;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import com.diadema.biometriciot.entities.ApiError;
import com.diadema.biometriciot.entities.Door;
import com.diadema.biometriciot.entities.DoorResponse;
import com.diadema.biometriciot.entities.Reporte;
import com.diadema.biometriciot.entities.ReporteResponse;
import com.diadema.biometriciot.services.ApiService;
import com.diadema.biometriciot.services.NetworkStatusManager;
import com.diadema.biometriciot.services.RetrofitBuilder;
import com.diadema.biometriciot.services.TokenManager;
import com.diadema.biometriciot.services.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.transition.TransitionManager;

import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
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
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DatePickerDialog.OnDateSetListener {
    private static final String TAG = "MainActivity";
    @BindView(R.id.fecha_inicial)
    EditText fecha_inicial;
    @BindView(R.id.fecha_final)
    EditText fecha_final;
    Spinner tipo, departamento;
    ArrayList<String> deptoArray;
    ArrayList<Integer> idDeptoArray;
    ArrayList<Reporte> listReports;
    ApiService serviceWithAuth;
    TokenManager tokenManager;
    Call<ResponseBody> callDepto;
    Call<ResponseBody> callReport;
    Call<DoorResponse> call2;
    private ConstraintLayout contenedorPadreReporte, contenedorFormularioReporte;
    private LinearLayout contenedorMensajeReporte;
    private ImageView imagenMensajeReporte;
    private TextView tvMensajeReporte;
    private int estado;
    private String fecha;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            MainActivity.this.reporte();
        }
        );
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        iniComponent();
        deptoArray = new ArrayList<>();
        idDeptoArray = new ArrayList<>();
        fecha_inicial.setOnClickListener(v -> {
            MainActivity.this.mostrarDialogo();
            estado = 1;
        }
        );

        fecha_final.setOnClickListener(v -> {
            MainActivity.this.mostrarDialogo();
            estado = 2;
        });
        tokenManager = TokenManager.getInstance(getApplicationContext().getSharedPreferences("prefs", MODE_PRIVATE));
        serviceWithAuth = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);

        switch (NetworkStatusManager.status(Objects.requireNonNull(getApplicationContext()))) {
            case "Offline":
                showMensaje(getResources().getDrawable(R.drawable.no_wifi_black), getString(R.string.sin_internet));
                break;
            case "Mobile data":
                showMensaje(getResources().getDrawable(R.drawable.no_wifi_black), getString(R.string.no_wifi));
                break;
            case "SSID Incorrect":
                showMensaje(getResources().getDrawable(R.drawable.no_wifi_black), getString(R.string.no_coorporativa));
                break;
            case "SSID Correct":
                try {
                    showFormulario();
                    setSpinDepto();
                    // showMensaje(getResources().getDrawable(R.drawable.sin_respuesta), getString(R.string.fuera_de_servicio));
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.no_te_emociones),Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public void iniComponent(){
        contenedorPadreReporte = findViewById(R.id.contenedorPadreReporte);
        contenedorMensajeReporte = findViewById(R.id.contenedorMensajeReporte);
        contenedorFormularioReporte = findViewById(R.id.contenedorFormularioReporte);
        imagenMensajeReporte = findViewById(R.id.imagenMensajeReporte);
        tvMensajeReporte = findViewById(R.id.tvMensajeReporte);
        tipo = findViewById(R.id.spinnerTipo);
        departamento = findViewById(R.id.spinnerDepartamento);
        fecha_inicial = findViewById(R.id.fecha_inicial);
        fecha_final = findViewById(R.id.fecha_final);
    }

    private void showTableReport(){
        TransitionManager.beginDelayedTransition(contenedorPadreReporte);
    }

    private void showFormulario(){
        TransitionManager.beginDelayedTransition(contenedorPadreReporte);
        contenedorMensajeReporte.setVisibility(View.GONE);
    }

    private void showMensaje(Drawable imagMensaje, String mensaje){
        imagenMensajeReporte.setImageDrawable(imagMensaje);
        tvMensajeReporte.setText(mensaje);
        TransitionManager.beginDelayedTransition(contenedorPadreReporte);
        contenedorFormularioReporte.setVisibility(View.GONE);
        contenedorMensajeReporte.setVisibility(View.VISIBLE);
    }

    public void setSpinDepto(){
        Log.d(TAG, "initRecyclerView: init getListProducts.");
        //showLoading();
        callDepto = serviceWithAuth.departamentos();
        callDepto.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                Log.w(TAG, "onResponse: " + response);
                if(response.isSuccessful()){
                    assert response.body() != null;
                    try {
                        String respuesta = response.body().string();
                        Gson gson = new Gson();
                        JsonArray deptosObjectsArray = gson.fromJson (respuesta, JsonElement.class)
                                .getAsJsonObject().get("respuesta").getAsJsonArray();
                        for (int i = 0; i < deptosObjectsArray.size(); i++) {
                            String departamento = deptosObjectsArray.get(i).getAsJsonObject().get("Description").getAsString();
                            int idDepartamento = deptosObjectsArray.get(i).getAsJsonObject().get("IdDepartment").getAsInt();
                            Log.w(TAG, "onResponse33: " + departamento);
                            Log.w(TAG, "onResponse33: " + idDepartamento);
                            deptoArray.add(departamento);
                            idDeptoArray.add(idDepartamento);
                        }
                        setSpinner(departamento, deptoArray);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
                    assert response.errorBody() != null;
                    try {
                        Log.w(TAG, "onError: " + response.errorBody().string());
                        Log.w(TAG, "onError2: " + response.message());
                        switch (response.message()){
                            case "Internal Server Error":
                                showMensaje(getResources().getDrawable(R.drawable.sin_respuesta), getString(R.string.error_500));
                                break;
                            case "Unauthorized":
                                tokenManager.deleteToken();
                                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                                finish();
                                break;
                            default:
                                showMensaje(getResources().getDrawable(R.drawable.sin_respuesta), getString(R.string.sin_respuesta)+"Error");
                                break;
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                try {
                    t.printStackTrace();
                    String failure = t.getMessage();
                    if (failure.contains("Internal Server Error")){
                        //showMensaje(getResources().getDrawable(R.drawable.sin_respuesta), getString(R.string.error_500));
                    }

                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    //Método para llenar los spiners
    private void setSpinner(Spinner spinner, ArrayList<String> listdata)
    {
        //Mete el arraylist al spiner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listdata);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    void mostrarDialogo(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String date =  year + "-" + (month+1) + "-" + dayOfMonth;
        if (estado == 1){
            fecha_inicial.setText(date);
        }else
            fecha_final.setText(date);
    }

    public void reporte(){
        Log.d(TAG, "initRecyclerView: init getListProducts.");
        //showLoading();

        if (tipo.getSelectedItemPosition() == 0){
            int idDepartment = idDeptoArray.get(departamento.getSelectedItemPosition());
            fecha = fecha_inicial.getText().toString();
            int idDay = getDiaSemana(fecha_inicial.getText().toString());

            Log.w(TAG, "reporte2: " + fecha);
            Log.w(TAG, "reporte: " + idDepartment);
            Log.w(TAG, "reporte: " + idDay);

            Map<String, String> map = new HashMap<>();
            map.put("fecha", fecha_inicial.getText().toString());
            callReport = serviceWithAuth.reportes(fecha, idDay, idDepartment);
        }else{

            String idDepartmen = "";
            fecha = fecha_inicial.getText().toString()+","+fecha_final.getText().toString();
            int idDay = getDiaSemana(fecha_inicial.getText().toString());

            Log.w(TAG, "reporte2: " + fecha);
            Log.w(TAG, "reporte2: " + idDepartmen);
            Log.w(TAG, "reporte2: " + idDay);
            callReport = serviceWithAuth.reportes(fecha, idDay, idDepartmen);
        }

        callReport.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                Log.w(TAG, "onResponse: " + response);
                if(response.isSuccessful()){
                    assert response.body() != null;
                    try {
                        String respuesta = response.body().string();
                        Gson gson = new Gson();
                        JsonArray reportsObjectsArray = gson.fromJson (respuesta, JsonElement.class)
                                .getAsJsonObject().get("respuesta").getAsJsonArray();
                        int cantsObjectsArray = gson.fromJson (respuesta, JsonElement.class)
                                .getAsJsonObject().get("cantidadempleadosdepto").getAsInt();

                        Log.w(TAG, "onResponseReport: " + cantsObjectsArray+ " "+ reportsObjectsArray);
                        /*for (int i = 0; i < deptosObjectsArray.size(); i++) {
                            String departamento = deptosObjectsArray.get(i).getAsJsonObject().get("Description").getAsString();
                            Log.w(TAG, "onResponse3: " + departamento);
                            deptoArray.add(departamento);
                        }
                        setSpinner(departamento, deptoArray);*/
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    /*if(response.body().getData().size() == 0 ){
                        showMensaje(getResources().getDrawable(R.drawable.sin_respuesta), getString(R.string.sin_puertas));
                    }else {
                        //showRecycleList();
                        for (int i = 0; i < response.body().getData().size(); i++) {
                            Log.w(TAG, "onResponse3: " + response.body().getData().get(i).getNombre());
                            listReports.add(get(response.body().getData().get(i).getDepartamento(),
                                    response.body().getData().get(i).getDia(),
                                    response.body().getData().get(i).getFecha(),
                                    response.body().getData().get(i).getFecha_y_hora_marco_max(),
                                    response.body().getData().get(i).getFecha_y_hora_marco_min(),
                                    response.body().getData().get(i).getHora_entrada(),
                                    response.body().getData().get(i).getHora_salida(),
                                    response.body().getData().get(i).getHorasrealestrabajadas(),
                                    response.body().getData().get(i).getHorastrabajadas(),
                                    response.body().getData().get(i).getMinutos_entrada(),
                                    response.body().getData().get(i).getMinutos_salida(),
                                    response.body().getData().get(i).getNombre(),
                                    response.body().getData().get(i).getNombre_horario()));
                        }

                        //initRecycleList(listaDoors);
                    }*/
                }else {
                    assert response.errorBody() != null;
                    try {
                        Log.w(TAG, "onError: " + response.errorBody().string());
                        Log.w(TAG, "onError2: " + response.message());
                        switch (response.message()){
                            case "Internal Server Error":
                                showMensaje(getResources().getDrawable(R.drawable.sin_respuesta), getString(R.string.error_500));
                                break;
                            case "Unauthorized":
                                tokenManager.deleteToken();
                                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                                finish();
                                break;
                            default:
                                showMensaje(getResources().getDrawable(R.drawable.sin_respuesta), getString(R.string.sin_respuesta)+"Error");
                                break;
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                try {
                    t.printStackTrace();
                    String failure = t.getMessage();
                    if (failure.contains("Internal Server Error")){
                        //showMensaje(getResources().getDrawable(R.drawable.sin_respuesta), getString(R.string.error_500));
                    }

                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    private Reporte get(String departamento, String dia, String fecha, String fecha_y_hora_marco_max,
                        String fecha_y_hora_marco_min, String hora_entrada, String hora_salida,
                        String horasrealestrabajadas, String horastrabajadas, String minutos_entrada,
                        String minutos_salida, String nombre, String nombre_horario){
        return new Reporte(departamento, dia, fecha, fecha_y_hora_marco_max,
                fecha_y_hora_marco_min, hora_entrada, hora_salida,
                horasrealestrabajadas, horastrabajadas, minutos_entrada,
                minutos_salida, nombre, nombre_horario);
    }

    public int getDiaSemana(String fecha) {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date fechaParseada = null;
        try {
            fechaParseada = df.parse(fecha);
        } catch (ParseException e) {
            System.err.println("No se ha podido parsear la fecha.");
            e.printStackTrace();
        }
        GregorianCalendar fechaCalendario = new GregorianCalendar();
        fechaCalendario.setTime(fechaParseada);

        return fechaCalendario.get(Calendar.DAY_OF_WEEK)-1;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_doors) {
            Intent intent =  new Intent(getApplicationContext(), DoorsManager.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            logout();
        } /*else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void logout(){

        call2=serviceWithAuth.logout();
        call2.enqueue(new Callback<DoorResponse>() {
            @Override
            public void onResponse(@NonNull Call<DoorResponse> call, @NonNull Response<DoorResponse> response) {

                Log.w(TAG, "onResponse: " + response);

                if (response.isSuccessful()) {
                    tokenManager.deleteToken();
                    startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                    finish();
                } else {
                    try {
                        Log.w(TAG, "onError: " + response.errorBody().string());
                        Log.w(TAG, "onError2: " + response.message());
                        switch (response.message()){
                            case "Internal Server Error":
                                showMensaje(getResources().getDrawable(R.drawable.sin_respuesta), getString(R.string.error_500));
                                break;
                            case "Unauthorized":
                                tokenManager.deleteToken();
                                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                                finish();
                                break;
                            default:
                                showMensaje(getResources().getDrawable(R.drawable.sin_respuesta), getString(R.string.sin_respuesta)+"Error");
                                break;
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<DoorResponse> call, @NonNull Throwable t) {
                Log.w(TAG, "onFailure: " + t.getMessage());
            }
        });
    }
}
