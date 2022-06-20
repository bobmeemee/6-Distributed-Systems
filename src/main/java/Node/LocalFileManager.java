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
    private HashMap<File, FileLog> fileMap;
    private HashMap<Integer, File> fileIDMap; // collapse in one map?
    private ArrayList<String> filenames;

    public LocalFileManager(Node node, String filepath) {
        this.node = node;
        fileMap = new HashMap<>();
        fileIDMap = new HashMap<>();
    }



    public FileLog createFileLog(File file) {
        String filename = file.getName();
        fileIDMap.put(HashFunction.hash(filename), file);
        return new FileLog(filename, HashFunction.hash(filename), -1, this.node.getNodeID());
    }

    public File getFile(int fileID) {
        return this.fileIDMap.get(fileID);
    }

    public FileLog getFileLog(int fileID) {
        return this.fileMap.get(this.getFile(fileID));
    }


    public void initialize() {
        File f = new File("C:\\Users\\ilias\\derde_bachelor\\6-DS\\lab5\\src\\main\\java\\Node\\files");
        this.filenames = new ArrayList<>(Arrays.asList(Objects.requireNonNull(f.list())));
        System.out.println("[NODE]: Starting local file manager... ");
        System.out.println("[NODE]: Local files found: " + filenames);
        File[] fileList = f.listFiles();
        if(fileList != null) {
            for (File file :fileList) {
                fileMap.put(file, createFileLog(file));
            }
        }
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

        this.initialize();

        // send udp to request filenames
        try {
            for (File file : this.fileMap.keySet()) {
                System.out.println("[NODE]: Requesting replication address for: " + file.getName());
                int fileID = HashFunction.hash(file.getName());
                node.getUdpInterface().sendUnicast(new GetFileOwnerMessage(this.node.getNodeID(), fileID),
                        InetAddress.getByName("255.255.255.255"),
                        8000);
            }
        } catch (IOException e) {
            node.hasFailed = true;
            e.printStackTrace();
        }

        // check database for updates etc. in infinite loop
        while (true) {
            // interval, interrupt catch covers shutdown while sleeping -> not an error in our case
            try {
                sleep(4000);
            } catch (InterruptedException e) {
                return;
            }
            // keep checking folder
            File f1 = new File("C:\\Users\\ilias\\derde_bachelor\\6-DS\\lab5\\src\\main\\java\\Node\\files");
            File[] f1List = f1.listFiles();
            ArrayList<String> newFileNames = new ArrayList<>(Arrays.asList(Objects.requireNonNull(f1.list())));

            // file added -> send new file req
            if( (newFileNames.size() > this.filenames.size()) && (f1List != null) ) {
                System.out.println("[NODE]: New files found on node");
                for(File file : f1List) {
                    String filename = file.getName();
                    if(!this.filenames.contains(file.getName())) {
                        System.out.println("[NODE]: Requesting replication address for: " + filename);
                        // create file log entry for new file
                        this.fileMap.put(file, createFileLog(file));

                        // calculate hash and send req
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

            // file deleted -> send update if file is replicated
            if(this.filenames.size() > newFileNames.size() && f1List != null) {
                for (File file : f1List) {
                    String filename = file.getName();
                    if(!newFileNames.contains(filename) && fileMap.get(file).isReplicated()) {

                        int fileID = HashFunction.hash(filename);
                        // send delete file message tcp? udp?


                    }
                }
            }
        }
    }
}
