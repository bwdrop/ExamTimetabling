import model.Term;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Héliane Ly on 04/08/2016.
 */
public class TermTest {
    private Term t = Term.getInstance();
    @Before
    public void setUp() throws Exception {
        t.init("09/05/2016", "31/05/2016");
    }

    @Test
    public void initInvalidPeriod() throws Exception {
        assertFalse("Exam period end date before start date", t.init("09/05/2016", "01/05/2016") == 0);
    }

    @Test
    public void initInvalidDates() throws Exception {
        assertFalse("Invalid date format", t.init("hello", "world") == 0);
    }

    @Test
    public void init() throws Exception {
        assertTrue("Valid date format", t.init("09/05/2016", "31/05/2016") == 0);
        assertTrue("Valid exam period", t.getNbTerms() > 0);
    }

    @Test
    public void getDay() throws Exception {
        assertEquals("Get first day", t.getDay(0), t.getStartDate());
        assertEquals("Get last day", t.getDay(t.getNbTerms() - 1), t.getEndDate());
    }

    @Test
    public void getDayOfWeek() throws Exception {
        assertEquals("First day of exams is Monday", 1, t.getDayOfWeek(0));
        assertNotEquals("First day of exams isn't Sunday", 7, t.getDayOfWeek(0));
    }

    @Test
    public void isMorning() throws Exception {
        assertTrue("Morning term", t.isMorning(0));
        assertFalse("Afternoon term", t.isMorning(t.getNbTerms() - 1));
    }

    @Test
    public void isSameDay() throws Exception {
        assertTrue("Same day", t.isSameDay(0, 1));
        assertFalse("Different day", t.isSameDay(0, t.getNbTerms() - 1));
    }
}