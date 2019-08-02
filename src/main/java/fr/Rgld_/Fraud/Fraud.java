package fr.Rgld_.Fraud;

import fr.Rgld_.Fraud.Commands.FraudCommand;
import fr.Rgld_.Fraud.Events.JoinQuitEvent;
import fr.Rgld_.Fraud.Helpers.Console;
import fr.Rgld_.Fraud.Helpers.Updater.Updater;
import fr.Rgld_.Fraud.Storage.Configuration;
import fr.Rgld_.Fraud.Storage.Datas;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.text.MessageFormat;

import static org.bukkit.ChatColor.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Fraud extends JavaPlugin {


    public String actualVersionBc = "";
    private Configuration configuration;
    private Datas datas;
    private Console c;

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

    @Override
    public void onEnable() {
        this.c = new Console();
        PluginDescriptionFile pdf = this.getDescription();
        c.sm(MessageFormat.format("{0}--- {1} ---", GOLD, pdf.getName()));
        c.sm();

        try {
            new EventManager(this).register();
            c.sm(GREEN + "Events register success.");
        } catch (Exception e) {
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
        } catch (Exception e) {
            c.sm(RED + "Commands register failed: ");
            e.printStackTrace();
        }

        c.sm();

        getDataFolder().mkdirs();

        try {
            this.configuration = new Configuration(this);
            c.sm(GREEN + "Configurations File loading / creating success.");
        } catch (Throwable t) {
            c.sm(RED + "Configurations File loading / creating failed: ");
            File f = new File(getDataFolder(), "config.yml");
            if (f.exists()) {
                c.sm(RED + "Resetting of the Configuration File.");
                f.renameTo(new File(getDataFolder(), "config.yml.old"));
                try {
                    this.configuration = new Configuration(this);
                    c.sm(GREEN + "Configurations File loading / creating success(at 2nd time).");
                } catch (Throwable t2) {
                    c.sm(RED + "Configurations File loading / creating failed.");
                    t2.printStackTrace();
                }
            }
        }

        try {
            this.datas = new Datas(this);
            c.sm(GREEN + "Datas Files loading / creating success.");
            for (Player pls : Bukkit.getOnlinePlayers()) {
                datas.putPlayer(pls);
            }
        } catch (Throwable t) {
            c.sm(RED + "Datas Files loading / creating failed: ");
            t.printStackTrace();
        }

        if (configuration.checkForUpdate()) {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
                Updater updater = new Updater(this);
                Thread updaterThread = new Thread(updater);
                updaterThread.start();
            }, 0, 20 * 60 * 5);
        }

        c.sm();
        c.sm(MessageFormat.format("{0}--- {1} ---", GOLD, pdf.getName()));
    }

    private class EventManager {
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
