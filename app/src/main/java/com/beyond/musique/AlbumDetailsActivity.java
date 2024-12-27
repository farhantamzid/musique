package com.beyond.musique;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AlbumDetailsActivity extends AppCompatActivity {

    private static final String TAG = "AlbumDetailsActivity";
    private static final String API_KEY = "8cde7eb19387aac387fa9c498131b5c8";

    ImageView imageViewAlbumArt;
    TextView albumNameTV;
    TextView artistNameTV;
    TextView albumDate;
    TextView genre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_details);

        imageViewAlbumArt = findViewById(R.id.imageViewAlbumArt);
        albumNameTV = findViewById(R.id.albumName);
        artistNameTV = findViewById(R.id.artistName);
        albumDate = findViewById(R.id.albumDate);
        genre = findViewById(R.id.genre);

        // Get album and artist name from intent extras
        String albumNameExtra = getIntent().getStringExtra("albumName");
        String artistNameExtra = getIntent().getStringExtra("artistName");

        if (albumNameExtra != null && artistNameExtra != null) {
            new FetchAlbumInfoTask(albumNameExtra, artistNameExtra).execute();
        } else {
            Log.e(TAG, "Album name or artist name is missing in intent extras.");
        }
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
                // Build API URL
                String url = "https://ws.audioscrobbler.com/2.0/?method=album.getinfo" +
                        "&api_key=" + API_KEY +
                        "&artist=" + artistName.replace(" ", "%20") +
                        "&album=" + albumName.replace(" ", "%20") +
                        "&format=json";

                // Make HTTP request
                String response = HttpRequest.executeGet(url);

                // Parse JSON response
                JSONObject jsonObject = new JSONObject(response);
                JSONObject albumObject = jsonObject.getJSONObject("album");

                String name = albumObject.getString("name");
                String artist = albumObject.getString("artist");
                int totalTracks = 0;
                if (albumObject.has("tracks")) {
                    Object trackObject = albumObject.getJSONObject("tracks").get("track");
                    if (trackObject instanceof JSONArray) {
                        totalTracks = ((JSONArray) trackObject).length();
                    } else {
                        totalTracks = 1; // Single track
                    }
                }
                String coverArtUrl = "";
                JSONArray images = albumObject.getJSONArray("image");

                // Get cover art URL
                for (int i = 0; i < images.length(); i++) {
                    JSONObject imageObject = images.getJSONObject(i);
                    if ("extralarge".equals(imageObject.getString("size"))) {
                        coverArtUrl = imageObject.getString("#text");
                        break;
                    }
                }

                // Get genres
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

                return new AlbumDetails(name, artist, coverArtUrl, genresBuilder.toString(), totalTracks);
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing album.getinfo JSON", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(AlbumDetails albumDetails) {
            super.onPostExecute(albumDetails);

            if (albumDetails != null) {
                // Populate the views with fetched data
                albumNameTV.setText(albumDetails.name);
                artistNameTV.setText(albumDetails.artist);
                albumDate.setText("Total Tracks: " + albumDetails.totalTracks);
                genre.setText(albumDetails.genre);

                Glide.with(AlbumDetailsActivity.this)
                        .load(albumDetails.coverArtUrl)
                        .into(imageViewAlbumArt);
            } else {
                // Set default values if album details are not available
                albumNameTV.setText("N/A");
                artistNameTV.setText("N/A");
                albumDate.setText("Total Tracks: N/A");
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
        int totalTracks;

        AlbumDetails(String name, String artist, String coverArtUrl, String genre, int totalTracks) {
            this.name = name;
            this.artist = artist;
            this.coverArtUrl = coverArtUrl;
            this.genre = genre;
            this.totalTracks = totalTracks;
        }
    }
}