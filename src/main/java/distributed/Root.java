package distributed;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.pubsub.Topic;
import akka.cluster.typed.Cluster;
import akka.cluster.typed.Join;
import akka.japi.Pair;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import distributed.messages.ValueMsg;
import distributed.model.Sensor;
import distributed.messages.DetectedValueMsg;
import distributed.model.ZoneCoordinator;
import distributed.utils.ZoneCalculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Root {
    private City city;
    private static ZoneCalculator zoneCalculator;
    private static List<CityZone> cityZone;
    public Root(final City c) {
        this.city = c;
        this.zoneCalculator = new ZoneCalculator(this.city);
        this.cityZone = new ArrayList<>();
    }

    public static void startup(int port) {
        // Override the configuration of the port
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("akka.remote.artery.canonical.port", port);
        overrides.put("akka.actor.provider", "cluster");

        Config config = ConfigFactory.parseMap(overrides)
                .withFallback(ConfigFactory.load());

        // Create an Akka system
        ActorSystem<Void> system = ActorSystem.create(rootBehavior(), "ClusterSystem", config);

        Cluster cluster = Cluster.get(system);
        cluster.manager().tell(Join.create(cluster.selfMember().address()));
    }

    public static Behavior<Void> rootBehavior() {
        return Behaviors.setup(context -> {
            List<Pair<Integer, Integer>> zones = zoneCalculator.setZoneSensors();
            ActorRef<Topic.Command<ValueMsg>> topic = null;

            int zone = 0;
            for (int i = 0; i < zones.size(); i++) {
                int zoneNumber = zones.get(i).second();
                int sensorNumber = zones.get(i).first();
                if (zoneNumber > zone) {
                    topic = context.spawn(Topic.create(ValueMsg.class, "TopicZone" + zoneNumber), "TopicZone" + zoneNumber);
                    context.spawn(ZoneCoordinator.create(zoneNumber, topic), "ZoneCoordinator" + zoneNumber);
                    zone = zoneNumber;
                }
                // Create an actor that handles cluster domain events
                context.spawn(Sensor.create(sensorNumber, zone, topic), "Sensor" + sensorNumber);
            }

            return Behaviors.empty();
        });
    }
}
