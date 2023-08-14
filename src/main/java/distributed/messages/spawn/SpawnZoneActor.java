package distributed.messages.spawn;

import distributed.messages.ValueMsg;

/**
 * Message used to spawn a new Zone actor
 */
public class SpawnZoneActor extends ValueMsg {
    private final String idZone;
    private final int zoneNumber;
    private final int numSensorsInZone;

    public SpawnZoneActor(String idZone, int zoneNumber, int sensorsInZone) {
        this.idZone = idZone;
        this.zoneNumber = zoneNumber;
        this.numSensorsInZone = sensorsInZone;
    }

    /**
     * @return the zone identifier
     */
    public String getIdZone() {
        return idZone;
    }

    /**
     * @return the zone number
     */
    public int getZoneNumber() {
        return zoneNumber;
    }

    /**
     * @return the number of sensors in the zone
     */
    public int getNumSensorsInZone() {
        return numSensorsInZone;
    }
}
