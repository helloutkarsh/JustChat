package com.santara.justchat.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mukesh.OnOtpCompletionListener;
import com.santara.justchat.Models.UserSetup;
import com.santara.justchat.databinding.ActivityOtpActivationBinding;

import java.util.concurrent.TimeUnit;

public class otpActivationActivity extends AppCompatActivity {

    ActivityOtpActivationBinding binding;
    FirebaseAuth auth;

    String verificationId;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpActivationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending OTP...");
        dialog.setCancelable(false);
        dialog.show();

        binding.otpView.requestFocus();
        auth = FirebaseAuth.getInstance();

        String newUserPhone = getIntent().getStringExtra("newUserPhone");
        String newUserName = getIntent().getStringExtra("newUserName");
        Intent intent = new Intent(otpActivationActivity.this, UserSetup.class);

        intent.putExtra("newUserName",newUserName);

        binding.otpNum.setText("OTP sent to "+ newUserPhone);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(newUserPhone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(otpActivationActivity.this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {

                    }

                    @Override
                    public void onCodeSent(@NonNull String verifyId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        Toast.makeText(otpActivationActivity.this, "OTP Sent Successfully", Toast.LENGTH_SHORT).show();
                        super.onCodeSent(verifyId, forceResendingToken);
                        verificationId = verifyId;
                        dialog.dismiss();
                        InputMethodManager inputMethodManager =(InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                        binding.otpView.requestFocus();
                    }
                }).build();



        PhoneAuthProvider.verifyPhoneNumber(options);
        binding.otpView.setOtpCompletionListener(new OnOtpCompletionListener() {
            @Override
            public void onOtpCompleted(String otp) {
                 PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId,otp);
                 auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                     @Override
                     public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(otpActivationActivity.this, "Successful", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(otpActivationActivity.this, UserSetup.class);
                            String newUserName = getIntent().getStringExtra("newUserName");
                            intent.putExtra("newUserName",newUserName);
                            startActivity(intent);
                            finishAffinity();
                        }
                        else {
                            Toast.makeText(otpActivationActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                        }
                     }
                 });
            }
        });
    }
}