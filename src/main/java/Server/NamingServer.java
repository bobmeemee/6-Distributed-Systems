package Server;

import Utils.HashFunction;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
public class NamingServer {
    private final CustomMap nodeMap;
    private HashMap<Integer, Integer> fileMap;
    private HashMap<Integer, FailureWatcher> failureMap;

    private NamingServerUDPInterface udpInterface;

    public NamingServer()  {
        System.out.println("[NAMINGSERVER]: Starting...");
        nodeMap = new CustomMap();
        fileMap = new HashMap<>();
        failureMap = new HashMap<>();
        try {
            this.udpInterface = new NamingServerUDPInterface(this);
            new Thread(this.udpInterface).start();
        } catch (UnknownHostException | SocketException e) {
            System.err.println("Failed to start NSUDPinterface " + e);
        }

    }

    @PostMapping("/NamingServer/Nodes/{node}")
    public String addNodeREST(@PathVariable(value = "node") String name, @RequestBody String IP) throws IOException {
        int nodeID = HashFunction.hash(name);
        if (nodeMap.putIfAbsent(nodeID, IP) == null) {
            nodeMap.exportMap();
            return "[NS REST] Node added with ID=" + nodeID + "!";
        } else {
            return "[NS REST] This name is not available!\n";
        }
    }


    @DeleteMapping("/NamingServer/Nodes/{node}")
    public String removeNodeREST(@PathVariable(value = "node") String name) throws IOException {
        int nodeID = HashFunction.hash(name);
        if(nodeMap.remove(nodeID) == null) {
            return "[NS REST] Node " + name + " does not exist\n" ;

        } else {
            nodeMap.exportMap();
            return "[NS REST] Removed node " + name + "\n";
        }
    }


    @GetMapping("/NamingServer/Nodes/{node}")
    public String getNodes(@PathVariable(value = "node") String name){
        int nodeID = HashFunction.hash(name);
        String send;
        Set<Map.Entry<Integer,String>> entries = nodeMap.entrySet();
        if(nodeMap.containsKey(nodeID)) {
            int i = 1;
            StringBuilder nodes = new StringBuilder();

            for (Map.Entry<Integer, String> entry : entries) {
                nodes.append("Node #");
                nodes.append(i);
                nodes.append(": ");
                nodes.append(entry.getKey().toString());
                nodes.append(" with IP ");
                nodes.append(entry.getValue());
                nodes.append(",");
                i++;
            }

            send = "{\"Node status\":\"node exists\"," + "\"Node hash\":" + nodeID + "," +
                    "\"Nodes in network\":" + nodeMap.size() +
                    "\"All nodes\":\"" + nodes + "\"}";
        }
        else{
            send = "{\"Node does not exist\"}";
        }
        return send;
    }

    public String addNode(int nodeID, InetAddress IP) throws IOException {
        String ip = IP.getHostAddress();
        if (nodeMap.putIfAbsent(nodeID, ip) == null) {

            FailureWatcher f = new FailureWatcher(this, IP, nodeID);
            failureMap.put(nodeID, f);
            f.start();
            nodeMap.exportMap();
            return "Added node with hash " + nodeID + " and IP" + IP + " to database";
        } else {
            return "Name with hash " + nodeID + " not available";
        }
    }

    public String deleteNode(int nodeID) throws IOException {
        if(nodeMap.remove(nodeID) == null) {
            return "Node with hash " + nodeID + " does not exist";
        } else {
            failureMap.get(nodeID).interrupt();
            failureMap.remove(nodeID);
            nodeMap.exportMap();
            return "Node with hash " + nodeID + " is deleted";
        }
    }

    public String deleteFailedNode(int nodeID) throws IOException {
        if(nodeMap.remove(nodeID) == null) {
            return " [NAMINGSERVER] Node with hash " + nodeID + " does not exist";
        } else {
            failureMap.remove(nodeID);
            nodeMap.exportMap();
            return "[NAMINGSERVER] Node with hash " + nodeID + " is deleted";
        }
    }

    public int getLowerNodeID(int nodeID) throws IOException {
        if(nodeMap.lowerKey(nodeID) ==null) {
            return nodeMap.lastKey();
        } else {
            return nodeMap.lowerKey(nodeID);
        }
    }

    public int getUpperNodeID(int nodeID) throws IOException {
        if(nodeMap.higherKey(nodeID) == null ) {
            return nodeMap.firstKey();
        } else {
            return nodeMap.higherKey(nodeID);
        }
    }


    public int getServerID() {
        return 0;
    }

    public int getNodeCount() {
        return nodeMap.size();
    }

    public NamingServerUDPInterface getUdpInterface() {
        return udpInterface;
    }

    public FailureWatcher getNodeFailureWatcher(int nodeID) {
        return this.failureMap.get(nodeID);
    }

    public int getFileOwner(int fileID, int senderID) throws UnknownHostException {
        int owner = senderID;
        for(int key : this.nodeMap.keySet()) {
            if(key < fileID) {
                owner = key;
            }
        }
        return owner;
    }

    public String getNodeIP(int nodeID) {
        return nodeMap.get(nodeID);
    }

    public HashMap<Integer, Integer> getFileMap() {
        return fileMap;
    }

    // testing purposes
    public static void main(String[] args) {
        System.out.println("[NAMINGSERVER]: Starting...");
        NamingServer namingServer = new NamingServer();

    }
}
