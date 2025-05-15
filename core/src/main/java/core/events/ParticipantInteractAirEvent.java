package core.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;

import javax.annotation.Nonnull;

import core.Participant;

public class ParticipantInteractAirEvent extends ParticipantEvent implements Cancellable {
    private boolean cancelled;
    private final Action action;

    public ParticipantInteractAirEvent(@Nonnull Participant participant, Action action) {
        super(participant);
        this.action = action;
    }
    
    public Action getAction() {
        return action;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
