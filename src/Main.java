import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Created by Héliane Ly on 16/07/2016.
 */
public class Main {
    public static void main(String[] args) {
        Term t = Term.getInstance();
        if (t.init("09/05/2016", "31/05/2016") != 0)
            System.exit(-1);

//        // old
//        if (t.init("09/05/2016", "11/05/2016") != 0)
//            System.exit(-1);
//        students.add(new Person("md527", 1, new int[] {1, 1, 0, 0, 0, 0}));
//        examiners.add(new Person("dfc", 1, new int [] {0, 0, 0, 0, 1, 0}));
//        students.add(new Person("hl298", 2, new int[] {0, 1, 1, 0, 0, 0}));
//        students.add(new Person("ajw83", 2, new int[] {1, 0, 0, 1, 0, 0}));
//        examiners.add(new Person("mik", 2, new int [] {1, 1, 0, 1, 0, 0}));

        CSVManager csv = new CSVManager();
        if (csv.read("test02.csv") != 0)
            System.exit(-1);
        GA algorithm = new GA();
        algorithm.run(csv.getStudents(), csv.getExaminers());
        System.out.println("========================================");
        List<Timetable> best = algorithm.getBestSolutions();
        algorithm.printSolutions(best);
    }

    public void generateCSV() {
        List<Person> students = new ArrayList<>();
        List<Person> examiners = new ArrayList<>();
        GA.NB_GROUPS = 20;
        IntStream.range(1, GA.NB_GROUPS + 1)
                .forEach(x -> {
                    students.add(new Person(x));
                    students.add(new Person(x));
                    students.add(new Person(x));
                    students.add(new Person(x));
                    students.add(new Person(x));
                    examiners.add(new Person(x));
                    examiners.add(new Person(x));
                });
        CSVManager csv = new CSVManager();
        csv.setStudents(students);
        csv.setExaminers(examiners);
        csv.write("test02.csv");
    }
}
