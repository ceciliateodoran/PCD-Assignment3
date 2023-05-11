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

    public String getBarrackKey(int zone){
        return "barrack:" + zone;
    }

    public String getSensorsKey(int zone) { return "sensors:" + zone; }

    public String getBarracksKey() { return "barracks"; }

    public String getGuisKey(int zone){ return "guis:" + zone; }
}
