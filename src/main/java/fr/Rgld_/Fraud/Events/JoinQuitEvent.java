package fr.Rgld_.Fraud.Events;

import com.google.common.collect.Lists;
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
import java.util.List;

public class JoinQuitEvent implements Listener {

    private final Fraud fraud;

    public JoinQuitEvent(Fraud fraud) {
        this.fraud = fraud;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!fraud.getConfiguration().alertOnJoinIsEnabled()) return;
        Player p = e.getPlayer();
        if (p.hasPermission("fraud.bypass.ip")) return;
        Datas data = fraud.getDatas();
        data.putPlayer(p);
        List<String> copyOfList = Lists.newArrayList();
        copyOfList.addAll(data.getListByPlayer(p));
        if ((copyOfList.size() >= fraud.getConfiguration().getDoubleAccountLimit() && !Utils.canGetAnAlt(copyOfList)) || (copyOfList.size() >= (fraud.getConfiguration().getDoubleAccountLimit() + 1))) {
            if (p.hasPermission("fraud.bypass.alert")) return;
            for (int i = 0; i < copyOfList.size(); i++) {
                String abc = copyOfList.get(i);
                copyOfList.set(i, (Utils.isConnected(abc) ? ChatColor.GREEN + abc : ChatColor.RED + abc));
            }

            String formatted = MessageFormat.format(Messages.ALTS_DETECTED.getMessage(), p.getName(), Utils.joinList(copyOfList));
            fraud.getConsole().sendMessage(formatted);
            for (Player pls : Bukkit.getOnlinePlayers()) {
                if (pls.hasPermission("fraud.receive.alert")) {
                    pls.sendMessage(formatted);
                }
            }
        }
    }
}
