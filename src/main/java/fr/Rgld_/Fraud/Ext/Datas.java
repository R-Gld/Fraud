package fr.Rgld_.Fraud.Ext;

import fr.Rgld_.Fraud.Fraud;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.logging.Level;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Datas {

    private Fraud fraud;

    private YamlConfiguration fileConfig;
    private File file = new File(Fraud.getInstance().getDataFolder(), "data.yml");

    public Datas(Fraud fraud) {
        this.fraud = fraud;
        loadConfig();
    }

    private void loadConfig() {
        this.fileConfig = new YamlConfiguration();
        try {
            this.file.createNewFile();
            this.fileConfig.load(this.file);
        } catch (IOException | InvalidConfigurationException e) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred during load of the config. " + e.getMessage());
        }
    }

    private void saveConfig() {
        if(file.exists()) {
            try {
                fileConfig.save(file);
            } catch (IOException e) {
                Bukkit.getLogger().log(Level.SEVERE, "An error occurred during the saving of the config file. " + e.getMessage());
            }
        } else {
            loadConfig();
            saveConfig();
        }
    }
    private void setIps(String address, List<String> list){
        setStringList("data." + address, list);
    }

    private List<String> getStringList(String path) {
        return fileConfig.getStringList(path);
    }
    private void setStringList(String path, List<String> str){
        set(path, str);
    }

    private Object get(String path) {
        return fileConfig.get(path);
    }
    private void set(String path, Object obj){
        fileConfig.set(path, obj);
    }


    //\\ ---------------------------------------------------------------------------- //\\

    public void registerPlayer(Player p){
        if(p.hasPermission("fraud.bypass.ip")) return;
        List<String> lstr = getIps(p);
        String address = p.getAddress().toString();
        if(!lstr.contains(address))
            lstr.add(address);
        setIps(address, lstr);
    }

    public List<String> getIps(Player p){
        return getIps(p.getAddress());
    }

    public List<String> getIps(InetSocketAddress address) {
        return getStringList("data." + address);
    }

}
