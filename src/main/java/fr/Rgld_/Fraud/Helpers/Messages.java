package fr.Rgld_.Fraud.Helpers;

import org.bukkit.ChatColor;

public enum Messages {

    // Configurable message.

    PREFIX(Messages.defaultMessage),
    NO_PERMISSION(Messages.defaultMessage),

    HELP_COMMAND_CHECK(Messages.defaultMessage),
    HELP_COMMAND_VERSION(Messages.defaultMessage),
    HELP_COMMAND_RELOAD(Messages.defaultMessage),
    HELP_COMMAND_CONTACT(Messages.defaultMessage),
    HELP_COMMAND_CLEAN_DATAS(Messages.defaultMessage),
    HELP_COMMAND_ALL(Messages.defaultMessage),

    ALTS_DETECTED(Messages.defaultMessage),
    ALTS_ASKED(Messages.defaultMessage),
    NO_ALTS(Messages.defaultMessage),

    ALL_ALTS_ASKED_ANNOUNCER(Messages.defaultMessage),
    ALL_ALTS_ASKED(Messages.defaultMessage),
    ALL_EMPTY(Messages.defaultMessage),

    RELOAD_SUCCESS(Messages.defaultMessage),
    RELOAD_FAILED(Messages.defaultMessage),

    // final message.

    COMMAND_CLEAN_DATA_YES("§6The datas has been reset."),
    COMMAND_CLEAN_DATA_NO("§cAn error occur during the reset of datas. Please call an administrator or the developer of this plugin via /{0} contact."),
    ;

    private static final String defaultMessage = "§6§l§nPlease contact the developer to report this bug: /fraud contact !";

    private String message;

    Messages(String message) {
        this.message = message;
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
}
