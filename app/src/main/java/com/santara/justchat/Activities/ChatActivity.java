package com.santara.justchat.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.santara.justchat.Adapters.MessageAdapter;
import com.santara.justchat.Models.Message;
import com.santara.justchat.R;
import com.santara.justchat.databinding.ActivityChatBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    MessageAdapter messageAdapter;
    ArrayList<Message> messages;

    String senderRoom, receiverRoom;

    FirebaseDatabase database;
    FirebaseStorage storage;

    ProgressDialog dialog;
    String senderUid, receiverUid;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();


        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending image...");
        dialog.setCancelable(false);

        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messages);
        binding.recyclerChatView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerChatView.setAdapter(messageAdapter);

        String name = getIntent().getStringExtra("name");
        String dp =  getIntent().getStringExtra("image");
         receiverUid = getIntent().getStringExtra("uid");
         senderUid = FirebaseAuth.getInstance().getUid();

         database.getReference().child("presence").child(receiverUid).addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                 if(snapshot.exists()){
                     String status = snapshot.getValue(String.class);
                     if(!status.isEmpty()){
                         binding.onlineStat.setText(status);
                     }
                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {

             }
         });


         binding.personName.setText(name);
         Glide.with(ChatActivity.this).load(dp)
                 .placeholder(R.drawable.adddp).into((binding.personChatDp));


         binding.imageView2.setOnClickListener(new View.OnClickListener(){

             @Override
             public void onClick(View v) {
                 finish();
             }
         });

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for(DataSnapshot snapshot1 : snapshot.getChildren()){
                            Message message = snapshot1.getValue(Message.class);
                            messages.add(message);
                        }
                        messageAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageTxt  = binding.messageToBe.getText().toString();

                Date date = new Date();

                Message message = new Message(messageTxt,senderUid ,date.getTime());
                binding.messageToBe.setText("");

                String randomKey = database.getReference().push().getKey();



                HashMap<String, Object> lastMsgObj = new HashMap<>();
                lastMsgObj.put("lastMsg",message.getMessage());
                lastMsgObj.put("lastMsgTime", date.getTime());
                database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                database.getReference().child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        database.getReference().child("chats")
                                .child(receiverRoom)
                                .child("messages")
                                .child(randomKey)
                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                            }
                        });

                    }
                });
            }
        });

        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 25);
            }
        });
          getSupportActionBar().setDisplayShowTitleEnabled(false);
//        getSupportActionBar().setTitle(name);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 25) {
            if(data!=null){
                if(data.getData()!=null){
                    Uri selectedImage = data.getData();
                    Calendar calendar = Calendar.getInstance();
                    StorageReference reference = storage.getReference().child("chats").child(calendar.getTimeInMillis()+"");
                    dialog.show();
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            dialog.dismiss();
                            if(task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String filePath = uri.toString();

                                        String messageTxt  = binding.messageToBe.getText().toString();

                                        Date date = new Date();

                                        Message message = new Message(messageTxt,senderUid ,date.getTime());
                                        message.setMessage("Image");
                                        message.setImageUrl(filePath);
                                        binding.messageToBe.setText("");

                                        String randomKey = database.getReference().push().getKey();



                                        HashMap<String, Object> lastMsgObj = new HashMap<>();
                                        lastMsgObj.put("lastMsg",message.getMessage());
                                        lastMsgObj.put("lastMsgTime", date.getTime());
                                        database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                                        database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                                        database.getReference().child("chats")
                                                .child(senderRoom)
                                                .child("messages")
                                                .child(randomKey)
                                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                database.getReference().child("chats")
                                                        .child(receiverRoom)
                                                        .child("messages")
                                                        .child(randomKey)
                                                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                    }
                                                });

                                            }
                                        });

                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        String currentUser = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentUser).setValue("online");
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}