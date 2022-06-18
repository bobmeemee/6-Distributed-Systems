package Messages;

public class InsertAsPreviousMessage extends Message{
    public InsertAsPreviousMessage(int sender) {
        super(sender);
        super.type = "InsertAsPreviousMessage";

    }
}
