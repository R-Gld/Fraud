package fr.Rgld_.Fraud.Spigot.Events;

import com.google.common.collect.Lists;
import fr.Rgld_.Fraud.Global.IPInfo;
import fr.Rgld_.Fraud.Spigot.Events.Custom.DoubleAccountJoinEvent;
import fr.Rgld_.Fraud.Spigot.Fraud;
import fr.Rgld_.Fraud.Spigot.Helpers.Messages;
import fr.Rgld_.Fraud.Spigot.Helpers.Utils;
import fr.Rgld_.Fraud.Spigot.Storage.Configuration;
import fr.Rgld_.Fraud.Spigot.Storage.Data.Data;
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

/**
 * Check at every connection if a player has an alt, is from a bad country defined on the configuration...
 */
public class JoinQuitEvent implements Listener {

    private final Fraud fraud;
    private final ArrayList<Player> list;

    /**
     * Constructor of the class.
     * @param fraud The plugin instance.
     */
    public JoinQuitEvent(Fraud fraud) {
        this.fraud = fraud;
        this.list = Lists.newArrayList();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Configuration config = fraud.getConfiguration();

        Player p = e.getPlayer();
        if(p.hasPermission("fraud.bypass.ip")) return;
        Data data = fraud.getData();
        data.putPlayer(p);
        if(!config.alertOnJoinIsEnabled()) return;
        List<String> altsList = Lists.newArrayList();
        altsList.addAll(data.getList(p));
        int altsNum = altsList.size();
        if((altsNum >= config.getDoubleAccountLimit() && Utils.cantGetAnAlt(altsList)) || (altsNum > config.getDoubleAccountLimit())) {
            // Player connected is or has a double account.

            if(config.getKick().isEnabled() && !p.hasPermission("fraud.kick.evade")) {
                p.kickPlayer(config.getKick().getReason(altsNum));
            }

            if(p.hasPermission("fraud.bypass.alert")) return;

            DoubleAccountJoinEvent event = new DoubleAccountJoinEvent(p, e.getJoinMessage(), altsList);
            Bukkit.getPluginManager().callEvent(event);
            if(event.alert()) {
                for(int i = 0; i < altsNum; i++) {
                    String abc = altsList.get(i);
                    altsList.set(i, (Utils.isConnected(abc) ? ChatColor.GREEN + abc : ChatColor.RED + abc));
                }
                IPInfo ipInfo = fraud.getIpInfoManager().getIPInfoConformConfig(Utils.getAddress(p.getAddress()));
                List<String> bad_countries = config.getCountriesAlert();
                if(bad_countries.contains(ipInfo.getCountryCode())) {
                    String code = ipInfo.getCountryCode();
                    String countryName = ipInfo.getCountryName();
                    String countries_format = Messages.BAD_COUNTRY_DETECTED.format(p.getName(),
                                                                                   countryName == null ?
                                                                                           code :
                                                                                           countryName + "(" + code + ")");
                    for(Player pls : Bukkit.getOnlinePlayers()) {
                        if (pls.hasPermission("fraud.receive.alert") && !fraud.getFraudCommand().getNotAlerted().contains(pls.getName())) {
                            pls.sendMessage(countries_format);
                        }
                    }
                }

                String formatted = Messages.ALTS_DETECTED.format(p.getName(), Utils.joinList(altsList));
                fraud.getConsole().sendMessage(formatted);
                TextComponent info = new TextComponent("   §e§l➤ (i)");
                info.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Messages.INFO_HOVER.getMessage())));
                info.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fraud info " + altsList.get(0).substring(2)));
                for(Player pls : Bukkit.getOnlinePlayers())
                    if (pls.hasPermission("fraud.receive.alert") && !fraud.getFraudCommand().getNotAlerted().contains(pls.getName())) {
                        pls.sendMessage(formatted);
                        pls.spigot().sendMessage(info);
                    }
            }
        }
        if(altsNum >= config.getKick().getMaxAccounts() && config.isKickEnabled()) {
            p.kickPlayer(config.getKick().getReason(altsNum));
            e.setJoinMessage("");
            list.add(p);
        }
    }

    /**
     * @param event  the {@link PlayerQuitEvent} triggered when a player leave the server.
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        fraud.getIpInfoManager().getIpInfoMap().remove(p.getAddress().toString().split(":")[0].substring(1));
        if(list.contains(p)) {
            list.remove(p);
            event.setQuitMessage("");
        }
    }
}
