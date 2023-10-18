package fr.Rgld_.Fraud.Spigot.Helpers;

import com.google.gson.GsonBuilder;
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
     * Constructor.
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

    public boolean isAPIReachable() {
        String[] content = Utils.getContent(Links.RGLD_API_REACH.getUrl(), serverUUID);
        return content[1].equals("200") && content[0].equals("{\"reachable\": \"OK\"}");
    }

    public String getOwnIP() {
        return Utils.getContent(Links.RGLD_API_OWN_IP.getUrl())[0];
    }

    public int sendFraudStats() {
        Stats.Data data = new Stats.Data(fraud);
        return sendFraudStats(data);
    }

    public int sendFraudStats(Stats.Data data) {
        System.out.println("data.toString() = " + data.toString());
        return Utils.postContent(Links.RGLD_API_STATS.getUrl(), data.toString(), restAPIkey, serverUUID);
    }

    public IPInfo getIPInfo(String ip, boolean geoip) {
        return ipInfoManager.getIpInfo(ip, geoip);
    }


    public int askHelp(String sentence) {
        String data = new HelpData(sentence).toString();
        System.out.println("askHelp data sent = " + data);
        return Utils.postContent(Links.RGLD_API_ASK_HELP.getUrl(), data, restAPIkey, serverUUID);
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

        @Override
        public String toString() {
            return new GsonBuilder().create().toJson(this);
        }
    }
    
}
