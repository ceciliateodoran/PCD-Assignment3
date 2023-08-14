package distributed.model.utility;

public class IdGenerator {

    /***
     * get sensor id
     * @param zone is the zone in which the sensor is located
     * @param sensorNumber represent the number of the sensor inside its zone
     * @return the id
     */
    public String getSensorId(int zone, int sensorNumber){
        return "sensor:"+zone + "-" + sensorNumber;
    }

    /***
     * get zone id
     * @param zone is the number of the zone
     * @return the id
     */
    public String getZoneId(int zone){
        return "zone:" + zone;
    }

    /***
     * get barrack id
     * @param zone is the zone in which the barrack is located
     * @return the id
     */
    public String getBarrackId(int zone){
        return "barrack:" + zone;
    }

    /***
     * get a single barrack Receptionist key
     * @param zone is the number of the zone in which the barrack is located
     * @return the key
     */
    public String getBarrackKey(int zone){
        return "barrack:" + zone;
    }

    /***
     * get the Receptionist key of all the sensor of a zone
     * @param zone is the desired zone number
     * @return the key
     */
    public String getSensorsKey(int zone) { return "sensors:" + zone; }

    /***
     * get the Receptionist key of all the Barracks of the cluster
     * @return the key
     */
    public String getBarracksKey() { return "barracks"; }

    /***
     * get the Receptionist key of the GUIS of a zone
     * @param zone is the desired zone number
     * @return the key
     */
    public String getGuisKey(int zone){ return "guis:" + zone; }

    /***
     * get the Receptionist key of all actor subscribed to the gossip protocol
     * @return the key
     */
    public String getPingKey() { return "gossipProtocol"; }

    /***
     * get the Receptionist key of all the Deployers actors in the cluster
     * @return the key
     */
    public String getDeployersKey() { return "deployers"; }
}
