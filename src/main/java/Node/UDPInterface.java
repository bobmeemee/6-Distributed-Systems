package Node;

import Messages.Message;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.*;
import java.util.Objects;

public class UDPInterface implements Runnable {
    private final Node node;
    private final InetAddress multicastAddress = InetAddress.getByName("255.255.255.255");
    private final DatagramSocket socket;
    private final int port = 8001;

    public UDPInterface(Node node) throws UnknownHostException, SocketException {
        this.node = node;
        this.socket = new DatagramSocket(this.port);
    }

    public void sendMulticast(Message m) throws IOException {
        String json = new Gson().toJson(m);

        byte[] buf = json.getBytes();

        DatagramPacket packet1 = new DatagramPacket(buf, buf.length, this.multicastAddress, this.port);
        DatagramPacket packet2 = new DatagramPacket(buf, buf.length, this.multicastAddress, this.port - 1);

        this.socket.send(packet1);
        this.socket.send(packet2);
        System.out.println("[NODE UDP]: Multicast sent type " + m.getType() );

    }

    public void sendUnicast(Message m, InetAddress destinationAddress, int destinationPort) throws IOException {
        String json = new Gson().toJson(m);
        byte[] buf = json.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, destinationAddress, destinationPort);
        this.socket.send(packet);
        if(!Objects.equals(m.getType(), "PingMessage")) {
            System.out.println("[NODE UDP]: Unicast sent to " + destinationAddress.toString() +":" + destinationPort
                    + " type " + m.getType() );
        }
    }


    @Override
    public void run() {
        System.out.println("[NODE UDP]: waiting for messages on port " +  this.port);
        try {
            while(true) {
                byte[] buf = new byte[2047];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                this.socket.receive(packet);
                Thread rq = new Thread( new RequestHandler(node, multicastAddress,packet));
                rq.start();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
