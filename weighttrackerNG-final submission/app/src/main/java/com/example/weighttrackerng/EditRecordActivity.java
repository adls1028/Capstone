package com.example.weighttrackerng;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weighttrackerng.database.DailyWeightDao;
import com.example.weighttrackerng.database.WeightTrackerDatabase;
import com.example.weighttrackerng.helpers.ParseNumbers;
import com.example.weighttrackerng.model.DailyWeight;
import com.example.weighttrackerng.model.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditRecordActivity extends AppCompatActivity {

    private WeightTrackerDatabase mWeightTrackerDb;
    private DailyWeightDao mDailyWeightDao;
    private User mUser;
    private EditText mDate_editText;
    private EditText mWeight_editText;
    private Button mSaveButton;
    private Button mChangeButton;
    private Button mCancelButton;
    TextView mPrompt;
    Date mDate;
    DateFormat mFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_edit_record);

            // get singleton instance of database
            mWeightTrackerDb = WeightTrackerDatabase.getInstance(getApplicationContext());
            mDailyWeightDao = mWeightTrackerDb.dailyWeightDao();

            // get views from layout file
            mDate_editText = (EditText) this.findViewById(R.id.editTextDate);
            mWeight_editText = (EditText) this.findViewById(R.id.deleteRecord_editTextWeight);
            mSaveButton = (Button) this.findViewById(R.id.goalWeight_saveButton);
            mChangeButton = (Button) this.findViewById(R.id.changeRecord_button);
            mPrompt = (TextView) this.findViewById(R.id.change_record_prompt);

            // get current user from intent
            Intent intent = getIntent();
            mUser = (User) intent.getSerializableExtra("user");

            // set formatter
            mFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Callback for change date button
     * Looks for user's date input in database and allows user to enter new weight for the date
     */
    public void changeDate_buttonClick(View view) {
        try {
            // hide keyboard
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mChangeButton.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);

            // get date input and parse
            String date_string = mDate_editText.getText().toString();
            mDate = mFormatter.parse(date_string);

            // query the database for the date
            DailyWeight existingRecord = mDailyWeightDao.getRecordWithDate(mUser.getUsername(), mDate);

            // if date not found
            if (existingRecord == null) {
                mPrompt.setText("DATE NOT FOUND IN DATABASE");
                mPrompt.setTextColor(Color.RED);
                return;
            }

            // change widget properties so user can edit the weight
            mPrompt.setText("ENTER NEW WEIGHT");
            mPrompt.setTextColor(getResources().getColor(R.color.lavender));
            mChangeButton.setVisibility(View.INVISIBLE);
            mSaveButton.setVisibility(View.VISIBLE);
            mWeight_editText.setVisibility(View.VISIBLE);
            // lock the date so user can't change it
            mDate_editText.setFocusable(false);

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Callback for save button
     * Performs update query on the specified date in the DailyWeight table
     */
    public void change_saveButtonClick(View view) {
        try {
            if (mDate == null) {
                mPrompt.setText("SOMETHING WENT WRONG");
                mPrompt.setTextColor(Color.RED);
                return;
            }

            // get weight input
            String weight_string = mWeight_editText.getText().toString();
            Log.d("EditRecordActivity", "Weight input: " + weight_string);
            // if weight input is not a parsable double
            if (!ParseNumbers.isParsableDouble(weight_string)) {
                mPrompt.setText("WEIGHT ENTERED COULD NOT BE PARSED");
                mPrompt.setTextColor(Color.RED);
                Log.e("EditRecordActivity", "Weight input is not parsable");
            }
            // parse input to double
            double weight = Double.parseDouble(weight_string);
            Log.d("EditRecordActivity", "Parsed weight: " + weight);

            // perform update on database record
            mDailyWeightDao.updateDailyWeight(mUser.getUsername(), mDate, weight);
            Toast.makeText(this, "Record updated successfully", Toast.LENGTH_SHORT).show();
            Log.d("EditRecordActivity", "Record updated successfully");

            // set result and finish activity
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_OK, returnIntent);
            Log.d("EditRecordActivity", "Result set to RESULT_OK");
            finish();
            Log.d("EditRecordActivity", "Activity finished");

        }catch (Exception e) {
            e.printStackTrace();
            Log.e("EditRecordActivity", "Exception: " + e.getMessage());
        }
    }

    /**
     * Callback for cancel button
     * Stops this activity and returns to calling activity
     */
    public void change_cancelButtonClick(View view) {
        Intent returnIntent = getIntent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }
}
