package com.adls.weighttracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.adls.weighttracker.database.DailyWeightDao;
import com.adls.weighttracker.database.GoalWeightDao;
import com.adls.weighttracker.database.WeightTrackerDatabase;
import com.adls.weighttracker.model.DailyWeight;
import com.adls.weighttracker.model.GoalWeight;
import com.adls.weighttracker.model.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class WeightActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;
    private static final int PERMISSION_READ_STATE = 2;
    private final int LAUNCH_ADD_RECORD_ACTIVITY = 1;
    private final int LAUNCH_CHANGE_RECORD_ACTIVITY = 2;
    private final int LAUNCH_DELETE_RECORD_ACTIVITY = 3;
    private final int LAUNCH_CHANGE_TARGET_ACTIVITY = 4;
    private WeightTrackerDatabase mWeightTrackerDb; // Database instance
    private DailyWeightDao mDailyWeightDao; // DAO for daily weights
    private GoalWeightDao mGoalWeightDao; // DAO for goal weights
    private User mUser; // User object
    DailyWeight mNewDailyWeight; // New daily weight entry
    TableLayout mTableLayout; // Table layout to display weights
    TextView mTargetWeight; // TextView for target weight
    TableRow mNoRecordsRow; // Row to indicate no records

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {
            super.onCreate(savedInstanceState);
            // Hide the action bar
            Objects.requireNonNull(this.getSupportActionBar()).hide();
            // Set the content view to the layout file
            setContentView(R.layout.activity_weight);

            // Initialize UI components
            mTableLayout = findViewById(R.id.dailyWeightTable);
            mTargetWeight = findViewById(R.id.goalWeightText);

            // Get the singleton instance of the database
            mWeightTrackerDb = WeightTrackerDatabase.getInstance(getApplicationContext());
            mDailyWeightDao = mWeightTrackerDb.dailyWeightDao();

            // Row to tell user there are no records to display
            mNoRecordsRow = findViewById(R.id.noRecordsRow);

            // Get User object from login screen
            Intent intent = getIntent();
            mUser = (User) intent.getSerializableExtra("user");

            // Refresh the table and target weight TextView
            refreshTable();
            refreshTargetWeight();

            // Check for granted permissions, ask user for permissions if needed
            checkForAllPermissions();
        } catch (Exception e) {
            showErrorToast("Error initializing activity");
        }
    }

    /**
     * Refreshes target weight figure on screen
     */
    public void refreshTargetWeight() {
        try {
            // Retrieve the DAO from the goal weights database
            mGoalWeightDao = mWeightTrackerDb.goalWeightDao();
            int count = mGoalWeightDao.countGoalEntries(mUser.getUsername());
            // If there is no goal weight, insert default goal weight of 150lbs
            if (count == 0) {
                GoalWeight defaultGoal = new GoalWeight(150.0, mUser.getUsername());
                mGoalWeightDao.insertGoalWeight(defaultGoal);
                return;
            } else if (count < 0 || count > 1) {
                showErrorToast("Error retrieving goal weight data");
                return;
            }

            // Retrieve the current goal weight and display it
            GoalWeight currentGoal = mGoalWeightDao.getSingleGoalWeight(mUser.getUsername());
            mTargetWeight.setText(currentGoal.getGoal() + " lbs");

        } catch (Exception e) {
            showErrorToast("Error refreshing target weight");
        }
    }

    /**
     * Refreshes the table with daily weight records
     */
    public void refreshTable() {
        try {
            // Remove all but the header from the TableLayout
            cleanTable(mTableLayout);
            // Get all DailyWeight records of this user
            List<DailyWeight> userDailyWeights = mDailyWeightDao.getDailyWeightsOfUser(mUser.getUsername());

            // If there are no DailyWeights for this user, show "no records" row
            if (userDailyWeights.size() == 0) {
                addNoRecordsRow(mTableLayout);
            } else {
                // If there are DailyWeights for this user, populate the table
                DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                TableRow header = findViewById(R.id.headerRow);
                TextView headerDate = findViewById(R.id.headerDate);
                TextView headerWeight = findViewById(R.id.headerWeight);

                // Layout parameters for row and textViews
                TableLayout.LayoutParams layoutParamsTable = (TableLayout.LayoutParams) header.getLayoutParams();
                TableRow.LayoutParams layoutParamsRow = (TableRow.LayoutParams) headerDate.getLayoutParams();

                // Populate each row with daily weight records
                for (DailyWeight dailyWeight : userDailyWeights) {
                    TableRow row = new TableRow(this);
                    TextView dateTextView = new TextView(this);
                    TextView weightTextView = new TextView(this);

                    // Set layout parameters and properties for the views
                    row.setLayoutParams(layoutParamsTable);
                    dateTextView.setLayoutParams(layoutParamsRow);
                    weightTextView.setLayoutParams(layoutParamsRow);
                    dateTextView.setWidth(0);
                    dateTextView.setGravity(Gravity.CENTER);
                    dateTextView.setPadding(20, 20, 20, 20);
                    weightTextView.setWidth(0);
                    weightTextView.setGravity(Gravity.CENTER);
                    weightTextView.setPadding(20, 20, 20, 20);

                    // Set the text for the date and weight TextViews
                    dateTextView.setText(formatter.format(dailyWeight.getDate()));
                    weightTextView.setText(Double.toString(dailyWeight.getWeight()));

                    // Add the TextViews to the current row
                    row.addView(dateTextView);
                    row.addView(weightTextView);

                    // Add the row to the TableLayout
                    mTableLayout.addView(row);
                }
            }

        } catch (Exception e) {
            showErrorToast("Error refreshing table");
        }
    }

    /**
     * Remove all but the header from the TableLayout
     * Also leave "no records" row if there are no DailyWeights
     */
    private void cleanTable(TableLayout table) {
        try {
            int childCount = table.getChildCount();

            // Remove all rows except the first one
            if (childCount > 1) {
                table.removeViews(1, childCount - 1);
            }

        } catch (Exception e) {
            showErrorToast("Error cleaning table");
        }
    }

    /**
     * Adds a row indicating no records
     */
    private void addNoRecordsRow(TableLayout table) {
        mTableLayout.addView(mNoRecordsRow);
    }

    /**
     * Check if user has reached target weight
     */
    private void reachedGoalCheck() {
        try {
            // Get all DailyWeight records of this user
            List<DailyWeight> userDailyWeights = mDailyWeightDao.getDailyWeightsOfUser(mUser.getUsername());

            // Get the most recent weight (latest date in the list)
            if (!userDailyWeights.isEmpty()) {
                double currentWeight = userDailyWeights.get(userDailyWeights.size() - 1).getWeight();

                // Get target weight for this user
                double targetWeight = mGoalWeightDao.getSingleGoalWeight(mUser.getUsername()).getGoal();

                // If current weight is lower than goal, send congratulating text to user
                if (currentWeight <= targetWeight) {
                    sendTextToUser();
                }
            }

        } catch (Exception e) {
            showErrorToast("Error checking goal weight");
        }
    }

    /**
     * Sends a congratulatory text message to the user
     */
    public void sendTextToUser() {
        try {
            // Check for SEND_SMS and READ_PHONE_STATE permissions
            checkForAllPermissions();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                            == PackageManager.PERMISSION_GRANTED) {

                // Get the phone number and send the text message
                SmsManager smsManager = SmsManager.getDefault();
                TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
                String phoneNum = telephonyManager.getLine1Number();
                if (phoneNum != null) {
                    smsManager.sendTextMessage(phoneNum, null,
                            "Congratulations, you reached your target weight!",
                            null, null);
                }
            }

        } catch (Exception e) {
            showErrorToast("Error sending congratulatory message");
        }
    }

    /**
     * Checks for READ_PHONE_STATE permission and requests it if not granted
     */
    public void checkForPhoneStatePermissions() {
        try {
            // If we do not have this permission, ask the user
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED) {
                System.out.println("Asking for READ_PHONE_STATE permission...");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_READ_STATE);
            } else {
                System.out.println("App already has READ_PHONE_STATE permission");
            }
        } catch (Exception e) {
            showErrorToast("Error checking phone state permission");
        }
    }

    /**
     * Checks for necessary permissions and requests them if not granted
     */
    public void checkForAllPermissions() {
        try {
            // If we do not have one or both of the necessary permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                            != PackageManager.PERMISSION_GRANTED) {
                System.out.println("Asking for READ_PHONE_STATE permission...");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.SEND_SMS},
                        PERMISSION_READ_STATE);
            } else {
                System.out.println("App already has READ_PHONE_STATE permission");
            }
        } catch (Exception e) {
            showErrorToast("Error checking permissions");
        }
    }

    // Callback for add record button
    public void addRecordOnClick(View view) {
        try {
            // Call AddRecordActivity to get new date and weight input from user
            Intent intent = new Intent(this, AddRecordActivity.class);
            startActivityForResult(intent, LAUNCH_ADD_RECORD_ACTIVITY);

        } catch (Exception e) {
            showErrorToast("Error starting Add Record activity");
        }
    }

    // Callback for delete record button
    public void deleteRecordOnClick(View view) {
        try {
            // Call DeleteRecordActivity
            Intent intent = new Intent(this, DeleteRecordActivity.class);
            intent.putExtra("user", mUser);
            startActivityForResult(intent, LAUNCH_DELETE_RECORD_ACTIVITY);

        } catch (Exception e) {
            showErrorToast("Error starting Delete Record activity");
        }
    }

    // Callback for edit record button
    public void editRecordOnClick(View view) {
        try {
            // Call ChangeRecordActivity
            Intent intent = new Intent(this, ChangeRecordActivity.class);
            intent.putExtra("user", mUser);
            startActivityForResult(intent, LAUNCH_CHANGE_RECORD_ACTIVITY);

        } catch (Exception e) {
            showErrorToast("Error starting Edit Record activity");
        }
    }

    // Callback for edit target weight button
    public void editTargetOnClick(View view) {
        try {
            // Call ChangeRecordActivity
            Intent intent = new Intent(this, ChangeTargetActivity.class);
            intent.putExtra("user", mUser);
            startActivityForResult(intent, LAUNCH_CHANGE_TARGET_ACTIVITY);

        } catch (Exception e) {
            showErrorToast("Error starting Edit Target Weight activity");
        }
    }

    /**
     * Check for SEND_SMS permission and ask user for permission if not granted
     */
    private void checkForSmsPermission() {
        try {
            // If app doesn't have SEND_SMS permission yet
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.SEND_SMS) !=
                    PackageManager.PERMISSION_GRANTED) {
                System.out.println("System didn't have permission for texting yet. Asking permission now...");

                // Ask user for permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            } else {
                System.out.println("SEND_SMS permission already granted");
            }
        } catch (Exception e) {
            showErrorToast("Error checking SMS permission");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            try {
                // For adding a record
                if (requestCode == LAUNCH_ADD_RECORD_ACTIVITY) {
                    mNewDailyWeight = (DailyWeight) data.getSerializableExtra("newDailyWeight");
                    mNewDailyWeight.setUsername(mUser.getUsername());

                    mDailyWeightDao.insertDailyWeight(mNewDailyWeight);
                    refreshTable();
                    refreshTargetWeight();
                    reachedGoalCheck();

                    // Show toast
                    Toast toast = Toast.makeText(WeightActivity.this, "Weight record successfully added",
                            Toast.LENGTH_LONG);
                    Objects.requireNonNull(toast.getView()).setBackgroundColor(0xFFCC99FF);
                    toast.show();
                }

                // For changing a record
                if (requestCode == LAUNCH_CHANGE_RECORD_ACTIVITY) {
                    refreshTable();
                    refreshTargetWeight();
                    reachedGoalCheck();

                    // Show toast
                    Toast toast = Toast.makeText(WeightActivity.this, "Weight record successfully changed",
                            Toast.LENGTH_LONG);
                    Objects.requireNonNull(toast.getView()).setBackgroundColor(0xFFCC99FF);
                    toast.show();
                }

                // For deleting a record
                if (requestCode == LAUNCH_DELETE_RECORD_ACTIVITY) {
                    cleanTable(mTableLayout);
                    refreshTable();
                    refreshTargetWeight();

                    // Show toast
                    Toast toast = Toast.makeText(WeightActivity.this, "Weight record successfully deleted",
                            Toast.LENGTH_LONG);
                    Objects.requireNonNull(toast.getView()).setBackgroundColor(0xFFCC99FF);
                    toast.show();
                }

                // For changing target weight
                if (requestCode == LAUNCH_CHANGE_TARGET_ACTIVITY) {
                    refreshTable();
                    refreshTargetWeight();
                    reachedGoalCheck();

                    // Show toast
                    Toast toast = Toast.makeText(WeightActivity.this, "Target weight successfully updated",
                            Toast.LENGTH_LONG);
                    Objects.requireNonNull(toast.getView()).setBackgroundColor(0xFFCC99FF);
                    toast.show();
                }
            } catch (Exception e) {
                showErrorToast("Error processing activity result");
            }
        }
    }

    /**
     * Shows an error toast with the given message
     */
    private void showErrorToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
