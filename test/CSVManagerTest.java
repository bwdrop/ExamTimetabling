import model.CSVManager;
import model.Person;
import model.Term;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Heliane Ly on 24/08/2016.
 */
public class CSVManagerTest {
    private String[] line;
    private String[] groupError;
    private String[] scheduleError;
    private List<Person> persons = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        Term.getInstance().init("01/05/2016", "03/05/2016");
        line = new String[] {"1, 2", "foo", "foo@bar.com", "no", "01/05AM, 03/05PM"};
        groupError = new String[] {"abc", "foo", "foo@bar.com", "yes", "01/05AM, 03/05PM"};
        scheduleError = new String[] {"3", "foo", "foo@bar.com", "yes", "24/08PM"};
        persons.add(new Person("foo", "foo@bar.com", Arrays.asList(2, 3), new int[] {0, 1, 1, 0, 0, 1}));
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void addPerson() throws Exception {
        CSVManager csv = new CSVManager();
        csv.addPerson(line);
        Person p = csv.getExaminers().get(0);
        assertEquals("Number of examiners", 1, csv.getExaminers().size());
        assertEquals("Number of students", 0, csv.getStudents().size());
        assertTrue("Added login", p.getLogin().equals(line[1]));
        assertTrue("Added email", p.getMail().equals(line[2]));
        assertTrue("Added groups", p.getGroups().equals(Arrays.asList(1, 2)));
        assertTrue("Added schedule", Arrays.equals(p.getSchedule(), new int[]{1, 0, 0, 0, 0, 1}));
    }

    @Test
    public void addPersonGroupException() throws Exception {
        CSVManager csv = new CSVManager();
        exception.expect(ParseException.class);
        csv.addPerson(groupError);
    }

    @Test
    public void addPersonScheduleException() throws Exception {
        CSVManager csv = new CSVManager();
        exception.expect(ParseException.class);
        csv.addPerson(scheduleError);
    }

    @Test
    public void toStringList() throws Exception {
        CSVManager csv = new CSVManager();
        List<String[]> tmp = csv.toStringList(persons, false);
        assertEquals("String groups", "2,3", tmp.get(0)[0]);
        assertEquals("String login", persons.get(0).getLogin(), tmp.get(0)[1]);
        assertEquals("String email", persons.get(0).getMail(), tmp.get(0)[2]);
        assertEquals("String student", "no", tmp.get(0)[3]);
        assertEquals("String schedule", "01/05PM,02/05AM,03/05PM", tmp.get(0)[4]);
    }
}