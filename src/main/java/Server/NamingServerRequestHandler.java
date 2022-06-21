package Server;


import Messages.FileOwnerIDMessage;
import Messages.Message;
import Messages.NodeCountMessage;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

public class NamingServerRequestHandler extends Thread {
    private NamingServer server;
    private InetAddress multicastAddress;
    private DatagramPacket receivedMessage;
    private Message response;

    public NamingServerRequestHandler(NamingServer server, InetAddress multicastAddress,  DatagramPacket receivedMessage) {
        this.server = server;
        this.multicastAddress = multicastAddress;
        this.receivedMessage = receivedMessage;
    }

    public void run() {
        String json = new String(this.receivedMessage.getData(), 0, this.receivedMessage.getLength());
        Gson gson = new Gson();
        Message message = gson.fromJson(json, Message.class);

        InetAddress senderIP = receivedMessage.getAddress();
        int senderID = message.getSender();

        if(senderID == this.server.getServerID()) {
            return;
        } else if (!Objects.equals(message.getType(), "PingMessage")){
            System.out.println("[NS UDP]: received a " + message.getType() + " from " + senderID + " with address "
                    + senderIP + ":" + receivedMessage.getPort());
        }

        Message response = new Message(this.server.getServerID());
        boolean responseIsMulticast = true;
        switch(message.getType()) {
            case "DiscoveryMessage":
                try {
                    String s = server.addNode(senderID, senderIP);
                    System.out.println("[NS UDP]: " + s);
                    response = new NodeCountMessage(this.server.getServerID(), this.server.getNodeCount() - 1);
                    responseIsMulticast = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case "LeavingNetworkMessage":
                try {
                    String s = server.deleteNode(senderID);
                    System.out.println("[NS UDP]: " + s);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            default:
                break;

            case "PingMessage":
                this.server.getNodeFailureWatcher(senderID).incrementTimeOutCounter();

                break;

            case "GetFileOwnerMessage":
                // no replication if only node in network

                if(server.getNodeCount() > 1) {
                    // get IP node to replicate
                    try {
                        int fileID = message.getContent();
                        int ownerID = server.getFileOwner(fileID, senderID);
                        // do not replicate if local file owner is remote owner or if filename is taken
                        if(ownerID != senderID && !this.server.getFileMap().containsKey(fileID)) {
                            System.out.println("[NAMINGSERVER]: owner ID " + ownerID);
                            System.out.println("[NAMINGSERVER]: owner IP " + this.server.getNodeIP(ownerID));
                            InetAddress ownerIP = InetAddress.getByName(this.server.getNodeIP(ownerID));
                            response = new FileOwnerIDMessage(this.server.getServerID(),fileID, ownerID ,ownerIP);
                            responseIsMulticast = false;
                        } else if(this.server.getFileMap().containsKey(fileID)) {
                            response = new FileOwnerIDMessage(this.server.getServerID(),fileID); // no owner
                            responseIsMulticast = false;

                        }
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }

                break;

        }

        if(responseIsMulticast) {
            try {
                if(!Objects.equals(response.getType(), "message")) {
                    this.server.getUdpInterface().sendMulticast(response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                this.server.getUdpInterface().sendUnicast(response, senderIP, receivedMessage.getPort());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
