package com.beyond.musique;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity for leaving a review for an album.
 */
public class leaveReviewActivity extends AppCompatActivity {

    // UI elements
    private Button submitReview;
    private EditText etReview;
    private RatingBar ratingBar;

    // Firebase instances
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;

    // Album details
    private String albumName;
    private String artistName;

    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave_review);

        // Initialize Firebase instances
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Get album details from the intent
        albumName = getIntent().getStringExtra("albumName");
        artistName = getIntent().getStringExtra("artistName");

        // Initialize UI elements
        submitReview = findViewById(R.id.submitReview);
        etReview = findViewById(R.id.etReview);
        ratingBar = findViewById(R.id.ratingBar);

        // Set click listener for the submit button
        submitReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitReview(v);
            }
        });
    }

    /**
     * Submits the review to the Firestore database.
     * @param view The view that was clicked.
     */
    private void submitReview(View view) {
        // Get the review text and rating
        String reviewText = etReview.getText().toString().trim();
        float rating = ratingBar.getRating();

        // Check if the review text or rating is empty
        if (reviewText.isEmpty() || rating == 0) {
            Snackbar.make(view, "Please provide a rating and review text.", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Get the current user
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            final String[] userName = {user.getDisplayName()};

            // Check if the user name is null or empty
            if (userName[0] == null || userName[0].isEmpty()) {
                // Retrieve the user name from Firestore
                firestore.collection("users").document(userId).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(Task<DocumentSnapshot> task) {
                                if (task.isSuccessful() && task.getResult() != null) {
                                    DocumentSnapshot document = task.getResult();
                                    userName[0] = document.getString("name");
                                    if (userName[0] != null && !userName[0].isEmpty()) {
                                        checkIfReviewExists(userId, userName[0], rating, reviewText, view);
                                    } else {
                                        Snackbar.make(view, "Please set a display name in your profile.", Snackbar.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Snackbar.make(view, "Failed to retrieve user name.", Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                checkIfReviewExists(userId, userName[0], rating, reviewText, view);
            }
        } else {
            Snackbar.make(view, "User not logged in", Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Checks if a review already exists for the user.
     * @param userId The ID of the user.
     * @param userName The name of the user.
     * @param rating The rating given by the user.
     * @param reviewText The review text provided by the user.
     * @param view The view that was clicked.
     */
    private void checkIfReviewExists(String userId, String userName, float rating, String reviewText, View view) {
        // Create a unique album ID
        String albumId = albumName + "_" + artistName;

        // Query Firestore to check if a review already exists
        firestore.collection("albums").document(albumId)
                .collection("reviews")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                            Snackbar.make(view, "You have already reviewed this album.", Snackbar.LENGTH_SHORT).show();
                        } else {
                            saveReview(userId, userName, rating, reviewText, view);
                        }
                    }
                });
    }

    /**
     * Saves the review to the Firestore database.
     * @param userId The ID of the user.
     * @param userName The name of the user.
     * @param rating The rating given by the user.
     * @param reviewText The review text provided by the user.
     * @param view The view that was clicked.
     */
    private void saveReview(String userId, String userName, float rating, String reviewText, View view) {
        // Create a map to store the review details
        Map<String, Object> review = new HashMap<>();
        review.put("userId", userId);
        review.put("userName", userName);
        review.put("rating", rating);
        review.put("reviewText", reviewText);

        // Create a unique album ID
        String albumId = albumName + "_" + artistName;

        // Save the review to Firestore
        firestore.collection("albums").document(albumId)
                .collection("reviews").add(review)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            if (view != null) {
                                Snackbar.make(view, "Review submitted successfully.", Snackbar.LENGTH_SHORT).show();
                            }
                            finish();
                        } else {
                            if (view != null) {
                                Snackbar.make(view, "Failed to submit review. Please try again.", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
}