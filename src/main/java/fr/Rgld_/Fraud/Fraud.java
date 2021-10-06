package fr.Rgld_.Fraud;

import fr.Rgld_.Fraud.Commands.FraudCommand;
import fr.Rgld_.Fraud.Events.JoinQuitEvent;
import fr.Rgld_.Fraud.Helpers.Console;
import fr.Rgld_.Fraud.Helpers.IPInfoManager;
import fr.Rgld_.Fraud.Helpers.Messages;
import fr.Rgld_.Fraud.Helpers.Updater;
import fr.Rgld_.Fraud.Storage.Configuration;
import fr.Rgld_.Fraud.Storage.Datas;
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

import static org.bukkit.ChatColor.*;

/**
 * Represent the main class of the plugin <a href="https://www.spigotmc.org/resources/fraud-alts-finder.69872/" target="_blank">Fraud</a> on spigot.
 */
public class Fraud extends JavaPlugin {

    public String actualVersionBc = "";
    private Updater updater;
    private Configuration configuration;
    private Datas datas;
    private Console c;
    private FraudCommand fraudCommand;
    private IPInfoManager ipInfoManager;


    /**
     * Equivalent of a <code>public static main(String[] args)</code> in a Main class. It's the function that initialise the plugin.
     */
    @Override
    public void onEnable() {
        this.c = new Console();
        this.ipInfoManager = new IPInfoManager();
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
            Metrics metrics = new Metrics(this, pluginId);
            metrics.addCustomChart(new SimplePie("alts_limits", () -> String.valueOf(getConfiguration().getDoubleAccountLimit())));
            metrics.addCustomChart(new SimplePie("kick_when_alt_detected", () -> String.valueOf(getConfiguration().isKickEnabled())));
        } catch(Throwable t) {
            c.sm(RED + "Metrics connection failed.");
            t.printStackTrace();
        }
        Bukkit.getScheduler().scheduleSyncRepeatingTask(
                this, this::askReview,
                0, 5 * 60 * 20);

        c.sm();
        c.sm(MessageFormat.format("{0}--- {1} ---", GOLD, pdf.getName()));
    }

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
        c.sm(MessageFormat.format("{0}--- {1} ---", GOLD, pdf.getName()));
        c.sm();

        c.sm(RED + "Disabling " + pdf.getName() + "...");

        c.sm();
        c.sm(MessageFormat.format("{0}--- {1} ---", GOLD, pdf.getName()));
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

    /**
     * Class that manage the events on the plugin.
     */
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
