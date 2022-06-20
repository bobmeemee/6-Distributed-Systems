package Messages;

public class IPRequestMessage extends Message{


    public IPRequestMessage(int sender, int id) {
        super(sender);
        super.type = "IPRequestMessage";
        super.content = id;
    }
}
