import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

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
        students.add(new Person("md527", 1, new int[] {1, 1, 0, 0, 0, 0}));
        examiners.add(new Person("dfc", 1, new int [] {0, 0, 0, 0, 1, 0}));
        students.add(new Person("hl298", 2, new int[] {0, 1, 1, 0, 0, 0}));
        students.add(new Person("ajw83", 2, new int[] {1, 0, 0, 1, 0, 0}));
        examiners.add(new Person("mik", 2, new int [] {1, 1, 0, 1, 0, 0}));
    }

    @Test
    public void generateTimetable() throws Exception {
        Timetable t = new Timetable();
        assertEquals(Term.getInstance().getNbTerms(), t.getTerms().length);
        int nb_groups = 0;
        for (int i = 0; i < t.getTerms().length; ++i) {
            if (t.getTerms()[i] != null) {
                assertTrue("Number of exams at the same time", t.getTerms()[i].size() <= GA.NB_ROOMS);
                nb_groups += t.getTerms()[i].size();
            }
        }
        assertEquals("Number of groups in timetable", GA.NB_GROUPS, nb_groups);
    }

    @Test
    public void evaluateTimetable() throws Exception {
        ArrayList<Integer>[] terms = (ArrayList<Integer>[]) new ArrayList[Term.getInstance().getNbTerms()];
        terms[2] = new ArrayList<>();
        terms[2].add(1);
        terms[1] = new ArrayList<>();
        terms[1].add(2);
        Timetable t = new Timetable(terms);
        t.evaluateTimetable(students, examiners);
        int[] eval = t.getEval();
        assertEquals("Number of hard constraint violations", 2, eval[0]);
        assertEquals("Number of soft constraint violations", 1 + 1*4 + 2, eval[1]);
    }

    @Test(expected = NoSuchElementException.class)
    public void evaluateInvalidTimetable() {
        ArrayList<Integer>[] terms = (ArrayList<Integer>[]) new ArrayList[Term.getInstance().getNbTerms()];
        Timetable t = new Timetable(terms);
        t.evaluateTimetable(students, examiners);
    }

    @Test
    public void evaluatePerfectTimetable() throws Exception {
        ArrayList<Integer>[] terms = (ArrayList<Integer>[]) new ArrayList[Term.getInstance().getNbTerms()];
        terms[5] = new ArrayList<>();
        terms[5].add(1);
        terms[4] = new ArrayList<>();
        terms[4].add(2);
        Timetable t = new Timetable(terms);
        t.evaluateTimetable(students, examiners);
        int[] eval = t.getEval();
        assertEquals("Number of hard constraint violations", 0, eval[0]);
        assertEquals("Number of soft constraint violations", 1 + 1, eval[1]);
    }
}