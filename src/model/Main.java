package model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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
                        dir.mkdirs();
                        generateTimetable(x, true);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

//        generateTimetable("csv/test_20_5_2.csv");

//        generateCSV();

        System.exit(0);
    }

    /**
     * Run algorithm and generate best timetables
     * @param file input CSV file
     * @param debug whether to keep logs and charts or not
     * @return list of timetables
     */
    public static List<Timetable> generateTimetable(String file, boolean debug) {
        List<Timetable> solutions = new ArrayList<>();
        CSVManager csv = new CSVManager();
        if (csv.read(file) != 0)
            return null;
        for (int i = 1; i <= GA.NB_RUNS; ++i) {
            GA algorithm;
            if (debug) {
                algorithm = new GA(file);
            } else {
                algorithm = new GA();
            }
            solutions.add(algorithm.run(csv.getStudents(), csv.getExaminers()));
            System.out.println("==============================");
            List<Timetable> best = algorithm.getBestSolutions();
            algorithm.printSolutions(best);
            GuiModel.getInstance().setProgress(i * 1.0 / GA.NB_RUNS);
        }
        GuiModel.getInstance().setStudents(csv.getStudents());
        GuiModel.getInstance().setExaminers(csv.getExaminers());
        GA.sort(solutions);
        return solutions;
    }

    /**
     * Generate a csv file with random students and examiners
     * (for testing purposes)
     */
    public static void generateCSV() {
        List<Person> students = new ArrayList<>();
        List<Person> examiners = new ArrayList<>();
        GA.NB_GROUPS = 20;
        List<Integer> shuffled = new ArrayList<>();
        IntStream.range(1, GA.NB_GROUPS + 1).forEach(i -> shuffled.add(i));
        Collections.reverse(shuffled);
        System.err.println(shuffled);
        IntStream.range(1, GA.NB_GROUPS + 1)
                .forEach(x -> {
                    students.add(new Person(new Integer[] {x}));
                    students.add(new Person(new Integer[] {x}));
                    students.add(new Person(new Integer[] {x}));
                    students.add(new Person(new Integer[] {x}));
                    students.add(new Person(new Integer[] {x}));
                    examiners.add(new Person(new Integer[] {x, shuffled.get(x - 1)}));
                });
        CSVManager csv = new CSVManager();
        csv.setStudents(students);
        csv.setExaminers(examiners);
        csv.write("csv/test_20_5_2.csv");
    }
}
