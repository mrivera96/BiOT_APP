package com.diadema.biometriciot;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.diadema.biometriciot.services.TokenManager;

/**
 * Created by César Andrade on 29/04/2019.
 */
public class SplashScreen extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        setTheme(R.style.AppTheme);
        //3000=3 segundos
        int loading = 3000;
        TokenManager tokenManager = TokenManager.getInstance(getApplicationContext().getSharedPreferences("prefs", MODE_PRIVATE));
        String token=tokenManager.getToken().getAccessToken();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //setelah loading maka akan langsung berpindah ke home activity
                if(token==null){
                    Intent login = new Intent(SplashScreen.this.getApplicationContext(), LoginActivity.class);
                    SplashScreen.this.startActivity(login);
                    SplashScreen.this.finish();
                }else{
                    Intent home = new Intent(SplashScreen.this.getApplicationContext(), MainActivity.class);
                    SplashScreen.this.startActivity(home);
                    SplashScreen.this.finish();
                }

            }
        }, loading);
    }
}
