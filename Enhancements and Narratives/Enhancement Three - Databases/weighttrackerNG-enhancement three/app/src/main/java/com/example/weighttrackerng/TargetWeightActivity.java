package com.example.weighttrackerng;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weighttrackerng.database.GoalWeightDao;
import com.example.weighttrackerng.database.WeightTrackerDatabase;
import com.example.weighttrackerng.helpers.ParseNumbers;
import com.example.weighttrackerng.model.GoalWeight;
import com.example.weighttrackerng.model.User;

public class TargetWeightActivity extends AppCompatActivity {

    private static final String TAG = "TargetWeightActivity";

    private WeightTrackerDatabase mWeightTrackerDb;
    private GoalWeightDao mGoalWeightDao;
    User mUser;
    EditText mGoalWeightEditText;
    Button mSaveButton;
    Button mCancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target_weight);

        // get singleton instance of database
        mWeightTrackerDb = WeightTrackerDatabase.getInstance(getApplicationContext());
        mGoalWeightDao = mWeightTrackerDb.goalWeightDao();

        // get views from layout file
        mGoalWeightEditText = findViewById(R.id.goalWeight_editText);
        mSaveButton = findViewById(R.id.goalWeight_saveButton);
        mCancelButton = findViewById(R.id.goalWeight_cancelButton);

        if (mGoalWeightEditText == null) {
            throw new IllegalStateException("Required view 'goalWeight_editText' not found");
        }
        if (mSaveButton == null) {
            throw new IllegalStateException("Required view 'goalWeight_saveButton' not found");
        }
        if (mCancelButton == null) {
            throw new IllegalStateException("Required view 'goalWeight_cancelButton' not found");
        }

        // get current user from intent
        Intent intent = getIntent();
        mUser = (User) intent.getSerializableExtra("user");

        // Check if user is null
        if (mUser == null) {
            Toast.makeText(this, "User data is missing", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "User data is missing");
            finish();
            return;
        }

        // Check if a GoalWeight record exists for the current user
        GoalWeight existingGoalWeight = mGoalWeightDao.getSingleGoalWeight(mUser.getUsername());
        if (existingGoalWeight != null) {
            Intent newActivityIntent = new Intent(TargetWeightActivity.this, MainActivity.class);
            newActivityIntent.putExtra("user", mUser);
            startActivity(newActivityIntent);
            finish();
            return;
        }

        // Set click listeners
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveButtonClick(v);
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelButtonClick(v);
            }
        });

    }

    public void saveButtonClick(View view) {
        Log.d(TAG, "Save button clicked");
        try {
            String weightString = mGoalWeightEditText.getText().toString();

            // Check if weight input is parsable to double
            if (!ParseNumbers.isParsableDouble(weightString)) {
                Toast.makeText(TargetWeightActivity.this, "Weight entered could not be parsed", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Weight entered could not be parsed");
                return;
            }

            // Parse input to double
            double weight = Double.parseDouble(weightString);

            // Check if a record exists for the current user
            GoalWeight existingGoalWeight = mGoalWeightDao.getSingleGoalWeight(mUser.getUsername());

            if (existingGoalWeight != null) {
                // Update existing record
                int rowsUpdated = mGoalWeightDao.updateGoalWeight(weight, mUser.getUsername());
                Log.d(TAG, "Rows updated: " + rowsUpdated);

                if (rowsUpdated > 0) {
                    // Display success feedback
                    Toast.makeText(TargetWeightActivity.this, "Entry Successful", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Entry Successful");

                    // Start new activity
                    Intent newActivityIntent = new Intent(TargetWeightActivity.this, MainActivity.class);
                    startActivity(newActivityIntent);

                    // End this activity
                    finish();
                } else {
                    // Display failure feedback
                    Toast.makeText(TargetWeightActivity.this, "Entry Unsuccessful: No rows updated", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Entry Unsuccessful: No rows updated");
                }
            } else {
                // Insert new record
                GoalWeight newGoalWeight = new GoalWeight(weight, mUser.getUsername());
                mGoalWeightDao.insertGoalWeight(newGoalWeight);
                Toast.makeText(TargetWeightActivity.this, "Entry Successful", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Entry Successful");

                // Start new activity
                Intent newActivityIntent = new Intent(TargetWeightActivity.this, MainActivity.class);
                startActivity(newActivityIntent);

                // End this activity
                finish();
            }

        } catch (Exception e) {
            // Display failure feedback
            Toast.makeText(TargetWeightActivity.this, "Entry Unsuccessful", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Entry Unsuccessful", e);
        }
    }

    public void cancelButtonClick(View view) {
        Intent returnIntent = getIntent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }
}