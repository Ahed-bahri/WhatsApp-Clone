package com.bahri.whatsappclone.view.activities.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.bahri.whatsappclone.R;
import com.bahri.whatsappclone.databinding.ActivityPhoneLoginBinding;
import com.bahri.whatsappclone.model.user.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private ActivityPhoneLoginBinding binding;
    private static String TAG = "PhoneLoginActivity";
    private boolean mVerificationInProgress = false;

    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private ProgressDialog progressDialog;

    private FirebaseUser firebaseUser;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding= DataBindingUtil.setContentView(this, R.layout.activity_phone_login);
        mAuth = FirebaseAuth.getInstance();
        firestore=FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser!=null){
            startActivity(new Intent(this,SetUserInfoActivity.class));
        }

        progressDialog= new ProgressDialog(this);
        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (binding.btnNext.getText().toString().equals("Next")) {
                    String phone = "+" + binding.edCodeCountry.getText().toString() + binding.edPhone.getText().toString();
                    startPhoneNumberVerification(phone);
                } else {
                    progressDialog.setMessage("Please wait while we verify your Token...");
                    progressDialog.show();
                    verifyPhoneNumberWithCode(mVerificationId, binding.edCode.getText().toString());
                }
            }
        });
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                Log.d(TAG, "onVerificationCompleted:" + phoneAuthCredential);
                mVerificationInProgress = false;
                signInWithPhoneAuthCredential(phoneAuthCredential);

                progressDialog.dismiss();
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.d(TAG,"onVerificationFailed "+e.getMessage());

                mVerificationInProgress = false;

                if (e instanceof FirebaseAuthInvalidCredentialsException) {

                    binding.edPhone.setError("Invalid phone number.");
                } else if (e instanceof FirebaseTooManyRequestsException) {

                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                            Snackbar.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                Log.d(TAG, "onCodeSent:" + verificationId);

                mVerificationId = verificationId;
                mResendToken = token;

                binding.btnNext.setText("Confirm");
                binding.edCode.setVisibility(View.VISIBLE);
                binding.edCodeCountry.setEnabled(false);
                binding.edPhone.setEnabled(false);

                progressDialog.dismiss();
            }
        };

    }

    private void startPhoneNumberVerification(String phoneNumber) {
        progressDialog.setMessage("Please wait while we verify your number : "+phoneNumber);
        progressDialog.show();
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

        mVerificationInProgress = true;
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            progressDialog.dismiss();
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();
                            startActivity(new Intent(PhoneLoginActivity.this, SetUserInfoActivity.class));

                            if (user != null) {
                                String userID= user.getUid();
                                Users users= new Users(userID,"",
                                        user.getPhoneNumber(),
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        "");

                                firestore.collection("Users")
                                        .document("UserInfo")
                                        .collection(userID)
                                        .add(users)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                startActivity(new Intent(PhoneLoginActivity.this, SetUserInfoActivity.class));
                                            }
                                        });
                            }else {
                                Toast.makeText(getApplicationContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();
                            }

                            startActivity(new Intent(PhoneLoginActivity.this, SetUserInfoActivity.class));

                        } else {
                            progressDialog.dismiss();
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Log.d(TAG, "onComplete: Invalid code.");

                            }

                        }
                    }
                });
    }

}


