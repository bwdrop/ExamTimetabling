import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Héliane Ly on 04/08/2016.
 */
public class PersonTest {
    @Before
    public void setUp() throws Exception {
        Term t = Term.getInstance();
        t.init("09/05/2016", "31/05/2016");
    }

    @Test
    public void generateLogin() throws Exception {
        // assert that login starts with 2 to 4 letters and finishes with a number
        Person p = new Person();
        char[] c = p.getLogin().toCharArray();
        int i = 0;
        while (i < c.length && Character.isLetter(c[i]))
            ++i;
        assertTrue("Generated login initials length is correct", i >= 2 && i <= 4);
        while (i < c.length)
            assertTrue("Generated login ends with number", Character.isDigit(c[i++]));
    }

    @Test
    public void generateSchedule() throws Exception {
        Person p = new Person();
        int[] s = p.getSchedule();
        assertEquals("Schedule array length", Term.getInstance().getNbTerms(), s.length);
        int nb_exams = 0;
        for (int i = 0; i < s.length; ++i) {
            if (s[i] != 0)
                nb_exams++;
        }
        assertTrue("Number of exams within boundaries", nb_exams >= Person.MIN_EXAMS && nb_exams <= Person.MAX_EXAMS);
    }

    @Test
    public void checkGroup() throws Exception {
        Person p = new Person();
        assertTrue("Person group within boundaries", p.getGroup() > 0 && p.getGroup() <= GA.NB_GROUPS);
    }
}