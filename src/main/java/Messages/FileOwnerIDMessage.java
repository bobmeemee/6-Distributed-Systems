package Messages;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class FileOwnerIDMessage extends Message {
    private InetAddress ownerIP;
    private int ownerID;

    public FileOwnerIDMessage(int sender, int fileID, int ownerID ,InetAddress ownerIP) {
        super(sender);
        super.type="FileOwnerIDMessage";
        super.content = fileID;
        this.ownerIP = ownerIP;
        this.ownerID = ownerID;
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

    public int getOwnerID() {
        return ownerID;
    }
}
