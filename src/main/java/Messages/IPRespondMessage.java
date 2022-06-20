package Messages;

import java.net.InetAddress;

public class IPRespondMessage extends Message{
    private final InetAddress IP;
    public IPRespondMessage(int sender, InetAddress IP) {
        super(sender);
        super.type = "IPRespondMessage";
        this.IP = IP;
    }

    public InetAddress getIP() {
        return IP;
    }
}
