package fr.Rgld_.Fraud;

import fr.Rgld_.Fraud.Commands.FraudCommand;
import fr.Rgld_.Fraud.Events.JoinQuitEvent;
import fr.Rgld_.Fraud.Helpers.Console;
import fr.Rgld_.Fraud.Helpers.Updater;
import fr.Rgld_.Fraud.Storage.Configuration;
import fr.Rgld_.Fraud.Storage.Datas;
import org.bstats.bukkit.Metrics;
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
    private Updater updater;
    private Configuration configuration;
    private Datas datas;
    private Console c;
    private FraudCommand fraudCommand;

    public Updater getUpdater() {
        return updater;
    }

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

    public FraudCommand getFraudCommand() {
        return fraudCommand;
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
            fraudCommand = new FraudCommand(this);
            fraudPluginCommand.setExecutor(fraudCommand);
            fraudPluginCommand.setTabCompleter(fraudCommand);
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

        try {
            this.updater = new Updater(this);
            c.sm(GREEN + "Updater launched with success.");
            if(configuration.checkForUpdate()) {
                Bukkit.getScheduler().scheduleSyncRepeatingTask(
                        this, this::launchUpdater,
                        0, 5 * 60 * 5);
            }
        } catch(Throwable t) {
            c.sm(RED + "Updated failed to launch.");
            t.printStackTrace();
        }

        try {
            int pluginId = 12676;
            new Metrics(this, pluginId);
        } catch(Throwable t) {
            c.sm(RED + "Metrics connection failed.");
            t.printStackTrace();
        }

        c.sm();
        c.sm(MessageFormat.format("{0}--- {1} ---", GOLD, pdf.getName()));
    }

    @Override
    public void onDisable() {
        PluginDescriptionFile pdf = this.getDescription();
        c.sm(MessageFormat.format("{0}--- {1} ---", GOLD, pdf.getName()));
        c.sm();

        c.sm(RED + "Disabling " + pdf.getName() + "...");

        c.sm();
        c.sm(MessageFormat.format("{0}--- {1} ---", GOLD, pdf.getName()));
    }

    private void launchUpdater() {
        new Thread(new Updater(this)).start();
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
