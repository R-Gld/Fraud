package fr.Rgld_.Fraud.api;

import fr.Rgld_.Fraud.Fraud;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class Data {

    private final Fraud fraud;

    public Data() {
        this.fraud = (Fraud) Bukkit.getPluginManager().getPlugin("fraud");
    }

    public Fraud getFraud() {
        return this.fraud;
    }

    public List<String> getAlts(String pseudo) {
        return fraud.getDatas().getListByPseudo(pseudo);
    }

    public List<String> getAlts(Player player) {
        return fraud.getDatas().getList(player);
    }

}
