package distributed.model;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.japi.Pair;
import distributed.messages.ValueMsg;

public class BarrackServer extends AbstractBehavior<ValueMsg> {



    public BarrackServer(final ActorContext<ValueMsg> context) {
        super(context);
    }

    public static Behavior<ValueMsg> create(final int zoneNumber) {
        return Behaviors.setup(context -> {
            ActorRef<ValueMsg> userRegistryActor =
                    context.spawn(Barrack.create(zoneNumber), "UserRegistry");

            BarrackRoutes userRoutes = new BarrackRoutes(context.getSystem(), userRegistryActor);
            startHttpServer(userRoutes.userRoutes(), context.getSystem());

            return Behaviors.empty();
        });
    }

    @Override
    public Receive<ValueMsg> createReceive() {
        return null;
    }
}
