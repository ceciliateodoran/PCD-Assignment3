package distributed.messages.spawn;

import com.fasterxml.jackson.annotation.JsonProperty;
import distributed.messages.ValueMsg;

/**
 * Message used to spawn a new Barrack actor
 */
public class SpawnBarrackActor extends ValueMsg {
    private final Integer zoneNumber;

    public SpawnBarrackActor(@JsonProperty("zoneNumber") Integer zoneNumber) {
        this.zoneNumber = zoneNumber;
    }

    /**
     * @return the zone number
     */
    public int getZoneNumber() {
        return zoneNumber;
    }
}
