package Node;

public class TCPInterface implements Runnable{
    private Node node;
    private int sendPort = 8002;
    private int receivePort = sendPort +1;

    public TCPInterface(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        System.out.println("[NODE TCP]: Opening TCP listening on port" + receivePort);
        while(true) {

        }

    }
}
