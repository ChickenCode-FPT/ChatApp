package com.example.easychat;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.easychat.adapter.ChatRecyclerAdapter;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

import java.util.Arrays;

public class ChatActivity extends AppCompatActivity {

    private UserModel otherUser;
    private ChatroomModel chatroomModel;
    private ChatRecyclerAdapter adapter;
    private EditText messageInput;
    private ImageButton sendMessageBtn;
    private ImageButton backBtn;
    private TextView otherUsername;
    private String chatroomId;
    private RecyclerView recyclerView;
    private ImageView imageView;

    private static final String TAG = "ChatActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Khởi tạo các View
        initViews();

        // Xử lý nút Back
        backBtn.setOnClickListener(v -> onBackPressed());

        // Lấy thông tin người dùng/phòng chat
        loadChatData();

        // Xử lý nút gửi tin nhắn
        sendMessageBtn.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (message.isEmpty()) return;

            if (chatroomModel != null && chatroomModel.isGroup()) {
                sendMessageToRoom(message);
            } else {
                sendMessageToUser(message);
            }
        });

        getOrCreateChatRoomModel();
        setupChatRecyclerView();
    }

    /**
     * Khởi tạo các thành phần View từ layout.
     */
    private void initViews() {
        messageInput = findViewById(R.id.chat_message_input);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        backBtn = findViewById(R.id.back_btn);
        otherUsername = findViewById(R.id.other_username);
        recyclerView = findViewById(R.id.recycler_chat);
        imageView = findViewById(R.id.profile_pic_image_view);
    }

    /**
     * Tải dữ liệu phòng chat/người dùng khác dựa trên Intent.
     */
    private void loadChatData() {
        chatroomId = getIntent().getStringExtra("chatroomId");

        if (chatroomId != null) {
            // Đây là chat nhóm (đã có chatroomId)
            FirebaseUtil.getChatroomReference(chatroomId).get()
                    .addOnSuccessListener(snapshot -> {
                        chatroomModel = snapshot.toObject(ChatroomModel.class);
                        if (chatroomModel == null) return;

                        if (chatroomModel.isGroup()) {
                            otherUsername.setText(chatroomModel.getGroupName());
                            Glide.with(this)
                                    .load(chatroomModel.getGroupAvatar())
                                    .circleCrop()
                                    .placeholder(R.drawable.icon_chat)
                                    .error(R.drawable.icon_chat)
                                    .into(imageView);
                        }
                    });
        } else {
            // Đây là chat 1-1 (lấy thông tin người dùng khác từ Intent)
            otherUser = AndoirdUtil.getUserModelFromIntent(getIntent());
            if (otherUser == null) {
                Log.e(TAG, "UserModel is null, cannot proceed.");
                return;
            }

            chatroomId = FirebaseUtil.getChatroomnId(FirebaseUtil.currentUserId(), otherUser.getUserId());
            otherUsername.setText(otherUser.getUsername());

            // Tải ảnh đại diện người dùng khác
            if (otherUser.getProfilePic() != null && !otherUser.getProfilePic().isEmpty()) {
                Glide.with(this)
                        .load(otherUser.getProfilePic())
                        .circleCrop()
                        .placeholder(R.drawable.icon_chat)
                        .error(R.drawable.icon_chat)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.icon_chat);
            }
        }
    }

    /**
     * Thiết lập RecyclerView để hiển thị tin nhắn.
     */
    private void setupChatRecyclerView() {
        if (chatroomId == null) return;

        Query query = FirebaseUtil.getChatroomMessageReference(chatroomId)
                .orderBy("timesStamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatMessageModel> options = new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query, ChatMessageModel.class).build();

        boolean isGroup = chatroomModel != null && chatroomModel.isGroup();
        adapter = new ChatRecyclerAdapter(options, getApplicationContext(), isGroup);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        adapter.startListening();

        // Cuộn xuống tin nhắn mới nhất
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }

    /**
     * Gửi tin nhắn trong cuộc trò chuyện 1-1.
     * @param message Nội dung tin nhắn.
     */
    private void sendMessageToUser(String message) {
        if (chatroomModel == null || otherUser == null) {
            Log.e(TAG, "ChatroomModel or OtherUser is null, cannot send message.");
            return;
        }

        // Cập nhật Chatroom Model
        chatroomModel.setLastMessageStamp(Timestamp.now());
        chatroomModel.setLastMessageSenderId(FirebaseUtil.currentUserId());
        chatroomModel.setLastMessage(message);
        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);

        // Thêm tin nhắn vào Collection
        ChatMessageModel chatMessageModel = new ChatMessageModel(message, FirebaseUtil.currentUserId(), Timestamp.now());
        FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                messageInput.setText("");
                recyclerView.smoothScrollToPosition(0);
                sendNotification(message); // Gửi thông báo sau khi gửi tin nhắn thành công
            } else {
                Log.e(TAG, "Send message failed", task.getException());
            }
        });
    }

    /**
     * Gửi thông báo đẩy (Push Notification) đến người nhận.
     * @param message Nội dung tin nhắn.
     */
    private void sendNotification(String message) {
        if (otherUser == null) return;

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
                                    "Tin nhắn mới từ " + name,  // title
                                    message,                     // body
                                    FirebaseUtil.currentUserId() // senderId
                            );
                        });
                    }
                });
    }


    /**
     * Lấy hoặc tạo mới ChatroomModel nếu chưa tồn tại (chỉ áp dụng cho chat 1-1).
     */
    private void getOrCreateChatRoomModel() {
        if (chatroomId == null) return;

        FirebaseUtil.getChatroomReference(chatroomId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                chatroomModel = task.getResult().toObject(ChatroomModel.class);

                // Nếu là nhóm hoặc đã tồn tại, không cần tạo mới
                if (chatroomModel != null && chatroomModel.isGroup()) return;

                if (chatroomModel == null && otherUser != null) {
                    // Lần đầu chat 1-1 -> Tạo mới Chatroom Model
                    chatroomModel = new ChatroomModel(
                            chatroomId,
                            Arrays.asList(FirebaseUtil.currentUserId(), otherUser.getUserId()),
                            Timestamp.now(),
                            ""
                    );
                    FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);
                }
            } else {
                Log.e(TAG, "Failed to get chatroom model", task.getException());
            }
        });
    }

    /**
     * Gửi tin nhắn trong phòng chat nhóm.
     * @param message Nội dung tin nhắn.
     */
    void sendMessageToRoom(String message) {
        FirebaseUtil.getCurrentUserName(name -> {
            String displayMessage = name + ": " + message; // Thêm tên người gửi
            chatroomModel.setLastMessage(displayMessage);
            chatroomModel.setLastMessageSenderId(FirebaseUtil.currentUserId());
            chatroomModel.setLastMessageStamp(Timestamp.now());

            FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);

            ChatMessageModel chatMessage = new ChatMessageModel(message, FirebaseUtil.currentUserId(), Timestamp.now());
            FirebaseUtil.getChatroomMessageReference(chatroomId)
                    .add(chatMessage)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            messageInput.setText("");
                            recyclerView.smoothScrollToPosition(0);

                            // Gửi thông báo đến tất cả thành viên trừ mình
                            for (String memberId : chatroomModel.getUserIds()) {
                                if (!memberId.equals(FirebaseUtil.currentUserId())) {
                                    FirebaseUtil.allUserCollectionReference()
                                            .document(memberId)
                                            .get()
                                            .addOnSuccessListener(documentSnapshot -> {
                                                String token = documentSnapshot.getString("fcmToken");
                                                if (token != null && !token.isEmpty()) {
                                                    FCMHttpV1Sender.sendPushNotification(
                                                            getApplicationContext(),
                                                            token,
                                                            chatroomModel.getGroupName(),
                                                            displayMessage,
                                                            FirebaseUtil.currentUserId()
                                                    );
                                                }
                                            });
                                }
                            }
                        }
                    });
        });
    }


}