package hr.javafx.webtrackly.app.db;

import hr.javafx.webtrackly.app.exception.DbConnectionException;
import hr.javafx.webtrackly.app.exception.DbDataException;
import hr.javafx.webtrackly.app.exception.EmptyResultSetException;
import hr.javafx.webtrackly.app.exception.RepositoryException;
import hr.javafx.webtrackly.app.model.Session;
import hr.javafx.webtrackly.app.model.TrafficRecord;
import hr.javafx.webtrackly.app.model.Website;
import hr.javafx.webtrackly.utils.DbActiveUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static hr.javafx.webtrackly.main.HelloApplication.log;

public class TrafficRecordDbRepository1<T extends TrafficRecord> extends AbstractDbRepository<T> {
    private static final String FIND_BY_ID_QUERY = "SELECT ID, WEBSITE_ID, TIME_OF_VISIT , USER_COUNT, PAGE_VIEWS, BOUNCE_RATE FROM TRAFFIC_RECORD WHERE ID = ?";

    private static final String FIND_ALL_QUERY = "SELECT ID, WEBSITE_ID, TIME_OF_VISIT , USER_COUNT, PAGE_VIEWS, BOUNCE_RATE FROM TRAFFIC_RECORD";

    @Override
    public T findById(Long id){
        if (!DbActiveUtil.isDatabaseOnline()) {
            log.error("Database is inactive. Please check your connection.");
            throw new RepositoryException("Database is inactive. Please check your connection.");
        }
        try (Connection connection = DbActiveUtil.connectToDatabase();
             PreparedStatement stmt = connection.prepareStatement(FIND_BY_ID_QUERY)) {

            stmt.setLong(1, id);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return (T) extractTrafficRecordFromResultSet(resultSet);
                } else {
                    log.error("Traffic record with id {} not found!", id);
                    throw new EmptyResultSetException("Traffic record with id " + id + " not found!");
                }
            }
        } catch (IOException | SQLException | DbConnectionException e) {
            log.error("Error fetching traffic record with id {}", id, e);
            throw new RepositoryException("Error fetching traffic record with id ");
        }
    }

    @Override
    public List<T> findAll(){
        if (!(DbActiveUtil.isDatabaseOnline())) {
            return List.of();
        }
        List<T> trafficRecords = new ArrayList<>();
        try (Connection connection = DbActiveUtil.connectToDatabase();
             Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery(FIND_ALL_QUERY)) {

            while (resultSet.next()) {
                trafficRecords.add((T) extractTrafficRecordFromResultSet(resultSet));
            }
        } catch (IOException | SQLException | DbConnectionException e) {
            log.error("Error fetching traffic records ", e);
            throw new RepositoryException("Error fetching traffic records");
        }
        return trafficRecords;
    }

    @Override
    public void save(List<T> entities){
        String sql = "INSERT INTO TRAFFIC_RECORD (WEBSITE_ID, TIME_OF_VISIT , USER_COUNT, PAGE_VIEWS, BOUNCE_RATE) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DbActiveUtil.connectToDatabase();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            for (T entity : entities) {
                stmt.setLong(1, entity.getWebsite().getId());
                stmt.setTimestamp(2, Timestamp.valueOf(entity.getTimeOfVisit()));
                stmt.setInt(3, entity.getUserCount());
                stmt.setInt(4, entity.getPageViews());
                stmt.setBigDecimal(5, entity.getBounceRate());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException | IOException | DbConnectionException e) {
            log.error("Error saving traffic records ", e);
            throw new RepositoryException("Error saving traffic records");
        }

    }

    @Override
    public void save(T entity){
        String sql = "INSERT INTO TRAFFIC_RECORD (WEBSITE_ID, TIME_OF_VISIT , USER_COUNT, PAGE_VIEWS, BOUNCE_RATE) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DbActiveUtil.connectToDatabase();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, entity.getWebsite().getId());
            stmt.setTimestamp(2, Timestamp.valueOf(entity.getTimeOfVisit()));
            stmt.setInt(3, entity.getUserCount());
            stmt.setInt(4, entity.getPageViews());
            stmt.setBigDecimal(5, entity.getBounceRate());
            stmt.executeUpdate();
        } catch (SQLException | IOException | DbConnectionException e) {
            log.error("Error saving traffic records ", e);
            throw new RepositoryException("Error saving traffic records");
        }

    }

    private static TrafficRecord extractTrafficRecordFromResultSet(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("ID");

        Long websiteId = resultSet.getLong("WEBSITE_ID");
        WebsiteDbRepository1<Website> websiteRepository = new WebsiteDbRepository1<>();
        Website website = websiteRepository.findById(websiteId);

        LocalDateTime timeOfVisit = resultSet.getTimestamp("TIME_OF_VISIT").toLocalDateTime();
        Integer userCount = resultSet.getInt("USER_COUNT");
        Integer pageViews = resultSet.getInt("PAGE_VIEWS");
        BigDecimal bounceRate = resultSet.getBigDecimal("BOUNCE_RATE");

        List<Session> sessions = fetchSessionsForTrafficRecord(id);

        return new TrafficRecord.Builder()
                .setId(id)
                .setWebsite(website)
                .setTimeOfVisit(timeOfVisit)
                .setUserCount(userCount)
                .setPageViews(pageViews)
                .setBounceRate(bounceRate)
                .setSessions(sessions)
                .build();

    }

    private static List<Session> fetchSessionsForTrafficRecord(Long trafficRecordId){
        List<Session> sessions = new ArrayList<>();
        String query = "SELECT ID, WEBSITE_ID, USER_ID, DEVICE_TYPE, SESSION_DURATION, START_TIME, END_TIME, IS_ACTIVE, TRAFFIC_RECORD_ID FROM SESSION\n" +
                "WHERE TRAFFIC_RECORD_ID = ?";
        try (Connection connection = DbActiveUtil.connectToDatabase();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setLong(1, trafficRecordId);
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    Session session = SessionDbRepository1.extractFromSessionResultSet(resultSet);
                    sessions.add(session);
                }
            }
        } catch (SQLException | IOException | DbConnectionException | DbDataException e) {
            log.error("Error fetching users for website ID: {}", trafficRecordId, e);
            throw new RepositoryException("Error fetching users for website ID");
        }
        return sessions;
    }


}
