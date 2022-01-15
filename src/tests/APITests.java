import fr.Rgld_.Fraud.Global.IPInfo;
import fr.Rgld_.Fraud.Spigot.Helpers.Utils;
import fr.Rgld_.Fraud.Spigot.api.Data;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class APITests extends JavaPlugin {

    @Override
    public void onEnable() {
        Data data = new Data(); //
        List<String> altsOfRgld_ = data.getAlts("Rgld_");
        System.out.println("Alts of Rgld_:");
        for(String alt : altsOfRgld_) {
            System.out.println("\t- " + alt);
        }

        IPInfo ipInfoOfRgld_ = data.getIPInfo(Utils.getAddress(Bukkit.getPlayer("Rgld_").getAddress()));
        System.out.println("Rgld_ is located at these coordinates(lat/lon): " + ipInfoOfRgld_.getLatitude() + "/" + ipInfoOfRgld_.getLongitude());
    }
}
