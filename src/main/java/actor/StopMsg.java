package actor;

/**
 * Msg che invia la GUI per fermare gli attori
 */
public class StopMsg implements ControllerMsg {

    private Boolean stop;

    public StopMsg() {
        this.stop = false;
    }

    public void setStop(Boolean stopMsg){
        this.stop = stopMsg;
    }
    public Boolean isStopped(){
        return this.stop;
    }
}
