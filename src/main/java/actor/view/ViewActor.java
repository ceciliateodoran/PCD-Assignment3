package actor.view;

import actor.ControllerMsg;
import actor.ControllerStopMsg;
import actor.UpdatedPositionsMsg;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class ViewActor extends AbstractBehavior<ViewMsg> {

    private static int height;

    private static int width;

    private boolean state;

    private static ActorRef<ControllerMsg> controllerActorRef;

    private SimulationView viewer;

    public ViewActor(final ActorContext<ViewMsg> context) {
        super(context);
        this.viewer = new SimulationView(width,height, context.getSelf());
        this.viewer.display();
        this.state = false;
    }

    @Override
    public Receive createReceive() {
        return newReceiveBuilder()
                .onMessage(ViewStartMsg.class, this::onStart)
                .onMessage(ViewStopMsg.class, this::onStop)
                .onMessage(UpdatedPositionsMsg.class, this::onNewBodies)
                .onMessage(ControllerStopMsg.class, this::onEndIterations)
                .build();
    }

    // msg mandato da Controller quando sono finite le iterazioni
    private Behavior<ViewMsg> onEndIterations(ControllerStopMsg msg) {
        this.viewer.updateState("Stopped");
        return this;
    }

    // msg mandato da Controller con bodies aggiornati
    private Behavior<ViewMsg> onNewBodies(final UpdatedPositionsMsg msg) {
        if (state) {
            this.viewer.updateView(msg.getBodies(), msg.getVt(), msg.getIter(), msg.getBounds());
            controllerActorRef.tell(new ViewUpdatedMsg());
        }
        return this;
    }

    // msg che indica che è stato premuto il bottone start
    private Behavior<ViewMsg> onStart(final ViewStartMsg msg) {
        //this.getContext().getLog().info("ViewActor: received start event from GUI.");
        controllerActorRef.tell(msg);
        this.state = true;
        return this;
    }

    // msg che indica che è stato premuto il bottone stop
    private Behavior<ViewMsg> onStop(final ViewStopMsg msg) {
        //this.getContext().getLog().info("ViewActor: received stop event from GUI.");
        controllerActorRef.tell(msg);
        this.state = false;
        return this;
    }

    /* public factory to create the actor */
    public static Behavior<ViewMsg> create(final ActorRef<ControllerMsg> actorRef, final int w, final int h) {
        controllerActorRef = actorRef;
        width = w;
        height = h;
        return Behaviors.setup(ViewActor::new);
    }
}
