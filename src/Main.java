import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by Héliane Ly on 16/07/2016.
 */
public class Main {
    public static void main(String[] args) {
        Term t = Term.getInstance();
        if (t.init("09/05/2016", "31/05/2016") != 0)
            System.exit(-1);

        try (Stream<Path> paths = Files.walk(Paths.get("csv"))) {
            paths.filter(x -> x.toString().endsWith(".csv"))
                    .map(x -> x.toString())
                    .forEach(x -> {
                        File dir = new File("graph/" + x.substring(x.indexOf("_") + 1, x.indexOf(".")));
                        dir.mkdir();
                        generateTimetable(x);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

//        generateTimetable("csv/test_20_5_2.csv");

        System.exit(0);
    }

    public static List<Timetable> generateTimetable(String file) {
        List<Timetable> solutions = new ArrayList<>();
        CSVManager csv = new CSVManager();
        if (csv.read(file) != 0)
            System.exit(-1);
        for (int i = 0; i < 10; ++i) {
            GA algorithm = new GA();
            solutions.add(algorithm.run(csv.getStudents(), csv.getExaminers()));
            System.out.println("==============================");
            List<Timetable> best = algorithm.getBestSolutions();
            algorithm.printSolutions(best);
        }
        GA.sort(solutions);
        return solutions;
    }

    public static void generateCSV() {
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
        csv.write("csv/test_20_5_2.csv");
    }
}
