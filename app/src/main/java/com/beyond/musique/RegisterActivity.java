package com.beyond.musique;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    EditText editTextFullName;
    EditText editTextRegisterEmailAddress;

    EditText editTextRegisterPassword;
    Button buttonRegisterLayoutRegisterButton;

    FirebaseAuth mAuth;

    private final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registerLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextRegisterEmailAddress = findViewById(R.id.editTextRegisterEmailAddress);
        editTextRegisterPassword = findViewById(R.id.editTextRegisterPassword);
        buttonRegisterLayoutRegisterButton = findViewById(R.id.buttonRegisterLayoutRegisterButton);
        buttonRegisterLayoutRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                register();

            }
        });
    }

    private void register() {
        String email = editTextRegisterEmailAddress.getText().toString().trim();
        String password = editTextRegisterPassword.getText().toString().trim();
        
        if (!email.isEmpty() || !password.isEmpty()){
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                Toast.makeText(RegisterActivity.this, "Account created succesfully. Signing you in!", Toast.LENGTH_SHORT).show();
                                FirebaseUser user = mAuth.getCurrentUser();
                                updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                updateUI(null);
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "Please enter a valid email/password.", Toast.LENGTH_SHORT).show();
        }


    }

    private void updateUI(Object o) {
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
    }


}