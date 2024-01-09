package actor;

import actor.message.*;
import actor.view.SimulationView;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

/**
 * Represent the View actor implementation
 */
public class ViewActor extends AbstractBehavior<ViewMsg> {

    private static int height;
    private static int width;
    private boolean isRunning;
    private static ActorRef<ControllerMsg> controllerActorRef;
    private final SimulationView view;

    private ViewActor(final ActorContext<ViewMsg> context) {
        super(context);
        this.view = new SimulationView(width,height, context.getSelf());
        this.view.display();
        this.isRunning = false;
    }

    /**
     * Construct a new instance of the View actor and GUI
     *
     * @param h The height of the user interface
     * @param w The width of the user interface
     * @return The newly created instance of the View actor
     */
    public static Behavior<ViewMsg> create(final ActorRef<ControllerMsg> actorRef, final int w, final int h) {
        controllerActorRef = actorRef;
        width = w;
        height = h;
        return Behaviors.setup(ViewActor::new);
    }

    @Override
    public Receive<ViewMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(ViewStartMsg.class, this::onStart)
                .onMessage(ViewStopMsg.class, this::onStop)
                .onMessage(UpdatedPositionsMsg.class, this::onNewBodies)
                .onMessage(ControllerStopMsg.class, this::onEndIterations)
                .build();
    }

    //GUI update at the end of iterations
    private Behavior<ViewMsg> onEndIterations(ControllerStopMsg msg) {
        this.view.updateState("Stopped");
        controllerActorRef.tell(new ViewStopMsg());
        this.isRunning = false;
        return this;
    }

    //update Bodies in GUI during the simulation
    private Behavior<ViewMsg> onNewBodies(final UpdatedPositionsMsg msg) {
        if (isRunning) {
            this.view.updateView(msg.getBodies(), msg.getVt(), msg.getIter(), msg.getBounds());
            controllerActorRef.tell(new IterationCompleted());
        }
        return this;
    }

    //management of Start event
    private Behavior<ViewMsg> onStart(final ViewStartMsg msg) {
        //this.getContext().getLog().info("ViewActor: received start event from GUI.");
        controllerActorRef.tell(msg);
        this.isRunning = true;
        return this;
    }

    //management of Stop event
    private Behavior<ViewMsg> onStop(final ViewStopMsg msg) {
        //this.getContext().getLog().info("ViewActor: received stop event from GUI.");
        controllerActorRef.tell(msg);
        this.isRunning = false;
        return this;
    }
}