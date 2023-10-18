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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
        if(args.length > 1 && (args[0].equalsIgnoreCase("askhelp") || args[0].equalsIgnoreCase("ask-help"))) {
            String sentence = buildSentence(args);
            extAPI.askHelp(sentence);
            return false;
        }
        switch (args.length) {
            case 0:
                break;
            case 1:
                switch (args[0].toLowerCase()) {
                    case "askhelp":
                    case "ask-help": return cmd_askHelp(sender, args[0]);
                    case "v":
                    case "checkupdate":
                    case "version": return cmd_version(sender, up);
                    case "reload": return cmd_reload(sender);
                    case "contact": return cmd_contact(sender);
                    case "link":
                    case "links": return cmd_links(sender);
                    case "all": return cmd_all(sender);
                    case "dl":
                    case "download": return cmd_download(sender, up);
                    case "reach-geoapi": return cmd_reachGeoAPI(sender, extAPI);
                    case "alert": return cmd_alert(sender);
                }
                break;
            case 2:
                String arg1 = args[1];
                switch (args[0].toLowerCase()) {
                    case "check": return cmd_check(sender, arg1, data);
                    case "forgot": return cmd_forgot(sender, arg1, data);
                    case "geoip": return cmd_geoip(sender, arg1, extAPI);
                    case "info": return cmd_info(sender, arg1, data);
                }
                break;
        }
        sendHelp(sender, label);
        return false;
    }

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
        return false;
    }
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
        return false;
    }
    private boolean cmd_forgot(final CommandSender sender, final String arg1, final Data data) {
        if (!sender.hasPermission("fraud.forgot")) {
            sender.sendMessage(Messages.NO_PERMISSION.getMessage());
            return false;
        }
        if (data.isRegisteredInIps(arg1)) {
            data.forgotPlayer(arg1);
            sender.sendMessage(Messages.PLAYER_FORGOTTEN.format(arg1));
        } else sender.sendMessage(Messages.NOT_IN_DATAS.format(arg1));
        return false;
    }
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
        return false;
    }
    private boolean cmd_alert(final CommandSender sender) {
        if (!(sender.hasPermission("fraud.alert.switch"))) {
            sender.sendMessage(Messages.NO_PERMISSION.getMessage());
            return false;
        }
        if (na.contains(sender.getName())) {
            na.remove(sender.getName());
            sender.sendMessage(Messages.ALERT_ON.getMessage());
        } else {
            na.add(sender.getName());
            sender.sendMessage(Messages.ALERT_OFF.getMessage());
        }
        return false;
    }
    private boolean cmd_reachGeoAPI(final CommandSender sender, final ExtAPI extAPI) {
        sender.sendMessage(ChatColor.RED + "Try to contact the rest api...");
        if(extAPI.isAPIReachable()) {
            sender.sendMessage(ChatColor.GREEN + " ⟶ Reachable ! ✅");
        } else {
            sender.sendMessage(ChatColor.RED + " ⟶ Not reachable. ❌");
            sender.sendMessage(ChatColor.RED + " ⟶ Please contact the developer to fix this problem. (" + ChatColor.GOLD + "/fd contact" + ChatColor.RED + ") !");
        }
        return false;
    }
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
        return false;
    }
    private boolean cmd_all(final CommandSender sender) {
        if (!sender.hasPermission("fraud.check.player.all")) {
            sender.sendMessage(Messages.NO_PERMISSION.getMessage());
            return false;
        }
        List<String> everChecked = Lists.newArrayList();
        List<Player> concernedPlayers = Lists.newArrayList();
        for (Player pls : Bukkit.getOnlinePlayers()) {
            if (everChecked.contains(pls.getName())) continue;
            List<String> plsAlts = data.getList(pls);
            everChecked.addAll(plsAlts);
            if (plsAlts.size() >= 2 && Utils.cantGetAnAlt(plsAlts) || (plsAlts.size() >= (fraud.getConfiguration().getDoubleAccountLimit() + 1))) {
                concernedPlayers.add(pls);
            }
        }
        if (concernedPlayers.isEmpty()) {
            sender.sendMessage(Messages.ALL_EMPTY.getMessage());
        } else {
            sender.sendMessage(Messages.ALL_ALTS_ASKED_ANNOUNCER.getMessage());
            for (Player pls : concernedPlayers) {
                List<String> plsAlts = data.getList(pls);
                listAlts(plsAlts, sender, !pls.getName().equals(pls.getDisplayName()) ? pls.getName() + "§8(" + pls.getDisplayName() + "§8)" : pls.getName(), true);
            }
        }
        return false;
    }
    private boolean cmd_links(final CommandSender sender) {
        sender.sendMessage("§6 ⟶ §ePlugin Links");
        sender.sendMessage("\t§6Spigot Resource: §9§n" + Links.FRAUD_SPIGOT);
        sender.sendMessage("\t§6Source-Code: §9§n" + Links.FRAUD_SOURCECODE);
        sender.sendMessage("\t§6Download Latest: §9§n" + Links.FRAUD_DOWNLOAD);
        sender.sendMessage("§6 ⟶ §eServices used");
        sender.sendMessage("\t§6RIPE: §9§nhttps://www.ripe.net§r \n\t§7§o(Used to get information about ISP of an ip)");
        sender.sendMessage("\t§6MaxMind: §9§nhttps://www.maxmind.com/en/geoip2-services-and-databases§r \n\t§7§o(Used to get information about the geolocation of an ip)");
        return false;
    }
    private boolean cmd_contact(final CommandSender sender) {
        sender.sendMessage("§6§lYou can contact the developer of this plugin via:");
        String discord = "§6 ⟶ Discord: §e§l§oRomain | Rgld_#5344";
        String email = "§6 ⟶ Email: §e§l§ospigot@rgld.fr";
        if(sender instanceof Player) {
            TextComponent text = new TextComponent(email);
            text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Click here to send an §6email§7.").create()));
            text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "mailto://spigot@rgld.fr"));
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
        return false;
    }
    private boolean cmd_reload(final CommandSender sender) {
        if (!sender.hasPermission("fraud.reload")) {
            sender.sendMessage(Messages.NO_PERMISSION.getMessage());
            return false;
        }
        try {
            fraud.setDatas(new Data(fraud));
            fraud.getConfiguration().loadConfig();
            fraud.getIpInfoManager().getIpInfoMap().clear();
            sender.sendMessage(Messages.RELOAD_SUCCESS.getMessage());
        } catch (Throwable t) {
            sender.sendMessage(Messages.RELOAD_FAILED.getMessage());
            t.printStackTrace();
        }
        return false;
    }
    private boolean cmd_version(final CommandSender sender, final Updater up) {
        String version = fraud.getDescription().getVersion();
        double dVersion = up.parseVersion(version);
        String latest = up.getLatestOnlineVersion();
        double dLatest = up.parseVersion(latest);
        boolean isUpToDate = up.isUpToDate();
        sender.sendMessage(ChatColor.GRAY + "Installed Fraud version: v" + version);
        sender.sendMessage(ChatColor.GRAY + "Latest Fraud version available: v" + latest);
        sender.sendMessage((dLatest > dVersion ? "§c§l❌ §cOutdated\n§c§lYou should download the new version, check /fraud link" : (dLatest == dVersion ? "§a§l✔ §aUp-to-date" : "§6Ur a precursor 😉")));
        return false;
    }
    private boolean cmd_askHelp(final CommandSender sender, final String arg) {
        sender.sendMessage(ChatColor.RED + "You should add a sentence to this command. Example: /fd " + arg + " I can't detect alts.");
        return false;
    }

    /**
     * Build a sentence from the arguments given in parameters.
     * @param args
     * @return
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
        boolean isGeoIPApiActivated = fraud.getConfiguration().isGeoIPAPIActivated();
        boolean somethingSent = false;
        boolean geoIPOrCC = isGeoIPApiActivated || conformConfig;
        IPInfo ii;

        if(conformConfig) {
            ii = fraud.getIpInfoManager().getIPInfoConformConfig(ip);
        } else {
            ii = fraud.getIpInfoManager().getIpInfo(ip, isGeoIPApiActivated);
        }

        sender.sendMessage(Messages.INFO_IP_INFORMATION.format(ip));

        if(!isObjNull(ii.getContinent()) && geoIPOrCC) {
            String continent = ii.getContinent();
            sender.sendMessage(Messages.INFO_IP_continent.format(continent));
            somethingSent = true;
        }

        if(!isObjNull(ii.getCountryCode()) && !isObjNull(ii.getCountryName())) {
            String countryName = ii.getCountryName();
            String countryCode = ii.getCountryCode();
            sender.sendMessage(Messages.INFO_IP_country.format(countryName, countryCode));
            somethingSent = true;
        }

        if(!isObjNull(ii.getSubDivision()) && geoIPOrCC) {
            String subDiv = ii.getSubDivision();
            sender.sendMessage(Messages.INFO_IP_sub_division.format(subDiv));
            somethingSent = true;
        }

        if(!isObjNull(ii.getCity()) && geoIPOrCC) {
            String city = ii.getCity();
            sender.sendMessage(Messages.INFO_IP_city.format(city));
            somethingSent = true;
        }

        if(!isObjNull(ii.getPostalCode()) && geoIPOrCC) {
            String postalCode = ii.getPostalCode();
            sender.sendMessage(Messages.INFO_IP_postal_code.format(postalCode));
            somethingSent = true;
        }

        if(!isObjNull(ii.getLatitude()) && !isObjNull(ii.getLongitude()) && geoIPOrCC) {
            String lat = ii.getLatitude();
            String lon = ii.getLongitude();
            String urlMaps = MessageFormat.format("https://www.google.fr/maps/search/{0},{1}", lat, lon);
            if(sender instanceof Player) {
                TextComponent text = new TextComponent(Messages.INFO_IP_coordinates.format(lat, lon));
                text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Messages.INFO_IP_coordinates_click.getMessage()).create()));
                text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, urlMaps));
                ((Player) sender).spigot().sendMessage(text);
            } else
                sender.sendMessage(Messages.INFO_IP_coordinates.format(lat, lon) + "(" + urlMaps + ")");
            somethingSent = true;
        }

        String netname = ii.getNetname();
        String desc = buildDesc(ii);
        if(!(isObjNull(netname) && isObjNull(desc))) {
            String other_information =
                    (!isObjNull(netname) ? netname + "/" : "") +
                    desc;
            if(!isObjNull(other_information.replace("/", ""))) {
                sender.sendMessage(Messages.INFO_IP_others.format(other_information));
                somethingSent = true;
            }
        }

        if(!somethingSent) {
            sender.sendMessage(Messages.INFO_IP_no_information.getMessage());
        }
    }

    /**
     * Return true if the object is null or empty. False otherwise.
     * @param obj
     * @return true if the object is null or empty. False otherwise.
     */
    private boolean isObjNull(Object obj) {
        return obj == null || (obj instanceof String && ((String) obj).trim().isEmpty() || "null".equals(obj));
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
        sender.sendMessage(MessageFormat.format((all ? Messages.ALL_ALTS_ASKED.getMessage() : Messages.ALTS_ASKED.getMessage()), target, joined));
        String name = copyOfList.get(0).substring(2);
        if (sender instanceof Player && data.isFullRegistered(name)) {
            TextComponent info = new TextComponent("   §e§l➤ (i)");
            info.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Messages.INFO_HOVER.getMessage())));
            info.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fraud info " + name));
            ((Player) sender).spigot().sendMessage(info);
        }
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
                completions.addAll(Arrays.asList("askhelp", "check", "contact", "dl", "download", "forgot", "geoip",
                                                 "info", "link", "links", "reload", "v", "version", "all", "reach-geoapi", "alert"));
            }
            // If the user has typed a partial command, show all possible matching commands.
            else {
                String partialCommand = args[0].toLowerCase();
                for (String commandName : new String[] { "askhelp", "check", "contact", "dl", "download", "forgot", "geoip",
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
        // TODO: Implement this method to return all possible arguments that match the given partial argument.
        // This will depend on the specific logic of each command method.
        return Collections.emptyList();
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
        sender.sendMessage(Messages.HELP_COMMAND_INFO.format(label));           // /fd info <player>
        sender.sendMessage(Messages.HELP_COMMAND_LINK.format(label));           // /fd link
        sender.sendMessage(Messages.HELP_COMMAND_RELOAD.format(label));         // /fd reload
        sender.sendMessage(Messages.HELP_COMMAND_STATS.format(label));          // /fd stats
        sender.sendMessage(Messages.HELP_COMMAND_VERSION.format(label));        // /fd version
        sender.sendMessage("");
        sender.sendMessage("§7§oBy Rgld_");
        sender.sendMessage(ChatColor.GOLD + "----==={ " + ChatColor.YELLOW + fraud.getDescription().getName() + ChatColor.GOLD + " }===----");
    }

    /**
     * @param ipInfo IPInfo object
     * @return a {@link String} that contain every info about the ip of the player separated by a "|".
     */
    private String buildDesc(IPInfo ipInfo) {
        StringBuilder builder = new StringBuilder();
        for (String str : ipInfo.getDesc()) {
            builder.append(str);
            builder.append("/");
        }
        return builder.toString();
    }

    public void setDownloading(boolean downloading) {
        this.downloading = downloading;
    }
}
