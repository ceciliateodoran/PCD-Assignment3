package actor;

import actor.utils.Body;
import akka.actor.typed.ActorRef;

public class ResponseBody implements BodyMsg {
    private Body body;
    private ActorRef<BodyMsg> bodyActorRef;

    public ResponseBody(final Body b, final ActorRef<BodyMsg> ref) {
        this.body = b;
        this.bodyActorRef = ref;
    }

    public Body getBody() {
        return body;
    }

    public ActorRef<BodyMsg> getBodyActorRef() {
        return bodyActorRef;
    }
}
