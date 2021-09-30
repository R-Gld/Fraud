package fr.Rgld_.Fraud.Helpers;

import fr.Rgld_.Fraud.Fraud;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class Updater implements Runnable {

    private final Fraud fraud;

    public Updater(Fraud fraud) {
        this.fraud = fraud;
    }

    /*
     DOWNLOAD: http://api.spiget.org/v2/resources/69872/download?version=latest
     VERSIONS: https://api.spiget.org/v2/resources/69872/versions/latest
    */

    private String getLatestVersion() {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL("https://api.spiget.org/v2/resources/69872/versions/latest").openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "FraudClient");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } catch(IOException e) {
            e.printStackTrace();
            return "ERROR: " + Arrays.toString(e.getStackTrace());
        }
    }

    public String getLatestVersionFormatted() {
        JSONObject obj;
        try {
            obj = (JSONObject) new JSONParser().parse(getLatestVersion());
        } catch(ParseException parseException) {
            System.err.println("An error occur while executing the updater: " + parseException.getMessage());
            return "ERROR";
        }
        return (String) obj.get("name");
    }

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

    @Override
    public void run() {
        String version = getLatestVersionFormatted();
        double vFormat = parseVersion(version);
        String actualVersion = fraud.getDescription().getVersion();
        fraud.actualVersionBc = actualVersion;
        double actualVFormat = parseVersion(actualVersion);
        double actualVBcFormat = parseVersion(fraud.actualVersionBc);
        if(actualVFormat < vFormat || actualVBcFormat < vFormat) {
            String url = "https://www.spigotmc.org/resources/fraud.69872/";
            if(fraud.getConfiguration().autoDownloadLatest()) {
                downloadAndInstall();
                for(Player p : Bukkit.getOnlinePlayers()) {
                    if(p.hasPermission("fraud.update")) {
                        p.sendMessage(
                                "§6§m---------------------------------------\n" +
                                        "\n" +
                                        "§eA new Update of §6Fraud §ehas been installed !\n" +
                                        "§eYou have to restart the plugin or the server to update the plugin.\n" +
                                        "§8§nLast Version:§7 " + actualVersion + "\n" +
                                        "§8§nActual Version just installed:§7 " + version + "\n" +
                                        "\n" +
                                        "§6§m---------------------------------------");
                    }
                }
                fraud.getConsole().sm(
                        "§6§m---------------------------------------\n" +
                                "\n" +
                                "§eA new Update of §6Fraud §ehas been installed !\n" +
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
                fraud.getConsole().sm(
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

    public boolean downloadAndInstall() {
        try {
            final String name = URLDecoder.decode(String.valueOf(Fraud.class.getProtectionDomain().getCodeSource().getLocation().toURI()).replaceFirst("file:", "") + "_new", "UTF-8");
            final String original = name.substring(0, name.length() - 4);
            FileUtils.copyURLToFile(new URL("https://cdn.spiget.org/file/spiget-resources/69872.jar"), new File(name));
            Files.delete(Paths.get(original));
            Files.move(Paths.get(name), Paths.get(original));
            return true;
        } catch(IOException | URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }

}
