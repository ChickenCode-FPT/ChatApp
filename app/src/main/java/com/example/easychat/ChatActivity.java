package com.example.easychat;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.easychat.models.UserModel;
import com.example.easychat.utils.AndoirdUtil;

public class ChatActivity extends AppCompatActivity {

    UserModel otherUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //get user from intent
        otherUser = AndoirdUtil.getUserModelFromIntent(getIntent());
    }
}