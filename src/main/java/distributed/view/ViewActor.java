package distributed.view;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class ViewActor extends AbstractBehavior<ViewMsg> {

    private SimulationView viewer;

    public ViewActor(final ActorContext<ViewMsg> context) {
        super(context);

        this.viewer = new SimulationView(2, 4, 800,800, context.getSelf());
        this.viewer.display();
    }

    @Override
    public Receive createReceive() {
        return newReceiveBuilder()
                .onMessage(ViewUnderMngmtMsg.class, this::onUnderMngmt)
                .onMessage(ViewEndMngmtMsg.class, this::onEndMngmt)
                .build();
    }

    private Behavior<ViewMsg> onUnderMngmt(final ViewUnderMngmtMsg msg) {
        this.getContext().getLog().info("ViewActor: received under management event from GUI.");

        return this;
    }

    private Behavior<ViewMsg> onEndMngmt(final ViewEndMngmtMsg msg) {
        this.getContext().getLog().info("ViewActor: received end management event from GUI.");

        return this;
    }

    /* public factory to create the View actor */
    public static Behavior<ViewMsg> create() {
        return Behaviors.setup(ViewActor::new);
    }
}
