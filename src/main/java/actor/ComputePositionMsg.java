package actor;

import akka.actor.typed.ActorRef;

/**
 * Msg che invia il Controller per la nuova iterazione
 */
public class ComputePositionMsg implements BodyMsg {

    private final ActorRef<PositionCalculationMsg> posCalcActorRef;

    private final ActorRef<VelocityCalculationMsg> velCalcActorRef;

    /* virtual time step */
    private final double dt;

    public ComputePositionMsg(final ActorRef<PositionCalculationMsg> posCalcActor,
                              final ActorRef<VelocityCalculationMsg> velCalcActor, final double dt) {
        this.posCalcActorRef = posCalcActor;
        this.velCalcActorRef = velCalcActor;
        this.dt = dt;
    }

    public ActorRef<PositionCalculationMsg> getPosCalcActorRef() {
        return this.posCalcActorRef;
    }

    public ActorRef<VelocityCalculationMsg> getVelCalcActorRef() {
        return this.velCalcActorRef;
    }

    public double getDt() {
        return this.dt;
    }
}
