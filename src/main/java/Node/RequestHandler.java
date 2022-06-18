package Node;

import Messages.*;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
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


            default:
                break;
        }

        if(sendUnicastResponse) {
            try {
                this.node.getUdpInterface().sendUnicast(response, senderIP, receivedMessage.getPort());
            } catch (IOException e) {
                node.hasFailed = true;
                e.printStackTrace();
            }
        }

    }
}
