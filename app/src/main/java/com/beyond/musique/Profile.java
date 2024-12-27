package com.beyond.musique;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Fragment for displaying user profile information.
 */
public class Profile extends Fragment {

    // UI elements
    TextView name;
    TextView email;
    Button signOutButton;

    // Firebase authentication and Firestore instances
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    // Tag for logging
    private static final String TAG = "ProfileFragment";

    // Static variable to cache user data
    private static FirebaseUser cachedUser = null;
    private static String cachedUserName = null;

    /**
     * Called to have the fragment instantiate its user interface view.
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

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

        // Initialize UI elements
        name = rootView.findViewById(R.id.name);
        email = rootView.findViewById(R.id.email);
        signOutButton = rootView.findViewById(R.id.signOutButton);

        // Initialize Firebase authentication and Firestore instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get currently signed-in user
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Check if user data is cached
            if (cachedUser != null && cachedUser.getUid().equals(currentUser.getUid())) {
                // Use cached data
                email.setText(cachedUser.getEmail());
                name.setText(cachedUserName);
            } else {
                // Cache the current user
                cachedUser = currentUser;
                email.setText(currentUser.getEmail());

                // Try to fetch the display name directly from FirebaseUser
                String displayName = currentUser.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    cachedUserName = displayName;
                    name.setText(displayName);
                } else {
                    // If display name is not available, fetch it from Firestore
                    String userId = currentUser.getUid();
                    db.collection("users").document(userId).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    cachedUserName = documentSnapshot.getString("name");
                                    name.setText(cachedUserName);
                                } else {
                                    Log.d(TAG, "No such document for user ID: " + userId);
                                    name.setText("Unknown User");
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error fetching user details", e);
                                Toast.makeText(getActivity(), "Error loading profile", Toast.LENGTH_SHORT).show();
                            });
                }
            }
        } else {
            // Handle case when no user is signed in
            email.setText("Not signed in");
            name.setText("Guest");
        }

        // Set click listener for the sign-out button
        signOutButton.setOnClickListener(v -> {
            mAuth.signOut();
            cachedUser = null; // Clear cached user data
            cachedUserName = null; // Clear cached user name
            startActivity(new Intent(getActivity(), LoginActivity.class));
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        return rootView;
    }
}