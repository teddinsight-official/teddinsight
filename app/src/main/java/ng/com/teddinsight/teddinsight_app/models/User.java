package ng.com.teddinsight.teddinsight_app.models;


import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class User {

    public String id;
    public String username;
    public String email;
    public String firstName;
    public String lastName;
    public String deviceToken;
    public String role;
    public boolean hasAccess;

    public User() {
    }

    public User(String username, String firstName, String lastName, String email, String role, String deviceToken) {
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.deviceToken = deviceToken;
        this.role = role;
        this.hasAccess = true;
    }

    public User(String id, String username) {
        this.id = id;
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRole() {
        return role;
    }


    public void setRole(String role) {
        this.role = role;
    }



    public static String getTableName() {
        return "Users";
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("email", email);
        result.put("firstname", firstName);
        result.put("lastname", lastName);
        result.put("deviceToken", deviceToken);
        result.put("role", role);
        result.put("dateRegistered", ServerValue.TIMESTAMP);
        result.put("hasAccess", this.hasAccess);
        return result;
    }

    @Exclude
    public Map<String, String> toStringMap() {
        HashMap<String, String> result = new HashMap<>();
        result.put("username", username);
        result.put("email", email);
        result.put("firstname", firstName);
        result.put("lastname", lastName);
        result.put("deviceToken", deviceToken);
        result.put("role", role);
        return result;
    }
}