package actor.message;

/**
 * Message from ViewActor to ControllerActor
 * when Stop button is pressed to stop the simulation
 * or message from ControllerActor to ViewActor
 * at the end of iterations
 */
public class ViewStopMsg implements ViewMsg, ControllerMsg {
}
