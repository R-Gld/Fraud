package fr.Rgld_.Fraud.Helpers;

import org.bukkit.ChatColor;

import java.text.MessageFormat;

public enum Messages {

    // Configurable message.
    ALL_ALTS_ASKED("all alts asked", true),
    ALL_ALTS_ASKED_ANNOUNCER("all alts announcer", true),
    ALL_EMPTY("all alts empty", true),
    ALTS_ASKED("alts asked", true),
    ALTS_DETECTED("alts detected", true),
    HELP_COMMAND_ALL("help.command.all", true),
    HELP_COMMAND_CHECK("help.command.check", true),
    HELP_COMMAND_CLEAN_DATAS("help.command.clean datas", true),
    HELP_COMMAND_CONTACT("help.command.contact", true),
    HELP_COMMAND_DOWNLOAD("help.command.download", true),
    HELP_COMMAND_FORGOT("help.command.forgot", true),
    HELP_COMMAND_RELOAD("help.command.reload", true),
    HELP_COMMAND_VERSION("help.command.version", true),
    NO_ALTS("no alts", true),
    NO_PERMISSION("no permission", true),
    NOT_IN_DATAS("not in datas", true),
    PLAYER_FORGOTTEN("player forgotten", true),
    PREFIX("prefix", true),
    RELOAD_SUCCESS("reload.success", true),
    RELOAD_FAILED("reload.failed", true),

    // final message.
    COMMAND_CLEAN_DATA_YES("§6The datas has been reset.", false),
    COMMAND_CLEAN_DATA_NO("§cAn error occur during the reset of datas. Please call an administrator or the developer of this plugin via /{0} contact.", false),
    ;

    private static final String defaultMessage = "§6§l§nPlease contact the developer to report this bug: /fraud contact !";
    private String getNotConfigurableDefaultMessage() {
        return "not editable";
    }
    private String message;

    private final String config;

    Messages(String value, boolean inConfig) {
        if(inConfig) {
            this.message = defaultMessage + ChatColor.GRAY + ChatColor.ITALIC + "(" + this.toString() + ")";
            this.config = value;
        } else {
            this.message = value;
            this.config = getNotConfigurableDefaultMessage();
        }
    }

    public boolean isEditable() {
        return !getConfig().equals(getNotConfigurableDefaultMessage());
    }

    public String getConfig() {
        return config;
    }

    public String getMessage() {
        String out;
        if (this == PREFIX || String.valueOf(this).startsWith("HELP_COMMAND_") || this == ALL_ALTS_ASKED) {
            out = message;
        } else {
            out = Messages.PREFIX.getMessage() + message;
        }
        return ChatColor.translateAlternateColorCodes('&', out);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String format(Object... values) {
        return MessageFormat.format(this.getMessage(), values);
    }
}
