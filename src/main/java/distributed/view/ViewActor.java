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

        this.viewer = new SimulationView(620,620, context.getSelf());
        this.viewer.display();

    }

    @Override
    public Receive createReceive() {
        return null;
    }

    /* public factory to create the View actor */
    public static Behavior<ViewMsg> create() {
        return Behaviors.setup(ViewActor::new);
    }
}
