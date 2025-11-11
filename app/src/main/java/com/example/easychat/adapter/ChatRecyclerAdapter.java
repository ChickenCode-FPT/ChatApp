package com.example.easychat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easychat.R;
import com.example.easychat.models.ChatMessageModel;
import com.example.easychat.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class ChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatMessageModel, ChatRecyclerAdapter.ChatModelViewHolder> {

    private final Context context;
    private final boolean isGroupChat;

    public ChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatMessageModel> options, Context context, boolean isGroupChat) {
        super(options);
        this.context = context;
        this.isGroupChat = isGroupChat;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatModelViewHolder holder, int position, @NonNull ChatMessageModel model) {
        boolean isMine = model.getSenderId().equals(FirebaseUtil.currentUserId());

        if (isMine) {
            holder.rightChatLayout.setVisibility(View.VISIBLE);
            holder.leftChatLayout.setVisibility(View.GONE);
            holder.rightChatView.setText(model.getMessage());
        } else {
            // Tin nhắn người khác → bên trái
            holder.leftChatLayout.setVisibility(View.VISIBLE);
            holder.rightChatLayout.setVisibility(View.GONE);

            holder.leftChatView.setText(model.getMessage());

            if (isGroupChat) {
                holder.leftChatSenderName.setVisibility(View.VISIBLE);
                // Lấy tên người gửi
                FirebaseUtil.getUserById(model.getSenderId())
                        .get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists()) {
                                String senderName = doc.getString("username");
                                holder.leftChatSenderName.setText(senderName != null ? senderName : "Unknown");
                            } else {
                                holder.leftChatSenderName.setText("Unknown");
                            }
                        });
            } else {
                // Chat cá nhân → ẩn tên
                holder.leftChatSenderName.setVisibility(View.GONE);
            }
        }
    }

    @NonNull
    @Override
    public ChatModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_message_recycler_row, parent, false);
        return new ChatModelViewHolder(view);
    }

    static class ChatModelViewHolder extends RecyclerView.ViewHolder {

        LinearLayout leftChatLayout, rightChatLayout;
        TextView leftChatView, rightChatView, leftChatSenderName;

        public ChatModelViewHolder(@NonNull View itemView) {
            super(itemView);
            leftChatLayout = itemView.findViewById(R.id.left_chat_layout);
            rightChatLayout = itemView.findViewById(R.id.right_chat_layout);
            leftChatView = itemView.findViewById(R.id.left_chat_textview);
            rightChatView = itemView.findViewById(R.id.right_chat_textview);
            leftChatSenderName = itemView.findViewById(R.id.left_chat_sender_name);
        }
    }
}
