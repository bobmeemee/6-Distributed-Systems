package Node;

import java.util.HashMap;

public class ReplicaManager extends Thread{
    Node node;
    HashMap<String, FileLog> files; // string filename or int fileID?
    String replication;

    public ReplicaManager(Node node, String replication) {
        this.node = node;
        this.files = new HashMap<>();
        this.replication = replication;
    }

    // has to interact with tcp interface, dk what filename type
    public void addReplica(String filename, FileLog log) {
        files.put(filename, log);
    }

    // transferfunction for deletion here? or maybe just a getter?


    @Override
    public void run() {
        // check for changes and update in replicated file map - should look like localfilemanager


    }
}
