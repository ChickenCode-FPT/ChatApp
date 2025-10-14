package com.example.easychat;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easychat.adapter.SearchUserRecyclerAdapter;
import com.example.easychat.models.UserModel;
import com.example.easychat.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.auth.User;

public class SearchUserActivity extends AppCompatActivity {

    EditText searchInput;
    ImageButton searchButton;
    ImageButton backButton;
    RecyclerView recyclerView;
    SearchUserRecyclerAdapter adapter;

    private Handler handler = new Handler();
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        searchInput = findViewById(R.id.search_username_input);
        searchButton  = findViewById(R.id.search_user_btn);
        backButton = findViewById(R.id.back_btn);
        recyclerView = findViewById(R.id.search_user_recycleview);

        searchInput.requestFocus();

        backButton.setOnClickListener( v ->{
            getOnBackPressedDispatcher().onBackPressed();
        });
        setupRecyclerView();
       searchInput.addTextChangedListener(new TextWatcher() {
           @Override
           public void afterTextChanged(Editable s) {
               String searchTerm = s.toString().trim();
               if (searchRunnable != null) {
                   handler.removeCallbacks(searchRunnable);
               }
               searchRunnable = () -> performSearch(searchTerm);
               handler.postDelayed(searchRunnable, 300);
           }

           @Override
           public void beforeTextChanged(CharSequence s, int start, int count, int after) {

           }

           @Override
           public void onTextChanged(CharSequence s, int start, int before, int count) {
           }
       });
    }


    void setupRecyclerView() {
        Query query = FirebaseUtil.allUserCollectionReference().whereEqualTo("username", "non_existent_user");

        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                .setQuery(query, UserModel.class).build();
        adapter = new SearchUserRecyclerAdapter(options, getApplicationContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    void performSearch(String searchTerm) {
        Query query;
        if (searchTerm.isEmpty()) {
            // Nếu không có nội dung tìm kiếm, query một cái gì đó không tồn tại
            query = FirebaseUtil.allUserCollectionReference().whereEqualTo("username", "non_existent_user");
        } else {
            query = FirebaseUtil.allUserCollectionReference()
                    .orderBy("username")
                    .startAt(searchTerm)
                    .endAt(searchTerm + "\uf8ff");
        }

        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                .setQuery(query, UserModel.class).build();

        adapter.updateOptions(options);
    }


//    void setupSearchRecyclerView(String searchTerm){
//        if (adapter != null) {
//            adapter.stopListening();
//        }
//        if (searchTerm.isEmpty()) {
//            recyclerView.setAdapter(null);
//            return;
//        }
//        Query query = FirebaseUtil.allUserCollectionReference()
//                .orderBy("username")
//                .startAt(searchTerm)
//                .endAt(searchTerm + "\uf8ff");
//
//            FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
//                    .setQuery(query,UserModel.class).build();
//
//            adapter = new SearchUserRecyclerAdapter(options,getApplicationContext());
//            recyclerView.setLayoutManager(new LinearLayoutManager(this));
//            recyclerView.setAdapter(adapter);
//            adapter.startListening();
//    }

    @Override
    protected void onStart() {
        super.onStart();
        if(adapter != null){
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(adapter !=null){
            adapter.stopListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adapter != null){
            adapter.startListening();
        }
    }
}