package distributed.model;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.pubsub.Topic;
import distributed.messages.*;

public class Subscriber extends AbstractBehavior<BarrackStatus> {

    private static ActorRef<Topic.Command<BarrackStatus>> topic;

    public Subscriber(final ActorContext<BarrackStatus> context) {
        super(context);
    }

    public static Behavior<BarrackStatus> create() {
        return Behaviors.setup(context -> {
            topic = context.spawn(Topic.create(BarrackStatus.class, "my-topic"), "MyTopic");
            topic.tell(Topic.subscribe(context.getSelf()));

            return new Subscriber(context);
        });
    }

    @Override
    public Receive<BarrackStatus> createReceive() {
        return newReceiveBuilder()
                .onMessage(BarrackStatus.class, this::onPublish)
                .build();
    }

    public Behavior<BarrackStatus> onPublish(final BarrackStatus msg) {
        System.out.println("Publish message received");
        return Behaviors.same();
    }
}
