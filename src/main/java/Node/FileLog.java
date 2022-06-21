package Node;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;

public class FileLog implements Serializable {
    private String filename;
    private int fileID;
    private int ownerID;
    private int localOwnerID;
    private boolean replicated;
    private ArrayList<Integer> downloadLocations;

    // not sure if needed
    private InetAddress ownerIP;
    private InetAddress localOwnerIP;

    public void setLocalOwnerIP(InetAddress localOwnerIP) {
        this.localOwnerIP = localOwnerIP;
    }

    public FileLog(String filename, int fileID, int ownerID, int localOwnerID) {
        this.filename = filename;
        this.fileID = fileID;
        this.ownerID = ownerID;
        this.localOwnerID = localOwnerID;
        this.downloadLocations = new ArrayList<Integer>();
        this.downloadLocations.add(this.localOwnerID);
    }

    public void setOwnerIP(InetAddress ownerIP) {
        this.ownerIP = ownerIP;
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
        if(this.downloadLocations.size() == 1) {
            this.downloadLocations.add(ownerID);
        }
    }

    public void setDownloadLocations(ArrayList<Integer> downloadLocations) {
        this.downloadLocations = downloadLocations;
    }


    public void setLocalOwnerID(int localOwnerID) {
        this.localOwnerID = localOwnerID;
    }

    public InetAddress getOwnerIP() {
        return ownerIP;
    }

    public InetAddress getLocalOwnerIP() {
        return localOwnerIP;
    }
}
