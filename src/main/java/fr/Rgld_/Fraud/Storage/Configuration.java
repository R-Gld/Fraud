package fr.Rgld_.Fraud.Storage;

import fr.Rgld_.Fraud.Fraud;
import fr.Rgld_.Fraud.Helpers.Messages;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.text.MessageFormat;

public class Configuration {

    private final Fraud fraud;
    private final File file;
    private YamlConfiguration fileConfig;

    public Configuration(Fraud fraud) throws Throwable {
        this.fraud = fraud;
        this.file = new File(fraud.getDataFolder(), "config.yml");
        loadConfig();
    }

    public void loadConfig() throws Throwable {
        if(!file.exists()) {
            file.createNewFile();
            fraud.saveResource(this.file.getName(), true);
        }
        this.fileConfig = new YamlConfiguration();
        this.fileConfig.load(this.file);
        initializeMessages();
    }

    private String getString(String path) {
        return fileConfig.getString(path);
    }

    private Boolean getBoolean(String path) {
        return fileConfig.getBoolean(path);
    }

    //\\ ---------------------------------------------------------------------------- //\\

    public int getDoubleAccountLimit() {
        int i = fileConfig.getInt("alts limit", 2); // private int getInt(String path) { return fileConfig.getInt(path, 2); }
        return i == -1 ? Integer.MAX_VALUE: i;
    }

    public boolean checkForUpdate() {
        return getBoolean("update.check for update");
    }

    public boolean autoDownloadLatest() {
        return getBoolean("update.auto download");
    }

    public boolean askForReviews() {
        return getBoolean("ask for review");
    }

    public boolean alertOnJoinIsEnabled() {
        return getBoolean("onJoin alert");
    }

    private void initializeMessages() {
        for(Messages message : Messages.values()) {
            if(!message.isEditable()) continue;
            if(getString("messages." + message.getConfig()) == null || getString("messages." + message.getConfig()).equals("null")) {
                System.err.println("An error occur during the initialisation of a message:");
                System.err.println("getString(\"messages." + message.getConfig() + "\") return " + getString("messages." + message.getConfig()));
                continue;
            }
            message.setMessage(ChatColor.translateAlternateColorCodes('&', getString("messages." + message.getConfig())));
        }
    }

    public KickSection getKick() {
        return new KickSection(fileConfig.getConfigurationSection("kick"), this);
    }

    public boolean isKickEnabled() {
        return getKick().isEnabled();
    }

    public static class KickSection {
        private final ConfigurationSection kick;
        private final Configuration configuration;

        KickSection(ConfigurationSection kick, Configuration configuration) {
            this.kick = kick;
            this.configuration = configuration;
        }

        public String getReason(int accounts) {
            return MessageFormat.format(kick.getString("kick reason"), accounts);
        }

        public boolean isEnabled() {
            return kick.getBoolean("enabled");
        }

        public int getMaxAccounts() {
            return configuration.getDoubleAccountLimit();
        }
    }
}
