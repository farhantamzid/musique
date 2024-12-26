package com.beyond.musique;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import com.bumptech.glide.Glide;

public class Popular extends Fragment {

    private static List<Album> albumList = null;


    TextView tv1name, tv1artist, tv2name, tv2artist, tv3name, tv3artist, tv4name, tv4artist, tv5name, tv5artist, tv6name, tv6artist,textView3;
    ImageView imageView1, imageView2, imageView3, imageView4, imageView5, imageView6;

    ProgressBar progressBar;

    CardView cardView1, cardView2, cardView3, cardView4, cardView5, cardView6;

    private static final String TAG = "PopularFragment";
    private static final String API_KEY = "8cde7eb19387aac387fa9c498131b5c8";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_popular, container, false);

        if (getActivity() != null) {
            getActivity().getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }

        // Enable edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        tv1name = rootView.findViewById(R.id.tv1name);
        tv1artist = rootView.findViewById(R.id.tv1artist);
        tv2name = rootView.findViewById(R.id.tv2name);
        tv2artist = rootView.findViewById(R.id.tv2artist);
        tv3name = rootView.findViewById(R.id.tv3name);
        tv3artist = rootView.findViewById(R.id.tv3artist);
        tv4name = rootView.findViewById(R.id.tv4name);
        tv4artist = rootView.findViewById(R.id.tv4artist);
        tv5name = rootView.findViewById(R.id.tv5name);
        tv5artist = rootView.findViewById(R.id.tv5artist);
        tv6name = rootView.findViewById(R.id.tv6name);
        tv6artist = rootView.findViewById(R.id.tv6artist);
        imageView1 = rootView.findViewById(R.id.imageView1);
        imageView2 = rootView.findViewById(R.id.imageView2);
        imageView3 = rootView.findViewById(R.id.imageView3);
        imageView4 = rootView.findViewById(R.id.imageView4);
        imageView5 = rootView.findViewById(R.id.imageView5);
        imageView6 = rootView.findViewById(R.id.imageView6);
        progressBar = rootView.findViewById(R.id.loader);
        cardView1 = rootView.findViewById(R.id.popCard1);
        cardView2 = rootView.findViewById(R.id.popCard2);
        cardView3 = rootView.findViewById(R.id.popCard3);
        cardView4 = rootView.findViewById(R.id.popCard4);
        cardView5 = rootView.findViewById(R.id.popCard5);
        cardView6 = rootView.findViewById(R.id.popCard6);
        textView3 = rootView.findViewById(R.id.textView3);




        // Fetch album details if the album list is empty (i.e., not already cached)
        if (albumList == null) {
            new FetchAlbumDetailsTask().execute();
        } else {
            // If the album list is cached, populate the views directly
            populateViews(albumList);
        }




        return rootView;
    }


    private void populateViews(List<Album> albumList) {
        for (int i = 0; i < albumList.size(); i++) {
            Album album = albumList.get(i);
            switch (i) {
                case 0:
                    tv1name.setText(album.albumName);
                    tv1artist.setText(album.artistName);
                    Glide.with(getContext()).load(album.coverArtUrl).into(imageView1);
                    break;
                case 1:
                    tv2name.setText(album.albumName);
                    tv2artist.setText(album.artistName);
                    Glide.with(getContext()).load(album.coverArtUrl).into(imageView2);
                    break;
                case 2:
                    tv3name.setText(album.albumName);
                    tv3artist.setText(album.artistName);
                    Glide.with(getContext()).load(album.coverArtUrl).into(imageView3);
                    break;
                case 3:
                    tv4name.setText(album.albumName);
                    tv4artist.setText(album.artistName);
                    Glide.with(getContext()).load(album.coverArtUrl).into(imageView4);
                    break;
                case 4:
                    tv5name.setText(album.albumName);
                    tv5artist.setText(album.artistName);
                    Glide.with(getContext()).load(album.coverArtUrl).into(imageView5);
                    break;
                case 5:
                    tv6name.setText(album.albumName);
                    tv6artist.setText(album.artistName);
                    Glide.with(getContext()).load(album.coverArtUrl).into(imageView6);
                    break;
                default:
                    break;
            }
        }
    }

    private class FetchAlbumDetailsTask extends AsyncTask<Void, Void, List<Album>> {

        @Override
        protected List<Album> doInBackground(Void... voids) {



            List<Album> newAlbumList = new ArrayList<>();

            try {
                // Step 1: Fetch top 9 tracks
                String topTracksResponse = HttpRequest.executeGet(
                        String.format(
                                "https://ws.audioscrobbler.com/2.0/?method=chart.getTopTracks&limit=6&api_key=%s&format=json",
                                API_KEY
                        )
                );

                JSONObject jsonResponse = new JSONObject(topTracksResponse);
                JSONArray trackArray = jsonResponse.getJSONObject("tracks").getJSONArray("track");

                // Step 2: Loop through the tracks and fetch album details
                for (int i = 0; i < trackArray.length(); i++) {
                    JSONObject trackObject = trackArray.getJSONObject(i);
                    String trackName = trackObject.getString("name");
                    String artistName = trackObject.getJSONObject("artist").getString("name");

                    // Fetch album details for this track
                    Album album = fetchAlbumDetails(trackName, artistName);
                    if (album != null) {
                        newAlbumList.add(album);
                    }
                }
                albumList = newAlbumList;
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing top tracks JSON", e);
            }


            albumList = newAlbumList;  // Cache the fetched data
            return albumList;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            textView3.setVisibility(View.GONE);
            cardView1.setVisibility(View.GONE);
            cardView2.setVisibility(View.GONE);
            cardView3.setVisibility(View.GONE);
            cardView4.setVisibility(View.GONE);
            cardView5.setVisibility(View.GONE);
            cardView6.setVisibility(View.GONE);

            progressBar.setVisibility(View.VISIBLE);




        }


        @Override
        protected void onPostExecute(List<Album> albumList) {
            super.onPostExecute(albumList);

            if (albumList != null && !albumList.isEmpty()) {
                Log.d(TAG, "All album details fetched: " + albumList);
                populateViews(albumList);


                progressBar.setVisibility(View.GONE);
                textView3.setVisibility(View.VISIBLE);
                cardView1.setVisibility(View.VISIBLE);
                cardView2.setVisibility(View.VISIBLE);
                cardView3.setVisibility(View.VISIBLE);
                cardView4.setVisibility(View.VISIBLE);
                cardView5.setVisibility(View.VISIBLE);
                cardView6.setVisibility(View.VISIBLE);



            } else {
                Log.e(TAG, "No album details fetched");
            }
        }


        private Album fetchAlbumDetails(String trackName, String artistName) {
            try {
                // First, attempt to search with both track name and artist name
                Album album = searchAlbum(trackName + " " + artistName);

                // If no album is found, retry with only the artist name
                if (album == null) {
                    Log.d(TAG, "Retrying with only artist name: " + artistName);
                    album = searchAlbum(artistName);
                }

                return album;
            } catch (Exception e) {
                Log.e(TAG, "Error fetching album details for track: " + trackName + ", artist: " + artistName, e);
                return null;
            }
        }

        private Album searchAlbum(String query) {
            try {
                String url = "https://ws.audioscrobbler.com/2.0/?method=album.search&album=" + query.replace(" ", "+") +
                        "&api_key=" + API_KEY + "&format=json";
                String jsonResponse = HttpRequest.executeGet(url);

                JSONObject root = new JSONObject(jsonResponse);
                JSONObject results = root.getJSONObject("results");
                JSONObject albumMatches = results.getJSONObject("albummatches");
                JSONArray albums = albumMatches.getJSONArray("album");

                if (albums.length() > 0) {
                    // Loop through albums and find one with a valid cover image
                    for (int i = 0; i < albums.length(); i++) {
                        JSONObject firstAlbum = albums.getJSONObject(i);
                        String albumName = firstAlbum.getString("name");
                        String artistName = firstAlbum.getString("artist");
                        JSONArray images = firstAlbum.getJSONArray("image");

                        String coverUrl = "";
                        // Prioritize "extralarge" image, then fallback to "large"
                        for (int j = 0; j < images.length(); j++) {
                            JSONObject imageObj = images.getJSONObject(j);
                            if ("extralarge".equals(imageObj.getString("size"))) {
                                coverUrl = imageObj.getString("#text");
                                break;
                            } else if ("large".equals(imageObj.getString("size"))) {
                                coverUrl = imageObj.getString("#text");
                            }
                        }

                        // If a valid cover URL is found, return the album
                        if (!coverUrl.isEmpty()) {
                            return new Album(albumName, artistName, coverUrl);
                        }
                    }
                    // If no valid cover image, return null to skip this album
                    Log.d(TAG, "No valid cover image found for query: " + query);
                    return null;
                } else {
                    Log.d(TAG, "No albums found for query: " + query);
                    return null;
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing album search JSON for query: " + query, e);
                return null;
            }
        }
    }

    // Data class for Album
    private static class Album {
        String albumName;
        String artistName;
        String coverArtUrl;

        Album(String albumName, String artistName, String coverArtUrl) {
            this.albumName = albumName;
            this.artistName = artistName;
            this.coverArtUrl = coverArtUrl;
        }

        @Override
        public String toString() {
            return "Album{" +
                    "albumName='" + albumName + '\'' +
                    ", artistName='" + artistName + '\'' +
                    ", coverArtUrl='" + coverArtUrl + '\'' +
                    '}';
        }
    }
}