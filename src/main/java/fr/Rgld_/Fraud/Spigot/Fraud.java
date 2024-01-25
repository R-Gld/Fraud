package fr.Rgld_.Fraud.Spigot;

import fr.Rgld_.Fraud.Spigot.Commands.FraudExecutor;
import fr.Rgld_.Fraud.Spigot.Events.JoinQuitEvent;
import fr.Rgld_.Fraud.Spigot.Helpers.*;
import fr.Rgld_.Fraud.Spigot.Storage.Configuration;
import fr.Rgld_.Fraud.Spigot.Storage.Data;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.MessageFormat;

import static fr.Rgld_.Fraud.Spigot.Storage.Configuration.DatabaseSection.Type.MYSQL;
import static fr.Rgld_.Fraud.Spigot.Storage.Configuration.DatabaseSection.Type.SQLITE;
import static org.bukkit.ChatColor.*;

/**
 * Represent the main class of the plugin <a href="https://www.spigotmc.org/resources/fraud-alts-finder.69872/" target="_blank">Fraud</a> on spigot.
 */
public class Fraud extends JavaPlugin {

    public String actualVersionBc = "";
    private Updater updater;
    private Configuration configuration;
    private Data data;
    private Console console;
    private FraudExecutor fraudExecutor;
    private IPInfoManager ipInfoManager;
    private GUIManager guiManager;

    /**
     * Equivalent of a <code>public static main(String[] args)</code> in a Main class. It's the function that initialise the plugin.
     */
    @Override
    public void onEnable() {
        this.console = new Console();
        PluginDescriptionFile pdf = this.getDescription();
        console.sm(MessageFormat.format("{0}--- {1} ---", GOLD, pdf.getName()));
        console.sm();


        try {
            console.sm(GREEN + "Configurations File loading / creating success.");
            this.configuration = new Configuration(this);
        } catch(Throwable t) {
            console.sm(RED + "Configurations File loading / creating failed: ");
            t.printStackTrace();
        }

        this.ipInfoManager = new IPInfoManager(this, new ExtAPI(this));
        this.guiManager = new GUIManager(this);

        try {
            console.sm(GREEN + "Events register success.");
            new EventManager(this).register();
        } catch(Exception e) {
            console.sm(RED + "Events register failed: ");
            e.printStackTrace();
        }


        try {
            console.sm(GREEN + "Commands register success.");
            PluginCommand fraudPluginCommand = getCommand("fraud");
            fraudExecutor = new FraudExecutor(this);
            fraudPluginCommand.setExecutor(fraudExecutor);
            fraudPluginCommand.setTabCompleter(fraudExecutor);
        } catch(Exception e) {
            console.sm(RED + "Commands register failed: ");
            e.printStackTrace();
        }

        try {
            console.sm(GREEN + "Stats are on.");
            new Stats(this);
        } catch(Throwable t) {
            console.sm(RED + "Stats launch failed.");
            t.printStackTrace();
        }

        try {
            console.sm(GREEN + "Datas Files loading / creating success.");
            Configuration.DatabaseSection.Type dataStorageType = configuration.getDatabase().getType();
            this.data = new Data(this);

            if(dataStorageType == MYSQL) {
                console.sm(GOLD + "Checking database connection...");
                long ping = data.ping();
                if(ping != -1) {
                    console.sm(GREEN + "Connection success in " + ping + "ms.");
                } else {
                    console.sm(RED + "Connection failed.");
                    console.sm(RED + "Shutting down the plugin.");
                    Bukkit.getPluginManager().disablePlugin(this);
                }
                console.sm(GREEN + "Mysql connection:");
                console.sm(GREEN + "\t- Host: " + configuration.getDatabase().getHost());
                console.sm(GREEN + "\t- Port: " + configuration.getDatabase().getPort());
                console.sm(GREEN + "\t- User: " + configuration.getDatabase().getUser());
                console.sm(GREEN + "\t- Database: " + configuration.getDatabase().getDatabaseName());
                console.sm(GRAY + "\t - URL generated: " + configuration.getDatabase().generateURL());
            } else if(dataStorageType == SQLITE) {
                console.sm(GREEN + "SQLite connection:");
                console.sm(GREEN + "\t- Path: '" + data.getFile().getAbsolutePath() + "'");
            } else {
                console.sm(RED + "No recognized database type.");
                console.sm(RED + "Shutting down the plugin.");
                Bukkit.getPluginManager().disablePlugin(this);
            }
        } catch(Throwable t) {
            console.sm(RED + "Datas Files loading / creating failed: ");
            t.printStackTrace();
        }

        try {
            console.sm(GREEN + "Updater launched with success.");
            this.updater = new Updater(this);
            if(configuration.checkForUpdate()) {
                Bukkit.getScheduler().scheduleSyncRepeatingTask(
                        this, this::launchUpdater,
                        0, 5 * 60 * 5);
            }
        } catch(Throwable t) {
            console.sm(RED + "Updated failed to launch.");
            t.printStackTrace();
        }

        try {
            final int pluginId = 12676;
            Metrics metrics = new Metrics(this, pluginId);
            metrics.addCustomChart(new SimplePie("alts_limits", () -> String.valueOf(getConfiguration().getDoubleAccountLimit())));
            metrics.addCustomChart(new SimplePie("kick_when_alt_detected", () -> String.valueOf(getConfiguration().isKickEnabled())));
        } catch(Throwable t) {
            console.sm(RED + "Metrics connection failed.");
            t.printStackTrace();
        }
        Bukkit.getScheduler().scheduleSyncRepeatingTask(
                this, this::askReview,
                0, 5 * 60 * 200);

        console.sm();
        console.sm(MessageFormat.format("{0}--- {1} ---", GOLD, pdf.getName()));
    }

    /**
     * Ask the High-staff of the server to leave a review on <a href="https://www.spigotmc.org/resources/fraud-alts-finder.69872/" target="_blank">the spigot page</a> of the plugin.
     */
    private void askReview() {
        if(configuration.askForReviews()) {
            String message = Messages.PREFIX.getMessage() + "&6&lDo not hesitate to give your opinion on the plugin directly from its spigot page! (/fraud link)";
            TextComponent info = new TextComponent(ChatColor.translateAlternateColorCodes('&', message));
            info.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("This message is clickable!")));
            info.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fraud link"));

            System.out.println(message);
            for(Player p : Bukkit.getOnlinePlayers())
                if(p.isOp())
                    p.spigot().sendMessage(info);
        }
    }

    /**
     * Last function executed before the plugin stop.
     */
    @Override
    public void onDisable() {
        PluginDescriptionFile pdf = this.getDescription();
        console.sm(MessageFormat.format("{0}--- {1} ---", GOLD, pdf.getName()));
        console.sm();

        console.sm(RED + "Disabling " + pdf.getName() + "...");

        data.getDataSource().close();

        console.sm();
        console.sm(MessageFormat.format("{0}--- {1} ---", GOLD, pdf.getName()));
    }

    private void launchUpdater() {
        new Thread(new Updater(this)).start();
    }

    public IPInfoManager getIpInfoManager() {
        return ipInfoManager;
    }

    public Updater getUpdater() {
        return updater;
    }
    public Configuration getConfiguration() {
        return configuration;
    }
    public Data getData() {
        return data;
    }
    public void setDatas(Data Data) {
        this.data = Data;
    }
    public Console getConsole() {
        return console;
    }
    public FraudExecutor getFraudCommand() {
        return fraudExecutor;
    }
    public GUIManager getGuiManager() {
        return guiManager;
    }

    /**
     * Class that manage the events on the plugin.
     */
    private static class EventManager {

        final Fraud fraud;
        EventManager(Fraud fraud) {
            this.fraud = fraud;
        }

        /**
         * Register all the events of the plugin.
         */
        void register() {
            a(new JoinQuitEvent(fraud));
            a(fraud.getGuiManager());
        }
        private void a(Listener listener) {
            Bukkit.getPluginManager().registerEvents(listener, fraud);
        }
    }
}
