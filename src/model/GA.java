package model;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by Héliane Ly on 02/08/2016.
 */
public class GA {
    // Algorithm Constants
    public static final int ITERATIONS = 2000;
    public static final int POPULATION = 200;
    public static final int PROB_MUT = 1;
    public static final int PROB_SWAP = 5;
    public static final int TOURNAMENT_SIZE = 3;

    // User defined constants (will be deduced from input CSV file)
    public static int NB_GROUPS = 20;

    // User defined constants (soft constraint)
    public static int NB_ROOMS = 1; // Number of rooms available at the same time for exams
    public static int NB_DAYS_IN_WEEK = 5; // Number of days in week available to exams (MAX = 7; MIN = 5)
    public static final int NB_RUNS = 10; // Number of runs of the algorithm

    private List<Timetable> pop = new ArrayList<>();
    private Random rnd = new Random();

    // debug specific properties
    private Chart chart = new Chart();
    private String dir;
    private Date now = new Date();
    private int salt = rnd.nextInt();
    private SimpleDateFormat fmt = new SimpleDateFormat("ddMMyyy_HHmmss");
    private PrintWriter out = null;

    public GA() {}

    /**
     * Debug constructor
     * Initializes debug variables
     * @param file path to csv file
     */
    public GA(String file) {
        dir = file.substring(file.indexOf("_") + 1, file.indexOf("."));
        try {
            out = new PrintWriter("graph/" + dir + "/graph_" + fmt.format(now) + "_" + salt + ".log");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Print string to log file
     * @param x
     */
    private void printLog(String x) {
        if (out != null)
            out.println(x);
    }

    /**
     * Add new best solution to chart
     * @param i the number of iterations
     */
    private void addLog(int i) {
        if (out != null)
            chart.addValue(getFitness(getBestSolutions().get(0)), i);
    }

    /**
     * Close log file and export chart
     */
    private void closeLog() {
        if (out != null) {
            out.close();
            chart.export("graph/" + dir + "/graph_" + fmt.format(now) + "_" + salt + ".jpeg");
        }
    }

    /**
     * Run algorithm
     * @param students list of students
     * @param examiners list of examiners
     * @return best solution
     */
    public Timetable run(List<Person> students, List<Person> examiners) {
        final long startTime = System.nanoTime();

        initPopulation(students, examiners);
        int i;
        for (i = 1; i <= ITERATIONS; ++i) {
            List<Timetable> parents = selectParents();
            Timetable child = crossover(parents.get(0), parents.get(1));
            mutate(child);
            child.evaluateTimetable(students, examiners);
            replaceWith(child);
            if (i % 100 == 0) {
                addLog(i);
                Timetable best = getBestSolutions().get(0);
                printLog(getFitness(best) + " | " + Arrays.toString(best.getGroups()) + " (" + i + ")");
            }
            if (i % 1000 == 0) {
                System.out.println("===== Iteration " + i + " / " + ITERATIONS + " (" + (i * 100.0 / ITERATIONS) + "%) =====");
                System.out.println("===== Different elements : " + pop.stream().distinct().count() + " / " + POPULATION + " =====");
                printSolutions(pop.subList(0, 10));
            }
        }
        System.out.println("==> TOTAL NUMBER OF ITERATIONS : " + (i - 1) + " <===");

        final long duration = System.nanoTime() - startTime;
        System.out.println("===> Duration = " + TimeUnit.NANOSECONDS.toSeconds(duration) + " seconds <===");
        printLog("==> Duration : " + TimeUnit.NANOSECONDS.toSeconds(duration) + " sec, " +
                (TimeUnit.NANOSECONDS.toMillis(duration) - TimeUnit.SECONDS.toMillis(TimeUnit.NANOSECONDS.toSeconds(duration))) + " millis");

        closeLog();

        return getBestSolutions().get(0);
    }

    /**
     * Initialize population
     * @param students list of students
     * @param examiners list of examiners
     */
    public void initPopulation(List<Person> students, List<Person> examiners) {
        GA.NB_GROUPS = Stream.concat(students.stream(), examiners.stream())
                .mapToInt(x -> Collections.max(x.getGroups()))
                .max().getAsInt();
        for (int i = 0; i < POPULATION; ++i) {
            Timetable t = new Timetable();
            t.evaluateTimetable(students, examiners);
            pop.add(t);
        }
    }

    /**
     * Get fitness of solution
     * @param sln timetable
     * @return fitness
     */
    public int getFitness(Timetable sln) {
        return sln.getEval()[0] * 1000 + sln.getEval()[1];
    }

    /**
     * Sort a list of timetables
     * @param t list
     */
    public static void sort(List<Timetable> t) {
        Collections.sort(t, (t1, t2) -> {
            int hComp = Integer.compare(t1.getEval()[0], t2.getEval()[0]);
            if (hComp != 0)
                return hComp;
            return Integer.compare(t1.getEval()[1], t2.getEval()[1]);
        });
    }

    /**
     * Select two parents from population
     * @return a list of two parents
     */
    public List<Timetable> selectParents() {
        List<Timetable> parents = new ArrayList<>();
        for (int i = 0; i < 2; ++i) {
            List<Timetable> tmp = new ArrayList<>();
            for (int j = 0; j < TOURNAMENT_SIZE; ++j) {
                int r = rnd.nextInt(POPULATION);
                tmp.add(pop.get(r));
            }
            sort(tmp);
            parents.add(tmp.get(0));
        }
        return parents;
    }

    /**
     * Crossover two timetables
     * @param p1 parent 1
     * @param p2 parent 2
     * @return child timetable
     */
    public Timetable crossover(Timetable p1, Timetable p2) {
        // uniform crossover
        int[] groups = Arrays.copyOf(p1.getGroups(), p1.getGroups().length);
        for (int i = 0; i < groups.length; ++i) {
            if (rnd.nextInt(2) == 1 && p1.isAvailableTerm(p2.getGroups()[i])) // CHECK NB_ROOM CONSTRAINT
                groups[i] = p2.getGroups()[i];
        }
        return new Timetable(groups);
    }

    /**
     * Mutate a solution
     * @param sln timetable
     */
    public void mutate(Timetable sln)  {
        if (rnd.nextInt(100) < PROB_MUT) {
            // Do course mutation
            int g = rnd.nextInt(GA.NB_GROUPS);
            int t = rnd.nextInt(Term.getInstance().getNbTerms());
            while (!sln.isAvailableTerm(t))
                t = rnd.nextInt(Term.getInstance().getNbTerms());
            sln.setGroup(g, t);
        }
        if (rnd.nextInt(100) < PROB_SWAP) {
            // Do term mutation
            int t1 = rnd.nextInt(Term.getInstance().getNbTerms());
            int t2 = rnd.nextInt(Term.getInstance().getNbTerms());
            int[] gTerm1 = IntStream.range(0, sln.getGroups().length).filter(i -> sln.getGroups()[i] == t1).toArray();
            int[] gTerm2 = IntStream.range(0, sln.getGroups().length).filter(i -> sln.getGroups()[i] == t2).toArray();
            for (int i = 0; i < gTerm1.length; ++i)
                sln.setGroup(gTerm1[i], t1);
            for (int i = 0; i < gTerm2.length; ++i)
                sln.setGroup(gTerm2[i], t2);
        }
    }

    /**
     * Find a solution in the population to replace with the child
     * @param child timetable
     */
    public void replaceWith(Timetable child) {
        List<List<Timetable>> quadrants = new ArrayList<>();
        sort(pop);
        quadrants.add(pop.stream()
                .filter(x -> x.getEval()[0] >= child.getEval()[0] && x.getEval()[1] >= child.getEval()[1])
                .collect(Collectors.toList()));
        quadrants.add(pop.stream()
                .filter(x -> x.getEval()[0] >= child.getEval()[0] && x.getEval()[1] < child.getEval()[1])
                .collect(Collectors.toList()));
        quadrants.add(pop.stream()
                .filter(x -> x.getEval()[0] < child.getEval()[0] && x.getEval()[1] >= child.getEval()[1])
                .collect(Collectors.toList()));
        quadrants.add(pop.stream()
                .filter(x -> x.getEval()[0] < child.getEval()[0] && x.getEval()[1] < child.getEval()[1])
                .collect(Collectors.toList()));
        for (int i = 0; i < quadrants.size(); ++i) {
            if (quadrants.get(i).size() > 0) {
                Timetable toReplace = quadrants.get(i).get(rnd.nextInt(quadrants.get(i).size()));
                pop.remove(toReplace);
                pop.add(child);
                return;
            }
        }
    }

    /**
     * @return the 10 best solutions of the population
     */
    public List<Timetable> getBestSolutions() {
        sort(pop);
        return pop.subList(0, 10);
    }

    /**
     * Print a list of timetables
     * @param t list of timetables
     */
    public void printSolutions(List<Timetable> t) {
        for (int i = 0; i < t.size(); ++i) {
            System.out.println(getFitness(t.get(i)) + " | " + Arrays.toString(t.get(i).getGroups()));
        }
    }
}
