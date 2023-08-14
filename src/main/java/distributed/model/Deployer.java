package distributed.model;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import distributed.messages.spawn.SpawnBarrackActor;
import distributed.messages.spawn.SpawnGuiActor;
import distributed.messages.spawn.SpawnSensorActor;
import distributed.messages.spawn.SpawnZoneActor;
import distributed.messages.ValueMsg;
import distributed.model.utility.IdGenerator;
import distributed.view.View;

import java.util.Random;

/**
 * Represents the actor used to spawn all other actors in the actor system
 */
public class Deployer extends AbstractBehavior<ValueMsg> {
    private static final IdGenerator idGenerator = new IdGenerator();

    private Deployer(final ActorContext<ValueMsg> context) {
        super(context);
    }

    /**
     * Construct a new instance of the Deployer actor
     *
     * @return The newly created instance of the Deployer actor
     */
    public static Behavior<ValueMsg> create() {
        return Behaviors.setup(context -> {
            context.getSystem()
                    .receptionist()
                    .tell(Receptionist.register(ServiceKey.create(ValueMsg.class, idGenerator.getDeployersKey()), context.getSelf()));
            return new Deployer(context);
        });
    }

    @Override
    public Receive<ValueMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(SpawnZoneActor.class, this::onSpawnZoneActor)
                .onMessage(SpawnSensorActor.class, this::onSpawnSensorActor)
                .onMessage(SpawnBarrackActor.class, this::onSpawnBarrackActor)
                .onMessage(SpawnGuiActor.class, this::onSpawnGuiActor)
                .build();
    }

    private Behavior<ValueMsg> onSpawnGuiActor(final SpawnGuiActor msg) {
        getContext().spawn(View.create(msg.getZoneNumber(), msg.getCityDimensions(), msg.getCityZones()), "gui" + msg.getZoneNumber());
        return this;
    }

    private Behavior<ValueMsg> onSpawnBarrackActor(final SpawnBarrackActor msg) {
        System.out.println("SPAWNING " + msg.getZoneNumber());
        getContext().spawn(Barrack.create(msg.getZoneNumber()), "barrack" + msg.getZoneNumber() + "-" + new Random().nextInt());
        return this;
    }

    private Behavior<ValueMsg> onSpawnZoneActor(final SpawnZoneActor msg) {
        System.out.println("SPAWNING " + msg.getIdZone());
        getContext().spawn(CoordinatorZone.create(msg.getIdZone(), msg.getZoneNumber(), msg.getNumSensorsInZone()), "zone" + msg.getZoneNumber() + "-" + new Random().nextInt());
        return this;
    }

    private Behavior<ValueMsg> onSpawnSensorActor(final SpawnSensorActor msg) {
        getContext().spawn(Sensor.create(msg.getId(), msg.getZoneNumber(), msg.getSensorCoords(), msg.getLimit()), "sensor" + msg.getSensorCounter());
        return this;
    }
}
