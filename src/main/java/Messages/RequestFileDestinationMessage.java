package Messages;

import java.net.InetAddress;

public class RequestFileDestinationMessage extends Message {
    private InetAddress requestorIP;
    private final int fileID;

    public RequestFileDestinationMessage(int sender, int destinationID, int fileID, InetAddress requestorIP) {
        super(sender);
        super.type = "RequestFileDestinationMessage";
        super.content = destinationID;
        this.fileID = fileID;
        this.requestorIP = requestorIP;
    }

    public InetAddress getRequestorIP() {
        return requestorIP;
    }

    public int getFileID() {
        return fileID;
    }
}
