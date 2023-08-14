package distributed.messages.statuses;

import distributed.messages.ValueMsg;
import distributed.model.utility.SensorSnapshot;
import distributed.utils.Pair;

import java.util.List;
import java.util.Map;

/**
 * Message used to send the City status
 */
public class CityStatus extends ValueMsg {
    private final Map<Integer, Pair<List<SensorSnapshot>, Boolean>> sensorStatuses;
    private final Map<Integer, String> barracksStatuses;

    public CityStatus(Map<Integer, Pair<List<SensorSnapshot>, Boolean>> status, Map<Integer, String> barracksStatuses) {
        this.sensorStatuses = status;
        this.barracksStatuses = barracksStatuses;
    }

    /**
     * @return the sensor statuses related to the city
     */
    public Map<Integer,  Pair<List<SensorSnapshot>, Boolean>> getSensorStatuses() { return sensorStatuses; }

    /**
     * @return the barrack statuses related to the city
     */
    public Map<Integer, String> getBarracksStatuses() { return barracksStatuses; }

    /**
     * @return the city status in a string
     */
    @Override
    public String toString() {return this.barracksStatuses.toString() + " " + this.sensorStatuses.toString(); }
}
