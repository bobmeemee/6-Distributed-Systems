package Messages;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class FileOwnerIDMessage extends Message {
    InetAddress ownerIP;

    public FileOwnerIDMessage(int sender, int fileID, InetAddress ownerIP) {
        super(sender);
        super.type="FileOwnerIDMessage";
        super.content = fileID;
        this.ownerIP = ownerIP;
    }

    public FileOwnerIDMessage(int sender, int fileID) throws UnknownHostException {
        super(sender);
        super.type="FileOwnerIDMessage";
        super.content = fileID;
        this.ownerIP = InetAddress.getByName("0.0.0.0");
    }

    public InetAddress getOwnerIP() {
        return ownerIP;
    }
}
