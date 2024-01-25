package fr.Rgld_.Fraud.Spigot.Commands;

import com.google.common.collect.Lists;
import fr.Rgld_.Fraud.Spigot.Fraud;
import fr.Rgld_.Fraud.Spigot.Helpers.*;
import fr.Rgld_.Fraud.Spigot.Storage.Data;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class FraudExecutor implements CommandExecutor, TabCompleter {

    private final Fraud fraud;
    private Data data;

    private final Updater up;
    private boolean downloading = false;

    public FraudExecutor(Fraud fraud) {
        this.fraud = fraud;
        this.na = new ArrayList<>();
        this.up = new Updater(fraud);
    }

    private final ArrayList<String> na;

    /**
     * @return an {@link ArrayList} of {@link String} in which there is all the players that don't want to receive the alerts.
     */
    public ArrayList<String> getNotAlerted() {
        return na;
    }

    /**
     * Function executed when a player, or the console execute a command of this list:
     * - /fraud
     * - /fd
     * - /alts
     * ... (all the aliases of the /fraud command)
     *
     * @param sender {@link CommandSender} the sender of the command.
     * @param command {@link Command} the command, it includes the name, the aliases, ...
     * @param label a {@link String} that is the command used by the {@link CommandSender} like fraud, fd, alts, ...
     * @param args an array of {@link String} that contain every args given by the {@link CommandSender}.
     * @return false (the result of this function is not used here).
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.data = fraud.getData();
        ExtAPI extAPI = new ExtAPI(fraud);
        switch (args.length) {
            case 0:
                break;
            case 1:
                switch (args[0].toLowerCase()) {
                    case "v":
                    case "checkupdate":
                    case "version":         return cmd_version(sender, up);
                    case "reload":          return cmd_reload(sender);
                    case "contact":         return cmd_contact(sender);
                    case "link":
                    case "links":           return cmd_links(sender);
                    case "all":             return cmd_all(sender);
                    case "dl":
                    case "download":        return cmd_download(sender, up);
                    case "reach-geoapi":    return cmd_reachGeoAPI(sender, extAPI);
                    case "alert":           return cmd_alert(sender);
                    case "gui":             return cmd_gui(sender);
                }
                break;
            case 2:
                String arg1 = args[1];
                switch (args[0].toLowerCase()) {
                    case "check":           return cmd_check(sender, arg1, data);
                    case "forgot":          return cmd_forgot(sender, arg1, data);
                    case "geoip":           return cmd_geoip(sender, arg1, extAPI);
                    case "info":            return cmd_info(sender, arg1, data);
                }
                break;
        }
        sendHelp(sender, label);
        return false;
    }

    /**
     * This method is used to open the main user interface for the player who executed the command.
     * @param sender The sender of the command.
     * @return Returns true if the command was executed successfully, otherwise false.
     */
    private boolean cmd_gui(final CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to use this command.");
            return false;
        }
        if (!sender.hasPermission("fraud.gui")) {
            sender.sendMessage(Messages.NO_PERMISSION.getMessage());
            return false;
        }
        fraud.getGuiManager().openMainGUI((Player) sender);
        return true;
    }

    /**
     * This method is used to get information about a specific player.
     * @param sender The sender of the command.
     * @param arg1 The name of the player to get information about.
     * @param data The player's data.
     * @return Returns true if the command was executed successfully, otherwise false.
     */
    private boolean cmd_info(final CommandSender sender, final String arg1, final Data data) {
        List<String> alts = data.getListByPseudo(arg1);
        if (!alts.contains(sender.getName()) && !sender.hasPermission("fraud.info")) {
            sender.sendMessage(Messages.NO_PERMISSION.getMessage());
            return false;
        }
        if (data.isFullRegistered(arg1)) {
            String ip;
            if (Bukkit.getPlayer(arg1) != null) {
                Player p = Bukkit.getPlayer(arg1);
                ip = p.getAddress().toString().split(":")[0].substring(1);
            } else {
                ip = data.getIP(arg1);
            }
            if (sender.hasPermission("fraud.info.ip")) {
                sender.sendMessage(Messages.INFO_HEADER_IP.format(arg1, ip));
            } else {
                sender.sendMessage(Messages.INFO_HEADER.format(arg1));
            }
            for (String alt : alts) {
                sender.sendMessage(
                        Messages.INFO_PLAYER.format(
                                (Utils.isConnected(alt) ? ChatColor.GREEN + alt : ChatColor.RED + alt), // {0}
                                Utils.formatDate(data.getFirstJoin(alt)), // {1}
                                Utils.formatDate(data.getLastJoin(alt))) // {2}
                                );
            }
            if (sender.hasPermission("fraud.info.ip")) {
                if (!fraud.getIpInfoManager().getIpInfoMap().containsKey(ip)) {
                    // You don't have to wait if the ip info of the ip given in a parameter has been already checked after the connection of a player.
                    sender.sendMessage(Messages.INFO_WAIT_FOR_THE_OTHER_PART.getMessage());
                    sendIPInfo(ip, sender);
                } else {
                    new Thread(() -> sendIPInfo(ip, sender)).start();
                }
            }
        } else sender.sendMessage(Messages.NOT_IN_DATAS.format(arg1));
        return true;
    }
    /**
     * This method is used to get geolocation information about a specific IP address.
     * @param sender The sender of the command.
     * @param arg1 The IP address to get geolocation information about.
     * @param extAPI The external API to use to get geolocation information.
     * @return Returns true if the command was executed successfully, otherwise false.
     */
    private boolean cmd_geoip(final CommandSender sender, final String arg1, final ExtAPI extAPI) {
        if(!sender.hasPermission("fraud.geoip")) {
            sender.sendMessage(Messages.NO_PERMISSION.getMessage());
            return false;
        }
        if (!Utils.isIPv4Address(arg1)) {
            sender.sendMessage(Messages.NOT_VALID_IP.getMessage());
            return false;
        }
        sendIPInfo(arg1, sender, false);
        return true;
    }

    /**
     * This method is used to forget a specific player.
     * @param sender The sender of the command.
     * @param arg1 The name of the player to forget.
     * @param data The player's data.
     * @return Returns true if the command was executed successfully, otherwise false.
     */
    private boolean cmd_forgot(final CommandSender sender, final String arg1, final Data data) {
        if (!sender.hasPermission("fraud.forgot")) {
            sender.sendMessage(Messages.NO_PERMISSION.getMessage());
            return false;
        }
        if (data.isRegisteredInIps(arg1)) {
            data.forgotPlayer(arg1);
            sender.sendMessage(Messages.PLAYER_FORGOTTEN.format(arg1));
        } else sender.sendMessage(Messages.NOT_IN_DATAS.format(arg1));
        return true;
    }

    /**
     * This method is used to check a specific player or IP address.
     * @param sender The sender of the command.
     * @param arg1 The name of the player or the IP address to check.
     * @param data The data of the player or the IP address.
     * @return Returns true if the command was executed successfully, otherwise false.
     */
    private boolean cmd_check(final CommandSender sender, final String arg1, final Data data) {
        if (!sender.hasPermission("fraud.check.player.one") && !arg1.equals(sender.getName())) {
            sender.sendMessage(Messages.NO_PERMISSION.getMessage());
            return false;
        }
        Player target;
        try {
            target = Bukkit.getPlayer(arg1);
        } catch (NullPointerException e) {
            target = null;
        }
        if (target != null) {
            listAlts(
                    data.getList(target),
                    sender,
                    !target.getName().equals(target.getDisplayName()) ? target.getName() + "§8(" + target.getDisplayName() + "§8)" : target.getName(),
                    false);
        } else {
            if (Utils.isIPv4Address(arg1)) {
                if (sender.hasPermission("fraud.check.ip")) {
                    InetSocketAddress add = new InetSocketAddress(arg1, 0);
                    listAlts(
                            data.getList(arg1),
                            sender,
                            (add.isUnresolved() ? add.getHostName() : add.getAddress().toString()),
                            false);
                } else {
                    sender.sendMessage(Messages.NO_PERMISSION.getMessage());
                }
            } else {
                listAlts(data.getListByPseudo(arg1), sender, arg1, false);
            }
        }
        return true;
    }

    /**
     * This method is used to enable or disable alerts for the sender of the command.
     * @param sender The sender of the command.
     * @return Returns true if the command was executed successfully, otherwise false.
     */
    private boolean cmd_alert(final CommandSender sender) {
        if (!(sender.hasPermission("fraud.alert.switch"))) {
            sender.sendMessage(Messages.NO_PERMISSION.getMessage());
            return false;
        }
        if (na.contains(sender.getName())) {
            na.remove(sender.getName());
            sender.sendMessage(Messages.ALERT_ON_.getMessage());
        } else {
            na.add(sender.getName());
            sender.sendMessage(Messages.ALERT_OFF_.getMessage());
        }
        return true;
    }

    /**
     * This method is used to check if the geolocation API is accessible.
     * @param sender The sender of the command.
     * @param extAPI The external API to use to check accessibility.
     * @return Returns true if the command was executed successfully, otherwise false.
     */
    private boolean cmd_reachGeoAPI(final CommandSender sender, final ExtAPI extAPI) {
        sender.sendMessage(ChatColor.RED + "Try to contact the rest api...");
        if(extAPI.isAPIReachable()) {
            sender.sendMessage(ChatColor.GREEN + " ⟶ Reachable ! ✅");
        } else {
            sender.sendMessage(ChatColor.RED + " ⟶ Not reachable. ❌");
            sender.sendMessage(ChatColor.RED + " ⟶ Please contact the developer to fix this problem. (" + ChatColor.GOLD + "/fd contact" + ChatColor.RED + ") !");
        }
        return true;
    }

    /**
     * This method is used to download and install the latest version of the plugin.
     * @param sender The sender of the command.
     * @param up The Updater object to use to download and install the update.
     * @return Returns true if the command was executed successfully, otherwise false.
     */
    private boolean cmd_download(final CommandSender sender, final Updater up) {
        if (!sender.hasPermission("fraud.download")) {
            sender.sendMessage(Messages.NO_PERMISSION.getMessage());
            return false;
        }
        if(downloading) {
            sender.sendMessage(ChatColor.RED + "The new version is already downloading.");
        } else {
            if (fraud.getUpdater().downloadAndInstall(new CommandSender[] { sender })) {
                sender.sendMessage(ChatColor.GOLD + "The download of the latest release of Fraud was a " + ChatColor.YELLOW + "success" + ChatColor.GOLD + ".");
                sender.sendMessage(ChatColor.GOLD + "The new release of Fraud will be effective at the next restart or reload of the plugin. You can use a plugin like PlugMan to reload just one plugin.");
            } else {
                sender.sendMessage(ChatColor.GOLD + "The download of the latest release of Fraud was a " + ChatColor.YELLOW + "failure" + ChatColor.GOLD + ".");
                sender.sendMessage(ChatColor.GRAY + "§o(You can download it manually with this url: " + ChatColor.BLUE + "§nhttps://url.rgld.fr/fraud-dl" + ChatColor.GRAY + "§o)");
            }
        }
        return true;
    }
    /**
     * This method is used to get a list of all players with alternate accounts.
     * @param sender The sender of the command.
     * @return Returns true if the command was executed successfully, otherwise false.
     */
    private boolean cmd_all(final CommandSender sender) {
        if (!sender.hasPermission("fraud.check.player.all")) {
            sender.sendMessage(Messages.NO_PERMISSION.getMessage());
            return false;
        }

        ConcurrentHashMap<String, List<String>> concernedPlayers = data.getAllPlayersWDA_String();
        if(concernedPlayers.isEmpty()) {
            sender.sendMessage(Messages.ALL_ALTS_EMPTY.getMessage());
        } else {
            sender.sendMessage(Messages.ALL_ALTS_ANNOUNCER.getMessage());
            for(String ip : concernedPlayers.keySet()) {
                List<String> players = concernedPlayers.get(ip);
                listAlts(players, sender, ip, true);
            }
        }

        /*
        List<Player> concernedPlayers = data.getAllPlayersWDA();

        if (concernedPlayers.isEmpty()) {
            sender.sendMessage(Messages.ALL_EMPTY.getMessage());
        } else {
            sender.sendMessage(Messages.ALL_ALTS_ASKED_ANNOUNCER.getMessage());
            for (Player player : concernedPlayers) {
                List<String> playerAlts = data.getList(player);
                String displayName = player.getName().equals(player.getDisplayName())
                                     ? player.getName()
                                     : player.getName() + "§8(" + player.getDisplayName() + "§8)";
                listAlts(playerAlts, sender, displayName, true);
            }
        }*/

        return true;
    }

    /**
     * Send to the {@link CommandSender} a string formatted with all the alts of the player concerned.
     *
     * @param listOfAlts a list of {@link String} that contain every alt of a player
     * @param sender {@link CommandSender} the sender of the command.
     * @param target A {@link String} that contain the name of the player that is looked up here.
     * @param all A {@link Boolean} true if it must show the string for the /fd all or other.
     */
    private void listAlts(List<String> listOfAlts, CommandSender sender, String target, boolean all) {
        if (listOfAlts == null || listOfAlts.isEmpty()) {
            sender.sendMessage(Messages.NO_ALTS.format(target));
            return;
        }
        List<String> copyOfList = Lists.newArrayList();
        copyOfList.addAll(listOfAlts);

        for (int i = 0; i < listOfAlts.size(); i++) {
            String p = listOfAlts.get(i);
            copyOfList.set(i, (Utils.isConnected(p) ? ChatColor.GREEN + p : ChatColor.RED + p));
        }
        String joined = Utils.joinList(copyOfList);
        String msg = (all ? Messages.ALL_ALTS_ASKED.format(target, joined) : Messages.ALTS_ASKED.format(target, joined));
        sender.sendMessage(msg);
        String name = copyOfList.get(0).substring(2);
        if(data.isFullRegistered(name)) {
            if (sender instanceof Player) {
                TextComponent info = new TextComponent("   §e§l➤ (i)");
                info.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Messages.INFO_HOVER.getMessage())));
                info.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fraud info " + name));
                ((Player) sender).spigot().sendMessage(info);
            } else {
                sender.sendMessage("   §e§l➤ (i) /fraud info " + name);
            }
        }
    }

    /**
     * This method is used to display the plugin's links.
     * @param sender The sender of the command.
     * @return Returns true if the command was executed successfully, otherwise false.
     */
    private boolean cmd_links(final CommandSender sender) {
        sender.sendMessage("§6 ⟶ §ePlugin Links");
        sender.sendMessage("\t§6Spigot Page: §9§n" + Links.FRAUD_SPIGOT);
        sender.sendMessage("\t§6Source-Code: §9§n" + Links.FRAUD_SOURCECODE);
        sender.sendMessage("\t§6Latest Download: §9§n" + Links.FRAUD_DOWNLOAD);
        sender.sendMessage("\t§6API Status Page: §9§n" + Links.RGLD_API_STATUS_PAGE);
        sender.sendMessage("§6 ⟶ §eServices used");
        sender.sendMessage("\t§6RIPE: §9§n" + Links.EXTERNAL_LINK_RIPE + "§r \n\t§7§o(Used to get information about ISP of an ip)");
        sender.sendMessage("\t§6MaxMind: §9§n" + Links.EXTERNAL_LINK_MAXMIND + "§r \n\t§7§o(Used to get information about the geolocation of an ip)");
        return true;
    }

    /**
     * This method is used to display the contact information of the plugin's developer.
     * @param sender The sender of the command.
     * @return Returns true if the command was executed successfully, otherwise false.
     */
    private boolean cmd_contact(final CommandSender sender) {
        sender.sendMessage("§6§lYou can contact the developer of this plugin via:");
        String discord = "§6 ⟶ Discord: §e§l§oRomain | Rgld_#5344";
        String email = "§6 ⟶ Email: §e§l§ospigot@rgld.fr";
        if(sender instanceof Player) {
            TextComponent text = new TextComponent(email);
            text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Click here to send an §6email§7.").create()));
            text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Links.SEND_MAIL.getUrl()));
            ((Player) sender).spigot().sendMessage(text);
            TextComponent text_1 = new TextComponent(discord);
            text_1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Click here to §6add me §7on §6discord§7.").create()));
            text_1.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Links.ADD_DISCORD.getUrl()));
            ((Player) sender).spigot().sendMessage(text_1);
        } else {
            sender.sendMessage(discord);
            sender.sendMessage(email);
        }
        sender.sendMessage("§6 ⟶ Twitter: §e§l§o" + Links.PERSONNAL_TWITTER);
        return true;
    }
    /**
     * This method is used to reload the plugin.
     * @param sender The sender of the command.
     * @return Returns true if the command was executed successfully, otherwise false.
     */
    private boolean cmd_reload(final CommandSender sender) {
        if (!sender.hasPermission("fraud.reload")) {
            sender.sendMessage(Messages.NO_PERMISSION.getMessage());
            return false;
        }
        try {
            fraud.setDatas(new Data(fraud));
            fraud.getConfiguration().loadConfig();
            fraud.getIpInfoManager().getIpInfoMap().clear();
            new ExtAPI(fraud).sendFraudStats();
            sender.sendMessage(Messages.RELOAD_SUCCESS.getMessage());
        } catch (Throwable t) {
            sender.sendMessage(Messages.RELOAD_FAILED.getMessage());
            t.printStackTrace();
        }
        return true;
    }

    /**
     * This method is used to display the version of the plugin.
     * @param sender The sender of the command.
     * @param up The Updater object to use to get version information.
     * @return Returns true if the command was executed successfully, otherwise false.
     */
    private boolean cmd_version(final CommandSender sender, final Updater up) {
        // Retrieve current and latest version information
        String currentVersion = fraud.getDescription().getVersion();
        double numericCurrentVersion = up.parseVersion(currentVersion);

        String latestVersion = up.getLatestOnlineVersion();
        double numericLatestVersion = up.parseVersion(latestVersion);

        // Send version information to the sender
        sender.sendMessage(ChatColor.GRAY + "Installed Fraud version: v" + currentVersion);
        sender.sendMessage(ChatColor.GRAY + "Latest Fraud version available: v" + latestVersion);

        // Compare versions and inform the sender accordingly
        if (numericLatestVersion > numericCurrentVersion) {
            sender.sendMessage("§c§l❌ §cOutdated\n§c§lYou should download the new version, check /fraud link");
        } else if (numericLatestVersion == numericCurrentVersion) {
            sender.sendMessage("§a§l✔ §aUp-to-date");
        } else {
            sender.sendMessage("§6Ur a precursor 😉");
        }

        return true;
    }


    /**
     * Build a sentence from the arguments given in parameters.
     * @param args an array of {@link String} that contain every args given by the {@link CommandSender}.
     * @return a {@link String} that contain every args given in parameters.
     */
    private String buildSentence(String[] args) {
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            builder.append(args[i]);
            builder.append(' ');
        }
        return builder.toString();
    }

    /**
     * Build and send the information about the ip given in parameters to the {@link CommandSender}.
     * @param ip the ip seen
     * @param sender the sender who receives the information
     */
    private void sendIPInfo(String ip, CommandSender sender) {
        sendIPInfo(ip, sender, true);
    }

    /**
     * Build and send the information about the ip given in parameters to the {@link CommandSender}.
     *
     * @param ip the ip seen
     * @param sender the sender who receives the information
     * @param conformConfig is the information send to the sender had to be conformed to the configuration (geolocation part) ?
     */
    private void sendIPInfo(String ip, CommandSender sender, boolean conformConfig) {
        IPInfo ipInfo = retrieveIPInfo(ip, conformConfig);
        sender.sendMessage(Messages.INFO_IP_INFORMATION.format(ip));

        boolean informationSent = sendGeoInformation(ipInfo, sender, conformConfig);
        informationSent |= sendCoordinateInformation(ipInfo, sender, conformConfig);

        if (!informationSent) {
            sender.sendMessage(Messages.INFO_NO_INFORMATION.getMessage());
        }
    }

    private IPInfo retrieveIPInfo(String ip, boolean conformConfig) {
        return conformConfig
            ? fraud.getIpInfoManager().getIPInfoConformConfig(ip)
            : fraud.getIpInfoManager().getIpInfo(ip, fraud.getConfiguration().isGeoIPAPIActivated());
    }

    private boolean sendGeoInformation(IPInfo ipInfo, CommandSender sender, boolean geoIPOrCC) {
        boolean somethingSent = false;

        if (isObjectNotNull(ipInfo.getContinent()) && geoIPOrCC) {
            sender.sendMessage(Messages.INFO_CONTINENT.format(ipInfo.getContinent()));
            somethingSent = true;
        }

        if (isObjectNotNull(ipInfo.getCountryCode()) && isObjectNotNull(ipInfo.getCountryName())) {
            sender.sendMessage(Messages.INFO_COUNTRY.format(ipInfo.getCountryName(), ipInfo.getCountryCode()));
            somethingSent = true;
        }

        if (isObjectNotNull(ipInfo.getSubDivision()) && geoIPOrCC) {
            sender.sendMessage(Messages.INFO_SUB_DIVISION.format(ipInfo.getSubDivision()));
            somethingSent = true;
        }

        if (isObjectNotNull(ipInfo.getCity()) && geoIPOrCC) {
            sender.sendMessage(Messages.INFO_CITY.format(ipInfo.getCity()));
            somethingSent = true;
        }

        if (isObjectNotNull(ipInfo.getPostalCode()) && geoIPOrCC) {
            sender.sendMessage(Messages.INFO_POSTAL_CODE.format(ipInfo.getPostalCode()));
            somethingSent = true;
        }

        return somethingSent;
    }


    private boolean sendCoordinateInformation(IPInfo ipInfo, CommandSender sender, boolean geoIPOrCC) {
        if (isObjectNotNull(ipInfo.getLatitude()) && isObjectNotNull(ipInfo.getLongitude()) && geoIPOrCC) {
            String lat = ipInfo.getLatitude();
            String lon = ipInfo.getLongitude();
            String urlMaps = Utils.generateURLGmap(lat, lon);
            if (sender instanceof Player) {
                TextComponent text = new TextComponent(Messages.INFO_COORDINATES.format(lat, lon));
                text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Messages.INFO_COORDINATES_CLICK.getMessage()).create()));
                text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, urlMaps));
                ((Player) sender).spigot().sendMessage(text);
            } else {
                sender.sendMessage(Messages.INFO_COORDINATES.format(lat, lon) + " (" + urlMaps + ")");
            }
            return true;
        }
        return false;
    }

    private boolean isObjectNotNull(Object obj) {
        return obj != null && (!(obj instanceof String) || !((String) obj).trim().isEmpty());
    }

    private String buildDesc(IPInfo ipInfo) {
        return String.join("/", ipInfo.getDesc());
    }

    /**
     * Let the player press the key <code>TAB</code>, and the program will auto-complete the command.
     *
     * @param sender the {@link CommandSender}.
     * @param command the {@link Command}.
     * @param alias not used here.
     * @param args An array of {@link String}, that include all the args of the command.
     * @return the {@link String} list of auto-complete text that will be suggested to the player.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // If the user hasn't typed anything yet, show all possible commands.
            if (args[0].isEmpty()) {
                completions.addAll(Arrays.asList("check", "contact", "dl", "download", "forgot", "geoip",
                                                 "info", "link", "links", "reload", "v", "version", "all", "reach-geoapi", "alert"));
            }
            // If the user has typed a partial command, show all possible matching commands.
            else {
                String partialCommand = args[0].toLowerCase();
                for (String commandName : new String[] { "check", "contact", "dl", "download", "forgot", "geoip",
                        "info", "link", "links", "reload", "v", "version", "all", "reach-geoapi", "alert" }) {
                    if (commandName.startsWith(partialCommand)) {
                        completions.add(commandName);
                    }
                }
            }
        } else if (args.length == 2) {
            String commandName = args[0].toLowerCase();
            if (commandName.equals("check") || commandName.equals("forgot") || commandName.equals("info") || commandName.equals("geoip")) {
                // If the user has typed a partial argument, show all possible matching arguments.
                String partialArgument = args[1].toLowerCase();
                if (!partialArgument.isEmpty()) {
                    completions.addAll(getMatchingArguments(partialArgument));
                }
            }
        }

        return completions;
    }

    /**
     * Returns a list of all possible arguments that match the given partial argument.
     */
    private List<String> getMatchingArguments(String partialArgument) {
        List<String> matchingArguments = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            String playerName = player.getName();
            if (playerName.toLowerCase().startsWith(partialArgument)) {
                matchingArguments.add(playerName);
            }
        }
        return matchingArguments;
    }


    /**
     * Just send the help of the plugin.
     * @param sender the {@link CommandSender}
     * @param label the alias of the command used here. (like fraud, fd, alts, ...).
     */
    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(ChatColor.GOLD + "----==={ " + ChatColor.YELLOW + fraud.getDescription().getName() + ChatColor.GOLD + " }===----");
        sender.sendMessage("");
        sender.sendMessage(Messages.HELP_COMMAND_ALERT.format(label));          // /fd alert
        sender.sendMessage(Messages.HELP_COMMAND_ALL.format(label));            // /fd all
        sender.sendMessage(Messages.HELP_COMMAND_CHECK.format(label));          // /fd check <player>
        sender.sendMessage(Messages.HELP_COMMAND_CONTACT.format(label));        // /fd contact
        sender.sendMessage(Messages.HELP_COMMAND_DOWNLOAD.format(label));       // /fd download
        sender.sendMessage(Messages.HELP_COMMAND_FORGOT.format(label));         // /fd forgot <player>
        sender.sendMessage(Messages.HELP_COMMAND_GEOIP.format(label));          // /fd info <player>
        sender.sendMessage(Messages.HELP_COMMAND_GUI.format(label));            // /fd gui
        sender.sendMessage(Messages.HELP_COMMAND_INFO.format(label));           // /fd info <player>
        sender.sendMessage(Messages.HELP_COMMAND_LINK.format(label));           // /fd link
        sender.sendMessage(Messages.HELP_COMMAND_RELOAD.format(label));         // /fd reload
        sender.sendMessage(Messages.HELP_COMMAND_STATS.format(label));          // /fd stats
        sender.sendMessage(Messages.HELP_COMMAND_VERSION.format(label));        // /fd version
        sender.sendMessage("");
        sender.sendMessage("§7§oBy Rgld_");
        sender.sendMessage(ChatColor.GOLD + "----==={ " + ChatColor.YELLOW + fraud.getDescription().getName() + ChatColor.GOLD + " }===----");
    }

    public void setDownloading(boolean downloading) {
        this.downloading = downloading;
    }
}
