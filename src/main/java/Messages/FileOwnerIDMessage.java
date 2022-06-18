package Messages;

import java.net.InetAddress;

public class FileOwnerIDMessage extends Message {
    InetAddress ownerIP;
    public FileOwnerIDMessage(int sender, int ownerID, InetAddress ownerIP) {
        super(sender);
        super.content = ownerID;
        this.ownerIP = ownerIP;
    }

    public InetAddress getOwnerIP() {
        return ownerIP;
    }
}
