package com.adls.weighttracker.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.adls.weighttracker.model.User;

import java.util.List;

@Dao
public interface UserDao {

    /**
     * Get list of all users
     */
    @Query("SELECT * FROM users ORDER BY username")
    public List<User> getUsers();

    @Insert(onConflict = OnConflictStrategy.ABORT)
    public void insertUser(User user);

    @Update
    public void updateUser(User user);

    @Delete
    public void deleteUser(User user);
}
