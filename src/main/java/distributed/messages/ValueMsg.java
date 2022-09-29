package distributed.messages;


import akka.actor.typed.receptionist.Receptionist;



public class ValueMsg extends Receptionist.Command {
    private final String value;

    public ValueMsg(){
        this.value = "";
    };

    public ValueMsg(String o){
        this.value = o;
    }

    public String getValue(){
        return this.value;
    }

}
