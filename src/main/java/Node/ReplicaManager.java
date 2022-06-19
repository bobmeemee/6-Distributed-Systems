package Node;

import java.util.ArrayList;

public class ReplicaManager extends Thread{
    Node node;
    ArrayList<String> filenames;

    public ReplicaManager(Node node) {
        this.node = node;
        // empty out
    }

    // has to interact with tcp interface, dk what filename type
    public void addReplica(String filename) {
        filenames.add(filename);
    }


    @Override
    public void run() {
        // check for changes and update


    }
}
