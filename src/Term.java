import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Days;

/**
 * Created by Héliane Ly on 02/08/2016.
 */
public final class Term {
    private static Term instance = null;
    private Date startDate;
    private Date endDate;
    private int nbTerms; // length of Term array

    private Term() {}

    public static Term getInstance() {
        if (instance == null)
            instance = new Term();
        return instance;
    }

    public int init(String start, String end) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
            startDate = fmt.parse(start);
            endDate = fmt.parse(end);
            int days = Days.daysBetween(new DateTime(startDate), new DateTime(endDate)).getDays();
            nbTerms = (days + 1) * 2;
            if (nbTerms <= 0)
                throw new IllegalArgumentException("End date must be posterior to start date");
        } catch (ParseException | IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
        return 0;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public int getNbTerms() {
        return nbTerms;
    }

    public Date getDay(int index) {
        DateTime start = new DateTime(startDate);
        return start.plusDays(index / 2).toDate();
    }

    // True if AM, False if PM
    public boolean isMorning(int index) {
        return (index % 2) == 0;
    }

    public boolean isSameDay(int idx1, int idx2) {
        DateTime start = new DateTime(startDate);
        return start.plusDays(idx1 / 2).equals(start.plusDays(idx2 / 2));
    }
}
