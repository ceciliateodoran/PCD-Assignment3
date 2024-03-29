package distributed;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import distributed.messages.statuses.CityStatus;
import distributed.messages.ValueMsg;
import distributed.messages.statuses.ZoneStatus;
import distributed.model.Barrack;
import distributed.model.CoordinatorZone;
import distributed.model.utility.IdGenerator;
import distributed.model.Sensor;
import distributed.utils.Pair;
import org.junit.ClassRule;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class FunctionalRequirementsTest {
    @ClassRule public static final TestKitJunitResource testKit = new TestKitJunitResource();

    /***
     * Test coordinatorZone behavior.
     * @throws InterruptedException
     */
    @Test
    public void testCoordinatorZone() throws InterruptedException {
        TestProbe<ValueMsg> probe = testKit.createTestProbe();
        IdGenerator idGen = new IdGenerator();
        Thread.sleep(3000);
        testKit.system().receptionist().tell(Receptionist.register(ServiceKey.create(ValueMsg.class, idGen.getBarrackKey(0)), probe.ref()));
        Thread.sleep(3000);
        List<Sensor> sensors = new ArrayList<>();

        for (int i = 0; i < 3; i++){
            testKit.spawn(Sensor.create(idGen.getSensorId(0, i), 0, new Pair<>(0,0), 100));
            Thread.sleep(3000);
        }
        testKit.spawn(CoordinatorZone.create(idGen.getZoneId(0), 0, 3));
        Thread.sleep(6000);
        probe.expectMessageClass(ZoneStatus.class);
    }

    /***
     * test Barrack behavior
     * @throws InterruptedException
     */
    @Test
    public void testBarrack() throws InterruptedException {
        TestProbe<ValueMsg> probe = testKit.createTestProbe();
        IdGenerator idGen = new IdGenerator();
        Thread.sleep(3000);
        testKit.system().receptionist().tell(Receptionist.register(ServiceKey.create(ValueMsg.class, idGen.getGuisKey(2)), probe.ref()));
        Thread.sleep(3000);
        List<Sensor> sensors = new ArrayList<>();
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
}