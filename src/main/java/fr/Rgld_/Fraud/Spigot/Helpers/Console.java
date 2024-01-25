package fr.Rgld_.Fraud.Spigot.Helpers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 * Facilitates the sending of message in console.
 */
public class Console {

    public void sm(String message) {
        sendMessage(message);
    }
    public void sm() {
        sm("");
    }
    public void sendMessage() {
        sm();
    }

    public void sendMessage(String message) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}
