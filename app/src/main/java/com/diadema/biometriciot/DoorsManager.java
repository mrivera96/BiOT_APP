package com.diadema.biometriciot;
/**
 * Updated by Melvin Rivera on 14/05/2019.
 */
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.diadema.biometriciot.adapters.RecyclerViewAdapter;
import com.diadema.biometriciot.entities.Door;
import com.diadema.biometriciot.entities.DoorResponse;
import com.diadema.biometriciot.services.ApiService;
import com.diadema.biometriciot.services.NetworkStatusManager;
import com.diadema.biometriciot.services.RetrofitBuilder;
import com.diadema.biometriciot.services.TokenManager;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoorsManager extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private static final String TAG = "DoorsManager";
    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
    private static ArrayList<Door> listaDoors;
    private static String type = "";
    private Call<DoorResponse> callDoors;
    ApiService serviceWithAuth;
    TokenManager tokenManager;
    private ProgressBar progressBarPuerta;
    private ConstraintLayout contenedorPadrePuerta;
    private LinearLayout contenedorMensajePuerta, contenedorRecycleListPuerta;
    private ImageView imagenMensajePuerta;
    private TextView tvMensajePuerta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doors_manager);
        setTitle(getString(R.string.title_list_doors));

        iniComponent();
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
                    getDoors();
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.no_te_emociones),Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public void iniComponent(){
        recyclerView = findViewById(R.id.recycle_list);
        progressBarPuerta = findViewById(R.id.progressBarPuerta);
        contenedorPadrePuerta = findViewById(R.id.contenedorPadrePuerta);
        contenedorMensajePuerta = findViewById(R.id.contenedorMensajePuerta);
        contenedorRecycleListPuerta = findViewById(R.id.contenedorRecycleListPuerta);
        imagenMensajePuerta = findViewById(R.id.imagenMensajePuerta);
        tvMensajePuerta = findViewById(R.id.tvMensajePuerta);
    }

    private void getDoors(){
        Log.d(TAG, "initRecyclerView: init getDoors.");
        showLoading();
        listaDoors = new ArrayList<>();
        callDoors = serviceWithAuth.dispositivos();
        callDoors.enqueue(new Callback<DoorResponse>() {
            @Override
            public void onResponse(@NonNull Call<DoorResponse> call, @NonNull Response<DoorResponse> response) {
                if(response.isSuccessful()){
                    assert response.body() != null;
                    if(response.body().getData().size() == 0 ){
                        showMensaje(getResources().getDrawable(R.drawable.sin_respuesta), getString(R.string.sin_puertas));
                    }else {
                            showRecycleList();
                            for (int i = 0; i < response.body().getData().size(); i++) {
                                if (response.body().getData().get(i).getType().equals("11")){
                                    type = "bw";
                                }else{
                                    type = "bioface";
                                }
                                Door door= new Door(response.body().getData().get(i).getDescription(),
                                        response.body().getData().get(i).getIP(),
                                        type,
                                        response.body().getData().get(i).getHDoor());

                                if (door.getHDoor().equals("1") ) {

                                    listaDoors.add(door);
                                }

                                initRecycleList(listaDoors);
                            }

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
                            case "Unauthenticated.":
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
                try {
                    t.printStackTrace();
                    String failure = t.getMessage();
                    if (failure.contains("Internal Server Error")){
                        showMensaje(getResources().getDrawable(R.drawable.sin_respuesta), getString(R.string.error_500));
                    }

                    if (failure.contains("Expected BEGIN_ARRAY")){
                        showMensaje(getResources().getDrawable(R.drawable.sin_respuesta),getString(R.string.deformed_data));
                    }else
                        showMensaje(getResources().getDrawable(R.drawable.sin_respuesta), getString(R.string.sin_respuesta));
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    /*private Door get(String Description, String IP, String Type){
        return new Door(Description, IP, Type);
    }*/

    private void initRecycleList(List<Door> listaDoors){
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(getApplicationContext(), R.anim.layout_animation_left_to_right);
        adapter = new RecyclerViewAdapter(getApplicationContext(), listaDoors);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutAnimation(controller);
        Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
        recyclerView.setOnClickListener(v -> {
            String IP = ((TextView) v.findViewById(R.id.tvIP)).getText().toString();
            Snackbar.make(v, IP, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (callDoors != null) {
            callDoors.cancel();
            callDoors = null;
        }
    }

    private void showLoading(){
        TransitionManager.beginDelayedTransition(contenedorPadrePuerta);
        contenedorRecycleListPuerta.setVisibility(View.VISIBLE);
        contenedorMensajePuerta.setVisibility(View.GONE);
        progressBarPuerta.setVisibility(View.VISIBLE);
    }

    private void showRecycleList(){
        TransitionManager.beginDelayedTransition(contenedorPadrePuerta);
        progressBarPuerta.setVisibility(View.GONE);
    }

    private void showMensaje(Drawable imagMensaje, String mensaje){
        imagenMensajePuerta.setImageDrawable(imagMensaje);
        tvMensajePuerta.setText(mensaje);
        TransitionManager.beginDelayedTransition(contenedorPadrePuerta);
        contenedorMensajePuerta.setVisibility(View.VISIBLE);
        contenedorRecycleListPuerta.setVisibility(View.GONE);
        progressBarPuerta.setVisibility(View.GONE);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.buscador, menu);
        super.onCreateOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.buscador);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(this);

        item.setOnActionExpandListener( new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                adapter.setFilter(listaDoors);
                return true;
            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        try{
            ArrayList<Door> nuevalista = filter(listaDoors, s);
            adapter.setFilter(nuevalista);
        }catch (Exception e){
            e.printStackTrace();
        }

        return false;
    }
    private ArrayList<Door> filter(ArrayList<Door> data, String buscar ){
        ArrayList<Door> filtrado = new ArrayList<>();
        try{
            buscar = buscar.toLowerCase();
            for (Door encuentra: data){
                String valor = encuentra.getDescription().toLowerCase();
                if (valor.contains(buscar)){
                    filtrado.add(encuentra);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return filtrado;
    }

}
