package fr.Rgld_.Fraud.api;

import fr.Rgld_.Fraud.Fraud;
import fr.Rgld_.Fraud.Storage.Datas;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class Data {

    private final Fraud fraud;
    private final Datas datas;

    public Data() {
        this.fraud = (Fraud) Bukkit.getPluginManager().getPlugin("fraud");
        if(this.fraud == null) {
            this.datas = null;
            System.out.println(ChatColor.RED + "ERROR: Fraud isn't loaded. You can download the latest version at: http://fraud.rgld.fr");
        } else this.datas = fraud.getDatas();
    }

    public Fraud getFraud() {
        return this.fraud;
    }

    public List<String> getAlts(String pseudo) {
        return datas.getListByPseudo(pseudo);
    }

    public List<String> getAlts(Player player) {
        return datas.getList(player);
    }

    public long getFirstJoin(Player player) {
        return getFirstJoin(player.getName());
    }

    public long getFirstJoin(String pseudo) {
        return datas.getFirstJoin(pseudo);
    }

    public long getLastJoin(Player player) {
        return getLastJoin(player.getName());
    }

    public long getLastJoin(String pseudo) {
        return datas.getLastJoin(pseudo);
    }

}
