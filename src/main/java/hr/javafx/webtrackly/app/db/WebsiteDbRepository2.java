package hr.javafx.webtrackly.app.db;
import hr.javafx.webtrackly.app.exception.DbConnectionException;
import hr.javafx.webtrackly.app.exception.RepositoryException;
import hr.javafx.webtrackly.app.model.Website;
import hr.javafx.webtrackly.utils.DbActiveUtil;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import static hr.javafx.webtrackly.main.App.log;

/**
 * Klasa koja predstavlja repozitorij za upravljanje web stranicama u bazi podataka.
 * Ova klasa omogućuje brisanje i ažuriranje web stranica u bazi podataka.
 *
 * @param <T> Tip entiteta koji nasljeđuje klasu Website.
 */

public class WebsiteDbRepository2<T extends Website> {

    private static boolean dbLock = false;

    /**
     * Metoda za brisanje web stranice iz baze podataka.
     * Ova metoda koristi zaključavanje kako bi se spriječile istovremene promjene u bazi podataka.
     *
     * @param id ID web stranice koja se briše.
     * @throws RepositoryException Ako dođe do greške prilikom brisanja web stranice.
     */

    public synchronized void delete(Long id){
        while (dbLock) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RepositoryException(e);
            }
        }
        dbLock = true;
        try (Connection connection = DbActiveUtil.connectToDatabase()) {
            connection.setAutoCommit(false);
            performDeleteOperation(connection, id);
        } catch (IOException | SQLException | DbConnectionException e) {
            log.error("Error while deleting website from database: {}", e.getMessage());
            throw new RepositoryException("Error while deleting website from database");
        } finally {
            dbLock = false;
            notifyAll();
        }
    }

/**
     * Izvršava brisanje web stranice i svih povezanih podataka iz baze podataka.
     * Ova metoda koristi pripremljene izjave za brisanje podataka iz povezanih tablica.
     *
     * @param connection Veza s bazom podataka.
     * @param id ID web stranice koja se briše.
     */

    private void performDeleteOperation(Connection connection, Long id) {
        try {
            executeDeleteWebsiteQuery(connection, id);
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                log.error("Rollback failed: {}", rollbackEx.getMessage());
                throw new RepositoryException("Rollback failed");
            }
            log.error("Error while deleting website from database: {}", e.getMessage());
            throw new RepositoryException("Error while deleting website from database");
        }
    }

    /**
     * Izvršava pripremljene izjave za brisanje web stranice i svih povezanih podataka iz baze podataka.
     * Ova metoda koristi pripremljene izjave za brisanje podataka iz povezanih tablica.
     *
     * @param connection Veza s bazom podataka.
     * @param id ID web stranice koja se briše.
     * @throws SQLException Ako dođe do greške prilikom izvršavanja SQL upita.
     */

    private void executeDeleteWebsiteQuery(Connection connection, Long id) throws SQLException {
        try (PreparedStatement deleteUserActionStmt = connection.prepareStatement("DELETE FROM USER_ACTION WHERE WEBSITE_ID = ?");
             PreparedStatement deleteSessionStmt = connection.prepareStatement("DELETE FROM SESSION WHERE WEBSITE_ID = ?");
             PreparedStatement deleteTrafficRecordStmt = connection.prepareStatement("DELETE FROM TRAFFIC_RECORD WHERE WEBSITE_ID = ?");
             PreparedStatement deleteAppUserStmt = connection.prepareStatement("DELETE FROM APP_USER WHERE WEBSITE_ID = ?");
             PreparedStatement deleteWebsiteStmt = connection.prepareStatement("DELETE FROM WEBSITE WHERE ID = ?")) {

            deleteUserActionStmt.setLong(1, id);
            deleteUserActionStmt.executeUpdate();

            deleteSessionStmt.setLong(1, id);
            deleteSessionStmt.executeUpdate();

            deleteTrafficRecordStmt.setLong(1, id);
            deleteTrafficRecordStmt.executeUpdate();

            deleteAppUserStmt.setLong(1, id);
            deleteAppUserStmt.executeUpdate();

            deleteWebsiteStmt.setLong(1, id);
            deleteWebsiteStmt.executeUpdate();

        } catch (SQLException e) {
            log.error("Error while deleting website from database: {}", e.getMessage());
            throw new RepositoryException("Error while deleting website from database");
        }
    }

    /**
     * Metoda za ažuriranje web stranice u bazi podataka.
     * Ova metoda koristi zaključavanje kako bi se spriječile istovremene promjene u bazi podataka.
     *
     * @param entity Entitet web stranice koji se ažurira.
     * @throws RepositoryException Ako dođe do greške prilikom ažuriranja web stranice.
     */

    public synchronized void update(T entity) throws RepositoryException {
        while (dbLock) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RepositoryException(e);
            }
        }
        dbLock = true;
        String query = "UPDATE WEBSITE SET WEBSITE_NAME = ?, WEBSITE_URL = ?, WEBSITE_CATEGORY = ?, " +
                "WEBSITE_DESCRIPTION = ? WHERE ID = ?";

        try (Connection connection = DbActiveUtil.connectToDatabase();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, entity.getWebsiteName());
            stmt.setString(2, entity.getWebsiteUrl());
            stmt.setString(3, entity.getWebsiteCategory().name());
            stmt.setString(4, entity.getWebsiteDescription());
            stmt.setLong(5, entity.getId());
            stmt.executeUpdate();

        } catch (IOException | SQLException | DbConnectionException e) {
            log.error("Error while updating website in database: {}", e.getMessage());
            throw new RepositoryException("Error while updating website in database");
        } finally {
            dbLock = false;
            notifyAll();
        }
    }

}




