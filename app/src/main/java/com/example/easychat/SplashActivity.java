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

import com.example.easychat.models.UserModel;
import com.example.easychat.utils.AndoirdUtil;
import com.example.easychat.utils.FirebaseUtil;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if(FirebaseUtil.isLoggedIn() && getIntent().getExtras()!=null){
            String userId =getIntent().getExtras().getString("userId");
            FirebaseUtil.allUserCollectionReference().document(userId).get()
                    .addOnCompleteListener(task -> {
                       if(task.isSuccessful()){
                           UserModel model = task.getResult().toObject(UserModel.class);

                           Intent mainIntent = new Intent(this,MainActivity.class);
                           mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                           startActivity(mainIntent);

                           Intent intent = new Intent(this, ChatActivity.class);
                           AndoirdUtil.passUserModelAsIntent(intent,model);
                           intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                           startActivity(intent);
                           finish();
                       }
                    });
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(FirebaseUtil.isLoggedIn()){
                        startActivity(new Intent(SplashActivity.this,MainActivity.class));
                    } else {
                        startActivity(new Intent(SplashActivity.this,LoginPhoneNumberActivity.class));
                    }
                    finish();
                }
            }, 3000);
        }



    }
}