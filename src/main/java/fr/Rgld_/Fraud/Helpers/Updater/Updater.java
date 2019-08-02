package fr.Rgld_.Fraud.Helpers.Updater;

import fr.Rgld_.Fraud.Fraud;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Updater implements Runnable {

    private final Fraud fraud;

    public Updater(Fraud fraud) {
        this.fraud = fraud;
    }

    /*
     DOWNLOAD: https://api.spiget.org/v2/resources/69872/versions/latest/download
     VERSIONS: https://api.spiget.org/v2/resources/69872/versions/latest
    */

    private String getLatestVersion() {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL("https://api.spiget.org/v2/resources/69872/versions/latest").openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "FraudClient");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    @Override
    public void run() {
        JSONParser parser = new JSONParser();
        JSONObject obj;
        try {
            obj = (JSONObject) parser.parse(getLatestVersion());
        } catch (ParseException ignored) {
            return;
        }
        String version = (String) obj.get("name");
        String actualVersion = fraud.getDescription().getVersion();
        if (!actualVersion.equals(version) && !fraud.actualVersionBc.equals(version)) {

            String url = "https://www.spigotmc.org/resources/fraud.69872/";

            TextComponent tc = new TextComponent("§lGo to the Spigot Page");
            tc.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§7§lCLICK HERE")));

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("fraud.update")) {
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
                            "§6Go to the Spigot Page: " + url +
                            "\n" +
                            "§6§m---------------------------------------");
            fraud.actualVersionBc = version;
        }
    }
}
