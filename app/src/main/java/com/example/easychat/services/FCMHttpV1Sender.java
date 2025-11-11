package com.example.easychat.services;

import android.content.Context;
import android.util.Log;

import com.example.easychat.R;
import com.google.auth.oauth2.GoogleCredentials;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;

public class FCMHttpV1Sender {

    private static final String PROJECT_ID = "easychat-back-end";
    private static final String FCM_ENDPOINT =
            "https://fcm.googleapis.com/v1/projects/" + PROJECT_ID + "/messages:send";

    public static void sendPushNotification(Context context, String targetToken, String title, String body, String senderId) {
        new Thread(() -> {
            try {
                InputStream serviceAccountStream = context.getResources().openRawResource(R.raw.service_account);

                GoogleCredentials credentials = GoogleCredentials
                        .fromStream(serviceAccountStream)
                        .createScoped(Collections.singleton("https://www.googleapis.com/auth/firebase.messaging"));
                credentials.refreshIfExpired();

                String accessToken = credentials.getAccessToken().getTokenValue();

                JSONObject notification = new JSONObject();
                notification.put("title", title);
                notification.put("body", body);

                JSONObject data = new JSONObject();
                data.put("senderId", senderId); // thêm senderId vào data

                JSONObject messageObj = new JSONObject();
                messageObj.put("token", targetToken);
                messageObj.put("notification", notification);
                messageObj.put("data", data);

                JSONObject fullMessage = new JSONObject();
                fullMessage.put("message", messageObj);

                URL url = new URL("https://fcm.googleapis.com/v1/projects/easychat-back-end/messages:send");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(fullMessage.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d("FCMHttpV1Sender", "FCM Response Code: " + responseCode);

            } catch (Exception e) {
                Log.e("FCMHttpV1Sender", "Error sending FCM notification", e);
            }
        }).start();
    }
}

