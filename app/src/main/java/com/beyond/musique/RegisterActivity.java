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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText editTextFullName;
    EditText editTextRegisterEmailAddress;
    EditText editTextRegisterPassword;
    Button buttonRegisterLayoutRegisterButton;

    FirebaseAuth mAuth;
    FirebaseFirestore firestore;

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

        // Initialize Firebase Authentication and Firestore
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Bind UI elements
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextRegisterEmailAddress = findViewById(R.id.editTextRegisterEmailAddress);
        editTextRegisterPassword = findViewById(R.id.editTextRegisterPassword);
        buttonRegisterLayoutRegisterButton = findViewById(R.id.buttonRegisterLayoutRegisterButton);

        // Register button click listener
        buttonRegisterLayoutRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    private void register() {
        String fullName = editTextFullName.getText().toString().trim();
        String email = editTextRegisterEmailAddress.getText().toString().trim();
        String password = editTextRegisterPassword.getText().toString().trim();

        // Validate inputs
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Register user with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                saveUserToFirestore(user.getUid(), fullName, email);
                            }
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserToFirestore(String userId, String fullName, String email) {
        // Prepare user data to save
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", fullName);
        userMap.put("email", email);

        // Save user details in Firestore
        firestore.collection("users").document(userId)
                .set(userMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User profile saved to Firestore");
                        Toast.makeText(RegisterActivity.this, "Account created successfully.", Toast.LENGTH_SHORT).show();
                        updateUI();
                    } else {
                        Log.e(TAG, "Error saving user to Firestore", task.getException());
                        Toast.makeText(RegisterActivity.this, "Failed to save user details. Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI() {
        // Navigate to MainActivity after successful registration
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}