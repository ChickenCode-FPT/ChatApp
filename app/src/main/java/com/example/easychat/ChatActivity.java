package com.example.easychat;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.easychat.adapter.ChatRecyclerAdapter;
import com.example.easychat.adapter.SearchUserRecyclerAdapter;
import com.example.easychat.models.ChatMessageModel;
import com.example.easychat.models.ChatroomModel;
import com.example.easychat.models.UserModel;
import com.example.easychat.services.FCMHttpV1Sender;
import com.example.easychat.utils.AndoirdUtil;
import com.example.easychat.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.Arrays;

public class ChatActivity extends AppCompatActivity {

    UserModel otherUser;
    ChatroomModel chatroomModel;
    ChatRecyclerAdapter adapter;
    EditText messageInput;
    ImageButton sendMessageBtn;

    ImageButton backBtn;

    TextView otherUsername;

    String chatroomId;

    RecyclerView recyclerView;

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //get user from intent
        otherUser = AndoirdUtil.getUserModelFromIntent(getIntent());
        chatroomId = FirebaseUtil.getChatroomnId(FirebaseUtil.currentUserId(), otherUser.getUserId());

        messageInput = findViewById(R.id.chat_message_input);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        backBtn = findViewById(R.id.back_btn);
        otherUsername =  findViewById(R.id.other_username);
        recyclerView = findViewById(R.id.recycler_chat);
        imageView = findViewById(R.id.profile_pic_image_view);


        backBtn.setOnClickListener((v) -> {
            onBackPressed();
        });
        otherUsername.setText(otherUser.getUsername());
        if (otherUser.getProfilePic() != null && !otherUser.getProfilePic().isEmpty()) {
            Glide.with(this)
                    .load(otherUser.getProfilePic())
                    .circleCrop()
                    .placeholder(R.drawable.icon_chat)
                    .error(R.drawable.icon_chat)
                    .into(imageView);

        } else {
            // Nếu người dùng chưa có ảnh, gán ảnh mặc định
            imageView.setImageResource(R.drawable.icon_chat);
        }



        sendMessageBtn.setOnClickListener((v -> {
            String message = messageInput.getText().toString().trim();
            if(message.isEmpty()){
                return;
            }
            sendMessageToUser(message);
        }));

        getOrCreateChatRoomModel();
        setupChatRecyclerView();

    }

    void setupChatRecyclerView(){
        Query query = FirebaseUtil.getChatroomMessageReference(chatroomId)
                .orderBy("timesStamp",Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatMessageModel> options = new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query,ChatMessageModel.class).build();

        adapter = new ChatRecyclerAdapter(options,getApplicationContext());
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }

    void sendMessageToUser(String message){
        chatroomModel.setLastMessageStamp(Timestamp.now());
        chatroomModel.setLastMessageSenderId(FirebaseUtil.currentUserId());
        chatroomModel.setLastMessage(message);
        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);

        ChatMessageModel chatMessageModel  = new ChatMessageModel(message,FirebaseUtil.currentUserId(),Timestamp.now());

        FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    messageInput.setText("");
                    recyclerView.smoothScrollToPosition(0);
                } else {
                     Log.e("ChatActivity", "Send message failed", task.getException());
                }
            }
        });
        FirebaseUtil.allUserCollectionReference()
                .document(otherUser.getUserId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String token = documentSnapshot.getString("fcmToken");
                    if (token != null && !token.isEmpty()) {
                        FirebaseUtil.getCurrentUserName(name -> {
                            FCMHttpV1Sender.sendPushNotification(
                                    getApplicationContext(),
                                    token,
                                    "Tin nhắn mới từ " + name,
                                    message
                            );
                        });

                    }
                });

    }

    void getOrCreateChatRoomModel(){
        FirebaseUtil.getChatroomReference(chatroomId).get().addOnCompleteListener(task -> {
           if(task.isSuccessful()){
                chatroomModel = task.getResult().toObject(ChatroomModel.class);
                if(chatroomModel == null){
                    //first time chat will create chat room id with userId1 + _ + userId2
                    chatroomModel = new ChatroomModel(
                            chatroomId,
                            Arrays.asList(FirebaseUtil.currentUserId(),otherUser.getUserId()),
                            Timestamp.now(),
                            ""
                    );
                    FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);
                }
           }
        });
    }
}