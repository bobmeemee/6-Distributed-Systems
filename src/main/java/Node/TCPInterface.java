package Node;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPInterface implements Runnable{
    private final Node node;
    private final int port = 8002;
    private ServerSocket receiveSocket;
    private final String path = "src/main/java/Node/replicas";

    public TCPInterface(Node node) {
        this.node = node;
        try {
            receiveSocket = new ServerSocket(port);
        } catch (IOException e) {
            this.node.hasFailed =  true;
            e.printStackTrace();
        }
    }

    public void sendFile(InetAddress destinationAddress, File file, FileLog log) {
        try {
            Socket sendSocket = new Socket(destinationAddress,port);

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(sendSocket.getOutputStream());
            objectOutputStream.writeObject(log);

            DataOutputStream dataOutputStream = new DataOutputStream(sendSocket.getOutputStream());
            FileInputStream fileInputStream = new FileInputStream(file);

            dataOutputStream.writeLong(file.length());

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
        System.out.println("[NODE TCP]: Opening TCP listening on port " + port);
        while(true) {
            try {
                Socket socket = receiveSocket.accept();
                System.out.println("[NODE TCP]: accepted connection from"  + socket.getInetAddress() );
                /* uncomment if wanting to wait for first file written
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

                FileLog log = (FileLog) objectInputStream.readObject();


                String filename = log.getFilename();
                FileOutputStream fileOutputStream = new FileOutputStream(this.path + "/" + filename);

                int bytes;
                long size = dataInputStream.readLong();
                byte[] buffer = new byte[4*1024];
                while (size > 0 && (bytes = dataInputStream.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
                    fileOutputStream.write(buffer,0,bytes);
                    size -= bytes;
                }
                fileOutputStream.close();

                // add replica to database


                this.node.getReplicaManager().addReplica(log);

                 */
                TCPFileHandler tfh = new TCPFileHandler(this.node, socket );
                tfh.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
