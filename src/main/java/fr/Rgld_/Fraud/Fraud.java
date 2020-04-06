package fr.Rgld_.Fraud;

import fr.Rgld_.Fraud.Commands.FraudCommand;
import fr.Rgld_.Fraud.Events.JoinQuitEvent;
import fr.Rgld_.Fraud.Helpers.Console;
import fr.Rgld_.Fraud.Helpers.Updater;
import fr.Rgld_.Fraud.Storage.Configuration;
import fr.Rgld_.Fraud.Storage.Datas;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.MessageFormat;

import static org.bukkit.ChatColor.*;

public class Fraud extends JavaPlugin {

    public String actualVersionBc = "";
    private Configuration configuration;
    private Datas datas;
    private Console c;
    private Updater updater;

    public Configuration getConfiguration() {
        return configuration;
    }

    public Datas getDatas() {
        return datas;
    }
    public void setDatas(Datas datas) {
        this.datas = datas;
    }

    public Console getConsole() {
        return c;
    }

    public Updater getUpdater() {
        return updater;
    }

    @Override
    public void onEnable() {
        this.c = new Console();
        PluginDescriptionFile pdf = this.getDescription();
        c.sm(MessageFormat.format("{0}--- {1} ---", GOLD, pdf.getName()));
        c.sm();

        try {
            new EventManager(this).register();
            c.sm(GREEN + "Events register success.");
        } catch(Exception e) {
            c.sm(RED + "Events register failed: ");
            e.printStackTrace();
        }

        c.sm();

        try {
            PluginCommand fraudPluginCommand = getCommand("fraud");
            FraudCommand fraudClass = new FraudCommand(this);
            fraudPluginCommand.setExecutor(fraudClass);
            fraudPluginCommand.setTabCompleter(fraudClass);
            c.sm(GREEN + "Commands register success.");
        } catch(Exception e) {
            c.sm(RED + "Commands register failed: ");
            e.printStackTrace();
        }

        c.sm();

        getDataFolder().mkdirs();

        try {
            this.configuration = new Configuration(this);
            c.sm(GREEN + "Configurations File loading / creating success.");
        } catch(Throwable t) {
            c.sm(RED + "Configurations File loading / creating failed: ");
            t.printStackTrace();
        }

        try {
            this.datas = new Datas(this);
            c.sm(GREEN + "Datas Files loading / creating success.");
            for(Player pls : Bukkit.getOnlinePlayers()) {
                datas.putPlayer(pls);
            }
        } catch(Throwable t) {
            c.sm(RED + "Datas Files loading / creating failed: ");
            t.printStackTrace();
        }

        if(configuration.checkForUpdate()) {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(
                    this, this::launchUpdater,
                    0, 20 * 60 * 5);
        }

        c.sm();
        c.sm(MessageFormat.format("{0}--- {1} ---", GOLD, pdf.getName()));
    }

    private void launchUpdater() {
        new Thread(this.updater = new Updater(this)).start();
    }

    private static class EventManager {
        final Fraud fraud;

        EventManager(Fraud fraud) {
            this.fraud = fraud;
        }

        void register() {
            a(new JoinQuitEvent(fraud));
        }

        private void a(Listener listener) {
            Bukkit.getPluginManager().registerEvents(listener, fraud);
        }
    }
}
