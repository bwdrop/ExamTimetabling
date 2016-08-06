import java.util.Random;

/**
 * Created by Héliane Ly on 30/07/2016.
 */
public class Person {
    // Auto-generated schedule constants (for test purposes)
    public static final int MAX_EXAMS = 9;
    public static final int MIN_EXAMS = 4;

    private String login;
    private int group;
    private int[] schedule;

    public Person() {
        Random rnd = new Random();
        this.login = generateLogin(rnd);
        this.group = rnd.nextInt(GA.NB_GROUPS) + 1; // Group numbers start from 1
        this.schedule = generateSchedule(rnd);
    }

    public Person(String login, int group, int[] schedule) {
        this.login = login;
        this.group = group;
        this.schedule = schedule;
    }

    public String generateLogin(Random rnd) {
        String INITIALS = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder login = new StringBuilder();
        int length = rnd.nextInt(3) + 2;
        while (login.length() < length) {
            int index = (int) (rnd.nextFloat() * INITIALS.length());
            login.append(INITIALS.charAt(index));
        }
        login.append(rnd.nextInt(300) + 1);
        return login.toString();
    }

    public int[] generateSchedule(Random rnd) {
        int nb_exams = rnd.nextInt(MAX_EXAMS - MIN_EXAMS + 1) + MIN_EXAMS;
        int[] t = new int[Term.getInstance().getNbTerms()];
        for (int i = 0; i < nb_exams; ++i) {
            int r = rnd.nextInt(Term.getInstance().getNbTerms());
            while (t[r] != 0)
                r = rnd.nextInt(Term.getInstance().getNbTerms());
            t[r] = 1;
        }
        return t;
    }

    public String getLogin() {
        return login;
    }

    public int getGroup() {
        return group;
    }

    public int[] getSchedule() {
        return schedule;
    }
}
