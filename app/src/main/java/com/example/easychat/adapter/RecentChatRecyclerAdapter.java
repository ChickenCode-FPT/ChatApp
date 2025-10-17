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

    public RecentChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatroomModel> options,Context context) {
        super(options);
        this.context  = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatroomModelViewHolder holder, int position, @NonNull ChatroomModel model) {
            FirebaseUtil.getOtherUserFromChatroom(model.getUserIds())
                    .get().addOnCompleteListener(task -> {
                       if(task.isSuccessful()){
                           boolean lastMessageSentByMe = model.getLastMessageSenderId().equals(FirebaseUtil.currentUserId());

                           UserModel otheruserModel = task.getResult().toObject(UserModel.class);
                           if (otheruserModel.getProfilePic() != null && !otheruserModel.getProfilePic().isEmpty()) {
                               Uri profilePicUri = Uri.parse(otheruserModel.getProfilePic());
                               AndoirdUtil.setProfilePicture(context, profilePicUri, holder.profilePic);
                           }
                           holder.usernameText.setText(otheruserModel.getUsername());
                           if(lastMessageSentByMe){
                               holder.lastMessageText.setText("You: " + model.getLastMessage());
                           } else {
                               holder.lastMessageText.setText(model.getLastMessage());
                           }

                           holder.lastMessageTimeText.setText(FirebaseUtil.timestampToString(model.getLastMessageStamp()));

                           holder.itemView.setOnClickListener(v ->{
                               Intent intent = new Intent(context, ChatActivity.class);
                               AndoirdUtil.passUserModelAsIntent(intent,otheruserModel);
                               intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                               context.startActivity(intent);
                           });
                       }
                    });
    }

    @NonNull
    @Override
    public ChatroomModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row,parent,false);
        return new ChatroomModelViewHolder(view);
    }

    class ChatroomModelViewHolder extends RecyclerView.ViewHolder{

        TextView usernameText;
        TextView lastMessageText;

        TextView lastMessageTimeText;

        ImageView profilePic;

        public ChatroomModelViewHolder(@NonNull View itemView){
            super(itemView);
            usernameText = itemView.findViewById(R.id.user_name_text);
            lastMessageText = itemView.findViewById(R.id.last_message_text);
            lastMessageTimeText = itemView.findViewById(R.id.last_message_time_text);
            profilePic = itemView.findViewById(R.id.profile_pic_image_view);

        }
    }
}
