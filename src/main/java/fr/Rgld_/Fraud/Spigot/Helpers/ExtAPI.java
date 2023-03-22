package fr.Rgld_.Fraud.Spigot.Helpers;

import com.google.gson.GsonBuilder;
import fr.Rgld_.Fraud.Global.IPInfo;
import fr.Rgld_.Fraud.Global.IPInfoManager;
import fr.Rgld_.Fraud.Spigot.Fraud;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ExtAPI {

    public final String restAPIBaseUrl;
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
        this.restAPIBaseUrl = fraud.getConfiguration().getRestApiChoice();
        this.ipInfoManager = new IPInfoManager(fraud, this);
        File bStatsFolder = new File(fraud.getDataFolder().getParentFile(), "bStats");
        File configFile = new File(bStatsFolder, "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        this.serverUUID = UUID.fromString(config.getString("serverUuid"));
    }

    /**
     * Constructor.
     */
    public ExtAPI() {
        this.fraud = (Fraud) Bukkit.getPluginManager().getPlugin("fraud");
        if(this.fraud == null) {
            this.ipInfoManager = null;
            this.restAPIBaseUrl = null;
            throw new RuntimeException("ERROR: Fraud isn't loaded. You can download the latest version at: " + Links.FRAUD_DOWNLOAD);
        } else {
            this.restAPIBaseUrl = fraud.getConfiguration().getRestApiChoice();
            this.ipInfoManager = new IPInfoManager(fraud, this);
            File bStatsFolder = new File(fraud.getDataFolder().getParentFile(), "bStats");
            File configFile = new File(bStatsFolder, "config.yml");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            this.serverUUID = UUID.fromString(config.getString("serverUuid"));
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

    public boolean isAPIReachable() {
        String[] content = Utils.getContent(restAPIBaseUrl + "reach", serverUUID);
        return content[1].equals("200") && content[0].equals("{\"reachable\": \"OK\"}");
    }

    public String getOwnIP() {
        return Utils.getContent(restAPIBaseUrl + "ownip")[0];
    }

    public int sendFraudStats() {
        Stats.Data data = new Stats.Data(fraud);
        return sendFraudStats(data);
    }

    public int sendFraudStats(Stats.Data data) {
        return Utils.postContent(restAPIBaseUrl + "fraud/stats", data.toString(), restAPIkey, serverUUID);
    }

    public IPInfo getIPInfo(String ip, boolean geoip) {
        return ipInfoManager.getIpInfo(ip, geoip);
    }


    public int askHelp(String sentence) {
        return Utils.postContent(restAPIBaseUrl + "fraud/askHelp", "", restAPIkey, serverUUID);
    }

    private static final class HelpData {

        private final String sentence;
        private final String ip;
        private final int port;
        private final String bukkitVersion;
        private final HashMap<String, String> plugins;

        private HelpData(String sentence) {
            this.sentence = sentence;
            Server srv = Bukkit.getServer();
            this.ip = srv.getIp();
            this.port = srv.getPort();
            this.bukkitVersion = srv.getBukkitVersion();
            this.plugins = new HashMap<>();
            for (Plugin plugin : srv.getPluginManager().getPlugins()) {
                PluginDescriptionFile pdf = plugin.getDescription();
                plugins.put(pdf.getName(), pdf.getVersion());
            }
        }

        public String getSentence() {
            return sentence;
        }

        @Override
        public String toString() {
            return new GsonBuilder().create().toJson(this);
        }
    }
    
}
