package actor;

import akka.actor.typed.ActorSystem;

public class ActorBodySimulation {

    public static void main(String[] args) {
        int width = 620;
        int height = 620;
        int totBodies = 5000;
        int maxIter = 1000;
        ActorSystem.create(ControllerActor.create(totBodies, maxIter, width, height), "controllerActor");
    }
}
