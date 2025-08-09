package com.example.easychat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Log.d(TAG, "SplashActivity started.");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Redirecting to MainActivity.");
                Intent intent = new Intent(SplashActivity.this, LoginPhoneNumberActivity.class);
                startActivity(intent);
                finish();
            }
        }, 3000);
    }
}