package fr.Rgld_.Fraud.Spigot.Commands;

import com.google.common.collect.Lists;
import fr.Rgld_.Fraud.Global.IPInfo;
import fr.Rgld_.Fraud.Global.Updater;
import fr.Rgld_.Fraud.Spigot.Fraud;
import fr.Rgld_.Fraud.Spigot.Helpers.Messages;
import fr.Rgld_.Fraud.Spigot.Helpers.Utils;
import fr.Rgld_.Fraud.Spigot.Storage.Data.Data;
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
import java.util.List;

public class FraudCommand implements CommandExecutor, TabCompleter {

    private final Fraud fraud;
    private Data Data;

    private final Updater up;

    public FraudCommand(Fraud fraud) {
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
     * Function executed when a player or the console execute a command of this list:
     * - /fraud
     * - /fd
     * - /alts
     * ... (all the aliases of the /fraud command)
     *
     * @param sender {@link CommandSender} the sender of the command.
     * @param command {@link Command} the command, it includes the name, the aliases, ...
     * @param label a {@link String} that is the command used by the {@link CommandSender} like fraud, fd, alts, ...
     * @param args an array of {@link String} that contain every args given by the {@link CommandSender}.
     * @return false (the result of this function is not really used here).
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.Data = fraud.getDatas();
        switch (args.length) {
            case 0:
                break;
            case 1:
                switch (args[0].toLowerCase()) {
                    case "v":
                    case "checkupdate":
                    case "version":
                        String version = fraud.getDescription().getVersion();
                        double dVersion = up.parseVersion(version);
                        String latest = up.getLatestVersionFormatted();
                        double dLatest = up.parseVersion(latest);
                        sender.sendMessage(ChatColor.GRAY + "Installed Fraud version: v" + version);
                        sender.sendMessage(ChatColor.GRAY + "Latest Fraud version available: v" + latest);
                        sender.sendMessage((dLatest > dVersion ? "§c§l❌ §cOutdated\n§c§lYou should download the new version, check /fraud link" : (dLatest == dVersion ? "§a§l✔ §aUp-to-date" : "§6Ur a precursor 😉")));
                        return false;
                    case "reload":
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
                    case "contact":
                        sender.sendMessage("§6§lYou can contact the developer of this plugin via:");
                        String discord = "§6 - Discord: §e§l§oRomain | Rgld_#5344";
                        String email = "§6 - Email: §e§l§ospigot@rgld.fr";
                        if(sender instanceof Player) {
                            TextComponent text = new TextComponent(email);
                            text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Click here to send an §6email§7.").create()));
                            text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "mailto://spigot@rgld.fr"));
                            ((Player) sender).spigot().sendMessage(text);
                            TextComponent text_1 = new TextComponent(discord);
                            text_1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Click here to §6add me §7on §6discord§7.").create()));
                            text_1.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://rgld.fr/discord/add/Rgld_"));
                            ((Player) sender).spigot().sendMessage(text_1);
                        } else {
                            sender.sendMessage(discord);
                            sender.sendMessage(email);
                        }
                        sender.sendMessage("§6 - Twitter: §e§l§ohttps://twitter.com/RGld_");
                        return false;
                    case "link":
                    case "links":
                        sender.sendMessage("§6§m--§r§6> §ePlugin Links");
                        sender.sendMessage("\t§6Source-Code: §9§nhttps://rgld.fr/fraud/source-code");
                        sender.sendMessage("\t§6Download Latest: §9§nhttps://rgld.fr/fraud/download");
                        sender.sendMessage("\t§6Spigot Resource: §9§nhttps://rgld.fr/fraud/spigot/link");
                        sender.sendMessage("§6§m--§r§6> §eServices used");
                        sender.sendMessage("\t§6RIPE: §9§nhttps://www.ripe.net§r \n\t§7§o(Used to get information about ISP of an ip)");
                        sender.sendMessage("\t§6MaxMind: §9§nhttps://www.maxmind.com/en/geoip2-services-and-databases§r \n\t§7§o(Used to get information about the geolocation of an ip)");
                        return false;
                    case "all":
                        if (!sender.hasPermission("fraud.check.player.all")) {
                            sender.sendMessage(Messages.NO_PERMISSION.getMessage());
                            return false;
                        }
                        List<String> everChecked = Lists.newArrayList();
                        List<Player> concernedPlayers = Lists.newArrayList();
                        for (Player pls : Bukkit.getOnlinePlayers()) {
                            if (everChecked.contains(pls.getName())) continue;
                            List<String> plsAlts = Data.getList(pls);
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
                                List<String> plsAlts = Data.getList(pls);
                                listAlts(plsAlts, sender, !pls.getName().equals(pls.getDisplayName()) ? pls.getName() + "§8(" + pls.getDisplayName() + "§8)" : pls.getName(), true);
                            }
                        }
                        return false;
                    case "dl":
                    case "download":
                        if (!sender.hasPermission("fraud.download")) {
                            sender.sendMessage(Messages.NO_PERMISSION.getMessage());
                            return false;
                        }
                        if (fraud.getUpdater().downloadAndInstall()) {
                            sender.sendMessage(ChatColor.GOLD + "The download of the latest release of Fraud was a " + ChatColor.YELLOW + "success" + ChatColor.GOLD + ".");
                            sender.sendMessage(ChatColor.GOLD + "The new release of Fraud will be effective at the next restart or reload of the plugin. You can use a plugin like PlugMan to reload just one plugin.");
                        } else {
                            sender.sendMessage(ChatColor.GOLD + "The download of the latest release of Fraud was a " + ChatColor.YELLOW + "failure" + ChatColor.GOLD + ".");
                        }
                        sender.sendMessage(ChatColor.GRAY + "§o(You can download it manually with this url: " + ChatColor.BLUE + "§nhttp://fraud.rgld.fr" + ChatColor.GRAY + "§o)");
                        return false;
                    case "alert":
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
                break;
            case 2:
                String arg1 = args[1];
                switch (args[0].toLowerCase()) {
                    case "check":
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
                                    Data.getList(target),
                                    sender,
                                    !target.getName().equals(target.getDisplayName()) ? target.getName() + "§8(" + target.getDisplayName() + "§8)" : target.getName(),
                                    false);
                        } else {
                            if (Utils.isValidIP(arg1)) {
                                if (sender.hasPermission("fraud.check.ip")) {
                                    InetSocketAddress add = new InetSocketAddress(arg1, 0);
                                    listAlts(
                                            Data.getList(arg1),
                                            sender,
                                            (add.isUnresolved() ? add.getHostName() : add.getAddress().toString()),
                                            false);
                                } else {
                                    sender.sendMessage(Messages.NO_PERMISSION.getMessage());
                                }
                            } else {
                                listAlts(Data.getListByPseudo(arg1), sender, arg1, false);
                            }
                        }
                        return false;
                    case "forgot":
                        if (!sender.hasPermission("fraud.forgot")) {
                            sender.sendMessage(Messages.NO_PERMISSION.getMessage());
                            return false;
                        }
                        if (Data.isRegisteredInIps(arg1)) {
                            Data.forgotPlayer(arg1);
                            sender.sendMessage(Messages.PLAYER_FORGOTTEN.format(arg1));
                        } else sender.sendMessage(Messages.NOT_IN_DATAS.format(arg1));
                        return false;
                    case "geoip":
                        if(!sender.hasPermission("fraud.geoip")) {
                            sender.sendMessage(Messages.NO_PERMISSION.getMessage());
                            return false;
                        }
                        if (!Utils.isValidIP(arg1)) {
                            sender.sendMessage(Messages.NOT_VALID_IP.getMessage());
                            return false;
                        }
                        return false;
                    case "info":
                        List<String> alts = Data.getListByPseudo(arg1);
                        if (!alts.contains(sender.getName()) && !sender.hasPermission("fraud.info")) {
                            sender.sendMessage(Messages.NO_PERMISSION.getMessage());
                            return false;
                        }
                        if (Data.isFullRegistered(arg1)) {
                            String ip;
                            if (Bukkit.getPlayer(arg1) != null) {
                                Player p = Bukkit.getPlayer(arg1);
                                ip = p.getAddress().toString().split(":")[0].substring(1);
                            } else {
                                ip = Data.getIP(arg1);
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
                                                Utils.formatDate(Data.getFirstJoin(alt)), // {1}
                                                Utils.formatDate(Data.getLastJoin(alt)) // {2}
                                        )
                                );
                            }
                            if (sender.hasPermission("fraud.info.ip")) {
                                if (!fraud.getIpInfoManager().getIpInfoMap().containsKey(ip)) {
                                    // You don't have to wait if the ip info of the ip given in parameter have been already checked after the connection of a player.
                                    sender.sendMessage(Messages.INFO_WAIT_FOR_THE_OTHER_PART.getMessage());
                                    sendIPInfo(ip, sender);
                                } else {
                                    new Thread(() -> sendIPInfo(ip, sender)).start();
                                }
                            }
                        } else sender.sendMessage(Messages.NOT_IN_DATAS.format(arg1));
                        return false;
                }
                break;
        }
        sendHelp(sender, label);
        return false;
    }

    /**
     * Build and send the information about the ip given in parameters to the {@link CommandSender}.
     *
     * @param ip the ip seen
     * @param sender the sender who receive the information
     */
    private void sendIPInfo(String ip, CommandSender sender) {
        boolean isGeoIPApiActivated = fraud.getConfiguration().isGeoIPAPIActivated();
        IPInfo ii = fraud.getIpInfoManager().getIPInfoConformConfig(ip);
        sender.sendMessage(Messages.INFO_IP_INFORMATION.format(ip));

        if(ii.getContinent() != null && isGeoIPApiActivated) {
            String continent = ii.getContinent();
            sender.sendMessage(Messages.INFO_IP_continent.format(continent));
        }

        if(ii.getCountryCode() != null && ii.getCountryName() != null) {
            String countryName = ii.getCountryName();
            String countryCode = ii.getCountryCode();
            sender.sendMessage(Messages.INFO_IP_country.format(countryName, countryCode));
        }

        if(ii.getSubDivision() != null && isGeoIPApiActivated) {
            String subDiv = ii.getSubDivision();
            sender.sendMessage(Messages.INFO_IP_sub_division.format(subDiv));
        }

        if(ii.getCity() != null && isGeoIPApiActivated) {
            String city = ii.getCity();
            sender.sendMessage(Messages.INFO_IP_city.format(city));
        }

        if(ii.getPostalCode() != null && isGeoIPApiActivated) {
            String postalCode = ii.getPostalCode();
            sender.sendMessage(Messages.INFO_IP_postal_code.format(postalCode));
        }

        if(ii.getLatitude() != null && ii.getLongitude() != null && isGeoIPApiActivated) {
            String lat = ii.getLatitude();
            String lon = ii.getLongitude();
            if(sender instanceof Player) {
                TextComponent text = new TextComponent(Messages.INFO_IP_coordinates.format(lat, lon));
                text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Messages.INFO_IP_coordinates_click.getMessage()).create()));
                text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, MessageFormat.format("https://www.google.fr/maps/search/{0},{1}", lat, lon)));
                ((Player) sender).spigot().sendMessage(text);
            } else {
                sender.sendMessage(Messages.INFO_IP_coordinates.format(lat, lon));
            }
        }

        String netname = ii.getNetname();
        String desc = buildDesc(ii);
        if(netname != null || desc != null) {
            String other_information =
                    (netname != null && !netname.equals("") ? netname + "/" : "") +
                    desc;
            if(!other_information.trim().equals(""))
                sender.sendMessage(Messages.INFO_IP_others.format(other_information));
        }
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
        if (sender instanceof Player && Data.isFullRegistered(name)) {
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
        List<String> list = Lists.newArrayList();
        switch (args.length) {
            case 1:
                String str = args[0].toLowerCase();
                if (str.startsWith("a")) {
                    if (str.startsWith("ale")) {
                        list = Lists.newArrayList("alert");
                        break;
                    }
                    list = Lists.newArrayList("alert", "all");
                } else if (str.startsWith("c")) {
                    if (str.startsWith("ch")) {
                        list = Lists.newArrayList("check");
                        break;
                    } else if (str.startsWith("co")) {
                        list = Lists.newArrayList("contact");
                        break;
                    }
                    list = Lists.newArrayList("check", "contact");
                } else if (str.startsWith("d")) {
                    list = Lists.newArrayList("dl", "download");
                } else if (str.startsWith("f")) {
                    list = Lists.newArrayList("forgot");
                } else if (str.startsWith("g")) {
                    list = Lists.newArrayList("geoip");
                } else if (str.startsWith("i")) {
                    list = Lists.newArrayList("info");
                } else if (str.startsWith("r")) {
                    list = Lists.newArrayList("reload");
                } else if (str.startsWith("v")) {
                    if (!str.equals("v")) {
                        list = Lists.newArrayList("version");
                    } else list = Lists.newArrayList("v", "version");
                } else {
                    list = Lists.newArrayList("all", "check", "contact", "dl", "download", "forgot", "info", "reload", "v", "version");
                }
                break;
            case 2:
                String arg = args[0].toLowerCase();
                if (arg.equals("check") || arg.equals("forgot") || arg.equals("info")) list = null;
                break;
        }
        return list;
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
        sender.sendMessage(Messages.HELP_COMMAND_GEOIP.format(label));         // /fd forgot <player>
        sender.sendMessage(Messages.HELP_COMMAND_INFO.format(label));           // /fd info <player>
        sender.sendMessage(Messages.HELP_COMMAND_LINK.format(label));           // /fd link
        sender.sendMessage(Messages.HELP_COMMAND_RELOAD.format(label));         // /fd reload
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
}
