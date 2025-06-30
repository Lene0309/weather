package edu.neu.weather.activity.SQLite;

public class User {
    private int id;
    private String name;
    private String email;
    private String profileImagePath;
    private String backgroundImagePath;
    private int themePreference; // 0=light, 1=dark, 2=system
    private String password;

    // 省略getter和setter方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

    public String getBackgroundImagePath() {
        return backgroundImagePath;
    }

    public void setBackgroundImagePath(String backgroundImagePath) {
        this.backgroundImagePath = backgroundImagePath;
    }

    public int getThemePreference() {
        return themePreference;
    }

    public void setThemePreference(int themePreference) {
        this.themePreference = themePreference;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
