package actor;

import actor.message.*;
import actor.message.test.DistributedTestResult;
import actor.message.test.FakeIterationCompleted;
import actor.message.test.StartTest;
import actor.utils.Body;
import actor.utils.BodyGenerator;
import actor.utils.Boundary;
import actor.utils.TestActor;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.MailboxSelector;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.routing.Broadcast;
import distributed.messages.ValueMsg;

import java.util.*;
import java.util.stream.Collectors;

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
    private boolean isStopped;
    private Integer runNumber;

    private ControllerActor(final ActorContext<ControllerMsg> context) {
        super(context);
        this.dt = 0.001;
        this.bounds =  new Boundary(-6.0, -6.0, 6.0, 6.0);
        this.viewActorRef = context.spawn(ViewActor.create(context.getSelf(), viewWidth, viewHeight), "viewActor", MailboxSelector.fromConfig("my-app.priority-mailbox"));
        this.testMode = false;
        this.isStopped = true;
        this.runNumber = 0;
    }

    private ControllerActor(final ActorContext<ControllerMsg> context, final boolean test) {
        super(context);
        this.dt = 0.001;
        this.bounds =  new Boundary(-6.0, -6.0, 6.0, 6.0);
        this.testMode = test;
        this.runNumber = 0;
    }

    @Override
    public Receive<ControllerMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(BodyComputationResultMsg.class, this::onBodyReceived)
                .onMessage(ViewStartMsg.class, this::onViewStart)
                .onMessage(ViewStopMsg.class, this::onStop)
                .onMessage(StartTest.class, this::onStartTest)
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
        if(isStopped) return this;
        if(msg.getRunNumber().equals(this.runNumber)) {
            this.bodies.addAll(msg.getBody());
        }

        if (this.bodies.size() == totBodies) {
            this.viewActorRef.tell(new UpdatedPositionsMsg(this.bodies, this.vt, this.currentIter, this.bounds));
            this.currentIter++;
            this.vt += this.dt;
            if (this.currentIter <= maxIter) {
                ComputePositionsMsg requestComputation = new ComputePositionsMsg(getContext().getSelf(), this.dt, this.bodies, this.bounds, runNumber);
                this.bodyActorRefList.forEach(bodyActor -> bodyActor.tell(requestComputation));
                this.bodies.clear();
            }
        }
        return this;
    }

    //message sent by the ViewActor when the Start button press event is captured
    private Behavior<ControllerMsg> onViewStart(final ViewStartMsg msg) {
        this.isStopped = false;
        this.runNumber++;
        initializeBodies();
        ComputePositionsMsg requestComputation = new ComputePositionsMsg(getContext().getSelf(), this.dt, this.bodies, this.bounds, runNumber);
        this.bodyActorRefList.forEach( bodyActor -> bodyActor.tell(requestComputation));
        this.bodies.clear();

        return this;
    }

    //message sent by the ViewActor when the Stop button press event is captured or at the end of iterations
    private Behavior<ControllerMsg> onStop(final ViewStopMsg msg) {
        this.isStopped = true;
        this.bodyActorRefList.forEach(actor -> actor.tell(new StopActorMsg()));
        this.bodyActorRefList.clear();
        return this;
    }

    private Behavior<ControllerMsg> onStartTest(StartTest msg) {
        this.testRef = msg.getRef();
        initializeBodies();

        testActor = getContext().spawn(TestActor.create(this.bodies, this.bounds, maxIter, dt), "TestActor");
        if (!msg.getNoGuiTest())
            testActor.tell(new StartTest(this.testRef, false));

        ComputePositionsMsg requestComputation = new ComputePositionsMsg(getContext().getSelf(), this.dt, this.bodies, this.bounds, runNumber);
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

        if(totBodies > 1000) {
            List<Body> bodies = bg.generateBodies(totBodies, this.bounds);
            this.bodies.addAll(bodies);
            for(int i = 0; i < 1000; i++) {
                final int nthActor = i;
                List<Body> nthBodies = bodies.stream().filter(b -> b.getId() % 1000 == nthActor).collect(Collectors.toList());
                this.bodyActorRefList.add(getContext().spawn(BodyActor.create(nthBodies), "bodyActor-" + new Random().nextInt()));
            }
        } else {
            for (final Body body : bg.generateBodies(totBodies, this.bounds)) {
                this.bodies.add(body);
                this.bodyActorRefList.add(getContext().spawn(BodyActor.create(List.of(body)), "bodyActor-" + new Random().nextInt()));
            }
        }
    }
}