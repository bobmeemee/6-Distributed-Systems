package Messages;

public class InsertAsNextMessage extends Message{

    public InsertAsNextMessage(int sender) {
        super(sender);
        super.type = "InsertAsNextMessage";
    }
}
