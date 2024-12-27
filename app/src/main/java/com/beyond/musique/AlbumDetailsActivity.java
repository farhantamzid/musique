package com.beyond.musique;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AlbumDetailsActivity extends AppCompatActivity {

    private static final String TAG = "AlbumDetailsActivity";
    private static final String API_KEY = "8cde7eb19387aac387fa9c498131b5c8";

    ImageView imageViewAlbumArt;
    TextView albumNameTV;
    TextView artistNameTV;
    TextView albumDate;
    TextView genre;
    TextView tvRating;
    RatingBar ratingBar;
    RecyclerView recyclerViewTracks;
    ProgressBar loader;

    Button btnLeaveReview;
    Button btnViewReviews;

    CardView cardView8;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_details);

        imageViewAlbumArt = findViewById(R.id.imageViewAlbumArt);
        albumNameTV = findViewById(R.id.albumName);
        artistNameTV = findViewById(R.id.artistName);
        albumDate = findViewById(R.id.albumDate);
        genre = findViewById(R.id.genre);
        tvRating = findViewById(R.id.tvRating);
        ratingBar = findViewById(R.id.ratingBar);
        recyclerViewTracks = findViewById(R.id.recyclerViewTracks);
        btnLeaveReview = findViewById(R.id.btnLeaveReview);
        btnViewReviews = findViewById(R.id.btnViewReviews);
        cardView8 = findViewById(R.id.cardView8);
        loader = findViewById(R.id.loader);

        firestore = FirebaseFirestore.getInstance();

        btnLeaveReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AlbumDetailsActivity.this, leaveReviewActivity.class);
                intent.putExtra("albumName", albumNameTV.getText());
                intent.putExtra("artistName", artistNameTV.getText());
                startActivity(intent);
            }
        });

        btnViewReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AlbumDetailsActivity.this, ViewReviewsActivity.class);
                intent.putExtra("albumName", albumNameTV.getText());
                intent.putExtra("artistName", artistNameTV.getText());
                startActivity(intent);
            }
        });

        recyclerViewTracks.setLayoutManager(new LinearLayoutManager(this));

        String albumNameExtra = getIntent().getStringExtra("albumName");
        String artistNameExtra = getIntent().getStringExtra("artistName");

        if (albumNameExtra != null && artistNameExtra != null) {
            new FetchAlbumInfoTask(albumNameExtra, artistNameExtra).execute();
            fetchReviews(albumNameExtra, artistNameExtra);
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
                        int totalReviews = 0;
                        float totalRating = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            totalReviews++;
                            totalRating += document.getDouble("rating");
                        }

                        if (totalReviews > 0) {
                            float averageRating = totalRating / totalReviews;
                            ratingBar.setRating(averageRating);
                            tvRating.setText(String.format("%.1f out of 5 (%d reviews)", averageRating, totalReviews));
                        } else {
                            tvRating.setText("No reviews yet.");
                        }
                    } else {
                        Toast.makeText(AlbumDetailsActivity.this, "Failed to fetch reviews.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error getting reviews: ", task.getException());
                    }
                });
    }

    private class FetchAlbumInfoTask extends AsyncTask<Void, Void, AlbumDetails> {
        String albumName;
        String artistName;

        FetchAlbumInfoTask(String albumName, String artistName) {
            this.albumName = albumName;
            this.artistName = artistName;
        }

        @Override
        protected AlbumDetails doInBackground(Void... voids) {
            try {
                String url = "https://ws.audioscrobbler.com/2.0/?method=album.getinfo" +
                        "&api_key=" + API_KEY +
                        "&artist=" + artistName.replace(" ", "%20") +
                        "&album=" + albumName.replace(" ", "%20") +
                        "&format=json";

                String response = HttpRequest.executeGet(url);

                JSONObject jsonObject = new JSONObject(response);
                JSONObject albumObject = jsonObject.getJSONObject("album");

                String name = albumObject.getString("name");
                String artist = albumObject.getString("artist");
                int playCount = albumObject.getInt("playcount");
                List<Track> trackList = new ArrayList<>();
                if (albumObject.has("tracks")) {
                    Object trackObject = albumObject.getJSONObject("tracks").get("track");
                    if (trackObject instanceof JSONArray) {
                        JSONArray tracksArray = (JSONArray) trackObject;
                        for (int i = 0; i < tracksArray.length(); i++) {
                            JSONObject track = tracksArray.getJSONObject(i);
                            String trackName = track.getString("name");
                            String trackDuration = track.getString("duration");
                            trackList.add(new Track(trackName, trackDuration));
                        }
                    } else {
                        JSONObject track = (JSONObject) trackObject;
                        String trackName = track.getString("name");
                        String trackDuration = track.getString("duration");
                        trackList.add(new Track(trackName, trackDuration));
                    }
                }
                String coverArtUrl = "";
                JSONArray images = albumObject.getJSONArray("image");

                for (int i = 0; i < images.length(); i++) {
                    JSONObject imageObject = images.getJSONObject(i);
                    if ("extralarge".equals(imageObject.getString("size"))) {
                        coverArtUrl = imageObject.getString("#text");
                        break;
                    }
                }

                JSONArray tags;
                Object tagsObject = albumObject.get("tags");
                if (tagsObject instanceof JSONObject) {
                    tags = ((JSONObject) tagsObject).getJSONArray("tag");
                } else {
                    tags = new JSONArray(); // Empty array if tags is not a JSONObject
                }
                StringBuilder genresBuilder = new StringBuilder();
                for (int i = 0; i < tags.length(); i++) {
                    JSONObject tag = tags.getJSONObject(i);
                    genresBuilder.append(tag.getString("name"));
                    if (i < tags.length() - 1) {
                        genresBuilder.append(", ");
                    }
                }

                return new AlbumDetails(name, artist, coverArtUrl, genresBuilder.toString(), playCount, trackList);
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing album.getinfo JSON", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(AlbumDetails albumDetails) {
            super.onPostExecute(albumDetails);

            if (albumDetails != null) {
                albumNameTV.setText(albumDetails.name);
                artistNameTV.setText(albumDetails.artist);
                albumDate.setText("Play Count: " + albumDetails.playCount);
                genre.setText(albumDetails.genre);

                Glide.with(AlbumDetailsActivity.this)
                        .load(albumDetails.coverArtUrl)
                        .into(imageViewAlbumArt);

                TrackAdapter adapter = new TrackAdapter(AlbumDetailsActivity.this, albumDetails.trackList);
                recyclerViewTracks.setAdapter(adapter);
                loader.setVisibility(View.GONE);
                cardView8.setVisibility(View.VISIBLE);
            } else {
                albumNameTV.setText("N/A");
                artistNameTV.setText("N/A");
                albumDate.setText("Play Count: N/A");
                genre.setText("N/A");
                Log.e(TAG, "Failed to fetch album details.");
            }
        }
    }

    private static class AlbumDetails {
        String name;
        String artist;
        String coverArtUrl;
        String genre;
        int playCount;
        List<Track> trackList;

        AlbumDetails(String name, String artist, String coverArtUrl, String genre, int playCount, List<Track> trackList) {
            this.name = name;
            this.artist = artist;
            this.coverArtUrl = coverArtUrl;
            this.genre = genre;
            this.playCount = playCount;
            this.trackList = trackList;
        }
    }
}