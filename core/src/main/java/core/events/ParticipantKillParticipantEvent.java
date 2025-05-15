package core.events;

import core.Participant;

import javax.annotation.Nonnull;

public class ParticipantKillParticipantEvent extends ParticipantEvent {
    private final Participant victim;

    public ParticipantKillParticipantEvent(@Nonnull Participant killer, @Nonnull Participant victim) {
        super(killer);
        this.victim = victim;
    }

    public Participant getVictim() {
        return victim;
    }
}
