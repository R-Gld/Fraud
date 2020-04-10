package fr.Rgld_.Fraud.Events;

import com.google.common.collect.Lists;
import fr.Rgld_.Fraud.Events.Custom.DoubleAccountJoinEvent;
import fr.Rgld_.Fraud.Fraud;
import fr.Rgld_.Fraud.Helpers.Messages;
import fr.Rgld_.Fraud.Helpers.Utils;
import fr.Rgld_.Fraud.Storage.Configuration;
import fr.Rgld_.Fraud.Storage.Datas;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;

public class JoinQuitEvent implements Listener {

    private final Fraud fraud;
    private final ArrayList<Player> list;

    public JoinQuitEvent(Fraud fraud) {
        this.fraud = fraud;
        this.list = Lists.newArrayList();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Configuration config = fraud.getConfiguration();

        Player p = e.getPlayer();
        if(p.hasPermission("fraud.bypass.ip")) return;
        Datas data = fraud.getDatas();
        data.putPlayer(p);
        if(!config.alertOnJoinIsEnabled()) return;
        List<String> altsList = Lists.newArrayList();
        altsList.addAll(data.getList(p));
        int altsNum = altsList.size();
        if((altsNum >= config.getDoubleAccountLimit() && !Utils.canGetAnAlt(altsList)) || (altsNum > config.getDoubleAccountLimit())) {
            // Player connected is a double account.
            if(p.hasPermission("fraud.bypass.alert")) return;

            if(config.getKick().isEnabled()) {
                p.kickPlayer(config.getKick().getReason(altsNum));
            }

            DoubleAccountJoinEvent event = new DoubleAccountJoinEvent(p, altsList);
            Bukkit.getPluginManager().callEvent(event);
            if(event.alert()) {
                for(int i = 0; i < altsNum; i++) {
                    String abc = altsList.get(i);
                    altsList.set(i, (Utils.isConnected(abc) ? ChatColor.GREEN + abc : ChatColor.RED + abc));
                }

                String formatted = Messages.ALTS_DETECTED.format(p.getName(), Utils.joinList(altsList));
                fraud.getConsole().sendMessage(formatted);
                TextComponent info = new TextComponent("   §e§l➤ (i)");
                info.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Messages.INFO_HOVER.getMessage())));
                info.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fraud info " + altsList.get(0).substring(2)));
                for(Player pls : Bukkit.getOnlinePlayers()) {
                    if(pls.hasPermission("fraud.receive.alert")) {
                        pls.sendMessage(formatted);
                        pls.spigot().sendMessage(info);
                    }
                }
            }
        }
        if(altsNum >= config.getKick().getMaxAccounts() && config.isKickEnabled()) {
            p.kickPlayer(config.getKick().getReason(altsNum));
            e.setJoinMessage("");
            list.add(p);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if(list.contains(p)) {
            list.remove(p);
            e.setQuitMessage("");
        }
    }
}
