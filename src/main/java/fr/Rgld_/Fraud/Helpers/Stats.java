package fr.Rgld_.Fraud.Helpers;

import com.google.gson.GsonBuilder;
import fr.Rgld_.Fraud.Fraud;
import org.bukkit.Bukkit;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

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
                0, 5 * 60 * 5);
    }

    public void sendInfo() throws IOException {
        String datas = new Data(fraud).toString();
        Socket socket = new Socket("rgld.fr", 61812);

        OutputStream outputStream = socket.getOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.writeUTF(datas);
        dataOutputStream.flush();
        dataOutputStream.close();
        socket.close();
    }

    private static class Sender implements Runnable {

        private final Stats stats;

        private Sender(Stats stats) {
            this.stats = stats;
        }

        @Override
        public void run() {
            try {
                stats.sendInfo();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            this.online_players = Bukkit.getOnlinePlayers().size();
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

        @Override
        public String toString() {
            return new GsonBuilder().setPrettyPrinting().create().toJson(this);
        }
    }

}
