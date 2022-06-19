package Node;

import Messages.GetFileOwnerMessage;
import Utils.HashFunction;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class LocalFileManager extends Thread{
    private final Node node;
    private HashMap<Integer, InetAddress> fileReplicaLocations;
    ArrayList<String> filenames;

    public LocalFileManager(Node node, String filepath) {
        this.node = node;
        fileReplicaLocations = new HashMap<>();
    }

    public void addFileOwner(int fileID, InetAddress ownerIP) {
        this.fileReplicaLocations.put(fileID, ownerIP);
    }

    @Override
    public void run() {

        // do not start until connection with the server is established
        // after testing -> change to do not start connection until other node joined the network
        while(this.node.getNextID() == -1  || this.node.getPreviousID() == -1) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return;
            }
        }

        // get file database
        File f = new File("C:\\Users\\ilias\\derde_bachelor\\6-DS\\lab5\\src\\main\\java\\Node\\files");
        this.filenames = new ArrayList<>(Arrays.asList(Objects.requireNonNull(f.list())));
        System.out.println("[NODE]: Starting local file manager... ");
        System.out.println("[NODE]: Local files found: " + filenames);

        // send udp to request filenames
        try {
            for (String filename : filenames) {
                System.out.println("[NODE]: Requesting replication address for: " + filename);
                int fileID = HashFunction.hash(filename);
                node.getUdpInterface().sendUnicast(new GetFileOwnerMessage(this.node.getNodeID(), fileID),
                        InetAddress.getByName("255.255.255.255"),
                        8000);
            }
        } catch (IOException e) {
            node.hasFailed = true;
            e.printStackTrace();
        }

        while (true) {
            // interval, interrupt catch covers shutdown while sleeping -> not an error in our case
            try {
                sleep(4000);
            } catch (InterruptedException e) {
                return;
            }
            // keep checking changes
            File f1 = new File("C:\\Users\\ilias\\derde_bachelor\\6-DS\\lab5\\src\\main\\java\\Node\\files");
            ArrayList<String> newFileNames = new ArrayList<>(Arrays.asList(Objects.requireNonNull(f1.list())));

            // update -> send new file req
            if(newFileNames.size() > this.filenames.size()) {
                System.out.println("[NODE]: New files found on node");
                for(String filename : newFileNames) {
                    if(!this.filenames.contains(filename)) {
                        System.out.println("[NODE]: Requesting replication address for: " + filename);
                        int fileID = HashFunction.hash(filename);
                        try {
                            node.getUdpInterface().sendUnicast(new GetFileOwnerMessage(this.node.getNodeID(), fileID),
                                    InetAddress.getByName("255.255.255.255"),
                                    8000);
                        } catch (IOException e) {
                            this.node.hasFailed = true;
                            e.printStackTrace();
                        }
                    }
                }
                // update file names
                this.filenames = newFileNames;
            }

            // file deleted
            if(this. filenames.size() > newFileNames.size()) {
                for (String filename : newFileNames) {
                    if(!newFileNames.contains(filename)) {
                        int fileID = HashFunction.hash(filename);
                        // send delete file message tcp? udp?

                    }
                }
            }
        }
    }
}
