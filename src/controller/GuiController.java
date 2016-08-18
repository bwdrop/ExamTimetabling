package controller;

import javafx.animation.FadeTransition;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import jfxtras.scene.control.agenda.Agenda;
import model.*;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

    @FXML private ButtonBar buttonBar;
    @FXML private Button prevWeek;
    @FXML private Button nextWeek;
    @FXML private Button sendBtn;
    @FXML private Button printBtn;
    @FXML private Label message;
    private FadeTransition fadeOut = new FadeTransition();

    @FXML private Slider nbRooms;
    @FXML private Slider nbDays;

    private final GuiModel model = GuiModel.getInstance();

    private final PseudoClass errorClass = PseudoClass.getPseudoClass("error");

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
        initErrorMessage();
        initTimetableTable();
        initAgenda();
    }

    private void initErrorMessage() {
        message.pseudoClassStateChanged(errorClass, true);
        fadeOut.setNode(message);
        fadeOut.setDuration(Duration.millis(2500));
        fadeOut.setDelay(Duration.millis(2000));
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setCycleCount(1);
        fadeOut.setAutoReverse(false);
        fadeOut.setOnFinished(actionEvent -> model.setErrorMessage(""));
        model.errorMessageProperty().addListener((obs, oldVal, newVal) -> {
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
        // init appointments
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
                                            .withDescription("Not displayed?")
                                            .withAppointmentGroup(lAppointmentGroupMap.get("group" + String.format("%02d", g + 1)))
                            );
                        });
            }
        });
        // init prevWeek and nextWeek buttons
        model.displayDateProperty().addListener((obs, oldVal, newVal) -> setDisablePrevNextButtons(newVal));
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
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
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
            String start = startDate.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String end = endDate.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            Term t = Term.getInstance();
            if (t.init(start, end) != 0) {
                System.err.println("Invalid start / end dates");
                model.setErrorMessage("Invalid start / end dates");
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
                    System.err.println("Invalid CSV file");
                    model.setErrorMessage("Invalid CSV file");
                    filePath.pseudoClassStateChanged(errorClass, true);
                    // TODO show invalid CSV input file
                    setDisableAllButtons(false);
                    return;
                }
                startDate.pseudoClassStateChanged(errorClass, false);
                endDate.pseudoClassStateChanged(errorClass, false);
                filePath.pseudoClassStateChanged(errorClass, false);
                model.setDisplayDate(Term.getInstance().getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atTime(14, 0));
                model.setTimetables(best);
                model.setCurrentTimetable(best.get(0));
                // Set selected tab
                SingleSelectionModel<Tab> selectionModel = timetableTab.getTabPane().getSelectionModel();
                selectionModel.select(timetableTab);
                setDisableAllButtons(false);
            });
            new Thread(task).start();
        }
    }

    @FXML public void handleTimetableTabChanged(Event event) {
        if (buttonBar != null && progressBar.getProgress() == 1.0) {
            buttonBar.getButtons().forEach(b -> {
                if (b instanceof Button)
                    b.setDisable(!timetableTab.isSelected());
            });
            if (timetableTab.isSelected())
                setDisablePrevNextButtons(model.getDisplayDate());
        }
    }

    @FXML public void handlePreviousButtonAction(ActionEvent actionEvent) {
        model.setDisplayDate(model.getDisplayDate().minusWeeks(1));
    }

    @FXML public void handleNextButtonAction(ActionEvent actionEvent) {
        model.setDisplayDate(model.getDisplayDate().plusWeeks(1));
    }

    @FXML public void handleSendButtonAction(ActionEvent actionEvent) {
        Mail mail = new Mail();
        for (int i = 0; i < GA.NB_GROUPS; ++i) {
            int g = i;
            int term = model.getCurrentTimetable().getGroups()[g];
            SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
            String date = fmt.format(Term.getInstance().getDay(term));
            String start = (Term.getInstance().isMorning(term)) ? "10am" : "2pm";
            String end = (Term.getInstance().isMorning(term)) ? "1pm" : "5pm";
            List<String> to = Stream.concat(model.getStudents().stream(), model.getExaminers().stream())
                    .filter(x -> x.getGroups().contains(g + 1))
                    .map(x -> x.getMail())
                    .collect(Collectors.toList());
            if (mail.send(to, date, start, end) != 0) {
                // TODO SHOW ERROR
                model.setErrorMessage("Error sending mails, please verify parameters and email addresses");
                System.err.println("Error sending mails, please verify parameters and email addresses");
                return;
            }
        }
    }

    @FXML public void handlePrintButtonAction(ActionEvent actionEvent) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(printBtn.getContextMenu())) {
            agenda.print(job);
            job.endJob();
        }
    }
}
