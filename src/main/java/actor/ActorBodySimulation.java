package actor;

import actor.message.ControllerMsg;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.MailboxSelector;
import akka.actor.typed.javadsl.Behaviors;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ActorBodySimulation {

    public static void main(String[] args) {
        int width = 620;
        int height = 620;
        int totBodies = 500;
        int maxIter = 50000;

        ActorSystem.create(
            Behaviors.setup(
                (ctx) -> {
                    ctx.spawn(ControllerActor.create(totBodies, maxIter, width, height),
                    "controllerActor",
                    MailboxSelector.fromConfig("my-app.priority-mailbox"));
                    return Behaviors.same();
                }
            ), "system", ConfigFactory.load());
    }
}
