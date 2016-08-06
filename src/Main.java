import java.text.ParseException;
import java.util.Arrays;

/**
 * Created by Héliane Ly on 16/07/2016.
 */
public class Main {
    public static void main(String[] args) {
        Term t = Term.getInstance();
        if (t.init("09/05/2016", "31/05/2016") != 0)
            System.exit(-1);
/*
        System.out.println(t.getDay(1));
        System.out.println(t.isMorning(2));
        System.out.println(t.getDay(2));
        System.out.println(t.getNbTerms());
        System.out.println("============");
*/
        Person p = new Person();
/*
        System.out.println("group n°" + p.getGroup() + " : " + p.getLogin());
        System.out.println(Arrays.toString(p.getSchedule()));
*/
    }
}
