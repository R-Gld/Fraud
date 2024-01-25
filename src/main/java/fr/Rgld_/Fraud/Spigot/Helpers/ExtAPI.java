package fr.Rgld_.Fraud.Spigot.Helpers;

import fr.Rgld_.Fraud.Spigot.Fraud;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class ExtAPI {

    public final String restAPIBaseUrl = Links.BASE_RGLD_API.getUrl();
    public final String restAPIkey = "edGfJSQqavVTWm";

    private final Fraud fraud;
    private final UUID serverUUID;
    private final IPInfoManager ipInfoManager;

    /**
     * Constructor.
     * @param fraud the plugin instance
     */
    public ExtAPI(Fraud fraud) {
        this.fraud = fraud;
        this.ipInfoManager = new IPInfoManager(fraud, this);
        this.serverUUID = obtainServerUUID();
    }

    /**
     * Get the server UUID from bStats or mcStats.
     * @return the server UUID
     */
    private UUID obtainServerUUID() {
        File bStatsFolder = new File(fraud.getDataFolder().getParentFile(), "bStats");
        File mcStatsFolder = new File(fraud.getDataFolder().getParentFile(), "PluginMetrics");

        File baseFolder;
        if(bStatsFolder.exists()) {
            baseFolder = bStatsFolder;
        } else if(mcStatsFolder.exists()) {
            baseFolder = mcStatsFolder;
        } else {
            return null;
        }

        File configFile = new File(baseFolder, "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        return UUID.fromString(baseFolder == bStatsFolder ? config.getString("serverUuid") : config.getString("guid"));
    }


    /**
     * Constructor without the plugin instance (useful for the API) (not recommended)
     */
    public ExtAPI() {
        this.fraud = (Fraud) Bukkit.getPluginManager().getPlugin("fraud");
        if(this.fraud == null) {
            this.ipInfoManager = null;
            throw new RuntimeException("ERROR: Fraud isn't loaded. You can download the latest version at: " + Links.FRAUD_DOWNLOAD);
        } else {
            this.ipInfoManager = new IPInfoManager(fraud, this);
            this.serverUUID = obtainServerUUID();
        }
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
        return fraud.getData().getListByPseudo(pseudo);
    }
    public UUID getServerUUID() {
        return serverUUID;
    }

    /**
     * Check if the API is reachable.
     * @return true if the API is reachable, false otherwise
     */
    public boolean isAPIReachable() {
        String[] content = Utils.getContent(Links.RGLD_API_REACH.getUrl(), serverUUID);
        return content[1].equals("200") && content[0].equals("{\"reachable\": \"OK\"}");
    }

    /**
     * Get the ip of the server.
     * @return the ip of the server
     */
    public String getOwnIP() {
        return Utils.getContent(Links.RGLD_API_OWN_IP.getUrl())[0];
    }

    /**
     * Send the stats of the server to the API.
     * @return the response code of the request
     */
    public int sendFraudStats() {
        Stats.Data data = new Stats.Data(fraud);
        return sendFraudStats(data);
    }

    /**
     * Send the stats of the server to the API.
     * @param data the data to send
     * @return the response code of the request
     */
    private int sendFraudStats(Stats.Data data) {
        return Utils.postContent(Links.RGLD_API_STATS.getUrl(), data.toString(), restAPIkey, serverUUID);
    }

    /**
     * Give some information about the ip given in argument.
     * @param ip (a {@link String}) the ip of the player that is looked up.
     * @param geoip if the geoip information should be returned
     * @return a {@link IPInfo} that contains all the information of the ip given in arguments (like the city: {@link IPInfo#getCity()}, or the latitude and the longitude: {@link IPInfo#getLatitude()}/{@link IPInfo#getLongitude()}).
     */
    public IPInfo getIPInfo(String ip, boolean geoip) {
        return ipInfoManager.getIpInfo(ip, geoip);
    }
    
}
