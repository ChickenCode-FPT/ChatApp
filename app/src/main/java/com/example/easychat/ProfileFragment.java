package com.example.easychat;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.easychat.models.UserModel;
import com.example.easychat.utils.AndoirdUtil;
import com.example.easychat.utils.FirebaseUtil;

public class ProfileFragment extends Fragment {

    ImageView profilePic;
    EditText usernameInput,phoneInput;
    Button updateProfileBtn;
    ProgressBar progressBar;
    TextView logoutBtn;


    UserModel currentUserModel;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container,false);
        profilePic =   view.findViewById(R.id.profile_image_view);
        usernameInput = view.findViewById(R.id.profile_username);
        phoneInput = view.findViewById(R.id.profile_phone);
        progressBar = view.findViewById(R.id.profile_progress_bar);
        logoutBtn = view.findViewById(R.id.profile_logout_btn);
        updateProfileBtn =view.findViewById(R.id.profile_update_btn);
        getUserData();
        updateProfileBtn.setOnClickListener((v ->{
            updateBtnClick();
        }));

        logoutBtn.setOnClickListener((v)->{
            FirebaseUtil.logout();
            Intent intent =new Intent(getContext(),SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return  view;

    }

    void updateBtnClick(){
        String newUsername = usernameInput.getText().toString();
        if(newUsername.isEmpty() || newUsername.length() < 3){
            usernameInput.setError("Username lenght should ve at least 3 char");
            return;
        }
        currentUserModel.setUsername(newUsername);
        setInProgress(true);
        updateToFirestore();
    }

    void  updateToFirestore(){
        FirebaseUtil.currentUserDetails().set(currentUserModel).addOnCompleteListener(task -> {
            setInProgress(false);
           if(task.isSuccessful()){
               AndoirdUtil.showToast(getContext(),"Update successfully");
           } else {
               AndoirdUtil.showToast(getContext(),"Update failed");
           }
        });

    }

    void getUserData(){
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            setInProgress(false);
            currentUserModel = task.getResult().toObject(UserModel.class);
            usernameInput.setText(currentUserModel.getUsername());
            phoneInput.setText(currentUserModel.getPhone());
        });
    }

    void setInProgress(boolean inProgress){
        if(inProgress){
            progressBar.setVisibility(View.VISIBLE);
            updateProfileBtn.setVisibility(View.GONE);
        } else{
            progressBar.setVisibility(View.GONE);
            updateProfileBtn.setVisibility(View.VISIBLE);
        }
    }
}