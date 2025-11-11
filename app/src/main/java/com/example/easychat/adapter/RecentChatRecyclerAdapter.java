package com.example.easychat.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easychat.ChatActivity;
import com.example.easychat.R;
import com.example.easychat.models.ChatroomModel;
import com.example.easychat.models.UserModel;
import com.example.easychat.utils.AndoirdUtil;
import com.example.easychat.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;

import org.w3c.dom.Text;

public class RecentChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatroomModel, RecentChatRecyclerAdapter.ChatroomModelViewHolder> {


    Context context;

    public RecentChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatroomModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatroomModelViewHolder holder, int position, @NonNull ChatroomModel model) {
        boolean lastMessageSentByMe = model.getLastMessageSenderId() != null &&
                model.getLastMessageSenderId().equals(FirebaseUtil.currentUserId());

        if (model.getLastMessageStamp() != null) {
            holder.lastMessageTimeText.setText(FirebaseUtil.timestampToString(model.getLastMessageStamp()));
        } else {
            holder.lastMessageTimeText.setText("");
        }

        if (model.isGroup()) {

            holder.usernameText.setText(model.getGroupName() != null ? model.getGroupName() : "Unnamed Group");

            // Sửa phần hiển thị lastMessage
            String lastMessage = model.getLastMessage();
            if (lastMessageSentByMe) {
                // Tách phần nội dung thực tế, bỏ tên người gửi nếu có "Tên: message"
                int index = lastMessage.indexOf(":");
                String cleanMessage = index != -1 ? lastMessage.substring(index + 1).trim() : lastMessage;
                holder.lastMessageText.setText("You: " + cleanMessage);
            } else {
                holder.lastMessageText.setText(lastMessage);
            }

            if (model.getGroupAvatar() != null && !model.getGroupAvatar().isEmpty()) {
                Uri groupPicUri = Uri.parse(model.getGroupAvatar());
                AndoirdUtil.setProfilePicture(context, groupPicUri, holder.profilePic);
            } else {
                holder.profilePic.setImageResource(R.drawable.btn_rouded_corner);
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("chatroomId", model.getRoomId());
                intent.putExtra("isGroup", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            });

        } else {
            FirebaseUtil.getOtherUserFromChatroom(model.getUserIds())
                    .get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            UserModel otherUser = task.getResult().toObject(UserModel.class);
                            if (otherUser == null) return;

                            holder.usernameText.setText(otherUser.getUsername());
                            holder.lastMessageText.setText(
                                    lastMessageSentByMe ? "You: " + model.getLastMessage() : model.getLastMessage()
                            );

                            if (otherUser.getProfilePic() != null && !otherUser.getProfilePic().isEmpty()) {
                                Uri uri = Uri.parse(otherUser.getProfilePic());
                                AndoirdUtil.setProfilePicture(context, uri, holder.profilePic);
                            } else {
                                holder.profilePic.setImageResource(R.drawable.edit_text_rounded_corner);
                            }

                            holder.itemView.setOnClickListener(v -> {
                                Intent intent = new Intent(context, ChatActivity.class);
                                AndoirdUtil.passUserModelAsIntent(intent, otherUser);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            });
                        }
                    });
        }
    }

    @NonNull
    @Override
    public ChatroomModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row, parent, false);
        return new ChatroomModelViewHolder(view);
    }

    class ChatroomModelViewHolder extends RecyclerView.ViewHolder {

        TextView usernameText;
        TextView lastMessageText;

        TextView lastMessageTimeText;

        ImageView profilePic;

        public ChatroomModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.user_name_text);
            lastMessageText = itemView.findViewById(R.id.last_message_text);
            lastMessageTimeText = itemView.findViewById(R.id.last_message_time_text);
            profilePic = itemView.findViewById(R.id.profile_pic_image_view);

        }
    }
}
