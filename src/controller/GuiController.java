package controller;

import javafx.animation.FadeTransition;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import jfxtras.scene.control.agenda.Agenda;
import model.*;
import org.controlsfx.control.ToggleSwitch;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GuiController {
    @FXML private DatePicker startDate;
    @FXML private DatePicker endDate;
    @FXML private Label filePath;
    @FXML private Button browseBtn;
    @FXML private ProgressBar progressBar;
    @FXML private Button createBtn;

    @FXML private Tab timetableTab;
    @FXML private TableView timetableTable;
    @FXML private TableColumn<Timetable, Number> hardConflictCol;
    @FXML private TableColumn<Timetable, Number> sameDayCol;
    @FXML private TableColumn<Timetable, Number> nextDayCol;
    @FXML private TableColumn<Timetable, Number> weekendCol;
    @FXML private Agenda agenda;
    private final WeekFields weekFields = WeekFields.of(Locale.getDefault());

    @FXML private ButtonBar buttonBar;
    @FXML private Button prevWeek;
    @FXML private Button nextWeek;
    @FXML private Button sendBtn;
    @FXML private Button printBtn;
    @FXML private Label message;
    private final FadeTransition fadeOut = new FadeTransition();

    @FXML private Tab settingsTab;
    @FXML private Accordion accordion;
    @FXML private TitledPane algoSettings;
    @FXML private Slider nbRooms;
    @FXML private Slider nbDays;

    @FXML private TitledPane serverSettings;
    @FXML private ToggleSwitch authSwitch;
    @FXML private ToggleSwitch starttlsSwitch;
    @FXML private TextField serverField;
    @FXML private TextField portField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    @FXML private TitledPane mailSettings;
    @FXML private TextField subjectField;
    @FXML private TextArea messageField;

    private final Mail mail = new Mail();

    private final GuiModel model = GuiModel.getInstance();

    private final PseudoClass errorClass = PseudoClass.getPseudoClass("error");
    private final PseudoClass infoClass = PseudoClass.getPseudoClass("info");

    @FXML public void initialize() {
        // Reset error pseudoclass on value change
        startDate.valueProperty().addListener((obs, oldVal, newVal) -> startDate.pseudoClassStateChanged(errorClass, false));
        endDate.valueProperty().addListener(((obs, oldVal, newVal) -> endDate.pseudoClassStateChanged(errorClass, false)));
        filePath.textProperty().addListener(((obs, oldVal, newVal) -> filePath.pseudoClassStateChanged(errorClass, false)));
        // Initialize controls
        model.progressProperty().addListener((obs, oldVal, newVal) -> progressBar.setProgress(newVal.doubleValue()));
        nbRooms.valueProperty().addListener((obs, oldVal, newVal) -> GA.NB_ROOMS = newVal.intValue());
        nbDays.valueProperty().addListener((obs, oldVal, newVal) -> GA.NB_DAYS_IN_WEEK = newVal.intValue());
        buttonBar.getButtons().forEach(b -> {
            if (b instanceof Button)
                b.setDisable(true);
        });
        accordion.setExpandedPane(algoSettings);
        initErrorMessage();
        initTimetableTable();
        initAgenda();
        initMail();
    }

    private void initMail() {
        // Init switches
        authSwitch.selectedProperty().addListener((obs, oldVal, newVal) -> {
            authSwitch.setText((authSwitch.isSelected()) ? "On" : "Off");
            mail.setSmtpAuth((authSwitch.isSelected()) ? "true" : "false");
        });
        starttlsSwitch.selectedProperty().addListener((obs, oldVal, newVal) -> {
            starttlsSwitch.setText((starttlsSwitch.isSelected()) ? "On" : "Off");
            mail.setSmtpStarttls((starttlsSwitch.isSelected()) ? "true" : "false");
        });
        authSwitch.setSelected(true);
        starttlsSwitch.setSelected(true);
        // Bind to Mail settings
        serverField.textProperty().bindBidirectional(mail.smtpServerProperty());
        portField.textProperty().bindBidirectional(mail.smtpPortProperty());
        emailField.textProperty().bindBidirectional(mail.senderProperty());
        passwordField.textProperty().bindBidirectional(mail.passwordProperty());
        subjectField.textProperty().bindBidirectional(mail.subjectProperty());
        messageField.textProperty().bindBidirectional(mail.bodyProperty());
        // Reset error pseudoclass on value change
        serverField.textProperty().addListener((obs, oldVal, newVal) -> serverField.pseudoClassStateChanged(errorClass, false));
        portField.textProperty().addListener((obs, oldVal, newVal) -> portField.pseudoClassStateChanged(errorClass, false));
        emailField.textProperty().addListener((obs, oldVal, newVal) -> emailField.pseudoClassStateChanged(errorClass, false));
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> passwordField.pseudoClassStateChanged(errorClass, false));
        subjectField.textProperty().addListener((obs, oldVal, newVal) -> subjectField.pseudoClassStateChanged(errorClass, false));
        messageField.textProperty().addListener((obs, oldVal, newVal) -> messageField.pseudoClassStateChanged(errorClass, false));
        // init texfield values
        serverField.setText("smtp.office365.com");
        portField.setText("587");
        subjectField.setText("[CO600] Exam date and time for group [GROUP]");
        messageField.setText("Hello,\n\n" +
                "This automatic message is to confirm that the final year group project examination for group [GROUP] " +
                "has been scheduled for the [DATE] from [START_TIME] to [END_TIME].\n" +
                "Further information about the room allocated to the exam will be communicated shortly.\n\n" +
                "Yours sincerely,\n" +
                "Timetabling services");
    }

    private void initErrorMessage() {
        fadeOut.setNode(message);
        fadeOut.setDuration(Duration.millis(2500));
        fadeOut.setDelay(Duration.millis(2000));
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setCycleCount(1);
        fadeOut.setAutoReverse(false);
        fadeOut.setOnFinished(actionEvent -> model.setMessage(""));
        model.messageProperty().addListener((obs, oldVal, newVal) -> {
            message.setText(newVal);
            message.setOpacity(1);
            fadeOut.playFromStart();
        });
    }

    private void initTimetableTable() {
        timetableTable.setItems(model.timetablesProperty());
        hardConflictCol.setCellValueFactory(cellData -> cellData.getValue().hardProperty());
        sameDayCol.setCellValueFactory(cellData -> cellData.getValue().sameDayProperty());
        nextDayCol.setCellValueFactory(cellData -> cellData.getValue().nextDayProperty());
        weekendCol.setCellValueFactory(cellData -> cellData.getValue().weekendProperty());
        timetableTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> model.setCurrentTimetable((Timetable) newVal));
    }

    private void initAgenda() {
        // setup appointment groups
        final Map<String, Agenda.AppointmentGroup> lAppointmentGroupMap = new TreeMap<>();
        for (Agenda.AppointmentGroup lAppointmentGroup : agenda.appointmentGroups()) {
            lAppointmentGroupMap.put(lAppointmentGroup.getDescription(), lAppointmentGroup);
        }
        // display
        model.displayDateProperty().bindBidirectional(agenda.displayedLocalDateTime());
        model.currentTimetableProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                agenda.appointments().clear();
                IntStream.range(0, newVal.getGroups().length)
                        .forEach(g -> {
                            LocalDate date = Term.getInstance().getDay(newVal.getGroups()[g])
                                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                            LocalDateTime start = (Term.getInstance().isMorning(newVal.getGroups()[g])) ? date.atTime(10, 0) : date.atTime(14, 0);
                            LocalDateTime end = (Term.getInstance().isMorning(newVal.getGroups()[g])) ? date.atTime(13, 0) : date.atTime(17, 0);
                            String students = model.getStudents().stream()
                                    .filter(x -> x.getGroups().contains(g + 1))
                                    .map(x -> x.getLogin())
                                    .collect(Collectors.joining(", "));
                            String examiners = model.getExaminers().stream()
                                    .filter(x -> x.getGroups().contains(g + 1))
                                    .map(x -> x.getLogin())
                                    .collect(Collectors.joining(", "));
                            agenda.appointments().add(
                                    new Agenda.AppointmentImplLocal()
                                            .withStartLocalDateTime(start)
                                            .withEndLocalDateTime(end)
                                            .withSummary("Group " + (g + 1) + "\n" +
                                                    "Students : " + students + "\n" +
                                                    "Examiners : " + examiners)
                                            .withAppointmentGroup(lAppointmentGroupMap.get("group" + String.format("%02d", g + 1)))
                            );
                        });
            }
        });
        // init prevWeek and nextWeek buttons
        model.displayDateProperty().addListener((obs, oldVal, newVal) -> setDisablePrevNextButtons(newVal));
    }

    private void showError(String msg) {
        message.pseudoClassStateChanged(infoClass, false);
        message.pseudoClassStateChanged(errorClass, true);
        model.setMessage(msg);
    }

    private void showInfo(String msg) {
        message.pseudoClassStateChanged(errorClass, false);
        message.pseudoClassStateChanged(infoClass, true);
        model.setMessage(msg);
    }

    @FXML public void handleBrowseButtonAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open CSV File");
        fileChooser.setInitialDirectory(new File(FileSystemView.getFileSystemView().getDefaultDirectory().getPath()));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV", "*.csv"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File file = fileChooser.showOpenDialog(browseBtn.getScene().getWindow());
        if (file != null)
            filePath.setText(file.getAbsolutePath());
    }

    private void setDisablePrevNextButtons(LocalDateTime newVal) {
        LocalDateTime start = Term.getInstance().getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atTime(14, 0);
        LocalDateTime end = Term.getInstance().getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atTime(14, 0);
        prevWeek.setDisable(!newVal.with(weekFields.dayOfWeek(), 1).isAfter(start.with(weekFields.dayOfWeek(), 1)));
        nextWeek.setDisable(!newVal.with(weekFields.dayOfWeek(), 1).isBefore(end.with(weekFields.dayOfWeek(), 1)));
    }

    private void setDisableAllButtons(boolean value) {
        createBtn.setDisable(value);
        browseBtn.setDisable(value);
        buttonBar.getButtons().forEach(b -> {
            if (b instanceof Button)
                b.setDisable(value);
        });
        if (!value)
            setDisablePrevNextButtons(model.getDisplayDate());
    }

    @FXML public void handleCreateButtonAction(ActionEvent actionEvent) {
        if (startDate.getValue() != null && endDate.getValue() != null && !filePath.getText().isEmpty()) {
            startDate.pseudoClassStateChanged(errorClass, false);
            endDate.pseudoClassStateChanged(errorClass, false);
            filePath.pseudoClassStateChanged(errorClass, false);
            String start = startDate.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String end = endDate.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            Term t = Term.getInstance();
            if (t.init(start, end) != 0) {
                showError("Invalid start / end dates");
                startDate.pseudoClassStateChanged(errorClass, true);
                endDate.pseudoClassStateChanged(errorClass, true);
                // TODO show invalid start / end date error
                return;
            }
            setDisableAllButtons(true);
            TimetableTask task = new TimetableTask(filePath.getText());
            task.setOnSucceeded(t1 -> {
                List<Timetable> best = task.getValue();
                if (best == null) {
                    showError("Invalid CSV file");
                    filePath.pseudoClassStateChanged(errorClass, true);
                    setDisableAllButtons(false);
                    return;
                }
                model.setDisplayDate(Term.getInstance().getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                        .with(weekFields.dayOfWeek(), 1).atTime(14, 0));
                model.setTimetables(best);
                model.setCurrentTimetable(best.get(0));
                // Set selected tab
                SingleSelectionModel<Tab> selectionModel = timetableTab.getTabPane().getSelectionModel();
                selectionModel.select(timetableTab);
                setDisableAllButtons(false);
            });
            new Thread(task).start();
        } else if (filePath.getText().isEmpty()) {
            showError("Please choose an input CSV file");
        } else {
            startDate.pseudoClassStateChanged(errorClass, true);
            endDate.pseudoClassStateChanged(errorClass, true);
            showError("Please fill exam start and end dates");
        }
    }

    @FXML public void handlePreviousButtonAction(ActionEvent actionEvent) {
        model.setDisplayDate(model.getDisplayDate().minusWeeks(1));
    }

    @FXML public void handleNextButtonAction(ActionEvent actionEvent) {
        model.setDisplayDate(model.getDisplayDate().plusWeeks(1));
    }

    private void setErrorMailFields(boolean value) {
        serverField.pseudoClassStateChanged(errorClass, value);
        portField.pseudoClassStateChanged(errorClass, value);
        emailField.pseudoClassStateChanged(errorClass, value);
        passwordField.pseudoClassStateChanged(errorClass, value);
        subjectField.pseudoClassStateChanged(errorClass, value);
        messageField.pseudoClassStateChanged(errorClass, value);
    }

    @FXML public void handleSendButtonAction(ActionEvent actionEvent) {
        if (serverField.getText() != null && portField.getText() != null &&
                emailField.getText() != null && passwordField.getText() != null &&
                subjectField.getText()!= null && messageField.getText() != null) {
            setErrorMailFields(false);
            mail.init();
            sendBtn.setDisable(true);
            MailTask task = new MailTask(mail);
            task.setOnSucceeded(t1 -> {
                if (task.getValue() != 0) {
                    showError("Error sending mails, please verify parameters and email addresses");
                    accordion.setExpandedPane(serverSettings);
                    setErrorMailFields(true);
                } else {
                    showInfo("Mails successfully sent");
                    System.err.println("Mails successfully sent");
                }
                sendBtn.setDisable(false);
            });
            new Thread(task).start();
            showInfo("Sending mails...");
        } else {
            showError("Mail parameters must be filled");
            SingleSelectionModel<Tab> selectionModel = settingsTab.getTabPane().getSelectionModel();
            selectionModel.select(settingsTab);
            accordion.setExpandedPane(serverSettings);
            setErrorMailFields(true);
        }
    }

    @FXML public void handlePrintButtonAction(ActionEvent actionEvent) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(printBtn.getContextMenu())) {
            agenda.print(job);
            job.endJob();
            showInfo("Print job sent");
            System.out.println("Print job sent");
        }
    }
}
