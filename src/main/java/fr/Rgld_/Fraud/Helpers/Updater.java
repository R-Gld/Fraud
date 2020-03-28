package fr.Rgld_.Fraud.Helpers;

import fr.Rgld_.Fraud.Fraud;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
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
            return "ERROR";
        }
    }

    @Override
    public void run() {
        JSONObject obj;
        try {
            obj = (JSONObject) new JSONParser().parse(getLatestVersion());
        } catch(ParseException parseException) {
            System.err.println("An error occur while executing the updater: " + parseException.getMessage());
            return;
        }
        String version = (String) obj.get("name");
        Long vFormat = Long.valueOf(version.replace(".", ""));
        String actualVersion = fraud.getDescription().getVersion();
        Long actualVFormat = Long.valueOf(actualVersion.replace(".", ""));
        Long actualVBcFormat = Long.valueOf(fraud.actualVersionBc);
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
                                        "§8§nActual Version just instaled:§7 " + version + "\n" +
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
                                "§8§nActual Version just instaled:§7 " + version + "\n" +
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
            FileUtils.copyURLToFile(
                    new URL("http://api.spiget.org/v2/resources/69872/download"),
                    new File(URLDecoder.decode(String.valueOf(Fraud.class.getProtectionDomain().getCodeSource().getLocation()
                            .toURI()), "UTF-8")),
                    Integer.MAX_VALUE,
                    Integer.MAX_VALUE);
            return true;
        } catch(IOException | URISyntaxException e) {
                    e.printStackTrace();
            return false;
        }
    }

}
