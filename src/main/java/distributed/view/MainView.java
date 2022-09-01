package distributed.view;

import akka.actor.typed.ActorSystem;

public class MainView {
    public static void main(String[] args) {
        ActorSystem.create(ViewActor.create(), "viewActor");
    }
}
