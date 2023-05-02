package distributed.messages;

import akka.japi.Pair;
import distributed.model.SensorSnapshot;

import java.util.List;
import java.util.Map;

public class CityStatus extends ValueMsg {
    private final Map<Integer, Pair<List<SensorSnapshot>, Boolean>> status;

    public CityStatus(Map<Integer, Pair<List<SensorSnapshot>, Boolean>> status) {
        this.status = status;
    }

    public Map<Integer,  Pair<List<SensorSnapshot>, Boolean>> getStatus() { return status; }
}
