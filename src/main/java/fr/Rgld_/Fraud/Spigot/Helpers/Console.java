package fr.Rgld_.Fraud.Spigot.Helpers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 * Facilitates the sending of message in console.
 */
public class Console implements fr.Rgld_.Fraud.Global.Console {

    public Console() {}

    public void sm(String message) {
        sendMessage(message);
    }

    public void sendMessage(String message) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public void sm() {
        sendMessage();
    }

    public void sendMessage() {
        sm("");
    }
}
