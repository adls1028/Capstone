package com.example.weighttrackerng.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.weighttrackerng.model.User;

import java.util.List;

@Dao
public interface UserDao {

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    @Query("SELECT * FROM users ORDER BY username")
    List<User> getUsers();

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(User user);

    @Update
    void updateUser(User user);

    @Delete
    void deleteUser(User user);
}