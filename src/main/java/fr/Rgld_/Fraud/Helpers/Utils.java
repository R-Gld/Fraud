package fr.Rgld_.Fraud.Helpers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class Utils {

    public static boolean isConnected(String p) {
        try {
            return Bukkit.getPlayer(p).isOnline();
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean canGetAnAlt(List<String> altsList) {
        for (String str : altsList) {
            Player p;
            try {
                p = Bukkit.getPlayer(str);
            } catch (NullPointerException e) {
                continue;
            }
            if (p == null) continue;
            if (p.hasPermission("fraud.notcause.alert")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidIP(String ipAddress) {
        return Pattern
                .compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$")
                .matcher(ipAddress)
                .matches();
    }

    public static String joinList(Object... list) {
        return joinList("§r, ", list);
    }

    private static String joinList(String separator, Object... list) {
        Arrays.sort(list);
        StringBuilder buf = new StringBuilder();
        for (Object each : list) {
            if (buf.length() > 0) {
                buf.append(separator);
            }
            if ((each instanceof Collection)) {
                buf.append(joinList(separator, ((Collection) each).toArray()));
            } else {
                buf.append(each.toString());
            }
        }
        return buf.toString();
    }

}
