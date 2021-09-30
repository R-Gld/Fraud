package fr.Rgld_.Fraud.Events.Custom;

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

    public DoubleAccountJoinEvent(Player who, String joinMessage, List<String> altsList) {
        super(who, joinMessage);
        this.altsList = altsList;
    }

    public List<String> getAltsList() {
        return altsList;
    }

    public boolean alert() {
        return alert;
    }

    public void setAlert(boolean alert) {
        this.alert = alert;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    private static final HandlerList handlers = new HandlerList();
}
