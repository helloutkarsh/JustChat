package com.santara.justchat.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;


import com.google.firebase.auth.FirebaseAuth;
import com.santara.justchat.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    ActivityRegisterBinding binding;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();
        auth = FirebaseAuth.getInstance();

        if(auth.getCurrentUser()!=null){
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }


        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.newUserName.getText().toString();

                if(name.isEmpty()){
                    binding.newUserName.setError("Please Enter Name");
                    return;
                }

                String phone = binding.newUserPhone.getText().toString();

                if(phone.length() < 13){
                    binding.newUserPhone.setError("Enter Phone with country code");
                    return;
                }
                Intent intent = new Intent(RegisterActivity.this, otpActivationActivity.class);
                intent.putExtra("newUserPhone", binding.newUserPhone.getText().toString());
                intent.putExtra("newUserName", binding.newUserName.getText().toString());
                startActivity(intent);
            }
        });
    }
}
