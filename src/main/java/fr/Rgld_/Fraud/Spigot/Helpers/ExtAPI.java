package fr.Rgld_.Fraud.Spigot.Helpers;

import fr.Rgld_.Fraud.Global.IPInfo;
import fr.Rgld_.Fraud.Global.IPInfoManager;
import fr.Rgld_.Fraud.Spigot.Fraud;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.UUID;

public class ExtAPI {
    public final String restAPIBaseUrl = "https://api.rgld.fr/";
    public final String restAPIkey = "edGfJSQqavVTWm";

    private final Fraud fraud;
    private final UUID serverUUID;
    private final IPInfoManager ipInfoManager;

    public ExtAPI(Fraud fraud) {
        this.fraud = fraud;
        this.ipInfoManager = new IPInfoManager(fraud);
        File bStatsFolder = new File(fraud.getDataFolder().getParentFile(), "bStats");
        File configFile = new File(bStatsFolder, "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        this.serverUUID = UUID.fromString(config.getString("serverUuid"));
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
        System.out.println("data.toString() = " + data.toString());
        return Utils.postContent(restAPIBaseUrl + "fraud/stats", data.toString(), restAPIkey, serverUUID);
    }

    public IPInfo getIPInfo(String ip, boolean geoip) {
        return ipInfoManager.getIpInfo(ip, geoip);
    }


}
