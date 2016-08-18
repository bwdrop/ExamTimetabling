package controller;

import javafx.concurrent.Task;
import model.GuiModel;
import model.Main;
import model.Timetable;

import java.util.List;

/**
 * Created by Héliane Ly on 17/08/2016.
 */
public class TimetableTask extends Task<List<Timetable>> {
    private String filePath;

    public TimetableTask(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected List<Timetable> call() throws Exception
    {
        return Main.generateTimetable(filePath);
    }
}