package Node;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class TCPFileHandler extends Thread{

    private final Node node;
    private final Socket socket;
    private final String path = "src/main/java/Node/replicas";

    public TCPFileHandler(Node node, Socket socket) {
        this.node = node;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

            FileLog log = (FileLog) objectInputStream.readObject();


            String filename = log.getFilename();
            FileOutputStream fileOutputStream = new FileOutputStream(path + "/" + filename);

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
            System.out.println("[NODE TCP]: received file to be replicated " + filename);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }



    }
}
