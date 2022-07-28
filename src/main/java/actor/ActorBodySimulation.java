package actor;

import akka.actor.typed.ActorSystem;

public class ActorBodySimulation {

    public static void main(String[] args) {

        int totBodies = 100;
        int maxIter = 100;

        ActorSystem.create(ControllerActor.create(totBodies, maxIter), "controllerActor");

    }
}
