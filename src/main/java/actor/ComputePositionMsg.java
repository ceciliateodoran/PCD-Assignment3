package actor;

import akka.actor.typed.ActorRef;

/**
 * Msg che invia il Controller per la nuova iterazione
 */
public class ComputePositionMsg implements BodyMsg {

    private ActorRef posCalcActorRef;

    private ActorRef velCalcActorRef;

    /* virtual time step */
    private final double dt;

    public ComputePositionMsg(ActorRef replyTo, ActorRef posCalcActor, ActorRef velCalcActor, double dt) {
        this.posCalcActorRef = posCalcActor;
        this.velCalcActorRef = velCalcActor;
        this.dt = dt;
    }

    public ActorRef getPosCalcActorRef() {
        return this.posCalcActorRef;
    }

    public ActorRef getVelCalcActorRef() {
        return this.velCalcActorRef;
    }

    public double getDt() {
        return this.dt;
    }
}
