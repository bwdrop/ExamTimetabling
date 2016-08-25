package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by Héliane Ly on 30/07/2016.
 */
public class Person {
    // Auto-generated schedule constants (for test purposes)
    public static final int MAX_EXAMS = 9;
    public static final int MIN_EXAMS = 4;

    private String login;
    private String mail;
    private List<Integer> groups = new ArrayList<>();
    private int[] schedule;

    /**
     * Generate random Person
     */
    public Person() {
        Random rnd = new Random();
        this.login = generateLogin(rnd);
        this.mail = login + "@kent.ac.uk";
        this.groups.add(rnd.nextInt(GA.NB_GROUPS) + 1); // Group numbers start from 1
        this.schedule = generateSchedule(rnd);
    }

    /**
     * Generate random Person with specified groups
     * @param groups list of groups
     */
    public Person(Integer[] groups) {
        this();
        this.groups = Arrays.asList(groups);
    }

    /**
     * Generate Person with specified arguments
     * @param login
     * @param mail
     * @param groups
     * @param schedule
     */
    public Person(String login, String mail, List<Integer> groups, int[] schedule) {
        this.login = login;
        this.mail = mail;
        this.groups = groups;
        this.schedule = schedule;
    }

    /**
     * Generate a random login
     * @param rnd random generator
     * @return login string
     */
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

    /**
     * Generate a random schedule
     * @param rnd random generator
     * @return array of int with terms as indexes (0 is free, 1 is occupied)
     */
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

    public List<Integer> getGroups() {
        return groups;
    }

    public int[] getSchedule() {
        return schedule;
    }

    public String getMail() {
        return mail;
    }
}
