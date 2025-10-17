package com.example.easychat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
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
import com.example.easychat.utils.CloudinaryUtil;
import com.example.easychat.utils.FirebaseUtil;
import com.github.dhaval2404.imagepicker.ImagePicker;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class ProfileFragment extends Fragment {

    ImageView profilePic;
    EditText usernameInput,phoneInput;
    Button updateProfileBtn;
    ProgressBar progressBar;
    TextView logoutBtn;


    UserModel currentUserModel;
    ActivityResultLauncher<Intent> imagePickerLaucher;
    Uri selectedImageUri;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePickerLaucher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result ->{
                    if(result.getResultCode() == ChatActivity.RESULT_OK){
                            Intent data = result.getData();
                            if(data !=null && data.getData() != null){
                                selectedImageUri = data.getData();
                                AndoirdUtil.setProfilePicture(getContext(),selectedImageUri,profilePic);
                                CloudinaryUtil.uploadImage(selectedImageUri, new CloudinaryUtil.UploadCallback() {
                                    @Override
                                    public void onSuccess(String imageUrl) {
                                        currentUserModel.setProfilePic(imageUrl);
                                        FirebaseUtil.currentUserDetails().update("profilePic", imageUrl);
                                        if (getActivity() != null) {
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    AndoirdUtil.showToast(getContext(), "Ảnh đã được cập nhật!");
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        if (getActivity() != null) {
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    AndoirdUtil.showToast(getContext(), "Lỗi upload ảnh: " + e.getMessage());
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                    }
                });
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

        profilePic.setOnClickListener((v) ->{
            ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512,512)
                    .createIntent(new Function1<Intent, Unit>() {
                        @Override
                        public Unit invoke(Intent intent) {
                            imagePickerLaucher.launch(intent);
                            return null;
                        }
                    });
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
            if (task.isSuccessful() && task.getResult() != null) {
                currentUserModel = task.getResult().toObject(UserModel.class);
                if (currentUserModel != null) {
                    usernameInput.setText(currentUserModel.getUsername());
                    phoneInput.setText(currentUserModel.getPhone());
                    if (currentUserModel.getProfilePic() != null && !currentUserModel.getProfilePic().isEmpty()) {
                        Uri profilePicUri = Uri.parse(currentUserModel.getProfilePic());
                        AndoirdUtil.setProfilePicture(getContext(), profilePicUri, profilePic);
                    }
                }
            }
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