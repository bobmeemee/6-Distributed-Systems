package Node;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class TCPFileHandler extends Thread{

    private final Node node;
    private final Socket socket;

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
            /*
            String filepath = node.getReplicaManager().addFile(log);

            int bytes;
            FileOutputStream fileOutputStream = new FileOutputStream(filepath);

            long size = dataInputStream.readLong();
            byte[] buffer = new byte[4*1024];
            while (size > 0 && (bytes = dataInputStream.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
                fileOutputStream.write(buffer,0,bytes);
                size -= bytes;
            }
            fileOutputStream.close();
            */
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }



    }
}
