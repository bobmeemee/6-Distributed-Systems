package Node;

import Messages.DeleteFileMessage;
import Utils.HashFunction;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

public class ReplicaManager extends Thread{
    private final Node node;
    private HashMap<File, FileLog> files; // string filename or int fileID?
    private HashMap<Integer, FileLog> fileLogs;
    private final String path;

    public ReplicaManager(Node node, String path) {
        this.node = node;
        this.files = new HashMap<>();
        this.fileLogs = new HashMap<>();
        this.path = path;
    }



    public void addReplica(FileLog log) {
        fileLogs.put(log.getFileID(), log);
        System.out.println("[NODE]: Replicamanager added file " + log.getFilename());
    }

    public List<Integer> getReplicaIDs() {
        return new ArrayList<>(fileLogs.keySet());
    }

    public HashMap<Integer, FileLog> getfileLogs() {
        return fileLogs;
    }

    // transferfunction for deletion here? or maybe just a getter?

    // not necessary apparently
    @Override
    public void run() {
        ArrayList<String> filenames = new ArrayList<>();
        while (true) {


            File f = new File(this.path);
            File[] fList = f.listFiles();
            ArrayList<String> newFileNames = new ArrayList<>(Arrays.asList(Objects.requireNonNull(f.list())));

            // file was deleted, send delete to replicas/localfilemanager
            if(filenames.size() > newFileNames.size() && fList != null) {
                for (File file : fList) {
                    String filename = file.getName();
                    if(!newFileNames.contains(filename) && files.get(file).isReplicated()) {
                        FileLog log = this.files.get(file);
                        DeleteFileMessage m = new DeleteFileMessage(this.node.getNodeID(), log.getFileID());
                        try {
                            // send to local owner
                            this.node.getUdpInterface().sendUnicast(m,log.getLocalOwnerIP() ,8001);
                            // send to server
                            this.node.getUdpInterface().sendUnicast(m, InetAddress.getByName("255.255.255.255"), 8000);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
           filenames = newFileNames;
        }
        // check for changes and update in replicated file map - should look like localfilemanager


    }
}
