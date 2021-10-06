package fr.Rgld_.Fraud.Helpers;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Contain several functions useful for the plugin.
 */
public class Utils {

    /**
     *
     * Return true or false if the player p is connected.
     *
     * @param p String
     * @return Boolean
     */
    public static boolean isConnected(String p) {
        try {
            return Bukkit.getPlayer(p).isOnline();
        } catch(Throwable t) {
            return false;
        }
    }

    /**
     *
     * @param altsList boolean
     * @return true or false if a player into the altsList has the permission to get alts.
     */
    public static boolean canGetAnAlt(List<String> altsList) {
        for(String str : altsList) {
            Player p;
            try {
                p = Bukkit.getPlayer(str);
            } catch(NullPointerException e) {
                continue;
            }
            if(p == null) continue;
            if(p.hasPermission("fraud.notcause.alert")) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param ipAddress boolean
     * @return true or false if the ip given is an ipv4 address.
     */
    public static boolean isValidIP(String ipAddress) {
        return Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$").matcher(ipAddress).matches();
    }

    public static String joinList(Object... list) {
        return joinList("§r, ", list);
    }

    private static String joinList(String separator, Object... list) {
        Arrays.sort(list);
        StringBuilder buf = new StringBuilder();
        for(Object each : list) {
            if(buf.length() > 0) {
                buf.append(separator);
            }
            if((each instanceof Collection)) {
                buf.append(joinList(separator, ((Collection) each).toArray()));
            } else {
                buf.append(each.toString());
            }
        }
        return buf.toString();
    }


    /**
     * Method taken into EssentialsX (from com.earth2me.essentials.utils.DateUtil) adapted for this plugin.
     * Download link to essentialsX: https://www.spigotmc.org/resources/essentialsx.9089/
     *
     * @param time String
     * @return date formatted.
     */
    public static String formatDate(long time) {
        boolean future = false;
        Calendar c = new GregorianCalendar(); /* -> */ c.setTimeInMillis(time);
        Calendar now = new GregorianCalendar();
        if(c.equals(now)) return Messages.NOW.getMessage();
        if(c.after(now)) future = true;
        StringBuilder sb = new StringBuilder();
        int[] types = { 1, 2, 5, 11, 12, 13 };
        String[] names =
              { Messages.YEAR.getMessage(), Messages.YEARS.getMessage(),
                Messages.MONTH.getMessage(), Messages.MONTHS.getMessage(),
                Messages.DAY.getMessage(), Messages.DAYS.getMessage(),
                Messages.HOUR.getMessage(), Messages.HOURS.getMessage(),
                Messages.MINUTE.getMessage(), Messages.MINUTES.getMessage(),
                Messages.SECOND.getMessage(), Messages.SECONDS.getMessage() };
        int accuracy = 0;
        for(int i = 0; i < types.length && accuracy <= 2; i++) {
            int diff = dateDiff(types[i], now, c, future);
            if(diff > 0) {
                accuracy++;
                sb.append(" ").append(diff).append(" ").append(names[i * 2 + ((diff > 1) ? 1 : 0)]);
            }
        }
        if(sb.length() == 0) {
            return Messages.NOW.getMessage();
        }
        LinkedList<String> words = new LinkedList<>(Lists.newArrayList(sb.toString().trim().split(" ")));
        if(words.size() > 2)
            words.add(words.size() - 2, Messages.AND.getMessage());
        return String.join(" ", words);
    }


    private static int dateDiff(int type, Calendar fromDate, Calendar toDate, boolean future) {
        int fromYear = fromDate.get(Calendar.YEAR);
        int toYear = toDate.get(Calendar.YEAR);
        if (Math.abs(fromYear - toYear) > 100000) {
            toDate.set(Calendar.YEAR, fromYear + (future ? 100000 : -100000));
        }

        int diff = 0;
        long savedDate = fromDate.getTimeInMillis();
        while ((future && !fromDate.after(toDate)) || (!future && !fromDate.before(toDate))) {
            savedDate = fromDate.getTimeInMillis();
            fromDate.add(type, future ? 1 : -1);
            diff++;
        }
        diff--;
        fromDate.setTimeInMillis(savedDate);
        return diff;
    }

    /**
     * @param address InetSocketAddress
     * @return the address formated to be used in the plugin.
     */
    public static String getAddress(InetSocketAddress address) {
        return address.toString().split(":")[0].substring(1);
    }
}
