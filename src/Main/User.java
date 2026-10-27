package Main;

public abstract class User {

    private String username;
    private String password;
    private String role;
    private boolean blocked;
    private int failedAttempts;

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.blocked = false;
        this.failedAttempts = 0;
    }

    public abstract String getDashboardTitle();

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void addFailedAttempt() {
        failedAttempts++;
    }

    public void resetFailedAttempts() {
        failedAttempts = 0;
    }

    public boolean checkPassword(String attempt) {
        return password != null && password.equals(attempt);
    }

    @Override
    public String toString() {
        return username + " (" + getRole() + ")";
    }

    public static class Admin extends User {

        public Admin(String username, String password) {
            super(username, password, "ADMIN");
        }

        @Override
        public String getDashboardTitle() {
            return "Admin Dashboard";
        }
    }

    public static class Staff extends User {

        public Staff(String username, String password) {
            super(username, password, "STAFF");
        }

        @Override
        public String getDashboardTitle() {
            return "Staff Dashboard";
        }
    }
}
