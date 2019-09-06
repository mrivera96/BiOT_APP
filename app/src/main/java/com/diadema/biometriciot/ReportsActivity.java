package com.diadema.biometriciot;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.diadema.biometriciot.adapters.RecyclerViewAdapter;
import com.diadema.biometriciot.entities.ApiError;

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

import android.view.Gravity;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.transition.TransitionManager;

import android.view.Menu;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
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
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private static final String TAG = "MainActivity";
    @BindView(R.id.fecha_inicial)
    EditText fecha_inicial;
    @BindView(R.id.fecha_final)
    EditText fecha_final;
    @BindView(R.id.spinnerTipo)
    Spinner tipo;
    @BindView(R.id.spinnerDepartamento)
    Spinner departamento;
    ArrayList<String> deptoArray;
    ArrayList<Integer> idDeptoArray;
    ArrayList<Reporte> listReports;
    ApiService serviceWithAuth;
    TokenManager tokenManager;
    Call<ResponseBody> callDepto;
    Call<ReporteResponse> callReport;
    @BindView(R.id.contenedorPadreReporte)
    RelativeLayout contenedorPadreReporte;
    @BindView(R.id.contenedorFormularioReporte)
    ConstraintLayout contenedorFormularioReporte;
    @BindView(R.id.contenedorMensajeReporte)
    LinearLayout contenedorMensajeReporte;
    @BindView(R.id.imagenMensajeReporte)
    ImageView imagenMensajeReporte;
    @BindView(R.id.tvMensajeReporte)
    TextView tvMensajeReporte;
    private int estado;
    private String fecha;
    @BindView(R.id.tabla)
    TableLayout tabla; // Layout donde se pintará la tabla
    @BindView(R.id.scrollvertical)
    ScrollView scrollView;
    private ArrayList<TableRow> filas; // Array de las filas de la tabla
    private Resources rs;
    private int FILAS, COLUMNAS;


    @BindView(R.id.fab)
    FloatingActionButton btnConsultar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);
        ButterKnife.bind(this);

        FILAS = COLUMNAS = 0;
        filas = new ArrayList<TableRow>();
        rs = this.getResources();
        listReports = new ArrayList<>();
        btnConsultar.setOnClickListener(v -> {
            if(fecha_inicial.getText().toString().isEmpty() ){
                Toast.makeText(this,"Ingrese una fecha",Toast.LENGTH_SHORT).show();
            }else{
                this.reporte();
                contenedorFormularioReporte.setVisibility(View.GONE);
                btnConsultar.hide();
            }

        });

        deptoArray = new ArrayList<>();
        idDeptoArray = new ArrayList<>();
        fecha_inicial.setOnClickListener(v -> {
                    ReportsActivity.this.mostrarDialogo();
                    estado = 1;
                }
        );

        fecha_final.setOnClickListener(v -> {
            ReportsActivity.this.mostrarDialogo();
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
                    //showFormulario();
                    setSpinDepto();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.no_te_emociones), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if(scrollView.getVisibility()==View.GONE){
            super.onBackPressed();
        }else{
            scrollView.setVisibility(View.GONE);
            contenedorMensajeReporte.setVisibility(View.GONE);
            contenedorFormularioReporte.setVisibility(View.VISIBLE);
            listReports.clear();
            filas.clear();
            tabla.removeAllViews();
            btnConsultar.show();
        }

    }

    private void showMensaje(Drawable imagMensaje, String mensaje) {
        imagenMensajeReporte.setImageDrawable(imagMensaje);
        tvMensajeReporte.setText(mensaje);
        TransitionManager.beginDelayedTransition(contenedorPadreReporte);
        contenedorFormularioReporte.setVisibility(View.GONE);
        contenedorMensajeReporte.setVisibility(View.VISIBLE);
    }

    public void setSpinDepto() {

        //showLoading();
        callDepto = serviceWithAuth.departamentos();
        callDepto.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    assert response.body() != null;
                    try {
                        String respuesta = response.body().string();
                        Gson gson = new Gson();
                        JsonArray deptosObjectsArray = gson.fromJson(respuesta, JsonElement.class)
                                .getAsJsonObject().get("respuesta").getAsJsonArray();
                        for (int i = 0; i < deptosObjectsArray.size(); i++) {
                            String departamento = deptosObjectsArray.get(i).getAsJsonObject().get("Description").getAsString();
                            int idDepartamento = deptosObjectsArray.get(i).getAsJsonObject().get("IdDepartment").getAsInt();

                            deptoArray.add(departamento);
                            idDeptoArray.add(idDepartamento);
                        }
                        setSpinner(departamento, deptoArray);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    assert response.errorBody() != null;
                    try {
                        Log.w(TAG, "onError: " + response.errorBody().string());
                        Log.w(TAG, "onError2: " + response.message());
                        switch (response.message()) {
                            case "Internal Server Error":
                                showMensaje(getResources().getDrawable(R.drawable.sin_respuesta), getString(R.string.error_500));
                                break;
                            case "Unauthorized":
                                tokenManager.deleteToken();
                                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                finish();
                                break;
                            default:
                                showMensaje(getResources().getDrawable(R.drawable.sin_respuesta), getString(R.string.sin_respuesta) + "Error");
                                break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                try {
                    t.printStackTrace();
                    String failure = t.getMessage();
                    if (failure.contains("Internal Server Error")) {
                        //showMensaje(getResources().getDrawable(R.drawable.sin_respuesta), getString(R.string.error_500));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //Método para llenar los spiners
    private void setSpinner(Spinner spinner, ArrayList<String> listdata) {
        //Mete el arraylist al spiner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listdata);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    void mostrarDialogo() {
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
        String date = year + "-" + (month + 1) + "-" + dayOfMonth;
        if (estado == 1) {
            fecha_inicial.setText(date);
        } else
            fecha_final.setText(date);
    }

    public void reporte() {
        Log.d(TAG, "initRecyclerView: init getListProducts.");
        //showLoading();

        if (tipo.getSelectedItemPosition() == 0) {
            int idDepartment = idDeptoArray.get(departamento.getSelectedItemPosition());
            fecha = fecha_inicial.getText().toString();
            int idDay = getDiaSemana(fecha_inicial.getText().toString());

            Log.w(TAG, "reporte2: " + fecha);
            Log.w(TAG, "reporte: " + idDepartment);
            Log.w(TAG, "reporte: " + idDay);

            Map<String, String> map = new HashMap<>();
            map.put("fecha", fecha_inicial.getText().toString());
            callReport = serviceWithAuth.reportes(fecha, idDay, idDepartment);
        } else {

            String idDepartmen = "";
            fecha = fecha_inicial.getText().toString() + "," + fecha_final.getText().toString();
            int idDay = getDiaSemana(fecha_inicial.getText().toString());

            Log.w(TAG, "reporte2: " + fecha);
            Log.w(TAG, "reporte2: " + idDepartmen);
            Log.w(TAG, "reporte2: " + idDay);
            callReport = serviceWithAuth.reportes(fecha, idDay, idDepartmen);
        }

        callReport.enqueue(new Callback<ReporteResponse>() {
            @Override
            public void onResponse(@NonNull Call<ReporteResponse> call, @NonNull Response<ReporteResponse> response) {

                if (response.isSuccessful()) {


                    if (response.body().getData().size() == 0) {
                        showMensaje(getResources().getDrawable(R.drawable.sin_respuesta), getString(R.string.sin_registros));
                    } else {

                        for (int i = 0; i < response.body().getData().size(); i++) {
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
                                    response.body().getData().get(i).getNombre_horario(),
                                    response.body().getData().get(i).getAsis(),
                                    response.body().getData().get(i).getSalioantes(),
                                    response.body().getData().get(i).getExtras()));
                        }

                        agregarCabecera(R.array.cabecera_tabla);
                        agregarFilaTabla(listReports);
                        scrollView.setVisibility(View.VISIBLE);


                    }
                } else {
                    assert response.errorBody() != null;
                    try {
                        Log.w(TAG, "onError: " + response.errorBody().string());
                        Log.w(TAG, "onError2: " + response.message());
                        switch (response.message()) {
                            case "Internal Server Error":
                                showMensaje(getResources().getDrawable(R.drawable.sin_respuesta), getString(R.string.error_500));
                                break;
                            case "Unauthorized":
                                tokenManager.deleteToken();
                                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                finish();
                                break;
                            default:
                                showMensaje(getResources().getDrawable(R.drawable.sin_respuesta), getString(R.string.sin_respuesta) + "Error");
                                break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReporteResponse> call, @NonNull Throwable t) {
                try {
                    t.printStackTrace();
                    String failure = t.getMessage();
                    if (failure.contains("Internal Server Error")) {
                        showMensaje(getResources().getDrawable(R.drawable.sin_respuesta), getString(R.string.error_500));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Reporte get(String departamento, String dia, String fecha, String fecha_y_hora_marco_max,
                        String fecha_y_hora_marco_min, String hora_entrada, String hora_salida,
                        String horasrealestrabajadas, String horastrabajadas, String minutos_entrada,
                        String minutos_salida, String nombre, String nombre_horario, String asis, String salioantes,
                        String hextras) {
        return new Reporte(departamento, dia, fecha, fecha_y_hora_marco_max,
                fecha_y_hora_marco_min, hora_entrada, hora_salida,
                horasrealestrabajadas, horastrabajadas, minutos_entrada,
                minutos_salida, nombre, nombre_horario,  asis,  salioantes,
                 hextras);
    }

    /**
     * Añade la cabecera a la tabla
     *
     * @param recursocabecera Recurso (array) donde se encuentra la cabecera de la tabla
     */
    public void agregarCabecera(int recursocabecera) {
        TableRow.LayoutParams layoutCelda;
        TableRow fila = new TableRow(this);
        TableRow.LayoutParams layoutFila = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        fila.setLayoutParams(layoutFila);

        String[] arraycabecera = rs.getStringArray(recursocabecera);
        COLUMNAS = arraycabecera.length;

        for (int i = 0; i < arraycabecera.length; i++) {
            TextView texto = new TextView(this);
            layoutCelda = new TableRow.LayoutParams(obtenerAnchoPixelesTexto(arraycabecera[i]), TableRow.LayoutParams.WRAP_CONTENT);
            texto.setText(arraycabecera[i]);
            texto.setGravity(Gravity.CENTER_HORIZONTAL);
            texto.setTextColor(getResources().getColor(R.color.icons));
            fila.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            texto.setTextSize(18);

            texto.setLayoutParams(layoutCelda);

            fila.addView(texto);
        }

        tabla.addView(fila);
        filas.add(fila);

        FILAS++;
    }

    /**
     * Agrega una fila a la tabla
     *
     * @param elementos Elementos de la fila
     */
    public void agregarFilaTabla(ArrayList<Reporte> elementos) {


        for (int i = 0; i < elementos.size(); i++) {
            TableRow.LayoutParams layoutCelda, layoutCelda1, layoutCelda2,
                    layoutCelda3, layoutCelda4, layoutCelda5, layoutCelda6,
                    layoutCelda7, layoutCelda8, layoutCelda9, layoutCelda10,
                    layoutCelda11;

            TableRow.LayoutParams layoutFila = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            TableRow fila = new TableRow(this);
            fila.setLayoutParams(layoutFila);
            TextView nombre = new TextView(this);
            nombre.setText(String.valueOf(elementos.get(i).getNombre()));

            TextView depto = new TextView(this);
            depto.setText(String.valueOf(elementos.get(i).getDepartamento()));

            TextView horario = new TextView(this);
            horario.setText(String.valueOf(elementos.get(i).getNombre_horario()));

            TextView hme = new TextView(this);
            hme.setText(String.valueOf(elementos.get(i).getFecha_y_hora_marco_min()));

            TextView hms = new TextView(this);
            hms.setText(String.valueOf(elementos.get(i).getFecha_y_hora_marco_max()));

            TextView fecha = new TextView(this);
            fecha.setText(String.valueOf(elementos.get(i).getFecha()));

            TextView dia = new TextView(this);
            dia.setText(String.valueOf(elementos.get(i).getDia()));

            TextView asis = new TextView(this);
            asis.setText(String.valueOf(elementos.get(i).getAsis()));
            if(asis.getText().equals("SÍ")){
                fila.setBackgroundColor(Color.RED);
            }

            TextView hteor = new TextView(this);
            hteor.setText(String.valueOf(elementos.get(i).getHorastrabajadas()));

            if(hteor.getText().equals("marcaje incorrecto")){
                fila.setBackgroundColor(Color.YELLOW);
            }

            TextView hreal = new TextView(this);
            hreal.setText(String.valueOf(elementos.get(i).getHorasrealestrabajadas()));

            TextView tsal = new TextView(this);
            tsal.setText(String.valueOf(elementos.get(i).getSalioantes()));

            TextView extras = new TextView(this);
            extras.setText(String.valueOf(elementos.get(i).getExtras()));


            layoutCelda = new TableRow.LayoutParams(obtenerAnchoPixelesTexto(nombre.getText().toString()), TableRow.LayoutParams.WRAP_CONTENT);
            nombre.setLayoutParams(layoutCelda);

            layoutCelda1 = new TableRow.LayoutParams(obtenerAnchoPixelesTexto(depto.getText().toString()), TableRow.LayoutParams.WRAP_CONTENT);
            depto.setLayoutParams(layoutCelda1);

            layoutCelda2 = new TableRow.LayoutParams(obtenerAnchoPixelesTexto(horario.getText().toString()), TableRow.LayoutParams.WRAP_CONTENT);
            horario.setLayoutParams(layoutCelda2);

            layoutCelda3 = new TableRow.LayoutParams(obtenerAnchoPixelesTexto(hme.getText().toString()), TableRow.LayoutParams.WRAP_CONTENT);
            hme.setLayoutParams(layoutCelda3);

            layoutCelda4 = new TableRow.LayoutParams(obtenerAnchoPixelesTexto(hms.getText().toString()), TableRow.LayoutParams.WRAP_CONTENT);
            hms.setLayoutParams(layoutCelda4);

            layoutCelda5 = new TableRow.LayoutParams(obtenerAnchoPixelesTexto(fecha.getText().toString()), TableRow.LayoutParams.WRAP_CONTENT);
            fecha.setLayoutParams(layoutCelda5);

            layoutCelda6 = new TableRow.LayoutParams(obtenerAnchoPixelesTexto(dia.getText().toString()), TableRow.LayoutParams.WRAP_CONTENT);
            dia.setLayoutParams(layoutCelda6);

            layoutCelda7 = new TableRow.LayoutParams(obtenerAnchoPixelesTexto(asis.getText().toString()), TableRow.LayoutParams.WRAP_CONTENT);
            asis.setLayoutParams(layoutCelda7);

            layoutCelda8 = new TableRow.LayoutParams(obtenerAnchoPixelesTexto(hteor.getText().toString()), TableRow.LayoutParams.WRAP_CONTENT);
            hteor.setLayoutParams(layoutCelda8);

            layoutCelda9 = new TableRow.LayoutParams(obtenerAnchoPixelesTexto(hreal.getText().toString()), TableRow.LayoutParams.WRAP_CONTENT);
            hreal.setLayoutParams(layoutCelda9);

            layoutCelda10 = new TableRow.LayoutParams(obtenerAnchoPixelesTexto(tsal.getText().toString()), TableRow.LayoutParams.WRAP_CONTENT);
            tsal.setLayoutParams(layoutCelda10);

            layoutCelda11 = new TableRow.LayoutParams(obtenerAnchoPixelesTexto(extras.getText().toString()), TableRow.LayoutParams.WRAP_CONTENT);
            extras.setLayoutParams(layoutCelda11);


            fila.addView(nombre);
            fila.addView(depto);
            fila.addView(horario);
            fila.addView(hme);
            fila.addView(hms);
            fila.addView(fecha);
            fila.addView(dia);
            fila.addView(asis);
            fila.addView(hteor);
            fila.addView(hreal);
            fila.addView(tsal);
            fila.addView(extras);

            tabla.addView(fila);


            filas.add(fila);

        }


        FILAS++;
    }

    /**
     * Obtiene el ancho en píxeles de un texto en un String
     *
     * @param texto Texto
     * @return Ancho en píxeles del texto
     */
    private int obtenerAnchoPixelesTexto(String texto) {
        Paint p = new Paint();
        Rect bounds = new Rect();
        p.setTextSize(50);

        p.getTextBounds(texto, 0, texto.length(), bounds);
        return bounds.width();
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

        return fechaCalendario.get(Calendar.DAY_OF_WEEK) - 1;
    }
}
