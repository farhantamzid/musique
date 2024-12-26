package com.beyond.musique;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Search extends Fragment {

    EditText etSearch;
    Button searchButton;
    TextView searchName1, searchName2, searchName3, searchName4, searchName5, searchName6, searchName7;
    TextView searchArtist1, searchArtist2, searchArtist3, searchArtist4, searchArtist5, searchArtist6, searchArtist7;
    ImageView searchImage1, searchImage2, searchImage3, searchImage4, searchImage5, searchImage6, searchImage7;
    ProgressBar loader;

    CardView resultContainer;

    private static final String API_KEY = "8cde7eb19387aac387fa9c498131b5c8"; // Replace with your actual API key
    private static final String TAG = "Search";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        // Enable edge-to-edge for this fragment
        if (getActivity() != null) {
            getActivity().getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }

        // Apply insets to ensure proper padding around system bars
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etSearch = rootView.findViewById(R.id.etSearch);
        searchButton = rootView.findViewById(R.id.searchButton);
        searchArtist1 = rootView.findViewById(R.id.searchArtist1);
        searchArtist2 = rootView.findViewById(R.id.searchArtist2);
        searchArtist3 = rootView.findViewById(R.id.searchArtist3);
        searchArtist4 = rootView.findViewById(R.id.searchArtist4);
        searchArtist5 = rootView.findViewById(R.id.searchArtist5);
        searchArtist6 = rootView.findViewById(R.id.searchArtist6);
        searchArtist7 = rootView.findViewById(R.id.searchArtist7);
        searchName1 = rootView.findViewById(R.id.searchName1);
        searchName2 = rootView.findViewById(R.id.searchName2);
        searchName3 = rootView.findViewById(R.id.searchName3);
        searchName4 = rootView.findViewById(R.id.searchName4);
        searchName5 = rootView.findViewById(R.id.searchName5);
        searchName6 = rootView.findViewById(R.id.searchName6);
        searchName7 = rootView.findViewById(R.id.searchName7);
        searchImage1 = rootView.findViewById(R.id.searchImage1);
        searchImage2 = rootView.findViewById(R.id.searchImage2);
        searchImage3 = rootView.findViewById(R.id.searchImage3);
        searchImage4 = rootView.findViewById(R.id.searchImage4);
        searchImage5 = rootView.findViewById(R.id.searchImage5);
        searchImage6 = rootView.findViewById(R.id.searchImage6);
        searchImage7 = rootView.findViewById(R.id.searchImage7);
        resultContainer = rootView.findViewById(R.id.resultContainer);
        loader = rootView.findViewById(R.id.loader);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    loader.setVisibility(View.VISIBLE);
                    new FetchSearchResultsTask().execute(query);
                }
            }
        });

        return rootView;
    }

    private class FetchSearchResultsTask extends AsyncTask<String, Void, JSONArray> {

        @Override
        protected JSONArray doInBackground(String... params) {
            String query = params[0];
            String url = "https://ws.audioscrobbler.com/2.0/?method=album.search&album=" +
                    query.replace(" ", "+") + "&limit=7&api_key=" + API_KEY + "&format=json";

            try {
                String jsonResponse = HttpRequest.executeGet(url);
                JSONObject root = new JSONObject(jsonResponse);
                JSONObject results = root.getJSONObject("results");
                JSONObject albumMatches = results.getJSONObject("albummatches");
                return albumMatches.getJSONArray("album");
            } catch (Exception e) {
                Log.e(TAG, "Error fetching search results", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONArray albums) {
            super.onPostExecute(albums);
            if (albums != null) {
                try {
                    for (int i = 0; i < albums.length() && i < 7; i++) {
                        JSONObject album = albums.getJSONObject(i);
                        String albumName = album.getString("name");
                        String artistName = album.getString("artist");
                        JSONArray images = album.getJSONArray("image");

                        // Get the "extralarge" image if available, fallback to "large"
                        String imageUrl = "";
                        for (int j = 0; j < images.length(); j++) {
                            JSONObject imageObj = images.getJSONObject(j);
                            if ("extralarge".equals(imageObj.getString("size"))) {
                                imageUrl = imageObj.getString("#text");
                                break;
                            } else if ("large".equals(imageObj.getString("size"))) {
                                imageUrl = imageObj.getString("#text");
                            }
                        }

                        // Populate UI elements using switch case
                        switch (i) {
                            case 0:
                                updateUI(searchName1, searchArtist1, searchImage1, albumName, artistName, imageUrl);
                                break;
                            case 1:
                                updateUI(searchName2, searchArtist2, searchImage2, albumName, artistName, imageUrl);
                                break;
                            case 2:
                                updateUI(searchName3, searchArtist3, searchImage3, albumName, artistName, imageUrl);
                                break;
                            case 3:
                                updateUI(searchName4, searchArtist4, searchImage4, albumName, artistName, imageUrl);
                                break;
                            case 4:
                                updateUI(searchName5, searchArtist5, searchImage5, albumName, artistName, imageUrl);
                                break;
                            case 5:
                                updateUI(searchName6, searchArtist6, searchImage6, albumName, artistName, imageUrl);
                                break;
                            case 6:
                                updateUI(searchName7, searchArtist7, searchImage7, albumName, artistName, imageUrl);
                                break;
                        }
                    }

                    loader.setVisibility(View.GONE);
                    resultContainer.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing album JSON", e);
                }
            }
        }

        private void updateUI(TextView nameView, TextView artistView, ImageView imageView, String albumName, String artistName, String imageUrl) {
            nameView.setText(albumName);
            artistView.setText(artistName);
            Glide.with(requireContext()).load(imageUrl).into(imageView);
        }
    }
}