package fr.Rgld_.Fraud.Spigot.Events.Custom;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

/**
 * If "onJoin alert" is set to false into the configuration file, this event will never be called.
 */
public class DoubleAccountJoinEvent extends PlayerJoinEvent {

    private final List<String> altsList;
    private boolean alert = true;

    /**
     * Triggered when a player join the server with some alts.
     *
     * @param who the player who join here.
     * @param joinMessage the join message
     * @param altsList is the alts list of the player.
     */
    public DoubleAccountJoinEvent(Player who, String joinMessage, List<String> altsList) {
        super(who, joinMessage);
        this.altsList = altsList;
    }

    /**
     *
     * @return the alts list of the player.
     */
    public List<String> getAltsList() {
        return altsList;
    }

    /**
     * @return true if the alerts are enabled on this case.
     */
    public boolean alert() {
        return alert;
    }

    /**
     * @param alert a {@link Boolean}. Enable or disable the alert for this case.
     */
    public void setAlert(boolean alert) {
        this.alert = alert;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    private static final HandlerList handlers = new HandlerList();
}
