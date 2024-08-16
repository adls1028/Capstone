package com.example.weighttrackerng;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * MainActivity class handles the main screen of the application.
 * It displays the user's name and provides a logout functionality.
 */
public class TestActivity extends AppCompatActivity {

    // TextView to display the user's name
    TextView userName;
    // Button to handle logout action
    Button logout;
    // GoogleSignInClient to manage Google Sign-In
    GoogleSignInClient gClient;
    // GoogleSignInOptions to configure Google Sign-In
    GoogleSignInOptions gOptions;

    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        // Initialize UI elements
        logout = findViewById(R.id.logout);
        userName = findViewById(R.id.userName);

        if (logout == null || userName == null) {
            throw new IllegalStateException("Required views not found");
        }

        // Configure Google Sign-In options
        gOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gClient = GoogleSignIn.getClient(this, gOptions);

        // Get the last signed-in Google account
        GoogleSignInAccount gAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (gAccount != null){
            // Set the user's name in the TextView
            String gName = gAccount.getDisplayName();
            if (gName != null) {
                userName.setText(gName);
            } else {
                userName.setText("Unknown User");
            }
        }

        // Set the logout button's onClick listener
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Sign out the user and redirect to LoginActivity
                gClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        startActivity(new Intent(TestActivity.this, LoginActivity.class));
                    }
                });
            }
        });
    }
}