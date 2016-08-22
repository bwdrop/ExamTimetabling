package controller;

import javafx.concurrent.Task;
import model.*;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Héliane Ly on 20/08/2016.
 */
public class MailTask extends Task<Integer> {
    private Mail mail;
    private GuiModel model = GuiModel.getInstance();

    public MailTask(Mail mail) {
        this.mail = mail;
    }

    @Override
    protected Integer call() throws Exception {
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
            if (mail.send(to, date, start, end, g + 1) != 0) {
                System.err.println("!!!error!!!!");
                return 1;
            }
        }
        return 0;
    }
}