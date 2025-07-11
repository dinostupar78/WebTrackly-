package hr.javafx.webtrackly.app.model;
import java.io.Serializable;
import java.util.Optional;

/**
 * Klasa koja predstavlja korisnika u aplikaciji WebTrackly.
 * Nasljeđuje klasu Person i implementira Serializable sučelje.
 * Sadrži informacije o korisničkom imenu, emailu, lozinki, ulozi i ID-u web stranice.
 */

public class User extends Person implements Serializable {
    private String username;
    private String email;
    private String password;
    private transient Role role;
    private Long websiteId;

    public User() {
        super();
    }

    public User(Long id, String name, String surname, PersonalData personalData,
                String username, String email, String password, Role role, Long websiteId) {
        super(id, name, surname, personalData);
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.websiteId = websiteId;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Long getWebsiteId() {
        return websiteId;
    }

    public void setWebsiteId(Long websiteId) {
        this.websiteId = websiteId;
    }

    /**
     * Builder klasa za izgradnju objekta User.
     * Omogućuje postavljanje svih atributa korisnika na jednostavan način.
     */

    public static class Builder {
        private Long id;
        private String name;
        private String surname;
        private PersonalData personalData;
        private String username;
        private String email;
        private String password;
        private Role role;
        private Long websiteId;

        public Builder setId(Long id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setSurname(String surname) {
            this.surname = surname;
            return this;
        }

        public Builder setPersonalData(PersonalData personalData) {
            this.personalData = personalData;
            return this;
        }
        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setRole(Role role) {
            this.role = role;
            return this;
        }

        public Builder setWebsiteId(Long websiteId) {
            this.websiteId = websiteId;
            return this;
        }

        public User build() {
            return new User(id, name, surname, personalData, username, email, password, role, websiteId);
        }
    }

    /**
     * Vraća string reprezentaciju korisnika.
     * Uključuje korisničko ime, email i ID web stranice.
     *
     * @return String reprezentacija korisnika.
     */

    @Override
    public String toString() {
        return String.format(
                "User: %s | Email: %s | WebsiteId: %d",
                Optional.ofNullable(username)
                        .orElse("N/A"),
                Optional.ofNullable(email)
                        .orElse("N/A"),
                Optional.ofNullable(websiteId)
                        .orElse(-1L)
        );
    }
}
