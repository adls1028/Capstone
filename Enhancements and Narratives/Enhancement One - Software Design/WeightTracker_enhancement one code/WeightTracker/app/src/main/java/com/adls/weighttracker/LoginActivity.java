package com.adls.weighttracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.adls.weighttracker.database.UserDao;
import com.adls.weighttracker.database.WeightTrackerDatabase;
import com.adls.weighttracker.model.User;

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private WeightTrackerDatabase mWeightTrackerDb; // Database instance
    private UserDao mUserDao; // DAO for user operations
    private User mUser; // User object
    private TextView mFeedback; // TextView for feedback messages

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Get singleton instance of the database
        mWeightTrackerDb = WeightTrackerDatabase.getInstance(getApplicationContext());
        mUserDao = mWeightTrackerDb.userDao();
        mFeedback = findViewById(R.id.feedbackTextView); // Initialize feedback TextView
    }

    /**
     * Login button callback
     * Searches User table for credentials input
     * Authenticates user and starts WeightActivity
     */
    public void onLoginClick(View view) {
        try {
            // Get user input from EditText fields
            String username = ((EditText) findViewById(R.id.editTextUsername)).getText().toString();
            String password = ((EditText) findViewById(R.id.editTextPassword)).getText().toString();

            // Search SQLite database for matching username and password
            boolean isAuthenticated = login(username, password);
            if (isAuthenticated) {
                mUser = new User(username, password); // Create a new User object
                changeToWeightActivity(); // Navigate to WeightActivity
                mFeedback.setText(R.string.login_textView_2); // Set success message
                mFeedback.setTextColor(Color.parseColor("#767676")); // Set text color to gray
            } else {
                mFeedback.setText(R.string.login_failed); // Set failure message
                mFeedback.setTextColor(Color.RED); // Set text color to red
            }
        } catch (Exception e) {
            // Log the exception
            String error = "";
            for (StackTraceElement elem : e.getStackTrace()) {
                error += elem.toString();
            }
            Log.e("LoginActivity", error);
        }
    }

    /**
     * Callback for account creation button
     * Queries User table to see if username chosen is already taken,
     * then inserts new user record into User table
     */
    public void onCreateAccountClick(View view) {
        // Get user input from EditText fields
        String username = ((EditText) findViewById(R.id.editTextUsername)).getText().toString();
        String password = ((EditText) findViewById(R.id.editTextPassword)).getText().toString();

        try {
            // If user input is not blank
            if (!username.isEmpty() && !password.isEmpty()) {
                // Get all users from the database
                List<User> userList = mUserDao.getUsers();
                boolean found = false;
                // Check if username is already taken
                for (User user : userList) {
                    if (user.getUsername().equals(username)) {
                        found = true;
                        break;
                    }
                }
                // If username is not in the database, create a new user
                if (!found) {
                    mUserDao.insertUser(new User(username, password));
                    mFeedback.setText(R.string.user_create_success); // Set success message
                    mFeedback.setTextColor(Color.parseColor("#0e6b0e")); // Set text color to green
                } else {
                    // If username is already taken, notify the user
                    mFeedback.setText(R.string.username_found);
                    mFeedback.setTextColor(Color.RED); // Set text color to red
                }
            } else {
                // If user input is blank, notify the user
                mFeedback.setText(R.string.user_create_fail);
                mFeedback.setTextColor(Color.RED); // Set text color to red
            }
        } catch (Exception e) {
            // Log the exception
            String error = "";
            for (StackTraceElement elem : e.getStackTrace()) {
                error += elem.toString();
            }
            Log.e("LoginActivity - ", error);
        }
    }

    /**
     * Query User table in SQLite database for matching username and password
     */
    private boolean login(String username, String password) {
        List<User> userList = mUserDao.getUsers();
        for (User user : userList) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return true; // Return true if a matching user is found
            }
        }
        return false; // Return false if no matching user is found
    }

    /**
     * Starts the WeightActivity
     */
    public void changeToWeightActivity() {
        Intent intent = new Intent(this, WeightActivity.class);
        intent.putExtra("user", mUser); // Pass the User object to the next activity
        startActivity(intent); // Start the WeightActivity
    }
}