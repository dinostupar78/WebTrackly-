package hr.javafx.webtrackly.app.model;

import hr.javafx.webtrackly.app.enums.DeviceType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public class Session extends Entity{
    private Website website;
    private User user;
    private DeviceType deviceType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean isActive;
    private Long trafficRecordId;

    public Session(Long id, Website website, User user, DeviceType deviceType, LocalDateTime startTime, LocalDateTime endTime, Boolean isActive, Long trafficRecord) {
        super(id);
        this.website = website;
        this.user = user;
        this.deviceType = deviceType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isActive = isActive;
        this.trafficRecordId = trafficRecord;
    }

    public Website getWebsite() {
        return website;
    }

    public void setWebsite(Website website) {
        this.website = website;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Optional<Long> getSessionDurationMinutes(){
        return Optional.ofNullable(startTime)
                .flatMap(start -> Optional.ofNullable(endTime)
                        .map(end -> Duration.between(start, end).toMinutes()));
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Long getTrafficRecordId() {
        return trafficRecordId;
    }

    public void setTrafficRecordId(Long trafficRecordId) {
        this.trafficRecordId = trafficRecordId;
    }

    @Override
    public String toString() {
        return String.format(
                "Session[id=%d, Website='%s', User='%s', DeviceType='%s', Duration=%s, Start='%s', End='%s', Active=%s, TrafficRecordId=%s]",
                getId(), website.getWebsiteName(), user.getUsername(), deviceType.toString(), getSessionDurationMinutes().toString(),
                startTime.toString(), endTime.toString(), isActive.toString(), trafficRecordId.toString()
        );
    }

    public static class Builder{
        private Long id;
        private Website website;
        private User user;
        private DeviceType deviceType;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Boolean isActive;
        private Long trafficRecordId;

        public Builder setId(Long id) {
            this.id = id;
            return this;
        }

        public Builder setWebsite(Website website) {
            this.website = website;
            return this;
        }

        public Builder setUser(User user) {
            this.user = user;
            return this;
        }

        public Builder setDeviceType(DeviceType deviceType) {
            this.deviceType = deviceType;
            return this;
        }

        public Builder setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder setActive(Boolean active) {
            isActive = active;
            return this;
        }

        public Builder setTrafficRecordId(Long trafficRecordId) {
            this.trafficRecordId = trafficRecordId;
            return this;
        }

        public Session build(){
            return new Session(id, website, user, deviceType, startTime, endTime, isActive, trafficRecordId);
        }
    }
}
