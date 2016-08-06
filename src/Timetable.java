import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by Héliane Ly on 02/08/2016.
 */
public class Timetable {
    private int[] eval = new int[2];

    //    private ArrayList<Integer>[] terms = (ArrayList<Integer>[]) new ArrayList[Term.getInstance().getNbTerms()];
    private int[] groups = new int[GA.NB_GROUPS];

    public Timetable() {
        generateTimetable();
    }

//    public Timetable(ArrayList<Integer>[] terms) {
//        this.terms = terms;
//    }

    public Timetable(int[] groups) {
        this.groups = groups;
    }

    public void generateTimetable() {
        Random rnd = new Random();
//        for (int i = 1; i <= GA.NB_GROUPS; ++i) {
//            int t = rnd.nextInt(Term.getInstance().getNbTerms());
//            while (Term.getInstance().getDayOfWeek(t) <= GA.NB_DAYS_IN_WEEK &&
//                    terms[t] != null && terms[t].size() >= GA.NB_ROOMS)
//                t = rnd.nextInt(Term.getInstance().getNbTerms());
//            if (terms[t] == null) {
//                terms[t] = new ArrayList<>(GA.NB_ROOMS);
//                terms[t].add(i);
//            } else if (terms[t].size() < GA.NB_ROOMS)
//                terms[t].add(i);
//        }
        for (int i = 0; i < GA.NB_GROUPS; ++i) {
            boolean done = false;
            while (!done) {
                int t = rnd.nextInt(Term.getInstance().getNbTerms());
                if (Term.getInstance().getDayOfWeek(t) <= GA.NB_DAYS_IN_WEEK &&
                        Arrays.stream(groups).filter(g -> g == t).count() < GA.NB_ROOMS) {
                    done = true;
                    groups[i] = t;
                }
            }
        }
    }

    public void evaluateTimetable(List<Person> students, List<Person> examiners) {
        // eval[0] = conflict count with students & examiners list
//        eval[0] = (int) Stream.concat(students.stream(), examiners.stream())
//                .filter(x -> {
//                    int index = IntStream.range(0, terms.length)
//                            .filter(i -> terms[i] != null && terms[i].contains(x.getGroup()))
//                            .findFirst().getAsInt();
//                    if (x.getSchedule()[index] == 0) // if no conflict, return false
//                        return false;
//                    return true;
//                }).count();
        eval[0] = (int) Stream.concat(students.stream(), examiners.stream())
                .filter(x -> x.getSchedule()[groups[x.getGroup() - 1]] == 1)
                .count();


        // eval[1] = quality count with students (same day x 4 + next day)
//        int same_day = (int) students.stream()
//                .filter(x -> {
//                    int i = IntStream.range(0, terms.length)
//                            .filter(j -> terms[j] != null && terms[j].contains(x.getGroup()))
//                            .findFirst().getAsInt();
//                    return (Term.getInstance().isMorning(i)) ? x.getSchedule()[i + 1] == 1 : x.getSchedule()[i - 1] == 1;
//                }).count();
        int same_day = (int) students.stream()
                .filter(x -> {
                    int i = groups[x.getGroup() - 1];
                    return (Term.getInstance().isMorning(i)) ? x.getSchedule()[i + 1] == 1 : x.getSchedule()[i - 1] == 1;
                }).count();

//        int next_day = (int) students.stream()
//                .filter(x -> {
//                    int index = IntStream.range(0, terms.length)
//                            .filter(i -> terms[i] != null && terms[i].contains(x.getGroup()))
//                            .findFirst().getAsInt();
//                    if (!Term.getInstance().isSameDay(index, 0)) {
//                        // Check previous day
//                        int t = (Term.getInstance().isMorning(index)) ? 2 : 3;
//                        for (int i = 1; i <= t; ++i)
//                            if (x.getSchedule()[index - i] == 1 && !Term.getInstance().isSameDay(index - i, index))
//                                return true;
//                    }
//                    if (!Term.getInstance().isSameDay(index, Term.getInstance().getNbTerms() - 1)) {
//                        // Check next day
//                        int t = (Term.getInstance().isMorning(index)) ? 3 : 2;
//                        for (int i = 1; i <= t; ++i)
//                            if (x.getSchedule()[index + i] == 1 && !Term.getInstance().isSameDay(index + i, index))
//                                return true;
//                    }
//                    return false;
//                }).count();
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

//    public ArrayList<Integer>[] getTerms() {
//        return terms;
//    }
//
//    public void setTerms(ArrayList<Integer>[] terms) {
//        this.terms = terms;
//    }

    public int[] getGroups() {
        return groups;
    }

    public void setGroups(int[] groups) {
        this.groups = groups;
    }
}
