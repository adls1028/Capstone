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

import com.example.weighttrackerng.database.DailyWeightDao;
import com.example.weighttrackerng.database.WeightTrackerDatabase;
import com.example.weighttrackerng.model.DailyWeight;
import com.example.weighttrackerng.model.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddRecordActivity extends AppCompatActivity {
    private static final String TAG = "AddRecordActivity";
    private WeightTrackerDatabase mWeightTrackerDb;
    private DailyWeightDao mDailyWeightDao;
    private User mUser;
    private EditText mDateEditText;
    private EditText mWeightEditText;
    private Button mSaveButton;
    private SimpleDateFormat mFormatter;
    private Button mCancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_add_record);

            // Initialize database and DAO
            mWeightTrackerDb = WeightTrackerDatabase.getInstance(getApplicationContext());
            mDailyWeightDao = mWeightTrackerDb.dailyWeightDao();

            // Get views from layout file
            mDateEditText = findViewById(R.id.editTextDate);
            mWeightEditText = findViewById(R.id.editTextWeight);
            mSaveButton = findViewById(R.id.saveRecord_Button);
            mCancelButton = (Button) this.findViewById(R.id.goalWeight_cancelButton);

            // Get current user from intent
            Intent intent = getIntent();
            mUser = (User) intent.getSerializableExtra("user");

            // Set formatter
            mFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

            // Set save button click listener
            mSaveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveRecord();
                }
         });
        } catch (Exception e) {
        Log.e(TAG, "Exception in onCreate: ", e);
        Toast.makeText(AddRecordActivity.this, "Failed to initialize activity", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Callback for save button
     * Parses user input and creates a new DailyWeight object to be inserted into database
     */
    private void saveRecord() {
        try {
            // Log the mUser object
            Log.e(TAG, "User object in saveRecord: " + mUser);

            // Get date and weight input
            String dateString = mDateEditText.getText().toString();
            String weightString = mWeightEditText.getText().toString();

            // Parse date and weight
            Date date = mFormatter.parse(dateString);
            double weight = Double.parseDouble(weightString);

            // Create new DailyWeight record
            DailyWeight newRecord = new DailyWeight(date, weight, mUser.getUsername());

            // Insert new record into database
            mDailyWeightDao.insert(newRecord);

            // Show success message and finish activity
            Toast.makeText(AddRecordActivity.this, "Record added successfully", Toast.LENGTH_SHORT).show();
            Intent mainActivityIntent = new Intent(AddRecordActivity.this, MainActivity.class);
            mainActivityIntent.putExtra("user", mUser);
            startActivity(mainActivityIntent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Exception in saveButtonClick: ", e);
            Toast.makeText(AddRecordActivity.this, "Failed to add record", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Callback for cancel button
     * Returns to calling activity
     */
    public void cancelButtonClick(View view) {
        Intent returnIntent = getIntent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }
}