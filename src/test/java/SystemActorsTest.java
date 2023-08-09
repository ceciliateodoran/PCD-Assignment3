import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import distributed.City;
import distributed.messages.barrack.commands.ClearBarrack;
import distributed.messages.barrack.commands.CommitBarrack;
import distributed.messages.barrack.commands.DesilenceBarrack;
import distributed.messages.barrack.commands.SilenceBarrack;
import distributed.messages.selftriggers.ListingResponse;
import distributed.messages.statuses.CityStatus;
import distributed.messages.ValueMsg;
import distributed.messages.statuses.ZoneStatus;
import distributed.model.actors.Barrack;
import distributed.model.actors.CoordinatorZone;
import distributed.model.utility.IdGenerator;
import distributed.model.actors.Sensor;
import distributed.utils.CalculatorZone;
import distributed.utils.ClusterStructure;
import distributed.utils.Pair;
import distributed.view.View;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class SystemActorsTest {
    private TestKitJunitResource testKit;
    private CalculatorZone calculatorZone;

    @BeforeEach
    public void initTestKit() {
        testKit = new TestKitJunitResource();
    }

    @Test
    public void testCoordinatorZone() throws InterruptedException {
        TestProbe<ValueMsg> probe = testKit.createTestProbe();
        IdGenerator idGen = new IdGenerator();
        Thread.sleep(3000);
        testKit.system().receptionist().tell(Receptionist.register(ServiceKey.create(ValueMsg.class, idGen.getBarrackKey(0)), probe.ref()));
        Thread.sleep(3000);

        for (int i = 0; i < 3; i++){
            testKit.spawn(Sensor.create(idGen.getSensorId(0, i), 0, new Pair<>(0,0), 100));
            Thread.sleep(3000);
        }
        testKit.spawn(CoordinatorZone.create(idGen.getZoneId(0), 0, 3));
        Thread.sleep(6000);
        probe.expectMessageClass(ZoneStatus.class);
    }

    @Test
    public void testBarrack() throws InterruptedException {
        TestProbe<ValueMsg> probe = testKit.createTestProbe();
        IdGenerator idGen = new IdGenerator();
        Thread.sleep(3000);
        testKit.system().receptionist().tell(Receptionist.register(ServiceKey.create(ValueMsg.class, idGen.getGuisKey(2)), probe.ref()));
        Thread.sleep(3000);
        int zones = 4;
        int sensorsPerZone = 3;
        for (int j = 0; j < zones; j++){
            for (int i = 0; i < sensorsPerZone; i++){
                testKit.spawn(Sensor.create(idGen.getSensorId(j, i), j, new Pair<>(0,0), 100));
            }
            testKit.spawn(CoordinatorZone.create(idGen.getZoneId(j), j, sensorsPerZone));
            testKit.spawn(Barrack.create(j));
        }
        Thread.sleep(10000);
        CityStatus msg = probe.expectMessageClass(CityStatus.class);
        Thread.sleep(500);
        System.out.println(msg.toString());
        assertTrue(msg.getBarracksStatuses().containsKey(2));
        assertEquals(msg.getBarracksStatuses().size(), zones);
        assertTrue(msg.getBarracksStatuses().get(2).equals("OK") || msg.getBarracksStatuses().get(2).equals("FLOOD"));
        assertEquals(msg.getSensorStatuses().get(2).first().size(), sensorsPerZone);
    }

    /**
     * Checks the change of status of a Barrack from OK to SILENCED
     *
     * @throws InterruptedException if interrupted while sleeping
     */
    @Test
    public void testOkSilencedBarracksStatuses() throws InterruptedException {
        // setting variables to create the city
        int zoneToConsider = 0;
        int cityRows = 1;
        int cityColumns = 2;
        int cityWidth = 400;
        int cityHeight = 200;
        Map<String, Integer> cityDimensions = Map.of(
                "rows", cityRows,
                "columns", cityColumns,
                "width", cityWidth,
                "height", cityHeight
        );

        ClusterStructure clusterStructure = new ClusterStructure();
        City city = new City(cityWidth, cityHeight, cityRows, cityColumns, 2, 100);
        calculatorZone = new CalculatorZone(city, clusterStructure);

        // create the probe actors to check the change of statuses
        TestProbe<ValueMsg> probe = testKit.createTestProbe();
        TestProbe<ValueMsg> probe2 = testKit.createTestProbe();
        IdGenerator idGen = new IdGenerator();
        Thread.sleep(3000);

        // create the main actors of the system
        testKit.spawn(Sensor.create(idGen.getSensorId(zoneToConsider, 2), zoneToConsider, new Pair<>(0,0), 100));
        testKit.spawn(CoordinatorZone.create(idGen.getZoneId(zoneToConsider), zoneToConsider, 2));
        testKit.spawn(Barrack.create(zoneToConsider));
        ActorRef<ValueMsg> ref = testKit.spawn(View.create(zoneToConsider, cityDimensions, calculatorZone.setSensorsInZones()));

        // register the probe actor to the receptionist as receiver of messages
        testKit.system().receptionist().tell(Receptionist.register(ServiceKey.create(ValueMsg.class, idGen.getGuisKey(zoneToConsider)), probe.ref()));
        Thread.sleep(10000);

        // check if the probe actor intercepts the messages
        probe.expectMessageClass(CityStatus.class);

        Thread.sleep(1500);

        // changing the status of the Barrack to OK
        View.changeBarrackStatus("OK");
        ref.tell(new ListingResponse(Receptionist.listing(ServiceKey.create(ValueMsg.class, idGen.getBarrackId(zoneToConsider)), Set.of(probe2.ref()))));
        probe2.expectMessageClass(ClearBarrack.class);

        Thread.sleep(1500);

        // changing the status of the Barrack to SILENCED
        View.changeBarrackStatus("SILENCED");
        ref.tell(new ListingResponse(Receptionist.listing(ServiceKey.create(ValueMsg.class, idGen.getBarrackId(zoneToConsider)), Set.of(probe2.ref()))));
        probe2.expectMessageClass(SilenceBarrack.class);

        Thread.sleep(1500);

        // changing the status of the Barrack to DESILENCED
        View.changeBarrackStatus("DESILENCED");
        ref.tell(new ListingResponse(Receptionist.listing(ServiceKey.create(ValueMsg.class, idGen.getBarrackId(zoneToConsider)), Set.of(probe2.ref()))));
        probe2.expectMessageClass(DesilenceBarrack.class);

        Thread.sleep(1500);
    }

    /**
     * Checks the change of status of a Barrack from OK to COMMITTED.
     *
     * @throws InterruptedException if interrupted while sleeping
     */
    @Test
    public void testCommittedBarracksStatus() throws InterruptedException {
        // setting variables to create the city
        int zoneToConsider = 0;
        int cityRows = 1;
        int cityColumns = 2;
        int cityWidth = 400;
        int cityHeight = 200;
        Map<String, Integer> cityDimensions = Map.of(
                "rows", cityRows,
                "columns", cityColumns,
                "width", cityWidth,
                "height", cityHeight
        );

        ClusterStructure clusterStructure = new ClusterStructure();
        City city = new City(cityWidth, cityHeight, cityRows, cityColumns, 2, 100);
        calculatorZone = new CalculatorZone(city, clusterStructure);

        // create the probe actors to check the change of statuses
        TestProbe<ValueMsg> probe = testKit.createTestProbe();
        TestProbe<ValueMsg> probe2 = testKit.createTestProbe();
        IdGenerator idGen = new IdGenerator();
        Thread.sleep(3000);

        // create the main actors of the system
        testKit.spawn(Sensor.create(idGen.getSensorId(zoneToConsider, 2), zoneToConsider, new Pair<>(0,0), 100));
        testKit.spawn(CoordinatorZone.create(idGen.getZoneId(zoneToConsider), zoneToConsider, 2));
        testKit.spawn(Barrack.create(zoneToConsider));
        ActorRef<ValueMsg> ref = testKit.spawn(View.create(zoneToConsider, cityDimensions, calculatorZone.setSensorsInZones()));

        // register the probe actor to the receptionist as receiver of messages
        testKit.system().receptionist().tell(Receptionist.register(ServiceKey.create(ValueMsg.class, idGen.getGuisKey(zoneToConsider)), probe.ref()));
        Thread.sleep(10000);

        // check if the probe actor intercepts the messages
        probe.expectMessageClass(CityStatus.class);

        Thread.sleep(1500);

        // changing the status of the Barrack to OK
        View.changeBarrackStatus("OK");
        ref.tell(new ListingResponse(Receptionist.listing(ServiceKey.create(ValueMsg.class, idGen.getBarrackId(zoneToConsider)), Set.of(probe2.ref()))));
        probe2.expectMessageClass(ClearBarrack.class);

        Thread.sleep(1500);

        // changing the status of the Barrack to COMMITTED
        View.changeBarrackStatus("COMMITTED");
        ref.tell(new ListingResponse(Receptionist.listing(ServiceKey.create(ValueMsg.class, idGen.getBarrackId(zoneToConsider)), Set.of(probe2.ref()))));
        probe2.expectMessageClass(CommitBarrack.class);

        Thread.sleep(1500);

        // changing the status of the Barrack to OK
        View.changeBarrackStatus("OK");
        ref.tell(new ListingResponse(Receptionist.listing(ServiceKey.create(ValueMsg.class, idGen.getBarrackId(zoneToConsider)), Set.of(probe2.ref()))));
        probe2.expectMessageClass(ClearBarrack.class);

        Thread.sleep(1500);
    }
}