package Messages;

public class DiscoveryMessage extends Message{
    public DiscoveryMessage(int sender) {
        super(sender);
        super.type = "DiscoveryMessage";

    }
}
