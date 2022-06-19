package Messages;

import java.net.InetAddress;

public class FileOwnerIDMessage extends Message {
    InetAddress ownerIP;

    public FileOwnerIDMessage(int sender, int fileID, InetAddress ownerIP) {
        super(sender);
        super.type="FileOwnerIDMessage";
        super.content = fileID;
        this.ownerIP = ownerIP;
    }

    public InetAddress getOwnerIP() {
        return ownerIP;
    }
}
