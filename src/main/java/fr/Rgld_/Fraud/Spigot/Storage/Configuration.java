package fr.Rgld_.Fraud.Spigot.Storage;

import fr.Rgld_.Fraud.Spigot.Fraud;
import fr.Rgld_.Fraud.Spigot.Helpers.Exceptions.IllegalConfigurationException;
import fr.Rgld_.Fraud.Spigot.Helpers.Messages;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

/**
 * Class used to configure the plugin. (See <a href="https://github.com/R-Gld/Fraud/blob/master/src/main/resources/config.yml" target="_blank">sp-config.yml</a>)
 */
public class Configuration {

    private final Fraud fraud;
    private final File file;
    private YamlConfiguration fileConfig;

    public MessageManager getMessageManager() {
        return messageManager;
    }

    private MessageManager messageManager;

    public Configuration(Fraud fraud) throws IOException, InvalidConfigurationException {
        this.fraud = fraud;
        this.file = new File(fraud.getDataFolder(), "config.yml");
        loadConfig();
    }

    /**
     * Load the config inside the {@link Configuration#fileConfig} so it can be read.
     *
     * @throws IOException if an I/O exception occur.
     * @throws InvalidConfigurationException if the configuration is not valid.
     */
    public void loadConfig() throws IOException, InvalidConfigurationException {
        fraud.getDataFolder().mkdirs();
        if(!file.exists()) {
            String spCfg = "sp-config.yml";
            File spConfig = new File(fraud.getDataFolder(), spCfg);
            spConfig.createNewFile();
            fraud.saveResource(spCfg, true);
            spConfig.renameTo(this.file);
        }
        this.fileConfig = new YamlConfiguration();
        this.fileConfig.load(this.file);
        checkConfiguration();
        initializeMessages();
    }

    /**
     * @param path yaml path on the config (@see <a href="https://github.com/R-Gld/Fraud/blob/master/src/main/resources/config.yml" target="_blank">sp-config.yml</a>)
     * @return the {@link String} associated to the key given in parameter.
     */
    private String getString(String path) {
        return fileConfig.getString(path);
    }

    /**
     * @param path yaml path on the config (@see <a href="https://github.com/R-Gld/Fraud/blob/master/src/main/resources/config.yml" target="_blank">sp-config.yml</a>)
     * @return the {@link Boolean} associated to the key given in parameter.
     */
    private Boolean getBoolean(String path) {
        return fileConfig.getBoolean(path);
    }


    //\\ ---------------------------------------------------------------------------- //\\

    /**
     * @return the alts limit settled up on the config.
     */
    public int getDoubleAccountLimit() {
        int i = fileConfig.getInt("alts limit", 2);
        return i <= -1 ? Integer.MAX_VALUE: i;
    }

    private void checkConfiguration() {
        if(getDoubleAccountLimit() == 0 || getDoubleAccountLimit() == 1)
            System.err.println("You shouldn't set the alt limit to " + getDoubleAccountLimit() + ", no one will can join except the players with the permission \"fraud.bypass.ip\".\n If you want to disable the limit, set it to -1.");
        if(!checkForUpdate())
            System.err.println("You shouldn't disable the check for update, you'll no be longer notified if a new version is released.");

    }

    /**
     * @return the countries alert settled up on the config.
     */
    public List<String> getCountriesAlert() {
        List<?> list = fileConfig.getList("countries alert");
        for(Object obj : list) {
            if(!(obj instanceof String)) {
                throw new IllegalArgumentException("The countries given in configuration must be surrounded by two \".\nExample: \"FR\"");
            }
        }
        return Collections.unmodifiableList((List<String>) list);
    }

    /**
     * @return true if the Geoip is enable, false, otherwise
     */
    public boolean isGeoIPAPIActivated() {
        return fileConfig.getBoolean("geoip-enable", true);
    }

    /**
     * @return the {@link Boolean} settled up on the config for the key "check for update".
     */
    public boolean checkForUpdate() {
        return getBoolean("update.check for update");
    }

    /**
     * @return the {@link Boolean} settled up on the config for the key "auto download".
     */
    public boolean autoDownloadLatest() {
        return getBoolean("update.auto download");
    }

    /**
     * @return the {@link Boolean} settled up on the config for the key "ask for review".
     */
    public boolean askForReviews() {
        return getBoolean("ask for review");
    }

    /**
     * @return the {@link Boolean} settled up on the config for the key "onJoin alert".
     */
    public boolean alertOnJoinIsEnabled() {
        return getBoolean("onJoin alert");
    }

    /**
     * Read all the messages settled up on the config and store them in the enum {@link Messages}.
     */
    private void initializeMessages() {
        try {
            this.messageManager = new MessageManager(fraud);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        /*for(Messages message : Messages.values()) {
            if(!message.isEditable()) continue;
            if(!checkMessages(message)) return;
            message.setMessage(ChatColor.translateAlternateColorCodes('&', getString("messages." + message.getConfig())));
        }*/
    }

    /**
     * @param message a {@link Messages} value.
     * @return true if the message given in argument is correctly configured.
     */
    private boolean checkMessages(Messages message) {
        if(getString("messages." + message.getConfig()) == null || getString("messages." + message.getConfig()).equals("null")) {
            try {
                String newName = renameFailedConfigFile();
                loadConfig();
                System.err.println("Due to the problem with the messages, " +
                        "the config file was reset and the old config file was renamed to " + newName + ". " +
                        "There can be two reasons for this, " +
                        "either a new version of the configuration file was set up with a new version of the plugin, " +
                        "or you made a mistake in configuring the plugin.");
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }

    /**
     * Rename the old config file if this one is mis-configured.
     *
     * @return the name of the old file renamed.
     */
    private String renameFailedConfigFile() {
        String newName = this.file.getName() + ".old";
        File oldFile = new File(this.file.getParentFile(), newName);
        int index = 1;
        while(oldFile.exists()) {
            newName = this.file.getName() + "-" + index + ".old";
            oldFile = new File(this.file.getParentFile(), newName);
            index++;
        }
        this.file.renameTo(oldFile);
        return newName;
    }

    public KickSection getKick() {
        return new KickSection(fileConfig.getConfigurationSection("kick"), this);
    }

    public DatabaseSection getDatabase() {
        return new DatabaseSection(fileConfig.getConfigurationSection("data store"));
    }

    public boolean isKickEnabled() {
        return getKick().isEnabled();
    }

    public static class DatabaseSection {
        private final ConfigurationSection database;

         DatabaseSection(ConfigurationSection database) {
            this.database = database;
        }

        public Type getType() {
            switch(database.getString("type").toLowerCase()) {
                case "sqlite":
                    return Type.SQLITE;
                case "mariadb":
                case "mysql":
                    return Type.MYSQL;
                default:
                    return Type.UNKNOWN;
            }
        }


        /**
         * @return the {@link String} associated to the key "host".
         */
        public String getHost() {
            checkMysqlType();
            return database.getString("parameters.ip");
        }

        /**
         * @return the {@link String} associated to the key "port".
         */
        public int getPort() {
            checkMysqlType();
            return database.getInt("parameters.port");
        }

        /**
         * @return the {@link String} associated to the key "user".
         */
        public String getUser() {
            checkMysqlType();
            return database.getString("parameters.user");
        }

        /** @return the {@link String} associated to the key "password". */
        public String getPassword() {
            checkMysqlType();
            return database.getString("parameters.password");
        }

        /** @return the {@link String} associated to the key "database". */
        public String getDatabaseName() {
            checkMysqlType();
            return database.getString("parameters.database");
        }

        private void checkMysqlType() {
            if(getType() != Type.MYSQL)
                throw new IllegalConfigurationException("The database type is not mysql or mariadb, you can't use the mysql or mariadb parameters.");
        }


        public String generateURL() {
            checkMysqlType();
            return "jdbc:mysql://" + getHost() + ":" + getPort() + "/" + getDatabaseName();
        }

        public enum Type { SQLITE, MYSQL, UNKNOWN }
    }

    public static class KickSection {
        private final ConfigurationSection kick;
        private final Configuration configuration;

        private KickSection(ConfigurationSection kick, Configuration configuration) {
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
