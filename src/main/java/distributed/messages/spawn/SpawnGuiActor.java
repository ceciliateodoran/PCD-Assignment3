package distributed.messages.spawn;

import distributed.CityZone;
import distributed.messages.ValueMsg;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Message used to spawn a new Gui actor
 */
public class SpawnGuiActor extends ValueMsg {

    private final int zoneNumber;
    private final Map<String, Integer> cityDimensions;
    private final List<CityZone> cityZones;

    public SpawnGuiActor(final int zoneNumber, final Map<String, Integer> cityDimensions, final List<CityZone> cityZones) {
        this.zoneNumber = zoneNumber;
        this.cityDimensions = cityDimensions;
        this.cityZones = cityZones;
    }

    /**
     * @return the zone number
     */
    public int getZoneNumber() {
        return zoneNumber;
    }

    /**
     * @return the city dimensions
     */
    public Map<String, Integer> getCityDimensions() {
        return Collections.unmodifiableMap(cityDimensions);
    }

    /**
     * @return the city zones
     */
    public List<CityZone> getCityZones() {
        return Collections.unmodifiableList(cityZones);
    }
}
