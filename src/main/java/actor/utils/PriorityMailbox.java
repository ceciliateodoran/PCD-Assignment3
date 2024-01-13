package actor.utils;

import actor.message.ViewStopMsg;
import akka.actor.ActorSystem;
import akka.dispatch.PriorityGenerator;
import akka.dispatch.UnboundedStablePriorityMailbox;
import com.typesafe.config.Config;

import java.util.Comparator;

public class PriorityMailbox extends UnboundedStablePriorityMailbox{
    public PriorityMailbox(ActorSystem.Settings settings, Config config) {
        super(new PriorityGenerator() {
            @Override
            public int gen(Object message) {
                if (message instanceof ViewStopMsg)
                    return 0;
                else
                    return 1;
            }
        });
    }
}
