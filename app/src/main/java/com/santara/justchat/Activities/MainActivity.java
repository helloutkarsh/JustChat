package com.santara.justchat.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.santara.justchat.Adapters.TopStatusAdapter;
import com.santara.justchat.Models.Status;
import com.santara.justchat.Models.UserStatus;
import com.santara.justchat.R;
import com.santara.justchat.Models.User;
import com.santara.justchat.Adapters.UsersAdapter;
import com.santara.justchat.databinding.ActivityMainBinding;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseDatabase database;

    ArrayList<User> users;
    ArrayList<UserStatus> userStatuses;

    UsersAdapter usersAdapter;
    TopStatusAdapter statusAdapter;


    ProgressDialog dialog;

   User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog = new ProgressDialog(this);
        dialog.setCancelable(true);
        dialog.setMessage("Uploading Status...");

        database = FirebaseDatabase.getInstance();
        users = new ArrayList<>();
        userStatuses = new ArrayList<>();

        database.getReference().child("users").child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user = snapshot.getValue(User.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        usersAdapter = new UsersAdapter(this, users);
        statusAdapter = new TopStatusAdapter(this,userStatuses);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);

        binding.statusList.setLayoutManager(linearLayoutManager);
        binding.statusList.setAdapter(statusAdapter);
        binding.recyclerView.setAdapter(usersAdapter);

        binding.recyclerView.showShimmerAdapter();

        database.getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for(DataSnapshot snapshot1: snapshot.getChildren()){
                    User user = snapshot1.getValue(User.class);
                    if(!user.getUserId().equals(FirebaseAuth.getInstance().getUid()))
                    users.add(user);

                }
                binding.recyclerView.hideShimmerAdapter();
                usersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference().child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    userStatuses.clear();
                    for(DataSnapshot storySnapshot : snapshot.getChildren()){
                        UserStatus status = new UserStatus();
                        status.setName(storySnapshot.child("name").getValue(String.class));
                        status.setProfileImage((storySnapshot.child("profileImage").getValue(String.class)));
                        status.setLastUpdated(storySnapshot.child("lastUpdated").getValue(Long.class));
                        userStatuses.add(status);

                        ArrayList<Status> statuses = new ArrayList<>();

                        for(DataSnapshot statusSnapshot : storySnapshot.child("statuses").getChildren()){
                            Status sampleStatus = statusSnapshot.getValue(Status.class);
                            statuses.add(sampleStatus);
                        }
                        status.setStatuses(statuses);
                    }
                    statusAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.bottomNavigationMenu.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch ((item.getItemId())){
                    case R.id.status:
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent,75);
                }
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data!=null){
            if(data.getData()!=null){
                dialog.show();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                Date date = new Date();
                StorageReference reference = storage.getReference().child("status").child(date.getTime()+"");

                reference.putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                UserStatus userStatus = new UserStatus();
                                userStatus.setName((user.getUserName()));
                                userStatus.setProfileImage(user.getUserDP());
                                userStatus.setLastUpdated(date.getTime());

                                HashMap<String, Object> obj = new HashMap<>();
                                obj.put("name",userStatus.getName());
                                obj.put("profileImage", userStatus.getProfileImage());
                                obj.put("lastUpdated",userStatus.getLastUpdated());


                                String imageUrl = uri.toString();
                                Status status = new Status(imageUrl, userStatus.getLastUpdated());

                                database.getReference().child("status")
                                        .child(FirebaseAuth.getInstance().getUid())
                                .updateChildren(obj);

                                database.getReference().child("status").child(FirebaseAuth.getInstance().getUid())
                                        .child("statuses")
                                        .push()
                                        .setValue(status);

                                dialog.dismiss();
                            }
                        });
                    }
                });
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentUser = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentUser).setValue("online");
    }

//    @Override
//    protected void onStop() {
//        String currentUser = FirebaseAuth.getInstance().getUid();
//        database.getReference().child("presence").child(currentUser).setValue("offline");
//        super.onStop();
//
//    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.search:
                Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
                break;
            case R.id.settings:
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                break;
            case R.id.invite:
                Toast.makeText(this, "Invite", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.topmenu,menu);
        return super.onCreateOptionsMenu(menu);
    }
}