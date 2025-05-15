package core.events;

import org.bukkit.event.Cancellable;

import javax.annotation.Nonnull;

import core.Participant;

public class ParticipantInteractParticipantEvent extends ParticipantEvent implements Cancellable {
    private boolean cancelled;
    private final Participant target;

    public ParticipantInteractParticipantEvent(@Nonnull Participant participant, @Nonnull Participant target) {
        super(participant);
        this.target = target;
    }

    public Participant getTarget() {
        return target;
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
