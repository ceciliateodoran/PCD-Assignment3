package actor;

import actor.utils.Body;
import akka.actor.typed.ActorRef;

import java.util.List;

/**
 * Msg che invia il Body con le nuove posizioni di corpi
 */
public class PositionsMsg implements ControllerMsg {
    private List<Body> bodies;
    private ActorRef<BodyMsg> replyTo;

    private double dt;

    public PositionsMsg(ActorRef<BodyMsg> replyTo, List<Body> allBodies, double dt) {
        this.replyTo = replyTo;
        this.bodies = allBodies;
        this.dt = dt;
    }

    /* Costruttore da usare nella GUI per lo start button:
    *   quando verrà premuto start, bisognerà inviare un messaggio dalla GUI
    *   al ControllerActor usando questo costruttore in modo tale da indicare
    *   all'attore di iniziare a comunicare con il BodyActor
    *  */
    public PositionsMsg(ActorRef replyTo) {

    }

    public List<Body> getBodies(){
        return this.bodies;
    }

    public ActorRef getReplyTo() {
        return this.replyTo;
    }

    public double getDt() {
        return this.dt;
    }
}
