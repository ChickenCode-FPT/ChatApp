package com.example.easychat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easychat.adapter.UserSelectAdapter;
import com.example.easychat.models.ChatroomModel;
import com.example.easychat.models.UserModel;
import com.example.easychat.utils.FirebaseUtil;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateGroupActivity extends AppCompatActivity {

    private RecyclerView userRecyclerView;
    private UserSelectAdapter adapter;
    private List<UserModel> userList = new ArrayList<>();

    private EditText groupNameInput;
    private Button createGroupBtn;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        db = FirebaseFirestore.getInstance();

        groupNameInput = findViewById(R.id.groupNameInput);
        createGroupBtn = findViewById(R.id.createGroupBtn);
        userRecyclerView = findViewById(R.id.userRecyclerView);

        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserSelectAdapter(userList);
        userRecyclerView.setAdapter(adapter);

        loadAllUsers();

        createGroupBtn.setOnClickListener(v -> createGroupChat());
    }

    private void loadAllUsers() {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        UserModel user = doc.toObject(UserModel.class);
                        // không hiển thị chính mình
                        if (!user.getUserId().equals(FirebaseUtil.currentUserId())) {
                            userList.add(user);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải danh sách người dùng", Toast.LENGTH_SHORT).show());
    }

    private void createGroupChat() {
        String groupName = groupNameInput.getText().toString().trim();
        if (groupName.isEmpty()) {
            Toast.makeText(this, "Nhập tên nhóm trước", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> selectedUserIds = adapter.getSelectedUserIds();
        if (selectedUserIds.isEmpty()) {
            Toast.makeText(this, "Chọn ít nhất 2 thành viên", Toast.LENGTH_SHORT).show();
            return;
        }

        // Thêm chính mình vào danh sách user
        selectedUserIds.add(FirebaseUtil.currentUserId());

        String roomId = UUID.randomUUID().toString();

        ChatroomModel groupRoom = new ChatroomModel(
                roomId,
                selectedUserIds,
                Timestamp.now(),
                FirebaseUtil.currentUserId()
        );
        groupRoom.setGroup(true);
        groupRoom.setGroupName(groupName);
        groupRoom.setGroupAvatar("https://cdn-icons-png.flaticon.com/512/9131/9131529.png");

        db.collection("chatrooms")
                .document(roomId)
                .set(groupRoom)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Tạo nhóm thành công!", Toast.LENGTH_SHORT).show();

                    // Mở ChatActivity của nhóm vừa tạo
                    Intent intent = new Intent(CreateGroupActivity.this, ChatActivity.class);
                    intent.putExtra("chatroomId", roomId);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Tạo nhóm thất bại", Toast.LENGTH_SHORT).show());
    }
}
