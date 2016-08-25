package model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by Héliane Ly on 02/08/2016.
 */
public class Timetable {
    public static final int SAME_DAY_K = 4;
    public static final int NEXT_DAY_K = 1;
    public static final int WEEKEND_K = 3;

    private int[] eval = new int[2];
    private int[] groups = new int[GA.NB_GROUPS];

    private final IntegerProperty hard = new SimpleIntegerProperty();
    private final IntegerProperty sameDay = new SimpleIntegerProperty();
    private final IntegerProperty nextDay = new SimpleIntegerProperty();
    private final IntegerProperty weekend = new SimpleIntegerProperty();

    public Timetable() {
        generateTimetable();
    }

    public Timetable(int[] groups) {
        this.groups = groups;
    }

    /**
     * @param index
     * @return true if term index is free, false otherwise
     */
    public boolean isAvailableTerm(int index) {
        return Term.getInstance().getDayOfWeek(index) <= GA.NB_DAYS_IN_WEEK &&
                Arrays.stream(groups).filter(g -> g == index).count() < GA.NB_ROOMS;
    }

    /**
     * Generate random timetable
     */
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

    /**
     * @param students students list
     * @return number of soft conflicts on the same day
     */
    private int evalSameDay(List<Person> students) {
        int same_day = (int) students.stream()
                .mapToLong(x -> x.getGroups().stream()
                        .filter(g -> (Term.getInstance().isMorning(groups[g - 1])) ?
                                x.getSchedule()[groups[g - 1] + 1] == 1 :
                                x.getSchedule()[groups[g - 1] - 1] == 1)
                        .count())
                .sum();
        return same_day;
    }

    /**
     * @param students students list
     * @return number of soft conflicts on the next day
     */
    private int evalNextDay(List<Person> students) {
        int next_day = (int) students.stream()
                .mapToLong(x -> x.getGroups().stream()
                        .filter(g -> {
                            int index = groups[g - 1];
                            if (!Term.getInstance().isSameDay(index, 0)) {
                                // Check previous day
                                int t = (Term.getInstance().isMorning(index)) ? 2 : 3;
                                for (int i = 1; i <= t; ++i)
                                    if (x.getSchedule()[index - i] == 1 &&
                                            !Term.getInstance().isSameDay(index - i, index))
                                        return true;
                            }
                            if (!Term.getInstance().isSameDay(index, Term.getInstance().getNbTerms() - 1)) {
                                // Check next day
                                int t = (Term.getInstance().isMorning(index)) ? 3 : 2;
                                for (int i = 1; i <= t; ++i)
                                    if (x.getSchedule()[index + i] == 1 &&
                                            !Term.getInstance().isSameDay(index + i, index))
                                        return true;
                            }
                            return false;
                        }).count())
                .sum();
        return next_day;
    }

    /**
     * @return number of soft conflicts on the weekend
     */
    private int evalWeekend() {
        return (int) IntStream.range(0, groups.length)
                .filter(g -> Term.getInstance().getDayOfWeek(groups[g]) >= 6)
                .count();
    }

    /**
     * Evaluate fitness of the current timetable
     * @param students list of students
     * @param examiners list of examiners
     */
    public void evaluateTimetable(List<Person> students, List<Person> examiners) {
        // eval[0] = conflict count with students & examiners list
        eval[0] = (int) Stream.concat(students.stream(), examiners.stream())
                .mapToLong(x -> x.getGroups().stream()
                        .filter(g -> x.getSchedule()[groups[g - 1]] == 1)
                        .count())
                .sum();
        hard.set(eval[0]);
        // eval[1] = quality count with students (same day x 4 + next day)
        sameDay.set(evalSameDay(students));
        nextDay.set(evalNextDay(students));
        weekend.set(evalWeekend());
        eval[1] = sameDay.get() * SAME_DAY_K + nextDay.get() * NEXT_DAY_K + weekend.get() * WEEKEND_K;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Timetable timetable = (Timetable) o;

        if (!Arrays.equals(eval, timetable.eval)) return false;
        return Arrays.equals(groups, timetable.groups);

    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(eval);
        result = 31 * result + Arrays.hashCode(groups);
        return result;
    }

    public int getHard() {
        return hard.get();
    }

    public IntegerProperty hardProperty() {
        return hard;
    }

    public void setHard(int hard) {
        this.hard.set(hard);
    }

    public int getSameDay() {
        return sameDay.get();
    }

    public IntegerProperty sameDayProperty() {
        return sameDay;
    }

    public void setSameDay(int sameDay) {
        this.sameDay.set(sameDay);
    }

    public int getNextDay() {
        return nextDay.get();
    }

    public IntegerProperty nextDayProperty() {
        return nextDay;
    }

    public void setNextDay(int nextDay) {
        this.nextDay.set(nextDay);
    }

    public int getWeekend() {
        return weekend.get();
    }

    public IntegerProperty weekendProperty() {
        return weekend;
    }

    public void setWeekend(int weekend) {
        this.weekend.set(weekend);
    }
}
