package fr.Rgld_.Fraud.Spigot.Helpers;

import org.bukkit.ChatColor;

import java.text.MessageFormat;

/**
 * Enum of the different configurable or not messages of the plugin.
 */
public enum Messages {

    // Configurable messages.
    ALL_ALTS_ASKED("all alts asked", true),
    ALL_ALTS_ASKED_ANNOUNCER("all alts announcer", true),
    ALL_EMPTY("all alts empty", true),
    ALTS_ASKED("alts asked", true),
    ALTS_DETECTED("alts detected", true),
    BAD_COUNTRY_DETECTED("bad country detected", true),
    HELP_COMMAND_ALL("help.command.all", true),
    HELP_COMMAND_CHECK("help.command.check", true),
    HELP_COMMAND_CONTACT("help.command.contact", true),
    HELP_COMMAND_DOWNLOAD("help.command.download", true),
    HELP_COMMAND_LINK("help.command.link", true),
    HELP_COMMAND_FORGOT("help.command.forgot", true),
    HELP_COMMAND_GEOIP("help.command.geoip", true),
    HELP_COMMAND_RELOAD("help.command.reload", true),
    HELP_COMMAND_VERSION("help.command.version", true),
    HELP_COMMAND_INFO("help.command.info", true),
    HELP_COMMAND_ALERT("help.command.alert", true),
    NO_ALTS("no alts", true),
    NO_PERMISSION("no permission", true),
    NOT_IN_DATAS("not in datas", true),
    PLAYER_FORGOTTEN("player forgotten", true),
    RELOAD_SUCCESS("reload.success", true),
    RELOAD_FAILED("reload.failed", true),

    ALERT_ON("alert.on_", true),
    ALERT_OFF("alert.off_", true),

    INFO_HEADER("info.header", true),
    INFO_HEADER_IP("info.header_ip", true),
    INFO_PLAYER("info.player", true),
    INFO_HOVER("info.hover", true),
    INFO_WAIT_FOR_THE_OTHER_PART("info.wait_for_the_other_part", true),
    INFO_IP_INFORMATION("info.ip_information", true),
    INFO_IP_continent("info.continent", true),
    INFO_IP_country("info.country", true),
    INFO_IP_sub_division("info.sub-division", true),
    INFO_IP_postal_code("info.postal-code", true),
    INFO_IP_city("info.city", true),
    INFO_IP_coordinates("info.coordinates", true),
    INFO_IP_coordinates_click("info.coordinates_click", true),
    INFO_IP_others("info.others", true),
    INFO_IP_no_information("info.no information", true),

    NOT_VALID_IP("not valid ip", true),

    // Time Messages,
    YEAR("time.year", true), YEARS("time.years", true),
    MONTH("time.month", true), MONTHS("time.months", true),
    DAY("time.day", true), DAYS("time.days", true),
    HOUR("time.hour", true), HOURS("time.hours", true),
    MINUTE("time.minute", true), MINUTES("time.minutes", true),
    SECOND("time.second", true), SECONDS("time.seconds", true),
    NOW("time.now", true),
    AND("time.and", true),

    // final messages.,
    PREFIX(" &6&lFraud &7» &e", false),
    COMMAND_CLEAN_DATA_YES("§6The datas has been reset.", false),
    COMMAND_CLEAN_DATA_NO("§cAn error occur during the reset of datas. Please call an administrator or the developer of this plugin via /{0} contact.", false),
    ;

    public static final String defaultMessage = "§6§l§nPlease contact the developer to report this bug: /fraud contact !";

    private String getNotConfigurableDefaultMessage() {
        return "not editable";
    }
    private String message;

    private final String config;

    Messages(String value, boolean isConfigurable) {
        if(isConfigurable) {
            this.message = defaultMessage + ChatColor.GRAY + ChatColor.ITALIC + "(" + this + ")";
            this.config = value;
        } else {
            this.message = value;
            this.config = getNotConfigurableDefaultMessage();
        }
    }

    /**
     * @return true if the message is editable, false otherwise.
     */
    public boolean isEditable() {
        return !getConfig().equals(getNotConfigurableDefaultMessage());
    }

    public String getConfig() {
        return config;
    }

    public String getMessage() {
        return format("");
    }

    public String format(Object... values) {
        return MessageFormat.format(message, values);
    }

    public void setMessage(String message) {
        String out;
        if (this == PREFIX
                || String.valueOf(this).startsWith("HELP_COMMAND_")
                || this == ALL_ALTS_ASKED
                || this.getConfig().startsWith("time.")
                || this.getConfig().startsWith("info.")) {
            out = message;
        } else {
            out = Messages.PREFIX.getMessage() + message;
        }
        this.message = ChatColor.translateAlternateColorCodes('&', out);
    }
}
