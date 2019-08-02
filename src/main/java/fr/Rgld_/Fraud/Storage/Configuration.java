package fr.Rgld_.Fraud.Storage;

import fr.Rgld_.Fraud.Fraud;
import fr.Rgld_.Fraud.Helpers.Messages;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Configuration {

    private final Fraud fraud;
    private final File file;
    private YamlConfiguration fileConfig;

    public Configuration(Fraud fraud) throws Throwable {
        this.fraud = fraud;
        this.file = new File(fraud.getDataFolder(), "config.yml");
        loadConfig();
    }

    public void reload() throws Throwable {
        loadConfig();
    }

    private void loadConfig() throws Throwable {
        if (!file.exists()) {
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

    private int getInt(String path) {
        return fileConfig.getInt(path, 2);
    }

    //\\ ---------------------------------------------------------------------------- //\\

    public int getDoubleAccountLimit() {
        int i = getInt("alts limit");
        if (i == -1) i = Integer.MAX_VALUE;
        return i;
    }

    public boolean checkForUpdate() {
        return getBoolean("check for update");
    }

    public boolean alertOnJoinIsEnabled() {
        return getBoolean("onJoin alert");
    }

    private void initializeMessages() {
        i18n(Messages.PREFIX, "prefix");
        i18n(Messages.NO_PERMISSION, "no permission");
        i18n(Messages.HELP_COMMAND_CHECK, "help.command.check");
        i18n(Messages.HELP_COMMAND_ALL, "help.command.all");
        i18n(Messages.HELP_COMMAND_VERSION, "help.command.version");
        i18n(Messages.HELP_COMMAND_RELOAD, "help.command.reload");
        i18n(Messages.HELP_COMMAND_CONTACT, "help.command.contact");
        i18n(Messages.HELP_COMMAND_CLEAN_DATAS, "help.command.clean datas");
        i18n(Messages.RELOAD_SUCCESS, "reload.success");
        i18n(Messages.RELOAD_FAILED, "reload.failed");
        i18n(Messages.ALTS_DETECTED, "alts detected");
        i18n(Messages.ALTS_ASKED, "alts asked");
        i18n(Messages.ALL_ALTS_ASKED_ANNOUNCER, "all alts announcer");
        i18n(Messages.ALL_ALTS_ASKED, "all alts asked");
        i18n(Messages.ALL_EMPTY, "all alts empty");
        i18n(Messages.NO_ALTS, "no alts");
    }

    private void i18n(Messages msg, String section) {
        msg.setMessage(ChatColor.translateAlternateColorCodes('&', getString("messages." + section)));
    }
}
