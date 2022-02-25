import fr.Rgld_.Fraud.Global.IPInfo;
import fr.Rgld_.Fraud.Spigot.Helpers.Utils;
import fr.Rgld_.Fraud.Spigot.api.Data;
import org.bukkit.Bukkit;

import java.net.InetSocketAddress;
import java.util.List;

public class Example {

    /**
     * Sends in the console the complete list of alts of the player given in parameters.
     *
     * @param pseudo The name in game of the player.
     */
    public void printAltsOf(String pseudo) {
        Data data = new Data();
        List<String> altsOfRgld_ = data.getAlts(pseudo);
        System.out.println("Alts of " + pseudo + ":");
        for (String alt : altsOfRgld_) {
            System.out.println("\t- " + alt);
        }
    }

    /**
     * Print in the console the geolocation of a player into the minecraft (spigot) server.
     * @param pseudo (a {@link String}) the name in game of the player that we want to obtain the geolocation.
     * @see Example#getLatitudeAndLongitudeOfAnIp(String)
     * @see Utils#getAddress(InetSocketAddress) to format the {@link InetSocketAddress} object to a string that is conforming to the database.
     */
    public void getLatitudeAndLongitudeOfAPlayer(String pseudo) {
        getLatitudeAndLongitudeOfAnIp(Utils.getAddress(Bukkit.getPlayer(pseudo).getAddress()));
    }

    /**
     * Print in the console the geolocation of an ip.
     * @param ip (a {@link String}) the ip that we want to obtain the geolocation.
     * @see Example#getLatitudeAndLongitudeOfAnIp(String)
     */
    public void getLatitudeAndLongitudeOfAnIp(String ip) {
        Data data = new Data();
        IPInfo ipInfoOfRgld_ = data.getIPInfo(ip);
        System.out.println(ip + " is located at these coordinates(lat/lon): " +
                ipInfoOfRgld_.getLatitude() + "/" + ipInfoOfRgld_.getLongitude()
        );
    }
}