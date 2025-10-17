package com.example.easychat.utils;

import android.net.Uri;
import android.util.Log;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.File;
import java.util.Map;

public class CloudinaryUtil {
    private static final String CLOUD_NAME = "dhwnptz55";
    private static final String API_KEY = "298769794463723";
    private static final String API_SECRET = "HiImkwzH5361j9_FmM8mHhAk5eI";

    private static final Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", CLOUD_NAME,
            "api_key", API_KEY,
            "api_secret", API_SECRET
    ));

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onError(Exception e);
    }

    public static void uploadImage(Uri imageUri, UploadCallback callback) {
        new Thread(() -> {
            try {
                File file = new File(imageUri.getPath());
                Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.emptyMap());
                String imageUrl = (String) uploadResult.get("secure_url");

                Log.d("Cloudinary", "Uploaded URL: " + imageUrl);
                if (callback != null) callback.onSuccess(imageUrl);
            } catch (Exception e) {
                e.printStackTrace();
                if (callback != null) callback.onError(e);
            }
        }).start();
    }
}
