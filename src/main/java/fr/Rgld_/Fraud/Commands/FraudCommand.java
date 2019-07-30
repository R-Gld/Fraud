package fr.Rgld_.Fraud.Commands;

import com.google.common.collect.Lists;
import fr.Rgld_.Fraud.Fraud;
import fr.Rgld_.Fraud.Helpers.Messages;
import fr.Rgld_.Fraud.Helpers.Utils;
import fr.Rgld_.Fraud.Storage.Datas;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.File;
import java.net.InetSocketAddress;
import java.text.MessageFormat;
import java.util.List;

public class FraudCommand implements CommandExecutor, TabCompleter {

    private final Fraud fraud;

    public FraudCommand(Fraud fraud){
        this.fraud = fraud;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch(args.length){
            case 0:
                sendHelp(sender, label);
                return false;
            case 1:
                switch(args[0].toLowerCase()){
                    case "version":
                        String version = fraud.getDescription().getVersion();
                        sender.sendMessage("Fraud version: " + (version.startsWith("v") ? version : "v" + version));
                        return false;
                    case "reload":
                        if(!sender.hasPermission("fraud.reload")){
                            sender.sendMessage(Messages.NO_PERMISSION.getMessage());
                            return false;
                        }
                        try {
                            fraud.setDatas(new Datas(fraud));
                            fraud.getConfiguration().reload();
                            sender.sendMessage(Messages.RELOAD_SUCCESS.getMessage());
                        } catch (Throwable t) {
                            sender.sendMessage(Messages.RELOAD_FAILED.getMessage());
                            t.printStackTrace();
                        }
                        return false;
                    case "clean-datas":
                        if(!sender.hasPermission("fraud.clean-datas")){
                            sender.sendMessage(Messages.NO_PERMISSION.getMessage());
                            return false;
                        }
                        try {
                            if(fraud.getDatas().getFile().renameTo(new File(fraud.getDataFolder(), "datas-before-reset.yml"))){
                                fraud.setDatas(new Datas(fraud));
                                for (Player pls : Bukkit.getOnlinePlayers()) fraud.getDatas().putPlayer(pls);
                                sender.sendMessage(Messages.COMMAND_CLEAN_DATA_YES.getMessage());
                            } else {
                                sender.sendMessage(Messages.COMMAND_CLEAN_DATA_NO.getMessage());
                            }
                        } catch(Throwable t){
                            sender.sendMessage(Messages.COMMAND_CLEAN_DATA_NO.getMessage());
                        }
                        return false;
                    case "contact":
                        sender.sendMessage("§6§lYou can contact the developer of this plugin via:");
                        sender.sendMessage("§6 - Discord: §e§l&oRomain | Rgld_#8275");
                        sender.sendMessage("§6 - Email: §e§l&ospigot@rgld.fr");
                        return false;
                }
                sendHelp(sender, label);
                return false;
            case 2:
                if(args[0].equalsIgnoreCase("check")) {
                    if(!sender.hasPermission("fraud.check.player")){
                        sender.sendMessage(Messages.NO_PERMISSION.getMessage());
                        return false;
                    }
                    Player target;
                    String arg1 = args[1];
                    Datas datas = fraud.getDatas();
                    try {
                        target = Bukkit.getPlayer(arg1);
                    } catch(NullPointerException e) {
                        target = null;
                    }
                    if (target != null) {
                        listAlts(datas.getListByPlayer(target),
                                sender,
                                !target.getName().equals(target.getDisplayName()) ?
                                        target.getName() + "§8(" + target.getDisplayName() + "§8)"
                                        : target.getName());
                    } else {
                        if(Utils.isValidIP(arg1)) {
                            if(sender.hasPermission("fraud.check.ip")) {
                                InetSocketAddress add = new InetSocketAddress(arg1, 0);
                                listAlts(
                                        datas.getListByAddress(arg1),
                                        sender,
                                        (add.isUnresolved() ?
                                                add.getHostName() :
                                                add.getAddress().toString()));
                            } else {
                                sender.sendMessage(Messages.NO_PERMISSION.getMessage());
                            }
                        } else {
                            listAlts(
                                    datas.getListByPseudo(arg1),
                                    sender,
                                    arg1);
                        }
                    }
                } else {
                    sendHelp(sender, label);
                }
                return false;
        }
        sendHelp(sender, label);
        return false;
    }

    private void listAlts(List<String> listOfAlts, CommandSender sender, String target) {
        if(null == listOfAlts || listOfAlts.isEmpty()){
            sender.sendMessage(MessageFormat.format(Messages.NO_ALTS.getMessage(), target));
            return;
        }
        for(int i = 0; i < listOfAlts.size(); i++){
            String p = listOfAlts.get(i);
            listOfAlts.set(i, (Utils.isConnected(p) ? ChatColor.GREEN + p :
                    ChatColor.RED + p));
        }
        String joined = Utils.joinList(listOfAlts);
        sender.sendMessage(MessageFormat.format(Messages.ALTS_ASKED.getMessage(), target, joined));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = null;
        switch(args.length) {
            case 1:
                if(args[0].toLowerCase().startsWith("c")) {
                    list = Lists.newArrayList("check", "clean-datas", "contact");
                } else if(args[0].toLowerCase().startsWith("ch")) {
                    list = Lists.newArrayList("check");
                } else if(args[0].toLowerCase().startsWith("co")) {
                    list = Lists.newArrayList("contact");
                } else if(args[0].toLowerCase().startsWith("cl")) {
                    list = Lists.newArrayList("clean-datas");
                } else if(args[0].toLowerCase().startsWith("v")) {
                    list = Lists.newArrayList("version");
                } else if(args[0].toLowerCase().startsWith("r")) {
                    list = Lists.newArrayList("reload");
                } else {
                    list = Lists.newArrayList("check", "clean-datas", "contact", "reload", "version");
                }
                break;
            case 2:
                if(args[0].equalsIgnoreCase("check"))
                    list = null;
                else
                    list = Lists.newArrayList();
                break;
        }
        return list;
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(ChatColor.GOLD + "----==={ " + ChatColor.YELLOW + fraud.getDescription().getName() + ChatColor.GOLD + " }===----");
        sender.sendMessage("");
        sender.sendMessage(MessageFormat.format(Messages.HELP_COMMAND_CHECK.getMessage(), label));
        sender.sendMessage(MessageFormat.format(Messages.HELP_COMMAND_VERSION.getMessage(), label));
        sender.sendMessage(MessageFormat.format(Messages.HELP_COMMAND_RELOAD.getMessage(), label));
        sender.sendMessage(MessageFormat.format(Messages.HELP_COMMAND_CONTACT.getMessage(), label));
        sender.sendMessage(MessageFormat.format(Messages.HELP_COMMAND_CLEAN_DATAS.getMessage(), label));
        sender.sendMessage("");
        sender.sendMessage("§7§oBy Rgld_");
        sender.sendMessage(ChatColor.GOLD + "----==={ " + ChatColor.YELLOW + fraud.getDescription().getName() + ChatColor.GOLD + " }===----");
    }
}
