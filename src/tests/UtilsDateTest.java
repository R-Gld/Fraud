import fr.Rgld_.Fraud.Spigot.Helpers.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class UtilsDateTest {

    public static void main(String[] args) {
        System.out.println("Utils.formatDate(new Date().getTime()/1000) = " + Utils.formatDate(new Date().getTime()*2));
        System.out.println("formatDate(new Date().getTime()/1000) = " + formatDate(new Date().getTime()*2));
    }


    public static String formatDate(long time) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        Calendar now = Calendar.getInstance();
        if(c.equals(now)) return Messages.NOW.getMessage();
        boolean future = c.after(now);
        int[] types = { Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND };
        String[] names = { Messages.YEAR.getMessage(), Messages.YEARS.getMessage(),
                Messages.MONTH.getMessage(), Messages.MONTHS.getMessage(),
                Messages.DAY.getMessage(), Messages.DAYS.getMessage(),
                Messages.HOUR.getMessage(), Messages.HOURS.getMessage(),
                Messages.MINUTE.getMessage(), Messages.MINUTES.getMessage(),
                Messages.SECOND.getMessage(), Messages.SECONDS.getMessage() };
        List<String> timeStrings = new ArrayList<>();
        int accuracy = 0;
        for(int i = 0; i < types.length && accuracy <= 2; i++) {
            int diff = c.get(types[i]) - now.get(types[i]);
            if(diff > 0 || (diff < 0 && i == 0)) {
                accuracy++;
                timeStrings.add(" " + Math.abs(diff) + " " + names[i * 2 + ((Math.abs(diff) > 1) ? 1 : 0)]);
            }
        }
        if(timeStrings.isEmpty()) {
            return Messages.NOW.getMessage();
        }
        if(timeStrings.size() > 2)
            timeStrings.add(timeStrings.size() - 1, Messages.AND.getMessage());
        return String.join(" ", timeStrings);
    }

    enum Messages {
        YEAR("année"),
        YEARS("années"),
        MONTH("mois"),
        MONTHS("mois"),
        DAY("jours"),
        DAYS("jour"),
        HOUR("heure"),
        HOURS("heures"),
        MINUTE("minute"),
        MINUTES("minutes"),
        SECOND("secondes"),
        SECONDS("seconde"),
        NOW("maintenant"),
        AND("et");

        private final String messages;

        Messages(String messages) {
            this.messages = messages;
        }

        public String getMessage() {
            return messages;
        }
    }


}
