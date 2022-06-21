package Node;

import Messages.*;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class RequestHandler extends Thread {
    private final Node node;
    private final InetAddress multicastAddress;
    private final DatagramPacket receivedMessage;

    public RequestHandler(Node node, InetAddress multicastAddress,  DatagramPacket receivedMessage) {
        this.node = node;
        this.multicastAddress = multicastAddress;
        this.receivedMessage = receivedMessage;
    }

    public void run() {
        InetAddress senderIP = receivedMessage.getAddress();
        String json = new String(this.receivedMessage.getData(), 0, this.receivedMessage.getLength());
        Gson gson = new Gson();
        Message message = gson.fromJson(json, Message.class);
        int senderID = message.getSender();

        if(senderID == this.node.getNodeID()) {
            return;
        } else if(!Objects.equals(message.getType(),"PingMessage")){
            System.out.println("[NODE UDP]: received a " + message.getType() + " from " + senderID + " with address "
                    + senderIP + ":" + receivedMessage.getPort());
        }

        Message response = new Message(senderID);
        boolean sendUnicastResponse = false;

        switch (message.getType()) {
            case "DiscoveryMessage":
                // node was the only one in network
                if (this.node.getNodeID() == this.node.getNextID() && this.node.getNextID() != -1) {
                    this.node.setNextID(senderID);
                    this.node.setPreviousID(senderID);
                    response = new InsertAsPreviousAndNextMessage(this.node.getNodeID());
                    sendUnicastResponse = true;
                    System.out.println("[NODE]: new next " + this.node.getNextID());
                    System.out.println("[NODE]: new previous " + this.node.getPreviousID());
                    // sender is new next
                } else if (((senderID < this.node.getNextID()) && (senderID > this.node.getNodeID())) // standard case
                        || (this.node.getNextID() == this.node.getPreviousID() && this.node.getPreviousID() < this.node.getNodeID()) //two nodes in network
                        || (this.node.getNextID() < this.node.getNodeID() && senderID < this.node.getNextID()) // smaller than smallest
                        || (this.node.getNextID() < this.node.getNodeID() && senderID > this.node.getNodeID())) // bigger than biggest
                {
                    this.node.setNextID(senderID);
                    response = new InsertAsPreviousMessage(this.node.getNodeID());
                    sendUnicastResponse = true;
                    System.out.println("[NODE]: new next " + this.node.getNextID());
                    System.out.println("[NODE]: previous " + this.node.getPreviousID());
                    // sender is new prev
                } else if ((senderID > this.node.getPreviousID() && senderID < this.node.getNodeID()) //normal
                        || (this.node.getNextID() == this.node.getPreviousID() && this.node.getNextID() > this.node.getNodeID()) //two nodes case
                        || (this.node.getPreviousID() > this.node.getNodeID() && senderID < this.node.getNodeID()) // smaller than smallest
                        || (this.node.getPreviousID() > this.node.getNodeID() && senderID > this.node.getPreviousID())) //bigger than biggest
                {
                    this.node.setPreviousID(senderID);
                    response = new InsertAsNextMessage(this.node.getNodeID());
                    sendUnicastResponse = true;
                    System.out.println("[NODE]: next " + this.node.getNextID());
                    System.out.println("[NODE]: new previous " + this.node.getPreviousID());
                }

                // check if replica needs to be moved to another node
                /* not needed, error prone
                HashMap<Integer, FileLog> h = this.node.getReplicaManager().getfileLogs();
                for (HashMap.Entry<Integer, FileLog> entry : h.entrySet()) {
                    if(entry.getKey() > senderID && senderID > this.node.getNodeID()) {
                        //
                        FileLog log = entry.getValue();
                        log.setOwnerID(senderID);
                        log.setOwnerIP(senderIP);

                        this.node.getTcpInterface().sendFile(senderIP, , log);
                    }
                }
                */


                break;
            case "LeavingNetworkMessage":
                LeavingNetworkMessage m = gson.fromJson(json, LeavingNetworkMessage.class);

                if (senderID == this.node.getPreviousID())
                {
                    node.setPreviousID(m.getPreviousID());
                System.out.println("[NODE]: Node (previousID) " + senderID + " left the network\n" +
                        "new previous node: " + this.node.getPreviousID());
                }
                if(senderID == this.node.getNextID())
                {
                    node.setNextID(m.getNextID());
                    System.out.println("[NODE]: Node (nextID) " + senderID + "left the network\n" +
                            "new next node: " + this.node.getNextID());
                }
                break;
            case "NodeCountMessage":
                System.out.println("[NODE UDP]: " + "currently " + message.getContent() + " other nodes in network");
                if(message.getContent() == 0) {
                    this.node.setNextID(this.node.getNodeID());
                    this.node.setPreviousID(this.node.getNodeID());
                    System.out.println("[NODE]: Only node in network");
                    System.out.println("[NODE]: nextNodeID: " + this.node.getNextID());
                    System.out.println("[NODE]: previousNodeID: " + this.node.getPreviousID());
                }
                break;

            case "InsertAsNextMessage":
                this.node.setNextID(senderID);
                System.out.println("[NODE UDP]: New next node ID: " + this.node.getNextID());
                break;
            case "InsertAsPreviousMessage" :
                this.node.setPreviousID(senderID);
                System.out.println("[NODE UDP]: New previous node ID: " + this.node.getPreviousID());
                break;

            case "InsertAsPreviousAndNextMessage":
                this.node.setPreviousID(senderID);
                this.node.setNextID(senderID);
                System.out.println("[NODE UDP]: New previous and next node ID: " + this.node.getPreviousID());

            case "FailureMessage":
                if(this.node.getPreviousID() == message.getContent()) {
                    FailureMessage mF = gson.fromJson(json, FailureMessage.class);
                    node.setPreviousID(mF.getFailedPrev());
                    System.out.println("[NODE UDP]: Previous node " + message.getContent() + " failed \n" +
                            "new previous node: " + this.node.getPreviousID());
                }
                if(this.node.getNextID() == message.getContent()) {
                    FailureMessage mF = gson.fromJson(json, FailureMessage.class);
                    node.setNextID(mF.getFailedNext());
                    System.out.println("[NODE UDP]: Next node " + message.getContent() + " failed \n" +
                            "new next node: " + this.node.getNextID());
                }

                break;

            case "PingMessage":
                if(!node.hasFailed) {
                    response = new PingMessage(this.node.getNodeID());
                    sendUnicastResponse = true;
                }

                break;

            case "FileOwnerIDMessage":
                FileOwnerIDMessage fileMessage = gson.fromJson(json, FileOwnerIDMessage.class);
                InetAddress ownerIP = fileMessage.getOwnerIP();
                int fileID = fileMessage.getContent();
                int ownerID = fileMessage.getOwnerID();
                try {
                    if(ownerIP == InetAddress.getByName("0.0.0.0")) {
                        System.out.println("[NODE]: filename for file with hash " + fileID + " is not available");
                    } else {
                        // send file to dest with tcp interface
                        File file = this.node.getFileManager().getFile(fileID);
                        FileLog log = this.node.getFileManager().getFileLog(fileID);

                        // change file log
                        log.setOwnerID(ownerID);
                        log.setLocalOwnerID(this.node.getNodeID());
                        log.setOwnerIP(ownerIP);
                        log.setLocalOwnerIP(InetAddress.getLocalHost());
                        ArrayList<Integer> d = new ArrayList<>();
                        d.add(this.node.getNodeID());
                        d.add(ownerID);
                        log.setDownloadLocations(d);
                        log.setReplicated(true);

                        // send replica to owner via tcp
                        this.node.getTcpInterface().sendFile(ownerIP, file,log);

                        // send updated log to local owner
                        UpdateFileLogMessage flm = new UpdateFileLogMessage(this.node.getNodeID(), fileID, log);
                        this.node.getUdpInterface().sendUnicast(flm, flm.getLog().getLocalOwnerIP(), 8001);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;

            case "UpdateFileLogMessage:":
                UpdateFileLogMessage filelogMessage = gson.fromJson(json, UpdateFileLogMessage.class);

                if( this.node.getFileManager().getFileLog(message.getContent()) != null) {
                    this.node.getFileManager().updateFileLog(message.getContent(), filelogMessage.getLog());
                }

                break;

            case "RequestFileDestinationMessage":
                if(message.getContent() == this.node.getNodeID()) {
                    RequestFileDestinationMessage fdm = gson.fromJson(json, RequestFileDestinationMessage.class);
                    if(this.node.getFileManager().getFile(fdm.getFileID()) != null) {
                        response = new RequestFileDestinationMessage(senderID, this.node.getPreviousID(), fdm.getFileID(),
                                senderIP);
                        System.out.println("[NODE]: Node is local owner, passing message");
                    } else {
                        // reply with dest
                        ResponseFileDestinationMessage rfdm = new ResponseFileDestinationMessage(this.node.getNodeID(),
                                fdm.getFileID());
                        try {
                            System.out.println("[NODE UDP]: Responding to " + senderID + " with this node as" +
                                    " file destination IP");
                            this.node.getUdpInterface().sendUnicast(rfdm, fdm.getRequestorIP(), receivedMessage.getPort());
                        } catch (IOException e) {
                            node.hasFailed = true;
                            e.printStackTrace();
                        }

                    }
                }
                break;
            case "ResponseFileDestinationMessage":
                // get file and log
                FileLog log = this.node.getReplicaManager().getFileLog(message.getContent());
                File file = new File("src/main/java/Node/replicas/" + log.getFilename());
                // update log
                log.setOwnerIP(senderIP);
                log.setOwnerID(senderID);
                ArrayList<Integer> l = new ArrayList<>();
                l.add(log.getLocalOwnerID());
                l.add(senderID);
                log.setDownloadLocations(l);

                // send file and updated log
                this.node.getTcpInterface().sendFile(senderIP, file, log);

                // update localowner log
                UpdateFileLogMessage ufm = new UpdateFileLogMessage(this.node.getNodeID(), log.getFileID(), log);
                try {
                    this.node.getUdpInterface().sendUnicast(ufm, log.getLocalOwnerIP(), 8001);
                } catch (IOException e) {
                    this.node.hasFailed = true;
                    e.printStackTrace();
                }

                // let node know file is replicated when shutdown
                this.node.getReplicaManager().decrementFilesToMove();



                break;


            default:
                break;
        }

        if(sendUnicastResponse && !Objects.equals(response.getType(), "message")) {
            try {
                this.node.getUdpInterface().sendUnicast(response, senderIP, receivedMessage.getPort());
            } catch (IOException e) {
                node.hasFailed = true;
                e.printStackTrace();
            }
        } else if(!Objects.equals(response.getType(), "message")) {
            try {
                this.node.getUdpInterface().sendMulticast(response);
            } catch (IOException e) {
                node.hasFailed = true;
                e.printStackTrace();
            }
        }

    }
}
