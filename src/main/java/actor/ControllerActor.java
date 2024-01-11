package actor;

import actor.message.*;
import actor.message.test.DistributedTestResult;
import actor.message.test.FakeIterationCompleted;
import actor.message.test.FakeUpdatePositionMsg;
import actor.message.test.StartTest;
import actor.utils.Body;
import actor.utils.BodyGenerator;
import actor.utils.Boundary;
import actor.utils.TestActor;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import distributed.messages.ValueMsg;

import java.util.*;

/**
 * Represent the Controller actor implementation,
 * Create each BodyActor and the ViewActor,
 * Intermediary actor between Bodies actors and the ViewActor
 */
public class ControllerActor extends AbstractBehavior<ControllerMsg> {
    private static int totBodies;
    private static int maxIter;
    private static int viewHeight;
    private static int viewWidth;
    private int currentIter;
    private double vt;
    private final double dt;
    private List<ActorRef<BodyMsg>> bodyActorRefList;
    private ActorRef<ViewMsg> viewActorRef;
    private final Boundary bounds;
    private List<Body> bodies;
    private boolean testMode;
    private ActorRef<ValueMsg> testRef;
    private ActorRef<ValueMsg> testActor;

    private ControllerActor(final ActorContext<ControllerMsg> context) {
        super(context);
        this.dt = 0.001;
        this.bounds =  new Boundary(-6.0, -6.0, 6.0, 6.0);
        this.viewActorRef = context.spawn(ViewActor.create(context.getSelf(), viewWidth, viewHeight), "viewActor");
        this.testMode = false;
    }

    private ControllerActor(final ActorContext<ControllerMsg> context, final boolean test) {
        super(context);
        this.dt = 0.001;
        this.bounds =  new Boundary(-6.0, -6.0, 6.0, 6.0);
        this.testMode = test;
    }

    @Override
    public Receive<ControllerMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(BodyComputationResultMsg.class, this::onBodyReceived)
                .onMessage(IterationCompletedMsg.class, this::onIterationCompleted)
                .onMessage(ViewStartMsg.class, this::onViewStart)
                .onMessage(ViewStopMsg.class, this::onStop)
                .onMessage(StartTest.class, this::onStartTest)
                .onMessage(FakeIterationCompleted.class, this::onFakeIterationCompleted)
                .build();
    }

    /**
     * Construct a new instance of the Controller actor
     *
     * @param bodies The number of total bodies
     * @param iter The number of total iterations
     * @param h The height of the user interface
     * @param w The width of the user interface
     * @return The newly created instance of the Controller actor
     */
    public static Behavior<ControllerMsg> create(final int bodies, final int iter, final int w, final int h) {
        totBodies = bodies;
        maxIter = iter;
        viewHeight = h;
        viewWidth = w;
        return Behaviors.setup(ControllerActor::new);
    }

    /**
     * Construct a new instance of the Controller actor with test mode
     *
     * @param bodies The number of total bodies
     * @param iter The number of total iterations
     * @param h The height of the user interface
     * @param w The width of the user interface
     * @param test The flag activating the test behavior
     * @return The newly created instance of the Controller actor
     */
    public static Behavior<ControllerMsg> create(final int bodies, final int iter, final int w, final int h, final boolean test) {
        totBodies = bodies;
        maxIter = iter;
        viewHeight = h;
        viewWidth = w;
        return Behaviors.setup((context) -> new ControllerActor(context, test));
    }

    //message sent by BodyActor when the new velocity and position values of the Bodies have been computed
    private Behavior<ControllerMsg> onBodyReceived(BodyComputationResultMsg msg) {
        this.bodies.add(msg.getBody());
        if(this.bodies.size() == this.bodyActorRefList.size() && !this.testMode) {
            this.viewActorRef.tell(new UpdatedPositionsMsg(this.bodies, this.vt, this.currentIter, this.bounds));
        } else if (this.bodies.size() == this.bodyActorRefList.size() && this.testMode) {
            this.testActor.tell(new FakeUpdatePositionMsg(getContext().getSelf()));
        }
        return this;
    }

    //message sent to the BodyActor at the end of each iteration and to the ViewActor when all iterations are completed
    private Behavior<ControllerMsg> onIterationCompleted(final IterationCompletedMsg msg) {
        this.currentIter++;
        this.vt += this.dt;
        if(this.currentIter <= maxIter) {
            ComputePositionsMsg requestComputation = new ComputePositionsMsg(getContext().getSelf(), this.dt, this.bodies, this.bounds);
            this.bodyActorRefList.forEach( bodyActor -> bodyActor.tell(requestComputation));
            this.bodies.clear();
        }
        return this;
    }

    //message sent by the ViewActor when the Start button press event is captured
    private Behavior<ControllerMsg> onViewStart(final ViewStartMsg msg) {
        initializeBodies();
        ComputePositionsMsg requestComputation = new ComputePositionsMsg(getContext().getSelf(), this.dt, this.bodies, this.bounds);
        this.bodyActorRefList.forEach( bodyActor -> bodyActor.tell(requestComputation));
        this.bodies.clear();
        return this;
    }

    //message sent by the ViewActor when the Stop button press event is captured or at the end of iterations
    private Behavior<ControllerMsg> onStop(final ViewStopMsg msg) {
        this.bodyActorRefList.forEach(actor -> actor.tell(new StopActorMsg()));
        this.bodyActorRefList.clear();
        return this;
    }

    private Behavior<ControllerMsg> onFakeIterationCompleted(FakeIterationCompleted msg) {
        this.currentIter++;
        this.vt += this.dt;
        if(this.currentIter <= maxIter) {
            ComputePositionsMsg requestComputation = new ComputePositionsMsg(getContext().getSelf(), this.dt, this.bodies, this.bounds);
            this.bodyActorRefList.forEach( bodyActor -> bodyActor.tell(requestComputation));
            this.bodies.clear();
        } else { // test mode
            testRef.tell(new DistributedTestResult(this.bodies));
        }

        return this;
    }

    private Behavior<ControllerMsg> onStartTest(StartTest msg) {
        this.testRef = msg.getRef();
        initializeBodies();

        testActor = getContext().spawn(TestActor.create(this.bodies, this.bounds, maxIter, dt), "TestActor");
        if (!msg.getNoGuiTest())
            testActor.tell(new StartTest(this.testRef, false));

        ComputePositionsMsg requestComputation = new ComputePositionsMsg(getContext().getSelf(), this.dt, this.bodies, this.bounds);
        this.bodyActorRefList.forEach( bodyActor -> bodyActor.tell(requestComputation));
        this.bodies.clear();

        return this;
    }

    private void initializeBodies() {
        BodyGenerator bg = new BodyGenerator();
        this.currentIter = 1;
        this.vt = 0;
        this.bodyActorRefList = new ArrayList<>();
        this.bodies = new ArrayList<>();

        for (final Body body : bg.generateBodies(totBodies, this.bounds)) {
            this.bodies.add(body);
            this.bodyActorRefList.add(getContext().spawn(BodyActor.create(body), "bodyActor-" + new Random().nextInt()));
        }
    }
}