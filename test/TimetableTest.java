import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Héliane Ly on 04/08/2016.
 */
public class TimetableTest {
    private List<Person> students = new ArrayList<>();
    private List<Person> examiners = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        Term t = Term.getInstance();
        t.init("01/05/2016", "03/05/2016");
        GA.NB_GROUPS = 2;
        students.add(new Person("foo", "foo@bar.co.uk", 1, new int[] {1, 1, 0, 0, 0, 0}));
        examiners.add(new Person("bar", "bar@bar.co.uk", 1, new int [] {0, 0, 0, 0, 1, 0}));
        students.add(new Person("foo1", "foo1@bar.co.uk", 2, new int[] {0, 1, 1, 0, 0, 0}));
        students.add(new Person("foo2", "foo2@bar.co.uk", 2, new int[] {1, 0, 0, 1, 0, 0}));
        examiners.add(new Person("bar1", "bar1@bar.co.uk", 2, new int [] {1, 1, 0, 1, 0, 0}));
    }

    @Test
    public void generateTimetable() throws Exception {
        Timetable t = new Timetable();
        assertEquals("Number of groups", GA.NB_GROUPS, t.getGroups().length);
        int[] nb_rooms = new int[Term.getInstance().getNbTerms()];
        for (int g : t.getGroups()) {
            assertTrue("Exam in allocated days of week", Term.getInstance().getDayOfWeek(g) <= GA.NB_DAYS_IN_WEEK);
            ++nb_rooms[g];
        }
        assertFalse("Number of exams at the same time", Arrays.stream(nb_rooms).anyMatch(x -> x > GA.NB_GROUPS));
    }

    @Test
    public void evaluateTimetable() throws Exception {
        int[] groups = new int[] {2, 1};
        Timetable t = new Timetable(groups);
        t.evaluateTimetable(students, examiners);
        int[] eval = t.getEval();
        assertEquals("Number of hard constraint violations", 2, eval[0]);
        assertEquals("Number of soft constraint violations", 1 + 1*4 + 2, eval[1]);
    }

//    @Test(expected = NoSuchElementException.class)
//    public void evaluateInvalidTimetable() {
////        ArrayList<Integer>[] terms = (ArrayList<Integer>[]) new ArrayList[Term.getInstance().getNbTerms()];
////        Timetable t = new Timetable(terms);
//        int[] groups = new int[] {0, 0};
//        Timetable t = new Timetable(groups);
//        t.evaluateTimetable(students, examiners);
//    }

    @Test
    public void evaluatePerfectTimetable() throws Exception {
        int[] groups = new int[] {5, 4};
        Timetable t = new Timetable(groups);
        t.evaluateTimetable(students, examiners);
        int[] eval = t.getEval();
        assertEquals("Number of hard constraint violations", 0, eval[0]);
        assertEquals("Number of soft constraint violations", 1 + 1, eval[1]);
    }
}