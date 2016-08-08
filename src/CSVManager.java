import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.joda.time.DateTime;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Héliane Ly on 08/08/2016.
 */
public class CSVManager {
    public static final int COLUMNS = 4;

    private List<Person> students = new ArrayList<>();
    private List<Person> examiners = new ArrayList<>();

    // CSV file is structured as followed:
    // Group | Login |  Student? |  Schedule
    // ---------------------------------------------------------
    //   1   | hl298 |    yes    | 10/05AM, 12/05PM, 20/05AM...
    //   2   |  mik  |    no     | 09/05AM, 10/05AM, 15/05/AM...

    public CSVManager() {}

    private void addPerson(String[] line) throws ParseException {
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MMa");
        List<Person> list = (line[2].equalsIgnoreCase("yes")) ? students : examiners;
        String[] s = line[3].split(",");
        int[] schedule = new int[Term.getInstance().getNbTerms()];
        for (int i = 0; i < s.length; ++i) {
            Date date = fmt.parse(s[i]);
            DateTime d = new DateTime(date);
            d = d.withYear(Term.getInstance().getYear(d));
            schedule[Term.getInstance().getIndex(d)] = 1;
        }
        if (Integer.parseInt(line[0]) <= 0)
            throw new ParseException("Group number must be positive", 0);
        list.add(new Person(line[1], Integer.parseInt(line[0]), schedule));
    }

    public int read(String file) {
        try {
            CSVReader reader = new CSVReader(new FileReader(file));
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

    public List<String[]> toStringList(List<Person> persons, boolean isStudent) {
        List<String[]> list = new ArrayList<>();
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM");
        IntStream.range(0, persons.size())
                .forEach(i -> {
                    String[] elem = new String[COLUMNS];
                    elem[0] = Integer.toString(persons.get(i).getGroup());
                    elem[1] = persons.get(i).getLogin();
                    elem[2] = isStudent ? "yes" : "no";
                    elem[3] = IntStream.range(0, persons.get(i).getSchedule().length)
                            .filter(x -> persons.get(i).getSchedule()[x] == 1)
                            .mapToObj(x -> {
                                Date d = Term.getInstance().getDay(x);
                                return fmt.format(d) + (Term.getInstance().isMorning(x) ? "AM" : "PM");
                            }).collect(Collectors.joining(","));
                    list.add(elem);
                });
        return list;
    }

    public int write(String file) {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(file));
            toStringList(students, true).forEach(writer::writeNext);
            toStringList(examiners, false).forEach(writer::writeNext);
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
