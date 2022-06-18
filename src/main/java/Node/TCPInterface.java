package Node;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPInterface implements Runnable{
    private final Node node;
    private final int sendPort = 8002;
    private final int receivePort = sendPort +1;
    ServerSocket receiveSocket;

    public TCPInterface(Node node) {
        this.node = node;
        try {
            receiveSocket = new ServerSocket(receivePort);
        } catch (IOException e) {
            this.node.hasFailed =  true;
            e.printStackTrace();
        }
    }

    public void sendUnicast(InetAddress destinationAddress) {
        try {
            Socket sendSocket = new Socket("localhost",sendPort);

            OutputStream outputStream;
            OutputStreamWriter outputStreamWriter;


        } catch (IOException e) {
            this.node.hasFailed = true;
            e.printStackTrace();
        }

    }


    @Override
    public void run() {
        System.out.println("[NODE TCP]: Opening TCP listening on port" + receivePort);
        while(true) {
            try {
                Socket sendSocket = receiveSocket.accept();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
