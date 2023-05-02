package distributed.messages;

import akka.actor.typed.receptionist.Receptionist;

public class ListingResponse extends ValueMsg{
    public final Receptionist.Listing listing;

    public ListingResponse(Receptionist.Listing listing) {
        this.listing = listing;
    }
}
