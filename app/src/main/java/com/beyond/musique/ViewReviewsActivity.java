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

public class ViewReviewsActivity extends AppCompatActivity {

    private static final String TAG = "ViewReviewsActivity";

    private RecyclerView recyclerViewReviews;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;

    private FirebaseFirestore firestore;

    private String albumName;
    private String artistName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reviews);

        recyclerViewReviews = findViewById(R.id.recyclerViewReviews);
        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));

        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(this, reviewList);
        recyclerViewReviews.setAdapter(reviewAdapter);

        firestore = FirebaseFirestore.getInstance();

        albumName = getIntent().getStringExtra("albumName");
        artistName = getIntent().getStringExtra("artistName");

        if (albumName != null && artistName != null) {
            fetchReviews(albumName, artistName);
        } else {
            Log.e(TAG, "Album name or artist name is missing in intent extras.");
        }
    }

    private void fetchReviews(String albumName, String artistName) {
        String albumId = albumName + "_" + artistName;

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