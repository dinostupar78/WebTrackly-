package hr.javafx.webtrackly.controller;

import hr.javafx.webtrackly.app.db.TrafficRecordDbRepository1;
import hr.javafx.webtrackly.app.db.WebsiteDbRepository1;
import hr.javafx.webtrackly.app.model.DataSerialization;
import hr.javafx.webtrackly.app.model.TrafficRecord;
import hr.javafx.webtrackly.app.model.Website;
import hr.javafx.webtrackly.utils.DataSerializeUtil;
import hr.javafx.webtrackly.utils.ShowAlertUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public class TrafficRecordAddController {
    @FXML
    private ComboBox<Website> trafficRecordComboBoxWebsite;

    @FXML
    private DatePicker trafficRecordDatePickerTimeOfVisit;

    @FXML
    private TextField trafficRecordTextFieldUserCount;

    @FXML
    private TextField trafficRecordTextFieldPageViews;

    @FXML
    private TextField trafficRecordTextFieldBounceRate;

    private WebsiteDbRepository1<Website> websiteRepository = new WebsiteDbRepository1<>();
    private TrafficRecordDbRepository1<TrafficRecord> trafficRecordRepository = new TrafficRecordDbRepository1<>();

    public void initialize() {
        trafficRecordComboBoxWebsite.getItems().setAll(websiteRepository.findAll());
    }

    public void addTrafficRecord(){
        StringBuilder errorMessages = new StringBuilder();

        Website website = trafficRecordComboBoxWebsite.getValue();
        Optional<Website> optWebsite = Optional.ofNullable(website);
        if (optWebsite.isPresent()) {
            website = optWebsite.get();
        } else {
            errorMessages.append("Website is required!\n");
        }
        String websiteName = Optional.ofNullable(website)
                .map(Website::getWebsiteName)
                .orElse("Unknown Website");

        LocalDateTime timeOfVisit = trafficRecordDatePickerTimeOfVisit.getValue().atStartOfDay();
        Optional<LocalDateTime> optDate = Optional.ofNullable(timeOfVisit);
        if (optDate.isPresent()) {
            timeOfVisit = optDate.get();
        } else {
            errorMessages.append("Time of visit is required!\n");
        }

        Integer userCount = Integer.parseInt(trafficRecordTextFieldUserCount.getText());
        if(userCount < 0 || trafficRecordTextFieldUserCount.getText().isEmpty()){
            errorMessages.append("User count is required and must be a positive number!\n");
        }

        Integer pageViews = Integer.parseInt(trafficRecordTextFieldPageViews.getText());
        if(pageViews < 0 || trafficRecordTextFieldPageViews.getText().isEmpty()){
            errorMessages.append("Page views are required and must be a positive number!\n");
        }

        BigDecimal bounceRate = new BigDecimal(trafficRecordTextFieldBounceRate.getText());
        if(bounceRate.compareTo(BigDecimal.ZERO) < 0 || bounceRate.compareTo(BigDecimal.valueOf(100)) > 0){
            errorMessages.append("Bounce rate must be a positive number less than 100!\n");
        }

        if (errorMessages.length() > 0) {
            ShowAlertUtil.showAlert("Error", errorMessages.toString(), Alert.AlertType.ERROR);
        } else {

            TrafficRecord newTrafficRecord = new TrafficRecord.Builder()
                    .setWebsite(website)
                    .setTimeOfVisit(timeOfVisit)
                    .setUserCount(userCount)
                    .setPageViews(pageViews)
                    .setBounceRate(bounceRate)
                    .build();

            trafficRecordRepository.save(newTrafficRecord);

            DataSerialization change = new DataSerialization(
                    "Traffic Record Added",
                    "N/A",
                    newTrafficRecord.toString(),
                    websiteName,
                    LocalDateTime.now()
            );

            DataSerializeUtil.serializeData(change);
            ShowAlertUtil.showAlert("Success", "Traffic Record has been successfully added!", Alert.AlertType.INFORMATION);
            clearForm();

        }
    }

    private void clearForm() {
        trafficRecordComboBoxWebsite.getSelectionModel().clearSelection();
        trafficRecordDatePickerTimeOfVisit.getEditor().clear();
        trafficRecordTextFieldUserCount.clear();
        trafficRecordTextFieldPageViews.clear();
        trafficRecordTextFieldBounceRate.clear();
    }
}
