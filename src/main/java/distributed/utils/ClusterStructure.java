package distributed.utils;

import akka.actor.typed.ActorSystem;
import distributed.messages.ValueMsg;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * It's the data structure with information on the remote nodes of the cluster.
 */
public class ClusterStructure {
    private String clusterName;
    private int sensorPerZone;
    private Map<Pair<Integer, Integer>, Pair<String, Integer>> physicalHostSensors;
    private Map<Integer, Pair<String, Integer>> physicalHostBarracksZones;
    private Map<Integer, Pair<String, ActorSystem<ValueMsg>>>  zoneSystems;
    private Map<Integer, Pair<String, Integer>> physicalGuiSystems;

    /**
     * Construct a new instance of the Cluster structure
     */
    public ClusterStructure() {
        this.physicalHostSensors = new HashMap<>();
        this.physicalHostBarracksZones = new HashMap<>();
        this.zoneSystems = new HashMap<>();
        this.physicalGuiSystems = new HashMap<>();
    }

    /**
     * It sets the cluster name
     *
     * @param name The cluster name
     */
    public void setClusterName(final String name) {
        this.clusterName = name;
    }

    /**
     * @return the cluster name
     */
    public String getClusterName() {
        return clusterName;
    }

    /**
     * It sets the value of the sensors number per zone
     *
     * @param sensorPerZone The value of the sensors number
     */
    public void setSensorPerZone(int sensorPerZone) {
        this.sensorPerZone = sensorPerZone;
    }

    /**
     * @return The value of the sensors number per zone
     */
    public int getSensorPerZone() {
        return sensorPerZone;
    }

    /**
     * It adds a new physical host with its corresponding sensor information
     *
     * @param physicalHost The physical host information
     * @param sensor The sensor information
     */
    public void addPhysicalHostSensor(Pair<Integer, Integer> physicalHost, Pair<String, Integer> sensor) {
        this.physicalHostSensors.putIfAbsent(physicalHost, sensor);
    }

    /**
     * It adds a new physical host with its corresponding zone information
     *
     * @param physicalHost The physical host information
     * @param zone The zone information
     */
    public void addPhysicalHostBarracksZone(Integer physicalHost, Pair<String, Integer> zone) {
        this.physicalHostBarracksZones.putIfAbsent(physicalHost, zone);
    }

    /**
     * It adds a new zone actor system with its associated zone
     *
     * @param zone The zone number associated to the actor system
     * @param zoneSystem The zone actor system
     */
    public void addZoneSystem(Integer zone, Pair<String, ActorSystem<ValueMsg>> zoneSystem) {
        this.zoneSystems.put(zone, zoneSystem);
    }

    /**
     * It adds a new client gui with its corresponding zone information
     *
     * @param zone The zone number
     * @param gui The client gui information
     */
    public void addPhysicalGuiSystem(Integer zone, Pair<String, Integer> gui) {
        this.physicalGuiSystems.putIfAbsent(zone, gui);
    }

    /**
     * @return the sensors physical hosts information
     */
    public Map<Pair<Integer, Integer>, Pair<String, Integer>> getPhysicalHostSensors() {
        return Collections.unmodifiableMap(physicalHostSensors);
    }

    /**
     * @return the barracks and zones physical hosts information
     */
    public Map<Integer, Pair<String, Integer>> getPhysicalHostBarracksZones() {
        return Collections.unmodifiableMap(physicalHostBarracksZones);
    }

    /**
     * @return the zone actor systems information
     */
    public Map<Integer, String> getZoneSystems() {
        return zoneSystems.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().second().address().hostPort()));
    }

    /**
     * @return the client gui actor systems information
     */
    public Map<Integer, Pair<String, Integer>> getPhysicalGuiSystems() {
        return Collections.unmodifiableMap(physicalGuiSystems);
    }

    /**
     *
     * @param zone The zone number
     * @return the corresponding address of the zone
     */
    public String getZoneAddress(Integer zone){
        return this.zoneSystems.get(zone).first();
    }

    /**
     *
     * @param zone The zone number
     * @return the corresponding actor system of the zone
     */
    public ActorSystem<ValueMsg> getZoneActorSystem(Integer zone){
        return this.zoneSystems.get(zone).second();
    }
}
