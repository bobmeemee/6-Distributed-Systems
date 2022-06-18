package Server;

import Messages.FailureMessage;
import Messages.Message;
import Messages.PingMessage;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;

public class FailureWatcher extends Thread {

    private final InetAddress address;
    private final int nodeID;
    private final NamingServer server;
    public AtomicInteger timeOutCounter;
    public final int timeoutInterval = 2000;

    public FailureWatcher(NamingServer server, InetAddress nodeAddress, int nodeID) {
        this.address = nodeAddress;
        this.nodeID = nodeID;
        this.server = server;
        this.timeOutCounter = new AtomicInteger(3);
    }

    public int getTimeOutCounter() {
        return timeOutCounter.get();
    }


    public void incrementTimeOutCounter() {
        while (true) {
            int existingValue = getTimeOutCounter();
            int newValue = existingValue + 1;
            if(timeOutCounter.compareAndSet(existingValue, newValue)) {
                return;
            }
        }
    }

    public void decrementTimeOutCounter() {
        while (true) {
            int existingValue = getTimeOutCounter();
            int newValue = existingValue - 1;
            if(timeOutCounter.compareAndSet(existingValue, newValue)) {
                return;
            }
        }
    }


    @Override
    public void run() {
        System.out.println("[NS UDP]: started FailureWatcher for node " + nodeID);
        while (true) {
            try {
                PingMessage ping = new PingMessage(server.getServerID());
                server.getUdpInterface().sendUnicast(ping, address, 8001);
                try {
                    Thread.sleep(timeoutInterval);
                } catch (InterruptedException e) {
                    return;
                }
                decrementTimeOutCounter();
                if(timeOutCounter.get() < 3)
                {
                    System.out.println("[NS FAIL]: Node " + nodeID + " unreachable for " + timeoutInterval* (3-timeOutCounter.get())
                    + "ms");

                }
                if(timeOutCounter.get() == 0) {

                    int belowFailed = server.getLowerNodeID(nodeID);
                    int aboveFailed = server.getUpperNodeID(nodeID);
                    FailureMessage mFailure = new FailureMessage(server.getServerID(), nodeID, belowFailed, aboveFailed);

                    server.getUdpInterface().sendMulticast(mFailure);

                    System.out.println("[NS FAIL]: Node " + nodeID + " failed \n");
                    String s = server.deleteFailedNode(nodeID);
                    System.out.println(s);
                    return;
                }

            } catch ( IOException e) {
                e.printStackTrace();
            }

        }

    }

}
