package fr.Rgld_.Fraud;

import fr.Rgld_.Fraud.Events.JoinQuitEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import static org.bukkit.ChatColor.*;

public class Fraud extends JavaPlugin {

    private static Fraud INSTANCE;
    public static Fraud getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        INSTANCE = this;

        PluginDescriptionFile pdf = this.getDescription();
        Console c = new Console();
        c.sm(GOLD + "--- " + pdf.getName() + " ---");
        c.sm("");

        try {
            new EventManager(this).register();
            c.sm(GREEN + "Events register success.");
        } catch(Exception t) {
            c.sm(RED + "Events register failed.");
        }
        c.sendMessage();

    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    private class EventManager {
        void register(){
            a(new JoinQuitEvent());
        }
        Fraud fraud;
        EventManager(Fraud fraud) {
            this.fraud = fraud;
        }
        private void a(Listener listener){
            Bukkit.getPluginManager().registerEvents(listener, fraud);
        }
    }
}
