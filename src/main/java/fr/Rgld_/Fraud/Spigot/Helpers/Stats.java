package fr.Rgld_.Fraud.Spigot.Helpers;

import com.google.gson.GsonBuilder;
import fr.Rgld_.Fraud.Spigot.Fraud;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Collection;

public class Stats {

    private final Fraud fraud;

    /**
     * @param fraud a Fraud Instance that let this class use the main function of the Fraud class.
     */
    public Stats(Fraud fraud) {
        this.fraud = fraud;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(
                fraud, () -> {
                    Thread th = new Thread(new Sender(this));
                    th.start();
                },
                0, 20*60*30);
    }

    /**
     * Send the info to the Rgld_'s Database.
     *
     * @see fr.Rgld_.Fraud.Spigot.Helpers.Stats.Data the information given.
     */
    public void sendInfo() {
        String data = new Data(fraud).toString();
        String url = Fraud.restAPIBaseUrl + "/api/fraud/stats/";
        String auth = "edGfJSQqavVTWmzQ";
        Utils.postContent(url, data, auth);
    }

    private static class Sender implements Runnable {

        private final Stats stats;

        private Sender(Stats stats) {
            this.stats = stats;
        }

        @Override
        public void run() {
            stats.sendInfo();
        }
    }

    @SuppressWarnings("FieldCanBeLocal")
    public static class Data {
        private final boolean hasWhitelist;
        private final int port;
        private final String pluginVersion;
        private final int online_players;
        private final int offline_players;
        private final String bukkit_version;

        public Data(Fraud fraud) {
            this.pluginVersion = fraud.getDescription().getVersion();
            this.online_players = getPlayerAmount();
            this.offline_players = Bukkit.getOfflinePlayers().length;
            this.bukkit_version = Bukkit.getBukkitVersion();
            this.hasWhitelist = Bukkit.hasWhitelist();
            this.port = Bukkit.getPort();
        }

        public Data(String pluginVersion, int online_players, int offline_players, String bukkit_version, boolean hasWhitelist, int port) {
            this.pluginVersion = pluginVersion;
            this.online_players = online_players;
            this.offline_players = offline_players;
            this.bukkit_version = bukkit_version;
            this.hasWhitelist = hasWhitelist;
            this.port = port;
        }

        private int getPlayerAmount() {
            try {
                Method onlinePlayersMethod = Class.forName("org.bukkit.Server").getMethod("getOnlinePlayers");
                return onlinePlayersMethod.getReturnType().equals(Collection.class)
                        ? ((Collection<?>) onlinePlayersMethod.invoke(Bukkit.getServer())).size()
                        : ((Player[]) onlinePlayersMethod.invoke(Bukkit.getServer())).length;
            } catch (Exception e) {
                return Bukkit.getOnlinePlayers().size(); // Just use the new method if the reflection failed
            }
        }

        @Override
        public String toString() {
            return new GsonBuilder().setPrettyPrinting().create().toJson(this);
        }
    }

}
