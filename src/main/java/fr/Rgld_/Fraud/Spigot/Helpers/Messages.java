package fr.Rgld_.Fraud.Spigot.Helpers;

import org.bukkit.ChatColor;

import java.text.MessageFormat;

/**
 * Enum of the different configurable or not messages of the plugin.
 */
public enum Messages {

    // Configurables messages.
    ALL_ALTS_ASKED("all alts asked", true),
    ALL_ALTS_ANNOUNCER("all alts announcer", true),
    ALL_ALTS_EMPTY("all alts empty", true),
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
    HELP_COMMAND_GUI("help.command.gui", true),
    HELP_COMMAND_RELOAD("help.command.reload", true),
    HELP_COMMAND_STATS("help.command.stats", true),
    HELP_COMMAND_VERSION("help.command.version", true),
    HELP_COMMAND_INFO("help.command.info", true),
    HELP_COMMAND_ALERT("help.command.alert", true),
    NO_ALTS("no alts", true),
    NO_PERMISSION("no permission", true),
    NOT_IN_DATAS("not in datas", true),
    PLAYER_FORGOTTEN("player forgotten", true),
    RELOAD_SUCCESS("reload.success", true),
    RELOAD_FAILED("reload.failed", true),

    ALERT_ON_("alert.on_", true),
    ALERT_OFF_("alert.off_", true),

    INFO_HEADER("info.header", true),
    INFO_HEADER_IP("info.header_ip", true),
    INFO_PLAYER("info.player", true),
    INFO_HOVER("info.hover", true),
    INFO_WAIT_FOR_THE_OTHER_PART("info.wait_for_the_other_part", true),
    INFO_IP_INFORMATION("info.ip_information", true),
    INFO_IP_LOCATION("info.ip_location", true),
    INFO_CONTINENT("info.continent", true),
    INFO_COUNTRY("info.country", true),
    INFO_SUB_DIVISION("info.sub-division", true),
    INFO_POSTAL_CODE("info.postal-code", true),
    INFO_CITY("info.city", true),
    INFO_COORDINATES("info.coordinates", true),
    INFO_COORDINATES_CLICK("info.coordinates_click", true),
    INFO_OTHERS("info.others", true),
    INFO_NO_INFORMATION("info.no information", true),

    NOT_VALID_IP("not valid ip", true),

    // Time Messages,
    TIME_YEAR("time.year", true), TIME_YEARS("time.years", true),
    TIME_MONTH("time.month", true), TIME_MONTHS("time.months", true),
    TIME_DAY("time.day", true), TIME_DAYS("time.days", true),
    TIME_HOUR("time.hour", true), TIME_HOURS("time.hours", true),
    TIME_MINUTE("time.minute", true), TIME_MINUTES("time.minutes", true),
    TIME_SECOND("time.second", true), TIME_SECONDS("time.seconds", true),
    TIME_NOW("time.now", true),
    TIME_AND("time.and", true),

    // Menu Messages // TODO
    GUI_GENERAL_CLOSE("gui.general.close", true),
    GUI_GENERAL_BACK("gui.general.back", true),

    GUI_MAIN_ALTS("gui.main.alts", true),
    GUI_MAIN_INFOS("gui.main.infos", true),

    GUI_ALTS_IP("gui.alts.ip", true),

    GUI_DETAIL_IP_DETAILS("gui.detail.ip details", true),
    GUI_DETAIL_GET_MORE_INFO("gui.detail.get more info", true),
    GUI_DETAIL_MORE_ALTS("gui.detail.more alts", true),
    GUI_DETAIL_NO_ALTS("gui.detail.no alts", true),
    GUI_DETAIL_NO_INFORMATION("gui.detail.no information", true),

    GUI_DETAIL_FIRST_CONNECTION("gui.detail.first connection", true),
    GUI_DETAIL_LAST_CONNECTION("gui.detail.last connection", true),

    GUI_DETAIL_GEO_CONTINENT("gui.detail.geo continent", true),
    GUI_DETAIL_GEO_COUNTRY("gui.detail.geo country", true),
    GUI_DETAIL_GEO_SUB_DIVISION("gui.detail.geo sub-division", true),
    GUI_DETAIL_GEO_POSTAL_CODE("gui.detail.geo postal-code", true),
    GUI_DETAIL_GEO_CITY("gui.detail.geo city", true),
    GUI_DETAIL_GEO_COORDINATES("gui.detail.geo coordinates", true),
    GUI_DETAIL_OTHERS("gui.detail.others", true),

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

    /**
     * Constructor of the enum.
     *
     * @param value the message to display
     * @param isConfigurable true if the message is configurable, false otherwise
     */
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
                || this.getConfig().startsWith("gui.")
                || this.getConfig().startsWith("info.")) {
            out = message;
        } else {
            out = Messages.PREFIX.getMessage() + message;
        }
        this.message = ChatColor.translateAlternateColorCodes('&', out);
    }
}
