package com.beyond.musique;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for viewing reviews of an album.
 */
public class ViewReviewsActivity extends AppCompatActivity {

    // Tag for logging
    private static final String TAG = "ViewReviewsActivity";

    // RecyclerView for displaying reviews
    private RecyclerView recyclerViewReviews;
    // Adapter for the RecyclerView
    private ReviewAdapter reviewAdapter;
    // List to store reviews
    private List<Review> reviewList;

    // Firestore instance
    private FirebaseFirestore firestore;

    // Album and artist names
    private String albumName;
    private String artistName;

    /**
     * Called when the activity is starting.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reviews);

        // Initialize RecyclerView and set its layout manager
        recyclerViewReviews = findViewById(R.id.recyclerViewReviews);
        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));

        // Initialize review list and adapter
        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(this, reviewList);
        recyclerViewReviews.setAdapter(reviewAdapter);

        // Initialize Firestore instance
        firestore = FirebaseFirestore.getInstance();

        // Get album and artist names from intent extras
        albumName = getIntent().getStringExtra("albumName");
        artistName = getIntent().getStringExtra("artistName");

        // Fetch reviews if album and artist names are available
        if (albumName != null && artistName != null) {
            fetchReviews(albumName, artistName);
        } else {
            Log.e(TAG, "Album name or artist name is missing in intent extras.");
        }
    }

    /**
     * Fetches reviews for the specified album and artist from Firestore.
     * @param albumName The name of the album.
     * @param artistName The name of the artist.
     */
    private void fetchReviews(String albumName, String artistName) {
        String albumId = albumName + "_" + artistName;

        // Fetch reviews from Firestore
        firestore.collection("albums").document(albumId).collection("reviews")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        reviewList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Review review = document.toObject(Review.class);
                            reviewList.add(review);
                        }
                        reviewAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(ViewReviewsActivity.this, "Failed to fetch reviews.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error getting reviews: ", task.getException());
                    }
                });
    }
}