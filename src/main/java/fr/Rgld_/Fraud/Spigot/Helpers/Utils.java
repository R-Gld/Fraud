package fr.Rgld_.Fraud.Spigot.Helpers;

import com.google.common.collect.Lists;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Contain several functions useful for the plugin.
 */
public class Utils {

    /**
     * Tell if the player player_name is connected or not.
     *
     * @param player_name a {@link String} a player name.
     * @return True if the player who has {@code player_name} as name is connected, False otherwise.
     */
    public static boolean isConnected(String player_name) {
        Player p = Bukkit.getPlayer(player_name);
        return p != null && p.isOnline();
    }

    /**
     *  Tell if one of the players inside the list given in parameters has the permission to get alts.
     *
     * @param altsList boolean
     * @return True if a player into the altsList has the permission to get alts. False otherwise.
     */
    public static boolean cantGetAnAlt(List<String> altsList) {
        for(String str : altsList) {
            Player p;
            try {
                p = Bukkit.getPlayer(str);
            } catch(NullPointerException e) {
                continue;
            }
            if(p == null) continue;
            if(p.hasPermission("fraud.notcause.alert")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tell if the string given in parameters is an ipv4 address.
     *
     * @param ipAddress the ip address.
     * @return True if the ip given is an ipv4 address. False otherwise.
     */
    public static boolean isValidIP(final String ipAddress) {
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
            if(each instanceof Collection) {
                //noinspection rawtypes
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


    /**
     * Method used by {@link Utils#formatDate(long)}.
     */
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
     * Gives the content of the web page associated with the url given in parameter.
     *
     * @param url an http url.
     * @return the content of this url.
     */
    public static String getContent(final String url) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "FraudClient");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } catch(IOException e) {
            e.printStackTrace();
            return "ERROR: " + Arrays.toString(e.getStackTrace());
        }
    }

    /**
     * Gives the content of the web page associated with the url given in parameter.
     *
     * @param url an http url.
     * @param auth the auth token.
     * @return the content of this url.
     */
    public static String[] getContent(final String url, final String auth) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "FraudClient");
            con.setRequestProperty("Authorization", auth);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            String respCode = String.valueOf(con.getResponseCode());
            in.close();
            con.disconnect();
            return new String[] {response.toString(), respCode};
        } catch(ConnectException e) {
            return new String[] {"ERROR: " + Arrays.toString(e.getStackTrace()), "-1"};
        } catch(IOException e) {
            e.printStackTrace();
            return new String[] {"ERROR: " + Arrays.toString(e.getStackTrace()), "-1"};
        }
    }

    /**
     * @param url the url where the request is sent.
     * @param content the content of the post request.
     * @return the html code of the request or -1 if the request as an error
     */
    public static int postContent(final String url, final String content, final String auth) {
        return postContent(url, content, "application/json; utf-8", "application/json", auth);
    }

    /**
     *
     * @param url the url where the request is sent.
     * @param content the content of the post request.
     * @param dataType the datatype (Example: "application/json")
     * @param accept Example: "application/json"
     * @return the html code of the request or -1 if the request as an error
     */
    public static int postContent(final String url, final String content, final String dataType, final String accept, final String auth) {
        HttpClient httpClient = HttpClientBuilder.create().build();
        String userAgent = "FraudClient";
        try {
            HttpPost request = new HttpPost(url);
            request.addHeader("Content-Type", dataType);
            request.addHeader("Accept", accept);
            request.addHeader("User-Agent", userAgent);
            request.addHeader("Authorization", auth);
            StringEntity params = new StringEntity(content, ContentType.APPLICATION_JSON.getMimeType(), StandardCharsets.UTF_8.toString());
            request.setEntity(params);
            return httpClient.execute(request).getStatusLine().getStatusCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }


    /**
     * Format a {@link InetSocketAddress} to be used in the plugin.
     *
     * @param address an {@link InetSocketAddress} object.
     * @return the address formatted as a {@link String} to be used in the plugin.
     */
    public static String getAddress(InetSocketAddress address) {
        return address.toString().split(":")[0].substring(1);
    }
}
