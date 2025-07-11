package hr.javafx.webtrackly.app.model;

/**
 * Singleton klasa koja upravlja korisničkom sesijom u aplikaciji.
 * Sadrži informacije o trenutno prijavljenom korisniku.
 */

public class UserSession {
    private static UserSession instance;
    private User currentUser;

    private UserSession(User user) {
        this.currentUser = user;
    }

    public static void createSession(User user) {
        instance = new UserSession(user);
    }

    public static UserSession getInstance() {
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }


}
