package Node;


import Messages.DiscoveryMessage;
import Messages.LeavingNetworkMessage;
import Messages.Message;
import Utils.HashFunction;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Node {
    private String name;
    private int nodeID;
    private int nextID;
    private int previousID;
    private UDPInterface udpInterface;
    private TCPInterface tcpInterface;
    public boolean hasFailed = false;

    private final LocalFileManager fileManager;
    private ReplicaManager replicaManager;


    public Node(String name) throws IOException {
        this.name = name;
        this.nodeID = HashFunction.hash(name);
        nextID = -1;
        previousID = -1;

        try {
            this.udpInterface = new UDPInterface(this);
            new Thread(this.udpInterface).start();

            this.tcpInterface = new TCPInterface(this);
            new Thread(this.tcpInterface).start();

        } catch (Exception e) {
            System.err.println("[NS] " + e);
            hasFailed = true;
        }

        // start discovery on this node
        this.discovery();

        // start monitoring local files
        this.fileManager = new LocalFileManager(this, "src/main/java/Node/files");
        new Thread(this.fileManager).start();

        this.replicaManager = new ReplicaManager(this, "src/main/java/Node/replicas");


        // shut node down
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[NODE] Shutdown hook");
            try {
                shutdown();
                this.replicaManager.shutdown();
                if(this.replicaManager.getFilesToMove() == 0) {
                    System.out.println("[NODE]: No replicas, shutting down...");
                } else {
                    Thread.sleep(5000);
                }
                if(replicaManager.getFilesToMove() == 0) {
                    System.out.println("[NODE] All replicas moved, shutting down...");
                } else {
                    System.out.println("[NODE] Moving replicas...");
                    Thread.sleep(5000);
                }
                 if(this.replicaManager.getFilesToMove() == 0) {
                     System.out.println("[NODE] All replicas moved, shutting down");
                 } else {
                     System.out.println("[NODE] Failed to move all replicas");
                 }

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }));


    }

    public int getNodeID() {
        return nodeID;
    }

    public UDPInterface getUdpInterface() {
        return udpInterface;
    }

    public LocalFileManager getFileManager() {
        return fileManager;
    }

    public int getNextID() {return nextID;}
    public void setNextID(int nextID) {this.nextID = nextID;}
    public int getPreviousID() {return previousID;}
    public void setPreviousID(int previousID) {this.previousID = previousID;}

    public TCPInterface getTcpInterface() {
        return tcpInterface;
    }

    public ReplicaManager getReplicaManager() {
        return replicaManager;
    }

    public void discovery() throws IOException {
        Message m = new DiscoveryMessage(this.nodeID);
        udpInterface.sendMulticast(m);
    }

    public void shutdown() throws IOException {
        Message m = new LeavingNetworkMessage(this.nodeID, this.previousID, this.nextID);
        udpInterface.sendMulticast(m);
    }


    public static void main(String[] args) throws IOException {
        Node node = new Node(args[0]);
    }

}
