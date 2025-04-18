package hr.javafx.webtrackly.controller;

import hr.javafx.webtrackly.app.db.UserActionDbRepository1;
import hr.javafx.webtrackly.app.db.UserDbRepository1;
import hr.javafx.webtrackly.app.db.WebsiteDbRepository1;
import hr.javafx.webtrackly.app.enums.BehaviorType;
import hr.javafx.webtrackly.app.model.DataSerialization;
import hr.javafx.webtrackly.app.model.User;
import hr.javafx.webtrackly.app.model.UserAction;
import hr.javafx.webtrackly.app.model.Website;
import hr.javafx.webtrackly.utils.DataSerializeUtil;
import hr.javafx.webtrackly.utils.ShowAlertUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static hr.javafx.webtrackly.utils.DateFormatterUtil.formatLocalDateTime;

public class UserActionAddController {
    @FXML
    private ComboBox<User> actionComboBoxUser;

    @FXML
    private ComboBox<BehaviorType> actionComboBoxAction;

    @FXML
    private ComboBox<Website> actionComboBoxWebsite;

    @FXML
    private DatePicker actionDatePickerTimestamp;

    @FXML
    private TextField actionTextFieldDetails;

    private UserDbRepository1<User> userRepository = new UserDbRepository1<>();
    private WebsiteDbRepository1<Website> websiteRepository = new WebsiteDbRepository1<>();
    private UserActionDbRepository1<UserAction> userActionRepository = new UserActionDbRepository1<>();

    public void initialize() {
        actionComboBoxUser.getItems().setAll(userRepository.findAll());
        actionComboBoxAction.getItems().setAll(BehaviorType.values());
        actionComboBoxWebsite.getItems().setAll(websiteRepository.findAll());
    }

    public void addAction() {
        StringBuilder errorMessages = new StringBuilder();

        User user = actionComboBoxUser.getValue();
        Optional<User> optUser = Optional.ofNullable(user);
        if (optUser.isPresent()) {
            user = optUser.get();
        } else {
            errorMessages.append("User is required!\n");
        }


        BehaviorType action = actionComboBoxAction.getValue();
        Optional<BehaviorType> optAction = Optional.ofNullable(action);
        if (optAction.isPresent()) {
            action = optAction.get();
        } else {
            errorMessages.append("Action is required!\n");
        }

        Website website = actionComboBoxWebsite.getValue();
        Optional<Website> optWebsite = Optional.ofNullable(website);
        if (optWebsite.isPresent()) {
            website = optWebsite.get();
        } else {
            errorMessages.append("Website is required!\n");
        }

        LocalDate timestamp = actionDatePickerTimestamp.getValue();
        LocalDateTime actionTimestamp = LocalDateTime.of(timestamp, LocalTime.now());
        Optional<LocalDateTime> optTimestamp = Optional.ofNullable(actionTimestamp);
        if (optTimestamp.isPresent()) {
            actionTimestamp = optTimestamp.get();
            formatLocalDateTime(actionTimestamp);
        } else {
            errorMessages.append("Timestamp is required!\n");
        }

        String details = actionTextFieldDetails.getText();
        if (details.isEmpty()) {
            errorMessages.append("Details are required!\n");
        }

        if (errorMessages.length() > 0) {
            ShowAlertUtil.showAlert("Error", errorMessages.toString(), Alert.AlertType.ERROR);
        } else {
            UserAction newUserAction = new UserAction.Builder()
                    .setUser(user)
                    .setAction(action)
                    .setPage(website)
                    .setActionTimestamp(actionDatePickerTimestamp.getValue().atStartOfDay())
                    .setDetails(details)
                    .build();

            userActionRepository.save(newUserAction);

            DataSerialization change = new DataSerialization(
                    "UserAction",
                    "Add",
                    newUserAction.toString(),
                    newUserAction.getUser().getRole().toString(),
                    newUserAction.getTimestamp());

            DataSerializeUtil.serializeData(change);

            ShowAlertUtil.showAlert("Success", "User Action successfully added!", Alert.AlertType.INFORMATION);
            StringBuilder sb = new StringBuilder();
            sb.append("User: ")
                    .append(user)
                    .append("\nAction: ")
                    .append(action)
                    .append("\nWebsite: ")
                    .append(website)
                    .append("\nTimestamp: ")
                    .append(actionDatePickerTimestamp.getValue().atStartOfDay())
                    .append("\nDetails: ")
                    .append(details);

            actionComboBoxUser.getSelectionModel().clearSelection();
            actionComboBoxAction.getSelectionModel().clearSelection();
            actionComboBoxWebsite.getSelectionModel().clearSelection();
            actionDatePickerTimestamp.getEditor().clear();
            actionTextFieldDetails.clear();

        }
    }
}
