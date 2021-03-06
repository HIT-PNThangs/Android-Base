package com.example.hit.pnt.sendotpfirebase;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class VerifyPhoneNumberActivity extends AppCompatActivity {

    private static final String TAG = VerifyPhoneNumberActivity.class.getName();

    private EditText inputPhoneNumber;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone_number);

        setTitleToolbar();

        inputPhoneNumber = findViewById(R.id.inputPhoneNumber);
        Log.d("NumberPhone", inputPhoneNumber.getText().toString());

        Button verifyPhoneNumber = findViewById(R.id.btnVerifyPhoneNumber);

        mAuth = FirebaseAuth.getInstance();

        verifyPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strPhoneNumber = inputPhoneNumber.getText().toString().trim();

                if(strPhoneNumber.length() != 12) {
                    Toast.makeText(VerifyPhoneNumberActivity.this, "Wrong Number Phone", Toast.LENGTH_SHORT).show();
                } else {
                    onClickVerifyPhoneNumber(strPhoneNumber);
                }
            }
        });
    }

    private void setTitleToolbar() {
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Verify Phone Number");
        }
    }

    private void onClickVerifyPhoneNumber(String str) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)  // -> truy???n v??o 1 Firebast Auth
                        .setPhoneNumber(str) // -> truy???n v??o  s??? ??i???n tho???i
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                // ???????c g???i trong 2 TH:
                                // 1: X??c minh ???????c ngay t???c thi
                                // 2: T??? ?????ng truy xu???t: google play c?? th??? t??? ?????ng ph??t hi???n SMS x??c
                                // minh v?? t??? ?????ng x??c minh m?? ko c???n h??nh ?????ng c???a ng?????i d??ng

                                signInWithPhoneAuthCredential(phoneAuthCredential);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(VerifyPhoneNumberActivity.this,
                                        "Verification Failed", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                super.onCodeSent(verificationId, forceResendingToken);

                                gotoEnterOTPActivity(str, verificationId);
                            }
                        })
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.e(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            // Update UI

                            gotoMainActivity(user.getPhoneNumber());
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(VerifyPhoneNumberActivity.this,
                                        "The verification code enter was invalid ", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void gotoMainActivity(String str) {
        Intent intent = new Intent(this, MainActivity.class);

        intent.putExtra("phone_number", str);

        startActivity(intent);
    }

    private void gotoEnterOTPActivity(String str, String verificationId) {
        Intent intent = new Intent(this, EnterOTPActivity.class);

        intent.putExtra("phone_number", str);
        intent.putExtra("verificationID", verificationId);

        startActivity(intent);
    }
}