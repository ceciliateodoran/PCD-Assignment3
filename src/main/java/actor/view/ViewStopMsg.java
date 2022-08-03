package actor.view;

import actor.ControllerMsg;

/**
 * messaggio che invia il ViewActor al ControllerActor
 * quando viene premuto Stop per far terminare le iterazioni
 */
public class ViewStopMsg implements ViewMsg, ControllerMsg {
}
