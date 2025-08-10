package com.example.easychat.utils;

import android.content.Context;
import android.widget.Toast;

public class AndoirdUtil {

    public static void showToast(Context context, String message){
        Toast.makeText(context, message,Toast.LENGTH_LONG).show();
    }
}
