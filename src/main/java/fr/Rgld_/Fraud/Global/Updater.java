package fr.Rgld_.Fraud.Global;

import fr.Rgld_.Fraud.Spigot.Fraud;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    private void refreshFraud() {
        this.spigotFraud = Plugin.getFraud();
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
        try {
            return spigotFraud.getVersion().getData().getName();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Format the version given in parameters, so it can be compared to another version.
     * Example: <code>parseVersion("1.5.9")</code> will return <code>1.59</code>.
     *
     * @param version the version of the plugin.
     * @return the version as a double. Example: 1.5.9 will be returned as 1.59, so it can be compared to another version and check if the version is up-to-date.
     */
    public double parseVersion(String version) {
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

    /**
     * Send a message to the high-staff of the server if a new version of Fraud has been released on spigot.
     */
    @Override
    public void run() {
        Console c = fraud.getConsole();
        String version = getLatestOnlineVersion();
        double vFormat = parseVersion(version);
        String actualVersion = fraud.getDescription().getVersion();
        double actualVBcFormat = parseVersion(fraud.actualVersionBc);
        double actualVFormat = parseVersion(actualVersion);
        if(actualVFormat < vFormat || actualVBcFormat < vFormat) {
            String url = "https://www.spigotmc.org/resources/fraud.69872/";
            if(fraud.getConfiguration().autoDownloadLatest()) {
                downloadAndInstall();
                for(Player p : Bukkit.getOnlinePlayers()) {
                    if(p.hasPermission("fraud.update")) {
                        p.sendMessage(
                                        "§6§m---------------------------------------\n" +
                                        "\n" +
                                        "§eA new Update of §6Fraud§e has been installed !\n" +
                                        "§eYou have to restart the plugin or the server to update the plugin.\n" +
                                        "§8§nLast Version:§7 " + actualVersion + "\n" +
                                        "§8§nActual Version just installed:§7 " + version + "\n" +
                                        "\n" +
                                        "§6§m---------------------------------------");
                    }
                }
                c.sm(
                        "§6§m---------------------------------------\n" +
                                "\n" +
                                "§eA new Update of §6Fraud§e has been installed !\n" +
                                "§eYou have to restart the plugin or the server to update the plugin.\n" +
                                "§8§nLast Version:§7 " + actualVersion + "\n" +
                                "§8§nActual Version just installed:§7 " + version + "\n" +
                                "\n" +
                                "§6§m---------------------------------------");
            } else {
                TextComponent tc = new TextComponent("§e§lDownload the new version.");
                tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fraud download"));
                tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§7§lCLICK HERE")));

                for(Player p : Bukkit.getOnlinePlayers()) {
                    if(p.hasPermission("fraud.update")) {
                        p.sendMessage(
                                "§6§m---------------------------------------\n" +
                                        "\n" +
                                        "§eA new Update of §6Fraud §eis now available !\n" +
                                        "§8§nActual Version:§7 " + actualVersion + "\n" +
                                        "§8§nNext Version:§7   " + version);
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
                                "§8§nNext Version:§7   " + version + "\n" +
                                "§6Go to the Spigot Page: " + url + "\n" +
                                "\n" +
                                "§6§m---------------------------------------");
            }
            fraud.actualVersionBc = version;
        }
    }

    /**
     * Download and Install the last version.
     *
     * @return true if the download and install is successful, false otherwise.
     */
    public boolean downloadAndInstall() {
        try {
            fraud.getFraudCommand().setDownloading(true);
            final String name = URLDecoder.decode(String.valueOf(Fraud.class.getProtectionDomain().getCodeSource().getLocation().toURI()).replaceFirst("file:", "") + "_new", "UTF-8");
            final String original = name.substring(0, name.length() - 4);
            FileUtils.copyURLToFile(new URL(Plugin.getFraud().getFile().getExternalUrl()), new File(name));
            Path origin = Paths.get(original);
            Files.delete(origin);
            Files.move(Paths.get(name), origin);
            fraud.getFraudCommand().setDownloading(false);
            return true;
        } catch(IOException | URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }

}
