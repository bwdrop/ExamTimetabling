package model;

import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.joda.time.DateTime;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Héliane Ly on 08/08/2016.
 */
public class CSVManager {
    public static final String[] HEADER = {"Group", "Login", "Mail", "Student?", "Schedule"};
    public static final int COLUMNS = HEADER.length;

    private List<Person> students = new ArrayList<>();
    private List<Person> examiners = new ArrayList<>();

    // CSV file is structured as followed:
    // Group | Login |       Mail       |  Student? |  Schedule
    // ---------------------------------------------------------------------------
    //   1   | hl298 | hl298@kent.ac.uk |    yes    | 10/05AM, 12/05PM, 20/05AM...
    //  2,3  |  mik  |  mik@kent.ac.uk  |    no     | 09/05AM, 10/05AM, 15/05/AM...

    public CSVManager() {}

    /**
     * Parse a schedule consisted of "01/05AM,02/05PM,05/05AM"
     * @param line
     * @return an array of integers with the group as index and term as value
     * @throws ParseException
     */
    private int[] parseSchedule(String line) throws ParseException {
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MMa");
        String[] s = line.split(",");
        int[] schedule = new int[Term.getInstance().getNbTerms()];
        for (int i = 0; i < s.length; ++i) {
            Date date = fmt.parse(s[i]);
            DateTime d = new DateTime(date);
            d = d.withYear(Term.getInstance().getYear(d));
            if (Term.getInstance().getIndex(d) < 0 || Term.getInstance().getIndex(d) >= Term.getInstance().getNbTerms())
                throw new ParseException("Term index must be within bounds", 0);
            schedule[Term.getInstance().getIndex(d)] = 1;
        }
        return schedule;
    }

    /**
     * Parse a list of groups "1, 3, 8"
     * @param line
     * @return a list of groups
     * @throws ParseException
     */
    private List<Integer> parseGroups(String line) throws ParseException {
        String[] g = line.split(",");
        List<Integer> groups = new ArrayList<>();
        for (int i = 0; i < g.length; ++i) {
            int tmp;
            try {
                tmp = Integer.parseInt(g[i].trim());
            } catch (NumberFormatException e) {
                throw new ParseException("Invalid group number", 0);
            }
            if (tmp <= 0)
                throw new ParseException("Group number must be positive", 0);
            groups.add(tmp);
        }
        return groups;
    }

    /**
     * Parse a line of the CSV file and add it to the corresponding list (students / examiners)
     * @param line
     * @throws ParseException
     */
    public void addPerson(String[] line) throws ParseException {
        List<Person> list = (line[3].equalsIgnoreCase("yes")) ? students : examiners;
        int[] schedule = parseSchedule(line[4]);
        List<Integer> groups = parseGroups(line[0]);
        list.add(new Person(line[1], line[2], groups, schedule));
    }

    /**
     * Read / parse the CSV input file and fill the persons lists
     * @param file
     * @return
     */
    public int read(String file) {
        try {
            CSVReader reader = new CSVReader(new FileReader(file), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, 1);
            String [] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                if (nextLine.length != COLUMNS)
                    throw new ParseException("Bad file format", (int) reader.getLinesRead());
                addPerson(nextLine);
            }
        } catch (IOException | ParseException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
        return 0;
    }

    /**
     * Convert a list of persons into a list of lines for the CSV file
     * @param persons
     * @param isStudent
     * @return a list of string arrays
     */
    public List<String[]> toStringList(List<Person> persons, boolean isStudent) {
        List<String[]> list = new ArrayList<>();
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM");
        IntStream.range(0, persons.size())
                .forEach(i -> {
                    String[] elem = new String[COLUMNS];
                    elem[0] = persons.get(i).getGroups().stream()
                            .map(x -> Integer.toString(x))
                            .collect(Collectors.joining(","));
                    elem[1] = persons.get(i).getLogin();
                    elem[2] = persons.get(i).getMail();
                    elem[3] = isStudent ? "yes" : "no";
                    elem[4] = IntStream.range(0, persons.get(i).getSchedule().length)
                            .filter(x -> persons.get(i).getSchedule()[x] == 1)
                            .mapToObj(x -> {
                                Date d = Term.getInstance().getDay(x);
                                return fmt.format(d) + (Term.getInstance().isMorning(x) ? "AM" : "PM");
                            }).collect(Collectors.joining(","));
                    list.add(elem);
                });
        return list;
    }

    /**
     * Write a CSV file from the students and examiners lists
     * @param file
     * @return error status (0 if no error)
     */
    public int write(String file) {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(file));
            writer.writeNext(HEADER);
            writer.writeAll(toStringList(students, true));
            writer.writeAll(toStringList(examiners, false));
            writer.close();
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
        return 0;
    }

    public List<Person> getStudents() {
        return students;
    }

    public void setStudents(List<Person> students) {
        this.students = students;
    }

    public List<Person> getExaminers() {
        return examiners;
    }

    public void setExaminers(List<Person> examiners) {
        this.examiners = examiners;
    }
}
