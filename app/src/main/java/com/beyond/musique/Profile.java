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

public class Profile extends Fragment {

    TextView name;
    TextView email;
    Button signOutButton;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    private static final String TAG = "ProfileFragment";

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

        name = rootView.findViewById(R.id.name);
        email = rootView.findViewById(R.id.email);
        signOutButton = rootView.findViewById(R.id.signOutButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get currently signed-in user
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Display email
            email.setText(currentUser.getEmail());

            // Try to fetch the display name directly from FirebaseUser
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                name.setText(displayName);
            } else {
                // If display name is not available, fetch it from Firestore
                String userId = currentUser.getUid();
                db.collection("users").document(userId).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String userName = documentSnapshot.getString("name");
                                name.setText(userName);
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
        } else {
            // Handle case when no user is signed in
            email.setText("Not signed in");
            name.setText("Guest");
        }

        signOutButton.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        return rootView;
    }
}