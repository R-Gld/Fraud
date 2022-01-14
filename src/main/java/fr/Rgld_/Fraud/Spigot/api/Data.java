package fr.Rgld_.Fraud.Spigot.api;

import fr.Rgld_.Fraud.Global.IPInfo;
import fr.Rgld_.Fraud.Spigot.Fraud;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.List;

public class Data {

    private final Fraud fraud;
    private final fr.Rgld_.Fraud.Spigot.Storage.Data.Data Data;

    public Data() {
        this.fraud = (Fraud) Bukkit.getPluginManager().getPlugin("fraud");
        if(this.fraud == null) {
            this.Data = null;
            System.out.println(ChatColor.RED + "ERROR: Fraud isn't loaded. You can download the latest version at: http://fraud.rgld.fr");
        } else this.Data = fraud.getDatas();
    }


    /**
     * Just a getter for the main class of this plugin.
     * @return the main class of this plugin: {@link Fraud}.
     */
    public Fraud getFraud() {
        return this.fraud;
    }

    /**
     * Give all the alts of the player inside the server.
     *
     * @param pseudo (a {@link String}) the name of the player that is looked up.
     * @return a {@link List} of {@link String} containing all the alts of the player.
     */
    public List<String> getAlts(String pseudo) {
        return Data.getListByPseudo(pseudo);
    }

    public IPInfo getIPInfo(String ip) {
        return fraud.getIpInfoManager().getIPInfoConformConfig(ip);
    }

}
