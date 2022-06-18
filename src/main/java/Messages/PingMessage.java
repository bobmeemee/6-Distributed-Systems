package Messages;

public class PingMessage extends Message {
    public PingMessage(int sender) {
        super(sender);
        super.type = "PingMessage";
    }
}
