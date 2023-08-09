package distributed.messages.selftriggers;

import akka.actor.typed.receptionist.Receptionist;
import distributed.messages.ValueMsg;

public class ListingResponse extends ValueMsg {
    public final Receptionist.Listing listing;

    public ListingResponse(Receptionist.Listing listing) {
        this.listing = listing;
    }
}
