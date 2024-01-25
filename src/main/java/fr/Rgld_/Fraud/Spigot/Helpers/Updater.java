package fr.Rgld_.Fraud.Spigot.Helpers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.Rgld_.Fraud.Spigot.Fraud;
import fr.Rgld_.Fraud.Spigot.api.Plugin;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Let the plugin check if a new version has been released <a href="https://www.spigotmc.org/resources/fraud-alts-finder.69872/" target="_blank">on the spigot page</a>.
 */
@SuppressWarnings("ALL")
public class Updater implements Runnable {

    private final fr.Rgld_.Fraud.Spigot.Fraud fraud;
    private Plugin spigotFraud;

    /**
     *
     * We're using the spiget api to obtain the information about the uploads in spigotmc about this plugin.
     *
     * <a href="http://api.spiget.org/v2/resources/69872/download?version=latest" target="_blank">Download last version link</a>.
     * <a href="https://api.spiget.org/v2/resources/69872/versions/latest" target="_blank">Last version informations link</a>.
     *
     * @param fraud the main class ({@link Fraud}).
     */
    public Updater(fr.Rgld_.Fraud.Spigot.Fraud fraud) {
        this.fraud = fraud;
        this.spigotFraud = Plugin.getFraud();
    }

    /**
     * @see Updater#Updater(fr.Rgld_.Fraud.Spigot.Fraud)
     */
    public Updater() {
        this.fraud = null;
        this.spigotFraud = Plugin.getFraud();
    }


    private Plugin refreshFraud() {
        this.spigotFraud = Plugin.getFraud();
        return spigotFraud;
    }

    /**
     * Exemple (06/10/2021) :
     *
     * 1.7.1
     *
     * @return only the version string.
     */
    public String getLatestOnlineVersion() {
        refreshFraud();
        return spigotFraud.getLatestVersion().getData().getName();
    }

    /**
     * Format the version given in parameters, so it can be compared to another version.
     * Example: <code>parseVersion("1.5.9")</code> will return <code>1.59</code>.
     *
     * @param version the version of the plugin.
     * @return the version as a double. Example: 1.5.9 will be returned as 1.59, so it can be compared to another version and check if the version is up-to-date.
     */
    public double parseVersion(String version) {
        return Updater.parseVersionStatic(version);
    }

    /**
     * Format the version given in parameters, so it can be compared to another version.
     * Example: <code>parseVersion("1.5.9")</code> will return <code>1.59</code>.
     *
     * @param version the version of the plugin.
     * @return the version as a double. Example: 1.5.9 will be returned as 1.59, so it can be compared to another version and check if the version is up-to-date.
     */
    public static double parseVersionStatic(String version) {
        String[] spl = version.split("\\.");
        if(spl.length <= 2) {
            return Double.parseDouble(version);
        } else {
            StringBuilder bld = new StringBuilder();
            bld.append(spl[0]).append(".");
            for (int i = 1; i < spl.length; i++) {
                bld.append(spl[i]);
            }
            return Double.parseDouble(bld.toString());
        }
    }


    private CommandSender[] getUpdaterNotifier() {
        Collection<CommandSender> coll = new ArrayList<>();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if(p.hasPermission("fraud.updater.notify")) {
                coll.add(p);
            }
        }
        coll.add(Bukkit.getConsoleSender());

        return coll.toArray(new CommandSender[0]);
    }

    /**
     * Send a message to the high-staff of the server if a new version of Fraud has been released on spigot.
     */
    @Override
    public void run() {
        if(fraud == null) return;
        Console c = fraud.getConsole();
        String lastVersion = getLatestOnlineVersion();
        double vFormat = parseVersion(lastVersion);
        String actualVersion = fraud.getDescription().getVersion();
        double actualVBcFormat = (fraud.actualVersionBc.equals("") ? parseVersion(actualVersion) : parseVersion(fraud.actualVersionBc));
        double actualVFormat = parseVersion(actualVersion);
        if(actualVFormat < vFormat || actualVBcFormat < vFormat) {

            if(fraud.getConfiguration().autoDownloadLatest()) {
                String broadcast;
                if(downloadAndInstall()) {
                    broadcast = "§6§m---------------------------------------\n" +
                            "\n" +
                            "§eA new Update of §6Fraud§e has been installed !\n" +
                            "§eYou have to restart the plugin or the server to update the plugin.\n" +
                            "§8§nLast Version:§7 " + actualVersion + "\n" +
                            "§8§nActual Version just installed:§7 " + lastVersion + "\n" +
                            "\n" +
                            "§6§m---------------------------------------";

                } else {
                    broadcast = "§6§m---------------------------------------\n" +
                            "\n" +
                            "§cAn error occur while trying to update §6Fraud§c.\n" +
                            "§cPlease report this error to §6" + Links.FRAUD_ISSUES + "§c.\n" +
                            "\n" +
                            "§6§m---------------------------------------";
                }
                for(Player p : Bukkit.getOnlinePlayers()) {
                    if(p.hasPermission("fraud.update")) {
                        p.sendMessage(broadcast);
                    }
                }
                c.sm(broadcast);
            } else {
                TextComponent tc = new TextComponent("§e§lUpdate to the new version.");
                tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fraud download"));
                tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§7§lCLICK HERE")));

                for(Player p : Bukkit.getOnlinePlayers()) {
                    if(p.hasPermission("fraud.update")) {
                        p.sendMessage(
                                "§6§m---------------------------------------\n" +
                                        "\n" +
                                        "§eA new Update of §6Fraud §eis now available !\n" +
                                        "§8§nActual Version:§7 " + actualVersion + "\n" +
                                        "§8§nNext Version:§7   " + lastVersion);
                        p.spigot().sendMessage(tc);
                        p.sendMessage(
                                "\n" +
                                        "§6§m---------------------------------------");
                    }
                }
                c.sm(
                        "§6§m---------------------------------------\n" +
                                "\n" +
                                "§eA new Update of §6Fraud §eis now available !\n" +
                                "§8§nActual Version:§7 " + actualVersion + "\n" +
                                "§8§nNext Version:§7   " + lastVersion + "\n" +
                                "§6Go to the Spigot Page: " + Links.FRAUD_SPIGOT.getUrl() + "\n" +
                                "\n" +
                                "§6§m---------------------------------------");
            }
            fraud.actualVersionBc = lastVersion;
        }
    }

    private static final String PLUGIN_FILE_PATH = "./plugins/Fraud.jar";
    private static final Links PLUGIN_DOWNLOAD_URL = Links.FRAUD_DOWNLOAD_WITHOUT_ANALYTICS_VERSION;

    /**
     * Download and Install the latest version.
     *
     * @return true if the download and install is successful, false otherwise.
     */
    public boolean downloadAndInstall(){
        return downloadAndInstall(null);
    }

    /**
     * Download and Install the latest version.
     *
     * @param updateReceivers the receivers of the update message.
     * @return true if the download and install is successful, false otherwise.
     */
    public boolean downloadAndInstall(CommandSender[] updateReceivers) {
        String latestVersion = getLatestOnlineVersion();

        if (isUpToDate()) {
            if(updateReceivers != null)
                for (CommandSender updateReceiver : updateReceivers)
                    updateReceiver.sendMessage("§aFraud is already up-to-date!");
            return false;
        }

        File destination = new File(PLUGIN_FILE_PATH);
        destination.getParentFile().mkdirs();

        try {
            URL url = new URL(PLUGIN_DOWNLOAD_URL.format(latestVersion));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Referer", "FraudUpdater");
            connection.setRequestProperty("User-Agent", "FraudUpdater");

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK && (responseCode < 300 || responseCode > 400)) {
                if(updateReceivers != null) {
                    for (CommandSender updateReceiver : updateReceivers) {
                        updateReceiver.sendMessage("§aThere is a problem with the url. Watch console and report to the Fraud's author.");
                    }
                }
                System.err.println("There is an error with the update of Fraud, the response code should be 200 but is " + connection.getResponseCode());
                return false;
            }

            try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                 FileOutputStream out = new FileOutputStream(destination)) {

                byte[] dataBuffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    out.write(dataBuffer, 0, bytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                connection.disconnect();
            }
            if(checkFileHash()) {
                System.out.println("§aNew Fraud version written to " + destination.toPath().toAbsolutePath().toString());
                return true;
            } else {
                System.err.println("§cThe downloaded file is corrupted. Please try again.");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean checkFileHash() {
        System.out.println("§aVerifying the integrity of the downloaded file...");
        String[] result = Utils.getContent("https://api.rgld.fr/fraud/hash");
        if(Integer.valueOf(result[1]) == 200) {
            JsonObject jsonObject = new JsonParser().parse(result[0]).getAsJsonObject().getAsJsonObject("data");
            String md5_distant = jsonObject.get("md5").getAsString();
            String md5_local = null;
            try {
                md5_local = Utils.getMD5Hash("./plugins/Fraud.jar");
            } catch (NoSuchAlgorithmException | IOException e) {
                System.err.println("§cError while getting the MD5 hash of the local file.");
                e.printStackTrace();
            }
            if(md5_distant.equals(md5_local)) {
                System.out.println("§aThe integrity of the downloaded file is verified.");
                return true;
            } else {
                System.err.println("§cThe integrity of the downloaded file is not verified.");
            }
        }
        return false;
    }

    /**
     * Check if the actual version is up-to-date.
     *
     * @return true if the actual version of Fraud installed is up-to-date, false otherwise.
     */
    public boolean isUpToDate() {
        if (fraud == null) return false;
        double actualVersion = parseVersion(fraud.getDescription().getVersion());
        double latestVersion = parseVersion(getLatestOnlineVersion());
        return latestVersion <= actualVersion;
    }
}
