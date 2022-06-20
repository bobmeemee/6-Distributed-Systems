package Node;

import java.io.*;
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

    public void sendFile(InetAddress destinationAddress, File file, FileLog log) {
        try {
            Socket sendSocket = new Socket(destinationAddress,sendPort);

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(sendSocket.getOutputStream());
            objectOutputStream.writeObject(log);

            DataOutputStream dataOutputStream = new DataOutputStream(sendSocket.getOutputStream());
            FileInputStream fileInputStream = new FileInputStream(file);

            int bytes;
            byte[] buf = new byte[1024];
            while ((bytes=fileInputStream.read(buf))!=-1){
                dataOutputStream.write(buf,0, bytes);
                dataOutputStream.flush();
            }
            fileInputStream.close();

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
                Socket socket = receiveSocket.accept();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
