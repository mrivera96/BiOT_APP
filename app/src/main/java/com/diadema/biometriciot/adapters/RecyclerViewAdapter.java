package com.diadema.biometriciot.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.diadema.biometriciot.R;
import com.diadema.biometriciot.entities.DataResponse;
import com.diadema.biometriciot.entities.Door;
import com.diadema.biometriciot.entities.DoorResponse;
import com.diadema.biometriciot.services.ApiService;
import com.diadema.biometriciot.services.RetrofitBuilder;
import com.diadema.biometriciot.services.TokenManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by CEK on 22/03/2019.
 */

/**
 * Updated by Melvin Rivera on 14/05/2019.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";
    private List<Door> data;
    private Context context;

    public RecyclerViewAdapter(Context context, List<Door> data) {
        this.context = context;
        this.data = data;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder{
        CardView mCardView;
        TextView mTextView, tvIP;
        ImageView imageView;
        Button btn_abrir;

        MyViewHolder(View v){
            super(v);
            mCardView = v.findViewById(R.id.materialCard);
            mTextView = v.findViewById(R.id.door);
            tvIP = v.findViewById(R.id.tvIP);
            imageView = v.findViewById(R.id.componente);
            btn_abrir = v.findViewById(R.id.btn_abrir);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.design_item_door, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position){
        Log.d(TAG, "onBindViewHolder: called." + position);
        Door all = data.get(position);
        holder.tvIP.setText(all.getIP());
        holder.mTextView.setText(all.getDescription());
        holder.imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.puerta_on));
        holder.btn_abrir.setOnClickListener(v -> {
            Toast.makeText(context,"Abriendo puerta... Por favor espere", Toast.LENGTH_LONG).show();
            Toast.makeText(context,"Abriendo puerta... Por favor espere", Toast.LENGTH_LONG).show();
            openDoor(v,all);
        });
    }

    @Override
    public int getItemCount()
    {
        return data.size();
    }

    public void setFilter(ArrayList<Door> dataAdd){
        this.data = new ArrayList<>();
        this.data.addAll(dataAdd);
        notifyDataSetChanged();
    }

    public void openDoor(View v, Door all){
        Log.d(TAG, "initRecyclerView: init getListProducts.");
        //showLoading();
        ApiService serviceWithAuth;
        TokenManager tokenManager;
        Call<DataResponse> callDepto;
        tokenManager = TokenManager.getInstance(context.getSharedPreferences("prefs", MODE_PRIVATE));
        serviceWithAuth = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);
        callDepto = serviceWithAuth.abrirpuerta(all.getType(), all.getIP());
        callDepto.enqueue(new Callback<DataResponse>() {
            @Override
            public void onResponse(@NonNull Call<DataResponse> call, @NonNull Response<DataResponse> response) {
                Log.w(TAG, "onResponse: " + response);
                if(response.isSuccessful()){
                    assert response.body() != null;
                    Snackbar snackbar = Snackbar.make(v,"Open",Snackbar.LENGTH_LONG);
                    snackbar.show();
                }else {
                    assert response.errorBody() != null;
                    try {
                        Log.w(TAG, "onError: " + response.errorBody().string());
                        switch (response.message()){
                            case "Internal Server Error":
                                Snackbar snackbar2 = Snackbar.make(v,"Error",Snackbar.LENGTH_LONG);
                                snackbar2.show();
                                //showMensaje(getResources().getDrawable(R.drawable.sin_respuesta), getString(R.string.error_500));
                                break;
                            default:
                                Snackbar snackbar = Snackbar.make(v,"Error",Snackbar.LENGTH_LONG);
                                snackbar.show();
                                break;
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<DataResponse> call, @NonNull Throwable t) {
                try {
                    t.printStackTrace();
                    Snackbar snackbar = Snackbar.make(v,"Por favor intente de nuevo",Snackbar.LENGTH_LONG);
                    snackbar.show();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

}