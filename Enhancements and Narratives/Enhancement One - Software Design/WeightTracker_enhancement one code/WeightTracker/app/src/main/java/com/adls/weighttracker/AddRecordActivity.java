package com.adls.weighttracker;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.adls.weighttracker.helpers.ParseNumbers;
import com.adls.weighttracker.model.DailyWeight;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddRecordActivity extends AppCompatActivity {

    private EditText mDate_editText;   // EditText field for date input
    private EditText mWeight_editText; // EditText field for weight input
    private Button mSaveButton;        // Button to save the record
    private Button mCancelButton;      // Button to cancel the action

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            // Ensure all steps in the parent class are completed
            super.onCreate(savedInstanceState);
            // Hide the action bar for this activity
            this.getSupportActionBar().hide();
            // Set the layout for this activity to the corresponding XML file which defines UI elements
            setContentView(R.layout.activity_add_record);

            // Get views from the layout file
            mDate_editText = (EditText) this.findViewById(R.id.editTextDate);
            mWeight_editText = (EditText) this.findViewById(R.id.deleteRecord_editTextWeight);
            mSaveButton = (Button) this.findViewById(R.id.goalWeight_saveButton);
            mCancelButton = (Button) this.findViewById(R.id.goalWeight_cancelButton);

        } catch (Exception e) {
            // Print stack trace if an exception occurs
            e.printStackTrace();
        }
    }

    /**
     * Callback for the save button
     * Parses user input and creates a new DailyWeight object to be inserted into the database
     */
    public void saveButtonClick(View view) {
        try {
            DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US); // Date formatter
            Date date;

            // Get date input and parse it
            String date_string = mDate_editText.getText().toString();
            date = formatter.parse(date_string);

            // Get weight input
            String weight_string = mWeight_editText.getText().toString();
            // If weight input is not a parsable double
            if (!ParseNumbers.isParsableDouble(weight_string)) {
                // Show a toast message indicating invalid weight input
                Toast.makeText(this, "Invalid weight input. Please enter a valid number.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Parse input to double
            double weight = Double.parseDouble(weight_string);

            // Create a new DailyWeight object
            DailyWeight newWeight = new DailyWeight();
            newWeight.setDate(date);
            newWeight.setWeight(weight);

            // Pass the parsed string input to the calling activity
            Intent returnableIntent = getIntent();
            returnableIntent.putExtra("newDailyWeight", newWeight);
            setResult(Activity.RESULT_OK, returnableIntent);

            // End this activity and return to the calling activity
            finish();

            // Show success message
            Toast.makeText(this, "Record saved successfully.", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            // Print stack trace if an exception occurs
            e.printStackTrace();
            // Show error message
            Toast.makeText(this, "Error saving record. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Callback for the cancel button
     * Returns to the calling activity
     */
    public void cancelButtonClick(View view) {
        // Show a toast message indicating the action was cancelled
        Toast.makeText(this, "Action cancelled.", Toast.LENGTH_SHORT).show();
        Intent returnIntent = getIntent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }
}
