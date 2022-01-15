import fr.Rgld_.Fraud.Global.IPInfo;
import fr.Rgld_.Fraud.Spigot.Helpers.Utils;
import fr.Rgld_.Fraud.Spigot.api.Data;
import org.bukkit.Bukkit;

import java.util.List;

public class Example {

    public void printAltsOf(String pseudo) {
        Data data = new Data();
        List<String> altsOfRgld_ = data.getAlts(pseudo);
        System.out.println("Alts of " + pseudo + ":");
        for(String alt : altsOfRgld_) {
            System.out.println("\t- " + alt);
        }
    }

    public void getLatitudeAndLongitudeOfPlayer(String pseudo) {
        Data data = new Data();
        IPInfo ipInfoOfRgld_ = data.getIPInfo(Utils.getAddress(Bukkit.getPlayer(pseudo).getAddress()));
        System.out.println(pseudo + " is located at these coordinates(lat/lon): " + ipInfoOfRgld_.getLatitude() + "/" + ipInfoOfRgld_.getLongitude());
    }
}
