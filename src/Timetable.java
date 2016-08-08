import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

/**
 * Created by Héliane Ly on 02/08/2016.
 */
public class Timetable {
    private int[] eval = new int[2];
    private int[] groups = new int[GA.NB_GROUPS];

    public Timetable() {
        generateTimetable();
    }

    public Timetable(int[] groups) {
        this.groups = groups;
    }

    public boolean isAvailableTerm(int index) {
        return Term.getInstance().getDayOfWeek(index) <= GA.NB_DAYS_IN_WEEK &&
                Arrays.stream(groups).filter(g -> g == index).count() < GA.NB_ROOMS;
    }

    public void generateTimetable() {
        Random rnd = new Random();
        for (int i = 0; i < GA.NB_GROUPS; ++i) {
            boolean done = false;
            while (!done) {
                int t = rnd.nextInt(Term.getInstance().getNbTerms());
                if (isAvailableTerm(t)) {
                    done = true;
                    groups[i] = t;
                }
            }
        }
    }

    public void evaluateTimetable(List<Person> students, List<Person> examiners) {
        // eval[0] = conflict count with students & examiners list
        eval[0] = (int) Stream.concat(students.stream(), examiners.stream())
                .filter(x -> x.getSchedule()[groups[x.getGroup() - 1]] == 1)
                .count();
        // eval[1] = quality count with students (same day x 4 + next day)
        int same_day = (int) students.stream()
                .filter(x -> {
                    int i = groups[x.getGroup() - 1];
                    return (Term.getInstance().isMorning(i)) ? x.getSchedule()[i + 1] == 1 : x.getSchedule()[i - 1] == 1;
                }).count();
        int next_day = (int) students.stream()
                .filter(x -> {
                    int index = groups[x.getGroup() - 1];
                    if (!Term.getInstance().isSameDay(index, 0)) {
                        // Check previous day
                        int t = (Term.getInstance().isMorning(index)) ? 2 : 3;
                        for (int i = 1; i <= t; ++i)
                            if (x.getSchedule()[index - i] == 1 && !Term.getInstance().isSameDay(index - i, index))
                                return true;
                    }
                    if (!Term.getInstance().isSameDay(index, Term.getInstance().getNbTerms() - 1)) {
                        // Check next day
                        int t = (Term.getInstance().isMorning(index)) ? 3 : 2;
                        for (int i = 1; i <= t; ++i)
                            if (x.getSchedule()[index + i] == 1 && !Term.getInstance().isSameDay(index + i, index))
                                return true;
                    }
                    return false;
                }).count();
        eval[1] = same_day * 4 + next_day;
    }

    public int[] getEval() {
        return eval;
    }

    public void setEval(int[] eval) {
        this.eval = eval;
    }

    public int[] getGroups() {
        return groups;
    }

    public void setGroups(int[] groups) {
        this.groups = groups;
    }

    public void setGroup(int group, int term) {
        groups[group] = term;
    }
}
