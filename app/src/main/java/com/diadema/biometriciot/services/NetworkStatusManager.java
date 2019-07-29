package com.diadema.biometriciot.services;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.PermissionInfoCompat;

import com.diadema.biometriciot.R;

import java.security.acl.Permission;

import static android.Manifest.permission_group.LOCATION;
import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * Created by César Andrade on 29/04/2019.
 */
public class NetworkStatusManager {
    private static String statusResponse;

    public static String status(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Log.d("NetworkStatusManager", "Online");
            Log.d("NetworkStatusManager", " Estado actual: " + networkInfo.getState());
            switch (networkInfo.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                    // connected to wifi get SSID
                    /***
                     * ACTUALIZADO
                     * MÉTODO  PARA OBTENER SSID DE LA RED WIFI
                     * ***/
                    WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                   Log.d("NetworkStatusManager", " SSID iguales: " + wifiInfo.getSSID().equals(context.getString(R.string.ssid)));
                    Log.d("NetworkStatusManager", " SSID: " + wifiInfo.getSSID());
                    //if (wifiInfo.getSSID().equals(context.getString(R.string.ssid)) ){
                        statusResponse = "SSID Correct";
                    //}else
                     //   statusResponse = "SSID Incorrect";

                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    // connected to mobile data
                    statusResponse = "Mobile data";
                    break;
                default:
                    break;
            }
        } else {
            Log.d("NetworkStatusManager", "Offline");
            statusResponse = "Offline";
        }
        return statusResponse;
    }
}
