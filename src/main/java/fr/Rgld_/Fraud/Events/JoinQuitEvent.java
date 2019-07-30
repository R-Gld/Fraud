package fr.Rgld_.Fraud.Events;

import fr.Rgld_.Fraud.Fraud;
import fr.Rgld_.Fraud.Helpers.Messages;
import fr.Rgld_.Fraud.Helpers.Utils;
import fr.Rgld_.Fraud.Storage.Datas;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class JoinQuitEvent implements Listener {

    private final Fraud fraud;

    public JoinQuitEvent(Fraud fraud) {
        this.fraud = fraud;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if(!fraud.getConfiguration().alertOnJoinIsEnabled()) return;
        Player p = e.getPlayer();
        if (p.hasPermission("fraud.bypass.ip")) return;
        Datas data = fraud.getDatas();
        data.putPlayer(p);
        List<String> lstr = new ArrayList<>(data.getListByPlayer(p));
        if ( (lstr.size() >= fraud.getConfiguration().getDoubleAccountLimit() && !p.hasPermission("fraud.notcause.alert")) || (lstr.size() >= (fraud.getConfiguration().getDoubleAccountLimit()+1)) ) {
            if(p.hasPermission("fraud.bypass.alert")) return;
            for(int i = 0; i < lstr.size(); i++){
                String abc = lstr.get(i);
                lstr.set(i, (Utils.isConnected(abc) ? ChatColor.GREEN + abc : ChatColor.RED + abc));
            }

            String formatted = MessageFormat.format(Messages.ALTS_DETECTED.getMessage(), p.getName(), Utils.joinList(lstr));
            fraud.getConsole().sendMessage(formatted);
            for (Player pls : Bukkit.getOnlinePlayers()) {
                if (pls.hasPermission("fraud.receive.alert")) {
                    pls.sendMessage(formatted);
                }
            }
        }
    }
}
