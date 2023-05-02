package distributed.model;

public class IdGenerator {
    public String getSensorId(int zone, int sensorNumber){
        return "sensor:"+zone + "-" + sensorNumber;
    }

    public String getZoneId(int zone){
        return "zone:" + zone;
    }

    public String getBarrackId(int zone){
        return "barrack:" + zone;
    }
}
