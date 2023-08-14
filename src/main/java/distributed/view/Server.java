package distributed.view;

import distributed.CityZone;
import distributed.model.utility.SensorSnapshot;
import distributed.utils.Pair;
import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents the Server implementation of the web service
 */
public class Server {

    private final Vertx vertx = Vertx.vertx();
    private final Router router = Router.router(vertx);
    private final HttpServer server;
    private int zone;
    private Map<String, Integer> cityDimensions;
    private List<CityZone> cityZones;
    private int selectedZone; //the zone selected from the gui
    private final List<List<SensorSnapshot>> sensorStatuses;
    private Map<Integer, String> barracksStatuses;
    private Map<Integer, Boolean> zonesPartialData;
    private final View view;

    /**
     * Construct a new instance of the Server
     *
     * @param view The instance of the View actor
     * @param zone The number of the zone to which the Server belongs
     * @param cityDimensions The dimensions of the city, such as width, height, rows and columns
     * @param cityZones The list of all the city zones
     * @return The newly created instance of the Server
     */
    public Server(final View view, final int zone, final Map<String, Integer> cityDimensions, final List<CityZone> cityZones){
        this.server = this.vertx.createHttpServer();
        this.sensorStatuses = new ArrayList<>();
        cityZones.forEach(z -> this.sensorStatuses.add(new ArrayList<>()));
        this.barracksStatuses = new HashMap<>();
        this.zonesPartialData = new HashMap<>();
        this.view = view;
        this.zone = zone;
        this.cityZones = cityZones;
        this.cityDimensions = cityDimensions;
        this.init();
    }

    private void init(){
        //return all the zones of the city
        this.router.get("/zones")
            .respond(ctx -> {
                ctx.response()
                        .putHeader("Access-Control-Allow-Origin" , "*")
                        .putHeader("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE");
                return Future.succeededFuture(new JsonObject().put("zones", this.getAllZones()));
            });

        //return the zone of this barrack
        this.router.get("/myzone")
            .respond(ctx -> {
                ctx.response()
                        .putHeader("Access-Control-Allow-Origin" , "*")
                        .putHeader("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE");
                return Future.succeededFuture(new JsonObject().put("myzone", String.valueOf(this.zone)));
            });

        //set the zone selected in gui
        this.router.post("/zone/:numZone")
            .respond(ctx -> {
                ctx.response()
                        .putHeader("Access-Control-Allow-Origin" , "*")
                        .putHeader("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE");
                return Future.succeededFuture(new JsonObject().put("sensorsinfo", this.getSelectedZoneSensorsInfo(Integer.parseInt(ctx.pathParam("numZone")))));
        });

        //return the number of sensors in the zone
        this.router.get("/sensors")
            .respond(ctx -> {
                ctx.response()
                        .putHeader("Access-Control-Allow-Origin" , "*")
                        .putHeader("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE");
                return Future.succeededFuture(new JsonObject().put("sensors", String.valueOf(this.getZoneNumberOfSensors(this.selectedZone))));
            });

        //return the status of the barrack of the selected zone
        this.router.get("/barrackstatus")
            .respond(ctx -> {
                ctx.response()
                        .putHeader("Access-Control-Allow-Origin" , "*")
                        .putHeader("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE");
                return Future.succeededFuture(new JsonObject().put("barrackstatus", this.getBarrackStatus(this.selectedZone)));
            });

        //change barrack status
        this.router.post("/alarm/:action/:numZone")
                .respond(ctx -> {
                    this.setStatusAlarm(ctx.pathParam("action"), Integer.parseInt(ctx.pathParam("numZone")));
                    ctx.response()
                            .putHeader("Access-Control-Allow-Origin" , "*")
                            .putHeader("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE");
                    return Future.succeededFuture(new JsonObject().put("action", ctx.pathParam("action")));
                });

        //return the number of rows and columns of the city grid
        this.router.get("/citysize")
            .respond(ctx -> {
                ctx.response()
                        .putHeader("Access-Control-Allow-Origin" , "*")
                        .putHeader("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE");
                return Future.succeededFuture(new JsonObject().put("citysize", this.cityDimensions));
        });

        //return the coordinates of all sensors
        this.router.get("/sensorscoords")
            .respond(ctx -> {
                ctx.response()
                        .putHeader("Access-Control-Allow-Origin" , "*")
                        .putHeader("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE");
                return Future.succeededFuture(new JsonObject().put("sensorscoords", this.getSensorsCoordinates()));
            });

        this.server.requestHandler(this.router).listen(8080);
    }

    private List<Integer> getAllZones() {
        return this.cityZones.stream().map(c -> c.getIndex()).collect(Collectors.toList());
    }

    private String getBarrackStatus(final int zone){
        Boolean isPartial = this.zonesPartialData.get(zone);
        String status = this.barracksStatuses.get(zone) == null ? "No barrack status" :this.barracksStatuses.get(zone);
        return status +"|"+isPartial;
    }

    private int getZoneNumberOfSensors(final int zone){
        if (this.sensorStatuses.get(zone) != null){
            return this.sensorStatuses.get(zone).size();
        }else{
            return -1;
        }
    }

    private List<String> getSensorsCoordinates(){
        List<String> coordinates = new ArrayList<>();
        this.cityZones.forEach(c-> c.getSensors().forEach((k, v) -> coordinates.add(c.getIndex() + "|"+ k + "|" + v.first() + "|" + v.second())));
        return coordinates;
    }

    private List<String> getSelectedZoneSensorsInfo(final int zone){
        this.setSelectedZone(zone);
        List<String> sensorsInfo = new ArrayList<>();
        if (this.sensorStatuses.get(zone) != null){
            this.sensorStatuses.get(zone).forEach(s ->{
                sensorsInfo.add(s.getId()+"|"+s.getValue()+"|"+s.getLimit());
            });
        }else{
            sensorsInfo.add("ERROR");
        }
        return sensorsInfo.stream().sorted().collect(Collectors.toList());
    }

    private void setSelectedZone(final int zone){
        this.selectedZone = zone;
    }

    private void setStatusAlarm(final String statusAlarm, final int zone) {
        String status = "";
        this.selectedZone = zone;
        switch(statusAlarm){
            case "manage":
                status = "COMMITTED";
                break;
            case "stop":
                status = "OK";
                break;
            case "silence":
                status = "SILENCED";
                break;
            case "desilence":
                status = "DESILENCED";
                break;
        }
        this.barracksStatuses.put(zone, status);
        this.view.changeBarrackStatus(status);
    }

    /**
     * It sets the new values detected by all sensors distributed in the city
     *
     * @param newValues The new values measured by sensors of all zones
     */
    public void setSensorStatuses(final Map<Integer, Pair<List<SensorSnapshot>, Boolean>> newValues) {
        newValues.entrySet().stream()
                .filter(entry -> entry.getValue().second()!=null)
                .forEach(entry -> this.zonesPartialData.put(entry.getKey(),entry.getValue().second()));

        newValues.entrySet().stream()
               .filter(entry -> !entry.getValue().first().isEmpty())
               .forEach(entry -> {
                   List<SensorSnapshot> toPreserve = this.sensorStatuses.get(entry.getKey()).stream()
                           .filter(ss -> !entry.getValue().first().stream()
                                   .map(SensorSnapshot::getId)
                                   .collect(Collectors.toList()).contains(ss.getId()))
                           .collect(Collectors.toList());
                   this.sensorStatuses.get(entry.getKey()).clear();
                   entry.getValue().first().addAll(toPreserve);
                   this.sensorStatuses.get(entry.getKey()).addAll( entry.getValue().first());
               });
    }

    /**
     * It sets the Barrack statuses of the city,
     * informations arriving from your Barrack
     *
     * @param barracksStatuses The statuses of all Barracks in the city
     */
    public void setBarracksStatuses(final Map<Integer, String> barracksStatuses) {
        this.barracksStatuses.clear();
        this.barracksStatuses.putAll(barracksStatuses);
    }
}
