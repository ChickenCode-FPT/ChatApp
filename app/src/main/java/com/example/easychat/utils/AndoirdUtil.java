package com.example.easychat.utils;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.easychat.models.UserModel;

public class AndoirdUtil {

    public static void showToast(Context context, String message){
        Toast.makeText(context, message,Toast.LENGTH_LONG).show();
    }
    public static void passUserModelAsIntent(Intent intent, UserModel model){
        intent.putExtra("username",model.getUsername());
        intent.putExtra("phone",model.getPhone());
        intent.putExtra("userid",model.getUserId());

    }
    public static UserModel getUserModelFromIntent(Intent intent){
        UserModel userModel = new UserModel();
        userModel.setUsername(intent.getStringExtra("username"));
        userModel.setPhone(intent.getStringExtra("phone"));
        userModel.setUserId(intent.getStringExtra("userid"));
        return userModel;
    }
}
