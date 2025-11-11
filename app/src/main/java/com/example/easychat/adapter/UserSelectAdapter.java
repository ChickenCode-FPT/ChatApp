package com.example.easychat.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.easychat.R;
import com.example.easychat.models.UserModel;

import java.util.ArrayList;
import java.util.List;

public class UserSelectAdapter extends RecyclerView.Adapter<UserSelectAdapter.ViewHolder> {

    private List<UserModel> userList;
    private List<String> selectedIds = new ArrayList<>();

    public UserSelectAdapter(List<UserModel> userList) {
        this.userList = userList;
    }

    public List<String> getSelectedUserIds() {
        return selectedIds;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_select, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = userList.get(position);
        holder.username.setText(user.getUsername());
        holder.phone.setText(user.getPhone());

        Glide.with(holder.itemView.getContext())
                .load(user.getProfilePic())
                .placeholder(R.drawable.edit_text_rounded_corner)
                .circleCrop()
                .into(holder.avatar);

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                selectedIds.add(user.getUserId());
            else
                selectedIds.remove(user.getUserId());
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView username, phone;
        CheckBox checkBox;

        ViewHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.userAvatar);
            username = itemView.findViewById(R.id.userName);
            phone = itemView.findViewById(R.id.userPhone);
            checkBox = itemView.findViewById(R.id.userCheckBox);
        }
    }
}
