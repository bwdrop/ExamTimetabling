import java.sql.Time;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by Héliane Ly on 02/08/2016.
 */
public class GA {
    // Algorithm Constants
    public static final int ITERATIONS = 4000000;
    public static final int POPULATION = 50;
    public static final int PROB_MUT = 1;
    public static final int PROB_SWAP = 5;

    // User defined constants (will be deduced from input CSV file)
    public static int NB_GROUPS = 20;

    // User defined constants (soft constraint)
    public static final int NB_ROOMS = 1; // Number of rooms available at the same time for exams
    public static final int NB_DAYS_IN_WEEK = 5; // Number of days in week available to exams (MAX = 7; MIN = 5)

    private List<Timetable> pop = new ArrayList<>();
    private Random rnd = new Random();

    public void run(List<Person> students, List<Person> examiners) {
        initPopulation(students, examiners);
        int i;
        for (i = 0; i < ITERATIONS && getFitness(getBestSolutions().get(0)) != 0; ++i) {
            List<Timetable> parents = selectParents();
            Timetable child = crossover(parents.get(0), parents.get(1));
            mutate(child);
            child.evaluateTimetable(students, examiners);
            replaceWith(child);
            if (i % 10000 == 0) {
                System.out.println("===== Iteration " + i + " / " + ITERATIONS + " =====");
                sort(pop);
                printSolutions(pop.subList(0, 10));
            }
        }
        System.out.println("==> TOTAL NUMBER OF ITERATIONS : " + i + " <===");
    }

    public void initPopulation(List<Person> students, List<Person> examiners) {
        GA.NB_GROUPS = Stream.concat(students.stream(), examiners.stream())
                .mapToInt(x -> x.getGroup())
                .max().getAsInt();
        for (int i = 0; i < POPULATION; ++i) {
            Timetable t = new Timetable();
            t.evaluateTimetable(students, examiners);
            pop.add(t);
        }
    }

    public int getFitness(Timetable sln) {
        return sln.getEval()[0] * 1000 + sln.getEval()[1];
    }

    public static void sort(List<Timetable> t) {
        Collections.sort(t, (t1, t2) -> {
            int hComp = Integer.compare(t1.getEval()[0], t2.getEval()[0]);
            if (hComp != 0)
                return hComp;
            return Integer.compare(t1.getEval()[1], t2.getEval()[1]);
        });
    }

    public List<Timetable> selectParents() {
        List<Timetable> parents = new ArrayList<>();
        for (int i = 0; i < 3; ++i) {
            int r = rnd.nextInt(POPULATION);
            while (parents.contains(pop.get(r))) // check if same instance
                r = rnd.nextInt(POPULATION);
            parents.add(pop.get(r));
        }
        sort(parents); // order is ascending, but best values are closest to 0 ==> Best solutions are first !
        parents.remove(2); // remove last (worst) timetable
        return parents;
    }

    public Timetable crossover(Timetable p1, Timetable p2) {
        // uniform crossover
        int[] groups = Arrays.copyOf(p1.getGroups(), p1.getGroups().length);
        for (int i = 0; i < groups.length; ++i) {
            if (rnd.nextInt(2) == 1 && p1.isAvailableTerm(p2.getGroups()[i])) // CHECK NB_ROOM CONSTRAINT
                groups[i] = p2.getGroups()[i];
        }
        return new Timetable(groups);
    }

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

    public void replaceWith(Timetable child) {
        List<List<Timetable>> quadrants = new ArrayList<>();
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

    public List<Timetable> getBestSolutions() {
        sort(pop);
        return pop.subList(0, 10);
    }

    public void printSolutions(List<Timetable> t) {
        for (int i = 0; i < t.size(); ++i) {
            System.out.println(getFitness(t.get(i)) + " | " + Arrays.toString(t.get(i).getGroups()));
        }
    }
}
