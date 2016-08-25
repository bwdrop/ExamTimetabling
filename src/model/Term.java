package model;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Years;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Héliane Ly on 02/08/2016.
 */
public final class Term {
    private static Term instance = null;
    private Date startDate;
    private Date endDate;
    private int nbTerms; // length of model.Term array

    private Term() {}

    public static Term getInstance() {
        if (instance == null)
            instance = new Term();
        return instance;
    }

    /**
     * Initialize Term with a start and end date
     * @param start
     * @param end
     * @return error status (0 if no error)
     */
    public int init(String start, String end) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
            startDate = fmt.parse(start);
            endDate = fmt.parse(end);
            int days = Days.daysBetween(new DateTime(startDate), new DateTime(endDate)).getDays();
            nbTerms = (days + 1) * 2;
            if (nbTerms <= 0)
                throw new IllegalArgumentException("End date must be posterior to start date");
            if (Years.yearsBetween(new DateTime(startDate), new DateTime(endDate)).getYears() > 0)
                throw new IllegalArgumentException("Exam period must not exceed one year");
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

    /**
     * Get day corresponding to specified index
     * @param index
     * @return Date
     */
    public Date getDay(int index) {
        DateTime start = new DateTime(startDate);
        return start.plusDays(index / 2).toDate();
    }

    /**
     * Get day of week (1 - 7) based on term index
     * @param index
     * @return day of week
     */
    public int getDayOfWeek(int index) {
        DateTime start = new DateTime(startDate);
        return start.plusDays(index / 2).getDayOfWeek();
    }

    /**
     * @param index
     * @return true if index is in the morning, false otherwise
     */
    public boolean isMorning(int index) {
        return (index % 2) == 0;
    }

    /**
     * @param idx1 index 1
     * @param idx2 index 2
     * @return true if both index are on the same day, false otherwise
     */
    public boolean isSameDay(int idx1, int idx2) {
        DateTime start = new DateTime(startDate);
        return start.plusDays(idx1 / 2).equals(start.plusDays(idx2 / 2));
    }

    /**
     * @param d date
     * @return year of specified date
     */
    public int getYear(DateTime d) {
        DateTime sd = new DateTime(startDate);
        DateTime ed = new DateTime(endDate);
        if (sd.getYear() == ed.getYear())
            return sd.getYear();
        return (d.withYear(sd.getYear()).isAfter(sd)) ? sd.getYear() : ed.getYear();
    }

    /**
     * @param d date
     * @return index corresponding to specified date
     */
    public int getIndex(DateTime d) {
        int index = Days.daysBetween(new DateTime(startDate), d).getDays() * 2;
        index += (d.getHourOfDay() > 0) ? 1 : 0;
        return index;
    }
}
