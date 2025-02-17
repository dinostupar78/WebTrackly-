package hr.javafx.webtrackly.controller;

import hr.javafx.webtrackly.app.db.SessionDbRepository;
import hr.javafx.webtrackly.app.generics.EditContainer;
import hr.javafx.webtrackly.app.model.Session;
import hr.javafx.webtrackly.utils.DateFormatterUtil;
import hr.javafx.webtrackly.utils.RowDeletion1Util;
import hr.javafx.webtrackly.utils.RowEditUtil;
import hr.javafx.webtrackly.utils.ScreenChangeButtonUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javafx.collections.FXCollections.observableArrayList;

public class SessionController {
    @FXML
    private TextField sessionTextFieldID;

    @FXML
    private DatePicker sessionDatePickerStartDate;

    @FXML
    private DatePicker sessionDatePickerEndDate;

    @FXML
    private TableView<Session> sessionTableView;

    @FXML
    private TableColumn<Session, String> sessionColumnID;

    @FXML
    private TableColumn<Session, String> sessionColumnWebsite;

    @FXML
    private TableColumn<Session, String> sessionColumnUser;

    @FXML
    private TableColumn<Session, String> sessionColumnStartTime;

    @FXML
    private TableColumn<Session, String> sessionColumnEndTime;

    @FXML
    private TableColumn<Session, String> sessionColumnDuration;

    @FXML
    private TableColumn<Session, String> sessionColumnDevice;

    @FXML
    private Button deleteSession;

    @FXML
    private LineChart<String, Number> sessionActivityLineChart;

    @FXML
    private PieChart sessionDeviceDistributionPieChart;

    private SessionDbRepository<Session> sessionRepository = new SessionDbRepository<>();

    @FXML
    private void openAddSessionScreen(ActionEvent event) {
        ScreenChangeButtonUtil.openSessionAddScreen(event);
    }

    public void initialize(){
        sessionColumnID.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getId()))
        );

        sessionColumnWebsite.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getWebsite().getWebsiteName()))
        );

        sessionColumnUser.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getUser().getUsername()))
        );

        sessionColumnStartTime.setCellValueFactory(cellData ->
                new SimpleStringProperty(DateFormatterUtil.formatLocalDateTime(cellData.getValue().getStartTime()))
        );

        sessionColumnEndTime.setCellValueFactory(cellData ->
                new SimpleStringProperty(DateFormatterUtil.formatLocalDateTime(cellData.getValue().getEndTime()))
        );

        sessionColumnDuration.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getSessionDuration()))
        );

        sessionColumnDevice.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getDeviceType()))
        );

        sessionTableView.getSortOrder().add(sessionColumnID);

        RowDeletion1Util.addSessionDeletionHandler(sessionTableView);
        deleteSession.setOnAction(event -> RowDeletion1Util.deleteSessionWithConfirmation(sessionTableView));

        RowEditUtil<Session> rowEditUtil = new RowEditUtil<>();
        rowEditUtil.addRowEditHandler(sessionTableView, selectedSession -> {
            EditContainer<Session> container = new EditContainer<>(selectedSession);
            ScreenChangeButtonUtil.openSessionEditScreen(container.getData());
        });
    }

    public void filterSessions(){
        showAverageSessionDurationLineChart();
        showDeviceDistributionPieChart();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<Session> initialSessionList = sessionRepository.findAll();

        String sessionID = sessionTextFieldID.getText();
        if(!(sessionID.isEmpty())){
            initialSessionList = initialSessionList.stream()
                    .filter(session -> session.getId().toString().contains(sessionID))
                    .toList();
        }

        if (sessionDatePickerStartDate.getValue() != null) {
            LocalDate filterStartDate = sessionDatePickerStartDate.getValue();
            initialSessionList = initialSessionList.stream()
                    .filter(session -> session.getStartTime().toLocalDate().equals(filterStartDate))
                    .collect(Collectors.toList());
        }

        if (sessionDatePickerEndDate.getValue() != null) {
            LocalDate filterEndDate = sessionDatePickerEndDate.getValue();
            initialSessionList = initialSessionList.stream()
                    .filter(session -> session.getEndTime().toLocalDate().equals(filterEndDate))
                    .collect(Collectors.toList());
        }

        ObservableList<Session> sessionObservableList = observableArrayList(initialSessionList);

        SortedList<Session> sortedList = new SortedList<>(sessionObservableList);

        sortedList.comparatorProperty().bind(sessionTableView.comparatorProperty());

        sessionTableView.setItems(sortedList);

    }

    private void showAverageSessionDurationLineChart() {
        sessionActivityLineChart.getData().clear();

        List<Session> sessions = sessionRepository.findAll();

        Map<String, Double> avgDurationByDate = sessions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getStartTime().toLocalDate().toString(),
                        Collectors.averagingDouble(s -> s.getSessionDuration().doubleValue())
                ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Average Session Duration");

        avgDurationByDate.forEach((date, avgDuration) ->
                series.getData().add(new XYChart.Data<>(date, avgDuration))
        );

        sessionActivityLineChart.getData().add(series);
    }

    private void showDeviceDistributionPieChart() {
        sessionDeviceDistributionPieChart.getData().clear();

        List<Session> sessions = sessionRepository.findAll();

        Map<String, Long> deviceCounts = sessions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getDeviceType().toString(),
                        Collectors.counting()
                ));

        deviceCounts.forEach((device, count) ->
                sessionDeviceDistributionPieChart.getData().add(new PieChart.Data(device, count))
        );

        sessionDeviceDistributionPieChart.setLegendSide(Side.BOTTOM);
        sessionDeviceDistributionPieChart.setLabelsVisible(true);
        sessionDeviceDistributionPieChart.layout();
    }
}
