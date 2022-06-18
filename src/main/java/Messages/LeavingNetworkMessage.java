package Messages;

public class LeavingNetworkMessage extends Message {
    private final int previousID;
    private final int nextID;
    public LeavingNetworkMessage(int sender, int previousID, int nextID) {
        super(sender);
        super.type = "LeavingNetworkMessage";
        this.previousID = previousID;
        this.nextID = nextID;
    }

    public int getPreviousID() {
        return previousID;
    }

    public int getNextID() {
        return nextID;
    }
}
