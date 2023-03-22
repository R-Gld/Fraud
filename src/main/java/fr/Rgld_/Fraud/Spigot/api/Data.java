package fr.Rgld_.Fraud.Spigot.api;

import fr.Rgld_.Fraud.Global.IPInfo;
import fr.Rgld_.Fraud.Spigot.Fraud;
import fr.Rgld_.Fraud.Spigot.Helpers.Links;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.List;

public class Data {

    private final Fraud fraud;
    private final fr.Rgld_.Fraud.Spigot.Storage.Data.Data Data;

    /**
     * Constructor.
     * @param fraud the plugin instance
     */
    public Data(Fraud fraud) {
        this.fraud = fraud;
        this.Data = fraud.getData();
    }

    /** Constructor. */
    public Data() {
        this.fraud = (Fraud) Bukkit.getPluginManager().getPlugin("fraud");
        if(this.fraud == null) {
            this.Data = null;
            System.out.println(ChatColor.RED + "ERROR: Fraud isn't loaded. You can download the latest version at: " + Links.FRAUD_DOWNLOAD);
        } else this.Data = fraud.getData();
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

    /**
     * Give some information about the ip given in argument.
     * @param ip (a {@link String}) the ip of the player that is looked up.
     * @return a {@link IPInfo} that contains all the information of the ip given in arguments (like the city: {@link IPInfo#getCity()}, or the latitude and the longitude: {@link IPInfo#getLatitude()}/{@link IPInfo#getLongitude()}).
     */
    public IPInfo getIPInfo(String ip) {
        return fraud.getIpInfoManager().getIPInfoConformConfig(ip);
    }

}
