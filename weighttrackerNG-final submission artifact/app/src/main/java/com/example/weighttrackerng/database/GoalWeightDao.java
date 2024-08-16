package com.example.weighttrackerng.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.weighttrackerng.model.GoalWeight;

import java.util.List;

@Dao
public interface GoalWeightDao {

    @Query("UPDATE `goalWeight` SET goal = :weight WHERE username = :username")
    int updateGoalWeight(double weight, String username);

    /**
     * Get list of all this user's goal weights
     * There should only be one record
     */
    @Query("SELECT * FROM goalWeight WHERE username = :username ORDER BY id")
    public List<GoalWeight> getAllUserGoalWeights(String username);

    /**
     * Read the single record in this table for the current user
     */
    @Query("SELECT * FROM goalWeight WHERE username = :username")
    public GoalWeight getSingleGoalWeight(String username);

    /**
     * Count number of records in this table
     */
    @Query("SELECT count(*) FROM goalWeight WHERE username = :username")
    public int countGoalEntries(String username);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    public void insertGoalWeight(GoalWeight goalWeight);
}