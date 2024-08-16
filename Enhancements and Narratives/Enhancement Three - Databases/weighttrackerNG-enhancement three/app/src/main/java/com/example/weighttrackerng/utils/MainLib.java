package com.example.weighttrackerng.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.weighttrackerng.database.DailyWeightDao;
import com.example.weighttrackerng.database.GoalWeightDao;
import com.example.weighttrackerng.model.DailyWeight;
import com.example.weighttrackerng.model.GoalWeight;
import com.example.weighttrackerng.model.User;

import java.util.List;

public class MainLib {
    private static final String TAG = "MainLib";

    public static void checkForAllPermissions(Activity activity) {
        try {
            // Check and request permissions
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE}, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendTextToUser(Activity activity, String phoneNumber, String message) {
        try {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                Toast.makeText(activity, "SMS sent successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "SMS permission not granted", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void refreshTargetWeight(GoalWeightDao goalWeightDao, User currentUser, TextView targetWeightView) {
        try {
            int count = goalWeightDao.countGoalEntries(currentUser.getUsername());
            if (count == 0) {
                GoalWeight defaultGoalWeight = new GoalWeight(150.0, currentUser.getUsername());
                goalWeightDao.insertGoalWeight(defaultGoalWeight);
            }
            GoalWeight currentGoal = goalWeightDao.getSingleGoalWeight(currentUser.getUsername());
            targetWeightView.setText(currentGoal.getGoal() + " lbs");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void refreshTable(DailyWeightDao dailyWeightDao, User currentUser, TableLayout tableLayout, TableRow noRecordsRow) {
        try {
            cleanTable(tableLayout);
            List<DailyWeight> userDailyWeights = dailyWeightDao.getDailyWeightsOfUser(currentUser.getUsername());
            if (userDailyWeights.size() == 0) {
                addNoRecordsRow(tableLayout, noRecordsRow);
            } else {
                // Add rows to the table for each DailyWeight record
                for (DailyWeight dailyWeight : userDailyWeights) {
                    TableRow row = new TableRow(tableLayout.getContext());
                    TextView dateView = new TextView(tableLayout.getContext());
                    TextView weightView = new TextView(tableLayout.getContext());
                    dateView.setText(dailyWeight.getDate().toString());
                    weightView.setText(String.valueOf(dailyWeight.getWeight()));
                    row.addView(dateView);
                    row.addView(weightView);
                    tableLayout.addView(row);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void cleanTable(TableLayout table) {
        try {
            int childCount = table.getChildCount();
            if (childCount > 1) {
                table.removeViews(1, childCount - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addNoRecordsRow(TableLayout table, TableRow noRecordsRow) {
        if (noRecordsRow.getParent() == null) {
            table.addView(noRecordsRow);
        }
        Log.d(TAG, "addNoRecordsRow() EXECUTED.");
    }
}
