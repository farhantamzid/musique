package com.beyond.musique;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class leaveReviewActivity extends AppCompatActivity {

    private Button submitReview;
    private EditText etReview;
    private RatingBar ratingBar;

    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;

    private String albumName;
    private String artistName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave_review);

        // Initialize Firestore and FirebaseAuth
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Retrieve extras
        albumName = getIntent().getStringExtra("albumName");
        artistName = getIntent().getStringExtra("artistName");

        // Bind UI elements
        submitReview = findViewById(R.id.submitReview);
        etReview = findViewById(R.id.etReview);
        ratingBar = findViewById(R.id.ratingBar);

        // Set click listener for submit button
        submitReview.setOnClickListener(v -> submitReview());
    }

    private void submitReview() {
        String reviewText = etReview.getText().toString().trim();
        float rating = ratingBar.getRating();

        if (reviewText.isEmpty() || rating == 0) {
            Toast.makeText(this, "Please provide a rating and review text.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            final String[] userName = {user.getDisplayName()};

            if (userName[0] == null || userName[0].isEmpty()) {
                // Retrieve user name from Firestore if not available from Google account
                firestore.collection("users").document(userId).get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                DocumentSnapshot document = task.getResult();
                                userName[0] = document.getString("name");
                                if (userName[0] != null && !userName[0].isEmpty()) {
                                    checkIfReviewExists(userId, userName[0], rating, reviewText);
                                } else {
                                    Toast.makeText(this, "Please set a display name in your profile.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(this, "Failed to retrieve user name.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                checkIfReviewExists(userId, userName[0], rating, reviewText);
            }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkIfReviewExists(String userId, String userName, float rating, String reviewText) {
        String albumId = albumName + "_" + artistName;

        firestore.collection("albums").document(albumId)
                .collection("reviews")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        Toast.makeText(this, "You have already reviewed this album.", Toast.LENGTH_SHORT).show();
                    } else {
                        saveReview(userId, userName, rating, reviewText);
                    }
                });
    }

    private void saveReview(String userId, String userName, float rating, String reviewText) {
        // Create review data
        Map<String, Object> review = new HashMap<>();
        review.put("userId", userId);
        review.put("userName", userName);
        review.put("rating", rating);
        review.put("reviewText", reviewText);

        // Create album ID by combining album name and artist name
        String albumId = albumName + "_" + artistName;

        // Save review to Firestore
        firestore.collection("albums").document(albumId)
                .collection("reviews").add(review)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(leaveReviewActivity.this, "Review submitted successfully.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(leaveReviewActivity.this, "Failed to submit review. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}