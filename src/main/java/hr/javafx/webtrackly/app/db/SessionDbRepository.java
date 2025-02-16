package hr.javafx.webtrackly.app.db;

import hr.javafx.webtrackly.app.enums.DeviceType;
import hr.javafx.webtrackly.app.exception.RepositoryAccessException;
import hr.javafx.webtrackly.app.model.Session;
import hr.javafx.webtrackly.app.model.User;
import hr.javafx.webtrackly.app.model.Website;
import hr.javafx.webtrackly.utils.DbActiveUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SessionDbRepository<T extends Session> extends AbstractDbRepository<T> {
    private static final String FIND_BY_ID_QUERY =
            "SELECT ID, WEBSITE_ID, USER_ID, DEVICE_TYPE, SESSION_DURATION, START_TIME, END_TIME, IS_ACTIVE, TRAFFIC_RECORD_ID FROM SESSION WHERE ID = ?";

    private static final String FIND_ALL_QUERY =
            "SELECT ID, WEBSITE_ID, USER_ID, DEVICE_TYPE, SESSION_DURATION, START_TIME, END_TIME, IS_ACTIVE, TRAFFIC_RECORD_ID FROM SESSION";

    @Override
    public T findById(Long id) throws RepositoryAccessException {
        if (!DbActiveUtil.isDatabaseOnline()) {
            throw new RepositoryAccessException("Database is inactive. Please check your connection.");
        }
        try (Connection connection = DbActiveUtil.connectToDatabase();
             PreparedStatement stmt = connection.prepareStatement(FIND_BY_ID_QUERY)) {

            stmt.setLong(1, id);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return (T) extractFromSessionResultSet(resultSet);
                } else {
                    throw new RepositoryAccessException("Session with id " + id + " not found!");
                }
            }
        } catch (IOException | SQLException e) {
            throw new RepositoryAccessException(e);
        }
    }

    @Override
    public List<T> findAll() throws RepositoryAccessException {
        if (!(DbActiveUtil.isDatabaseOnline())) {
            return List.of();
        }
        List<T> sessions = new ArrayList<>();
        try (Connection connection = DbActiveUtil.connectToDatabase();
             Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery(FIND_ALL_QUERY)) {

            while (resultSet.next()) {
                sessions.add((T) extractFromSessionResultSet(resultSet));
            }
        } catch (IOException | SQLException e) {
            throw new RepositoryAccessException(e);
        }
        return sessions;
    }

    @Override
    public void save(List<T> entities) throws RepositoryAccessException {
        String sql = "INSERT INTO SESSION (WEBSITE_ID, USER_ID, DEVICE_TYPE, SESSION_DURATION, START_TIME, END_TIME, IS_ACTIVE, TRAFFIC_RECORD_ID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DbActiveUtil.connectToDatabase();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            for (T entity : entities) {
                stmt.setLong(1, entity.getWebsite().getId());
                stmt.setLong(2, entity.getUser().getId());
                stmt.setString(3, entity.getDeviceType().name());
                stmt.setBigDecimal(4, entity.getSessionDuration());
                stmt.setTimestamp(5, Timestamp.valueOf(entity.getStartTime()));
                stmt.setTimestamp(6, Timestamp.valueOf(entity.getEndTime()));
                stmt.setBoolean(7, entity.getActive());
                stmt.setLong(8, entity.getTrafficRecordId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException | IOException e) {
            throw new RepositoryAccessException(e);
        }

    }

    @Override
    public void save(T entity) throws RepositoryAccessException {
        String sql = "INSERT INTO SESSION (WEBSITE_ID, USER_ID, DEVICE_TYPE, SESSION_DURATION, START_TIME, END_TIME, IS_ACTIVE, TRAFFIC_RECORD_ID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DbActiveUtil.connectToDatabase();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, entity.getWebsite().getId());
            stmt.setLong(2, entity.getUser().getId());
            stmt.setString(3, entity.getDeviceType().name());
            stmt.setBigDecimal(4, entity.getSessionDuration());
            stmt.setTimestamp(5, Timestamp.valueOf(entity.getStartTime()));
            stmt.setTimestamp(6, Timestamp.valueOf(entity.getEndTime()));
            stmt.setBoolean(7, entity.getActive());
            stmt.setLong(8, entity.getTrafficRecordId());
            stmt.executeUpdate();

        } catch (SQLException | IOException e) {
            throw new RepositoryAccessException(e);
        }

    }

    public static Session extractFromSessionResultSet(ResultSet resultSet) {
        try {
            Long id = resultSet.getLong("ID");
            
            Long userId = resultSet.getLong("USER_ID");
            UserDbRepository<User> userRepository = new UserDbRepository<>();
            User user = userRepository.findById(userId);

            Long websiteId = resultSet.getLong("WEBSITE_ID");
            WebsiteDbRepository<Website> websiteRepository = new WebsiteDbRepository<>();
            Website website = websiteRepository.findById(websiteId);

            String deviceTypeStr = resultSet.getString("DEVICE_TYPE");
            DeviceType deviceType = DeviceType.valueOf(deviceTypeStr.toUpperCase());
            BigDecimal sessionDuration = resultSet.getBigDecimal("SESSION_DURATION");
            LocalDateTime startTime = resultSet.getTimestamp("START_TIME").toLocalDateTime();
            LocalDateTime endTime = resultSet.getTimestamp("END_TIME").toLocalDateTime();
            Boolean active = resultSet.getObject("IS_ACTIVE") != null ? resultSet.getBoolean("IS_ACTIVE") : null;

            Long trafficRecordId = resultSet.getLong("TRAFFIC_RECORD_ID");


            return new Session(id, website, user, deviceType, sessionDuration, startTime, endTime, active, trafficRecordId);
        } catch (SQLException e) {
            throw new RepositoryAccessException(e);
        }
    }

    public void update(T entity) throws RepositoryAccessException {
        String query = "UPDATE SESSION " +
                "SET WEBSITE_ID = ?, USER_ID = ?, DEVICE_TYPE = ?, SESSION_DURATION = ?, " +
                "START_TIME = ?, END_TIME = ?, IS_ACTIVE = ?, TRAFFIC_RECORD_ID = ? " +
                "WHERE ID = ?";
        try (Connection connection = DbActiveUtil.connectToDatabase();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setLong(1, entity.getWebsite().getId());
            stmt.setLong(2, entity.getUser().getId());
            stmt.setString(3, entity.getDeviceType().name());
            stmt.setBigDecimal(4, entity.getSessionDuration());
            stmt.setTimestamp(5, Timestamp.valueOf(entity.getStartTime()));
            stmt.setTimestamp(6, Timestamp.valueOf(entity.getEndTime()));
            stmt.setBoolean(7, entity.getActive());
            stmt.setLong(8, entity.getTrafficRecordId());
            stmt.setLong(9, entity.getId());

            stmt.executeUpdate();
        } catch (IOException | SQLException e) {
            throw new RepositoryAccessException(e);
        }
    }

    public void delete(Long id) throws RepositoryAccessException {
        try (Connection connection = DbActiveUtil.connectToDatabase()) {
            connection.setAutoCommit(false);
            performDeleteOperation(connection, id);
        } catch (IOException | SQLException e) {
            throw new RepositoryAccessException(e);
        }
    }

    private void performDeleteOperation(Connection connection, Long id) throws RepositoryAccessException {
        try {
            executeDeleteSessionQuery(connection, id);
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                throw new RepositoryAccessException("Rollback failed: " + rollbackEx.getMessage(), rollbackEx);
            }
            throw new RepositoryAccessException(e);
        }
    }

    private void executeDeleteSessionQuery(Connection connection, Long id) throws SQLException {
        String deleteUserQuery = "DELETE FROM SESSION WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(deleteUserQuery)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

}
