package fr.Rgld_.Fraud.Commands;

import com.google.common.collect.Lists;
import fr.Rgld_.Fraud.Fraud;
import fr.Rgld_.Fraud.Helpers.Messages;
import fr.Rgld_.Fraud.Helpers.Updater;
import fr.Rgld_.Fraud.Helpers.Utils;
import fr.Rgld_.Fraud.Storage.Datas;
import net.md_5.bungee.api.chat.ClickEvent;
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
    private Datas datas;

    private final Updater up;
    public FraudCommand(Fraud fraud) {
        this.fraud = fraud;
        this.na = new ArrayList<>();
        this.up = new Updater(fraud);
    }

    private final ArrayList<String> na;
    public ArrayList<String> getNotAlerted() {
        return na;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.datas = fraud.getDatas();
        switch(args.length) {
            case 0:
                break;
            case 1:
                switch(args[0].toLowerCase()) {
                    case "v":
                    case "version":
                        String version = fraud.getDescription().getVersion();
                        double dVersion = up.parseVersion(version);
                        String latest = up.getLatestVersionFormatted();
                        double dLatest = up.parseVersion(latest);
                        sender.sendMessage(ChatColor.GRAY + "Installed Fraud version: v" + version);
                        sender.sendMessage(ChatColor.GRAY + "Latest Fraud version available: v" + latest);
                        sender.sendMessage((dLatest>dVersion ? "§c§l❌ §cOutdated" + "\n&c&lYou should download the new version, check /fraud link" : (dLatest==dVersion ? "§a§l✔ §aUp-to-date" : "§6Ur a precursor 😉")));
                        return false;
                    case "reload":
                        if(!sender.hasPermission("fraud.reload")) {
                            sender.sendMessage(Messages.NO_PERMISSION.getMessage());
                            return false;
                        }
                        try {
                            fraud.setDatas(new Datas(fraud));
                            fraud.getConfiguration().loadConfig();
                            sender.sendMessage(Messages.RELOAD_SUCCESS.getMessage());
                        } catch(Throwable t) {
                            sender.sendMessage(Messages.RELOAD_FAILED.getMessage());
                            t.printStackTrace();
                        }
                        return false;
                    case "contact":
                        sender.sendMessage("§6§lYou can contact the developer of this plugin via:");
                        sender.sendMessage("§6 - Discord: §e§l§oRomain | Rgld_#5344");
                        sender.sendMessage("§6 - Email: §e§l§ospigot@rgld.fr");
                        return false;
                    case "link":
                        sender.sendMessage("§6Github Page: §9§nhttps://github.com/R-Gld/Fraud");
                        sender.sendMessage("§6Spigot Ressource: §9§nhttps://www.spigotmc.org/resources/fraud-alts-finder.69872/");
                        return false;
                    case "all":
                        if(!sender.hasPermission("fraud.check.player.all")) {
                            sender.sendMessage(Messages.NO_PERMISSION.getMessage());
                            return false;
                        }
                        List<String> everChecked = Lists.newArrayList();
                        List<Player> concernedPlayers = Lists.newArrayList();
                        for(Player pls : Bukkit.getOnlinePlayers()) {
                            if(everChecked.contains(pls.getName())) continue;
                            List<String> plsAlts = datas.getList(pls);
                            everChecked.addAll(plsAlts);
                            if(plsAlts.size() >= 2 && !Utils.canGetAnAlt(plsAlts) || (plsAlts.size() >= (fraud.getConfiguration().getDoubleAccountLimit() + 1))) {
                                concernedPlayers.add(pls);
                            }
                        }
                        if(concernedPlayers.isEmpty()) {
                            sender.sendMessage(Messages.ALL_EMPTY.getMessage());
                        } else {
                            sender.sendMessage(Messages.ALL_ALTS_ASKED_ANNOUNCER.getMessage());
                            for(Player pls : concernedPlayers) {
                                List<String> plsAlts = datas.getList(pls);
                                listAlts(plsAlts, sender, !pls.getName().equals(pls.getDisplayName()) ? pls.getName() + "§8(" + pls.getDisplayName() + "§8)" : pls.getName(), true);
                            }
                        }
                        return false;
                    case "dl":
                    case "download":
                        if(!sender.hasPermission("fraud.download")) {
                            sender.sendMessage(Messages.NO_PERMISSION.getMessage());
                            return false;
                        }
                        if(fraud.getUpdater().downloadAndInstall()) {
                            sender.sendMessage(ChatColor.GOLD + "The download of the latest release of Fraud was a " + ChatColor.YELLOW + "success" + ChatColor.GOLD + ".");
                            sender.sendMessage(ChatColor.GOLD + "The new release of Fraud will be effective at the next restart or reload of the plugin. You can use a plugin like PlugMan to reload just one plugin.");
                        } else {
                            sender.sendMessage(ChatColor.GOLD + "The download of the latest release of Fraud was a " + ChatColor.YELLOW + "failure" + ChatColor.GOLD + ".");
                        }
                        sender.sendMessage(ChatColor.GRAY + "§o(You can download it manually with this url: " + ChatColor.BLUE + "§nhttp://fraud.rgld.fr" + ChatColor.GRAY + "§o)");
                        return false;
                    case "alert":
                        if(!(sender.hasPermission("fraud.alert.switch"))) {
                            sender.sendMessage(Messages.NO_PERMISSION.getMessage());
                            return false;
                        }
                        if(na.contains(sender.getName())){
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
                switch(args[0].toLowerCase()) {
                    case "check":
                        if(!sender.hasPermission("fraud.check.player.one") && !arg1.equals(sender.getName())) {
                            sender.sendMessage(Messages.NO_PERMISSION.getMessage());
                            return false;
                        }
                        Player target;
                        try {
                            target = Bukkit.getPlayer(arg1);
                        } catch(NullPointerException e) {
                            target = null;
                        }
                        if(target != null) {
                            listAlts(
                                    datas.getList(target),
                                    sender,
                                    !target.getName().equals(target.getDisplayName()) ? target.getName() + "§8(" + target.getDisplayName() + "§8)" : target.getName(),
                                    false);
                        } else {
                            if(Utils.isValidIP(arg1)) {
                                if(sender.hasPermission("fraud.check.ip")) {
                                    InetSocketAddress add = new InetSocketAddress(arg1, 0);
                                    listAlts(datas.getList(arg1), sender, (add.isUnresolved() ? add.getHostName() : add.getAddress().toString()), false);
                                } else {
                                    sender.sendMessage(Messages.NO_PERMISSION.getMessage());
                                }
                            } else {
                                listAlts(datas.getListByPseudo(arg1), sender, arg1, false);
                            }
                        }
                        return false;
                    case "forgot":
                        if(!sender.hasPermission("fraud.forgot")) {
                            sender.sendMessage(Messages.NO_PERMISSION.getMessage());
                            return false;
                        }
                        if(datas.isRegisteredInIps(arg1)) {
                            datas.forgotPlayer(arg1);
                            sender.sendMessage(Messages.PLAYER_FORGOTTEN.format(arg1));
                        } else sender.sendMessage(Messages.NOT_IN_DATAS.format(arg1));
                        return false;
                    case "info":
                        List<String> alts = datas.getListByPseudo(arg1);
                        if(!alts.contains(sender.getName()) && !sender.hasPermission("fraud.info")) {
                            sender.sendMessage(Messages.NO_PERMISSION.getMessage());
                            return false;
                        }
                        if(datas.isFullRegistered(arg1)) {
                            sender.sendMessage(Messages.INFO_HEADER.format(arg1));
                            for(String alt : alts) {
                                sender.sendMessage(
                                        Messages.INFO_ITERATION.format(
                                                (Utils.isConnected(alt) ? ChatColor.GREEN + alt : ChatColor.RED + alt), // {0}
                                                Utils.formatDate(datas.getFirstJoin(alt)), // {1}
                                                Utils.formatDate(datas.getLastJoin(alt)) // {2}
                                        )
                                );
                            }
                        } else sender.sendMessage(Messages.NOT_IN_DATAS.format(arg1));
                        return false;
                }
                break;
        }
        sendHelp(sender, label);
        return false;
    }

    private void listAlts(List<String> listOfAlts, CommandSender sender, String target, boolean all) {
        if(null == listOfAlts || listOfAlts.isEmpty()) {
            sender.sendMessage(Messages.NO_ALTS.format(target));
            return;
        }
        List<String> copyOfList = Lists.newArrayList();
        copyOfList.addAll(listOfAlts);

        for(int i = 0; i < listOfAlts.size(); i++) {
            String p = listOfAlts.get(i);
            copyOfList.set(i, (Utils.isConnected(p) ? ChatColor.GREEN + p : ChatColor.RED + p));
        }
        String joined = Utils.joinList(copyOfList);
        sender.sendMessage(MessageFormat.format((all ? Messages.ALL_ALTS_ASKED.getMessage() : Messages.ALTS_ASKED.getMessage()), target, joined));
        String name = copyOfList.get(0).substring(2);
        if(sender instanceof Player && datas.isFullRegistered(name)) {
            TextComponent info = new TextComponent("   §e§l➤ (i)");
            info.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Messages.INFO_HOVER.getMessage())));
            info.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fraud info " + name));
            ((Player) sender).spigot().sendMessage(info);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = Lists.newArrayList();
        switch(args.length) {
            case 1:
                String str = args[0].toLowerCase();
                if(str.startsWith("a")) {
                    if(str.startsWith("ale")) {
                        list = Lists.newArrayList("alert");
                        break;
                    }
                    list = Lists.newArrayList("alert", "all");
                } else if(str.startsWith("c")) {
                    if(str.startsWith("ch")) {
                        list = Lists.newArrayList("check");
                        break;
                    } else if(str.startsWith("co")) {
                        list = Lists.newArrayList("contact");
                        break;
                    }
                    list = Lists.newArrayList("check", "contact");
                } else if(str.startsWith("d")) {
                    list = Lists.newArrayList("dl", "download");
                } else if(str.startsWith("f")) {
                    list = Lists.newArrayList("forgot");
                } else if(str.startsWith("i")) {
                    list = Lists.newArrayList("info");
                } else if(str.startsWith("r")) {
                    list = Lists.newArrayList("reload");
                } else if(str.startsWith("v")) {
                    if(!str.equals("v")) {
                        list = Lists.newArrayList("version");
                    } else list = Lists.newArrayList("v", "version");
                } else {
                    list = Lists.newArrayList("all", "check", "contact", "dl", "download", "forgot", "info", "reload", "v", "version");
                }
                break;
            case 2:
                String arg = args[0].toLowerCase();
                if(arg.equals("check") || arg.equals("forgot") || arg.equals("info")) list = null;
                break;
        }
        return list;
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(ChatColor.GOLD + "----==={ " + ChatColor.YELLOW + fraud.getDescription().getName() + ChatColor.GOLD + " }===----");
        sender.sendMessage("");
        sender.sendMessage(Messages.HELP_COMMAND_ALERT.format(label));
        sender.sendMessage(Messages.HELP_COMMAND_ALL.format(label));
        sender.sendMessage(Messages.HELP_COMMAND_CHECK.format(label));
        sender.sendMessage(Messages.HELP_COMMAND_CONTACT.format(label));
        sender.sendMessage(Messages.HELP_COMMAND_DOWNLOAD.format(label));
        sender.sendMessage(Messages.HELP_COMMAND_FORGOT.format(label));
        sender.sendMessage(Messages.HELP_COMMAND_INFO.format(label));
        sender.sendMessage(Messages.HELP_COMMAND_LINK.format(label));
        sender.sendMessage(Messages.HELP_COMMAND_RELOAD.format(label));
        sender.sendMessage(Messages.HELP_COMMAND_VERSION.format(label));
        sender.sendMessage("");
        sender.sendMessage("§7§oBy Rgld_");
        sender.sendMessage(ChatColor.GOLD + "----==={ " + ChatColor.YELLOW + fraud.getDescription().getName() + ChatColor.GOLD + " }===----");
    }
}
