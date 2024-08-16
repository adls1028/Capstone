package com.example.weighttrackerng;

import static android.widget.Toast.makeText;

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
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.example.weighttrackerng.database.DailyWeightDao;
import com.example.weighttrackerng.database.GoalWeightDao;
import com.example.weighttrackerng.database.WeightTrackerDatabase;
import com.example.weighttrackerng.model.DailyWeight;
import com.example.weighttrackerng.model.GoalWeight;
import com.example.weighttrackerng.model.User;
import com.example.weighttrackerng.utils.MainLib;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_READ_STATE = 2;
    private final int LAUNCH_ADD_RECORD_ACTIVITY = 1;
    private final int LAUNCH_CHANGE_RECORD_ACTIVITY = 2;
    private final int LAUNCH_DELETE_RECORD_ACTIVITY = 3;
    private final int LAUNCH_CHANGE_TARGET_ACTIVITY = 4;
    private WeightTrackerDatabase mWeightTrackerDb;
    private DailyWeightDao mDailyWeightDao;
    DailyWeight mNewDailyWeight;
    TableLayout mTableLayout;
    TextView mTargetWeight;
    TableRow mNoRecordsRow;
    private GoalWeightDao goalWeightDao;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTableLayout = findViewById(R.id.dailyWeightTable);
        mNoRecordsRow = findViewById(R.id.noRecordsRow);
        mTargetWeight = findViewById(R.id.goalWeightText);

        // Get singleton instance of database
        mWeightTrackerDb = WeightTrackerDatabase.getInstance(getApplicationContext());
        mDailyWeightDao = mWeightTrackerDb.dailyWeightDao();
        goalWeightDao = mWeightTrackerDb.goalWeightDao();

        // Get User object from login screen
        Intent intent = getIntent();
        currentUser = (User) intent.getSerializableExtra("user");

        if (currentUser != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    GoalWeight goalWeight = goalWeightDao.getSingleGoalWeight(currentUser.getUsername());
                    if (goalWeight != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTargetWeight.setText(String.valueOf(goalWeight.getGoal()));
                            }
                        });
                    }
                }
            }).start();
        }

        try {
            checkForAllPermissions();
            refreshTable();
            refreshTargetWeight();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("MainActivity", "Error in onCreate: " + e.getMessage());
        }
    }

    public void checkForAllPermissions() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE}, PERMISSION_READ_STATE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private User getCurrentUser() {
        Intent intent = getIntent();
        return (User) intent.getSerializableExtra("user");
    }

    private void refreshTargetWeight() {
        try {
            GoalWeight goalWeight = goalWeightDao.getSingleGoalWeight(currentUser.getUsername());
            if (goalWeight != null) {
                mTargetWeight.setText(String.valueOf(goalWeight.getGoal()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshTable() {
        try {
            cleanTable(mTableLayout);
            List<DailyWeight> userDailyWeights = mDailyWeightDao.getDailyWeightsOfUser(currentUser.getUsername());
            if (userDailyWeights.isEmpty()) {
                addNoRecordsRow(mTableLayout);
            } else {
                DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                TableRow header = (TableRow) findViewById(R.id.headerRow);
                TextView headerDate = (TextView) findViewById(R.id.headerDate);
                TextView headerWeight = (TextView) findViewById(R.id.headerWeight);

                // layout parameters for row:
                TableLayout.LayoutParams layoutParamsTable = (TableLayout.LayoutParams) header.getLayoutParams();
                // layout parameters for textViews:
                TableRow.LayoutParams layoutParamsRow = (TableRow.LayoutParams) headerDate.getLayoutParams();

                for (int i = 0; i < userDailyWeights.size(); i++) {
                    TableRow row = new TableRow(this);
                    TextView dateTextView = new TextView(this);
                    TextView weightTextView = new TextView(this);

                    // activate the layout parameters
                    row.setLayoutParams(layoutParamsTable);
                    dateTextView.setLayoutParams(layoutParamsRow);
                    weightTextView.setLayoutParams(layoutParamsRow);
                    // set additional view properties
                    dateTextView.setWidth(0);
                    dateTextView.setGravity(Gravity.CENTER);
                    dateTextView.setPadding(20, 20, 20, 20);
                    weightTextView.setWidth(0);
                    weightTextView.setGravity(Gravity.CENTER);
                    weightTextView.setPadding(20, 20, 20, 20);

                    // set the value of the date TextView
                    dateTextView.setText(formatter.format(userDailyWeights.get(i).getDate()));
                    // set the value of the weight TextView
                    weightTextView.setText(Double.toString(userDailyWeights.get(i).getWeight()));

                    // add the 2 TextViews to the current row
                    row.addView(dateTextView);
                    row.addView(weightTextView);

                    // add row to TableLayout
                    mTableLayout.addView(row);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cleanTable(TableLayout table) {
        try {
            int childCount = table.getChildCount();
            if (childCount > 1) {
                table.removeViews(1, childCount - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addNoRecordsRow(TableLayout table) {
        if (mNoRecordsRow.getParent() == null) {
            mTableLayout.addView(mNoRecordsRow);
        }
        Log.d("MainActivity", "addNoRecordsRow() EXECUTED.");
    }

    private void reachedGoalCheck() {
        try {
            GoalWeight goalWeight = goalWeightDao.getSingleGoalWeight(currentUser.getUsername());
            List<DailyWeight> dailyWeights = mDailyWeightDao.getDailyWeightsOfUser(currentUser.getUsername());
            if (goalWeight != null && !dailyWeights.isEmpty()) {
                for (DailyWeight dailyWeight : dailyWeights) {
                    if (dailyWeight.getWeight() <= goalWeight.getGoal()) {
                        sendTextToUser();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendTextToUser() {
        try {
            // if SEND_SMS and READ_PHONE_STATE permissions granted, send the text
            checkForAllPermissions();
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.SEND_SMS) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this,
                            Manifest.permission.READ_PHONE_STATE)
                            == PackageManager.PERMISSION_GRANTED) {
                System.out.println("Sending a text congratulating user...");
                SmsManager smsManager = SmsManager.getDefault();
                TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
                String phoneNum = telephonyManager.getLine1Number();
                if (phoneNum != null) {
                    smsManager.sendTextMessage(phoneNum, null,
                            "Congratulations, you reached your target weight!",
                            null, null);
                }
            }
            else {
                // show toast
//                Toast toast = makeText(WeightActivity.this, "Congratulations, you reached your target weight!",
//                        Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.TOP, 0, 0);
//                toast.getView().setBackgroundColor(0xFFCC99FF);
//                toast.show();
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addRecordOnClick(View view) {
        try {
            Intent intent = new Intent(this, AddRecordActivity.class);
            intent.putExtra("user", currentUser);
            startActivityForResult(intent, LAUNCH_ADD_RECORD_ACTIVITY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteRecordOnClick(View view) {
        try {
            Intent intent = new Intent(this, DeleteRecordActivity.class);
            intent.putExtra("user", currentUser);
            startActivityForResult(intent, LAUNCH_DELETE_RECORD_ACTIVITY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void editRecordOnClick(View view) {
        try {
            Intent intent = new Intent(this, EditRecordActivity.class);
            intent.putExtra("user", currentUser);
            startActivityForResult(intent, LAUNCH_CHANGE_RECORD_ACTIVITY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void editTargetOnClick(View view) {
        try {
            Intent intent = new Intent(this, EditTargetActivity.class);
            intent.putExtra("user", currentUser);
            startActivityForResult(intent, LAUNCH_CHANGE_TARGET_ACTIVITY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("MainActivity", "onActivityResult called with requestCode: " + requestCode + ", resultCode: " + resultCode);

        if(resultCode == Activity.RESULT_OK) {
            if (requestCode == LAUNCH_ADD_RECORD_ACTIVITY) {
                refreshTable();
                refreshTargetWeight();
                reachedGoalCheck();
                Toast toast = Toast.makeText(MainActivity.this, "Weight record successfully added", Toast.LENGTH_LONG);
                Log.d("MainActivity", "Weight record successfully added");
            }

            if (requestCode == LAUNCH_CHANGE_RECORD_ACTIVITY) {
                refreshTable();
                refreshTargetWeight();
                reachedGoalCheck();
                Toast toast = makeText(MainActivity.this, "Weight record successfully changed", Toast.LENGTH_LONG);
                Log.d("MainActivity", "Weight record successfully changed");
            }

            if (requestCode == LAUNCH_DELETE_RECORD_ACTIVITY) {
                cleanTable(mTableLayout);
                refreshTable();
                refreshTargetWeight();
                Toast toast = makeText(MainActivity.this, "Weight record successfully deleted", Toast.LENGTH_LONG);
                Log.d("MainActivity", "Weight record successfully deleted");
            }

            if (requestCode == LAUNCH_CHANGE_TARGET_ACTIVITY) {
                refreshTable();
                refreshTargetWeight();
                reachedGoalCheck();
                Toast toast = makeText(MainActivity.this, "Target weight successfully updated", Toast.LENGTH_LONG);
                Log.d("MainActivity", "Target weight successfully updated");
            }
            else {
                Log.d("MainActivity", "onActivityResult: requestCode not recognized");
            }
        }
    }
}