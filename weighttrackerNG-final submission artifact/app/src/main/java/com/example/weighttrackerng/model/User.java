package com.example.weighttrackerng.model;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "users",
        indices = {@Index(value = {"email"}, unique = true), @Index(value = {"username"}, unique = true)})
public class User implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String username;
    private String email;
    private String password;

    // Default constructor
    public User() {}

    // Constructor with parameters
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}