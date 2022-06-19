package Node;

import java.io.Serializable;
import java.net.InetAddress;

public class FileLog implements Serializable {
    private String filename;
    private int fileID;
    private int ownerID;
    private int localOwnerID;
    private boolean replicated;

    // not sure if needed
    private InetAddress ownerIP;
    private InetAddress localOwnerIP;

    public FileLog(String filename, int fileID, int ownerID, int localOwnerID) {
        this.filename = filename;
        this.fileID = fileID;
        this.ownerID = ownerID;
        this.localOwnerID = localOwnerID;
    }

    public boolean isReplicated() {
        return replicated;
    }

    public void setReplicated(boolean replicated) {
        this.replicated = replicated;
    }

    public String getFilename() {
        return filename;
    }

    public int getFileID() {
        return fileID;
    }

    public int getOwnerID() {
        return ownerID;
    }

    public int getLocalOwnerID() {
        return localOwnerID;
    }

    public void setOwnerID(int ownerID) {
        this.ownerID = ownerID;
    }

    public void setLocalOwnerID(int localOwnerID) {
        this.localOwnerID = localOwnerID;
    }


}
