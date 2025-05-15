package core.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

import core.Participant;

public abstract class ParticipantEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Participant participant;

    public ParticipantEvent(@Nonnull Participant participant) {
        this.participant = participant;
    }

    public ParticipantEvent(@Nonnull Participant participant, boolean async) {
        super(async);
        this.participant = participant;
    }

    @Nonnull
    public Participant getParticipant() {
        return participant;
    }

    @Nonnull
    public ItemStack getActiveItem() {
        return participant.getPlayer().getInventory().getItemInMainHand();
    }

    @Override
    @Nonnull
    public HandlerList getHandlers() {
        return handlers;
    }

    @Nonnull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
