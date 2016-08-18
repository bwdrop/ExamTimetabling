package model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Héliane Ly on 15/08/2016.
 */
public final class GuiModel {
    private static GuiModel instance = null;

    private final ObservableList<Timetable> timetables = FXCollections.observableArrayList();
    private final ObjectProperty<Timetable> currentTimetable = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> displayDate = new SimpleObjectProperty<>();
    private final DoubleProperty progress = new SimpleDoubleProperty();
    private final StringProperty errorMessage = new SimpleStringProperty();

    private List<Person> students = new ArrayList<>();
    private List<Person> examiners = new ArrayList<>();

    private GuiModel() {}

    public static GuiModel getInstance() {
        if (instance == null)
            instance = new GuiModel();
        return instance;
    }

    public List<Timetable> getTimetables() {
        return new ArrayList<>(timetables);
    }

    public ObservableList<Timetable> timetablesProperty() {
        return timetables;
    }

    public void setTimetables(List<Timetable> l) {
        timetables.setAll(l);
    }

    public Timetable getCurrentTimetable() {
        return currentTimetable.get();
    }

    public ObjectProperty<Timetable> currentTimetableProperty() {
        return currentTimetable;
    }

    public void setCurrentTimetable(Timetable t) {
        currentTimetable.set(t);
    }

    public LocalDateTime getDisplayDate() {
        return displayDate.get();
    }

    public ObjectProperty<LocalDateTime> displayDateProperty() {
        return displayDate;
    }

    public void setDisplayDate(LocalDateTime displayDate) {
        this.displayDate.set(displayDate);
    }

    public double getProgress() {
        return progress.get();
    }

    public DoubleProperty progressProperty() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress.set(progress);
    }

    public String getErrorMessage() {
        return errorMessage.get();
    }

    public StringProperty errorMessageProperty() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage.set(errorMessage);
    }

    public List<Person> getStudents() {
        return students;
    }

    public void setStudents(List<Person> students) {
        this.students = students;
    }

    public List<Person> getExaminers() {
        return examiners;
    }

    public void setExaminers(List<Person> examiners) {
        this.examiners = examiners;
    }
}
