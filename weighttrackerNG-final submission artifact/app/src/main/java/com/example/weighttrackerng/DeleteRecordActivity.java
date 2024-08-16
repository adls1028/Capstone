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

import androidx.appcompat.app.AppCompatActivity;

import com.example.weighttrackerng.database.DailyWeightDao;
import com.example.weighttrackerng.database.WeightTrackerDatabase;
import com.example.weighttrackerng.model.DailyWeight;
import com.example.weighttrackerng.model.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DeleteRecordActivity extends AppCompatActivity {

    private WeightTrackerDatabase mWeightTrackerDb;
    private DailyWeightDao mDailyWeightDao;
    private User mUser;
    private EditText mDate_editText;
    private Button mDeleteButton;
    private Button mCancelButton;
    TextView mPrompt;
    Date mDate;
    DateFormat mFormatter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_delete_record);

            // get singleton instance of database
            mWeightTrackerDb = WeightTrackerDatabase.getInstance(getApplicationContext());
            mDailyWeightDao = mWeightTrackerDb.dailyWeightDao();

            // get views from layout file
            mDate_editText = (EditText) this.findViewById(R.id.editTextDate);
            mPrompt = (TextView) this.findViewById(R.id.change_record_prompt);
            mDeleteButton = (Button) this.findViewById(R.id.deleteRecord_button);
            mCancelButton = (Button) this.findViewById(R.id.cancelButton);

            // get current user from intent
            Intent intent = getIntent();
            mUser = (User) intent.getSerializableExtra("user");

            // set formatter
            mFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Callback for delete button
     * Finds record in DailyWeight table that matches the specified date
     * Asks user for to confirm they want to delete the record
     */
    public void deleteButtonOnClick(View view) {
        try {
            // hide keyboard
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mDeleteButton.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);

            // get date input and parse
            String date_string = mDate_editText.getText().toString();
            mDate = mFormatter.parse(date_string);

            // query the database for the date
            DailyWeight existingRecord = mDailyWeightDao.getRecordWithDate(mUser.getUsername(), mDate);

            // if date not found
            if (existingRecord == null) {
                Toast.makeText(this, "Date not found in database", Toast.LENGTH_SHORT).show();
                return;
            }

            // delete the record
            mDailyWeightDao.deleteDailyWeight(existingRecord);
            Toast.makeText(this, "Record deleted successfully", Toast.LENGTH_SHORT).show();

            // set result and finish activity
            setResult(Activity.RESULT_OK);
            finish();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to delete record", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Callback for cancel button
     * Stops this activity and returns to calling activity
     */
    public void cancelButtonClick(View view) {
        Intent returnIntent = getIntent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }
}