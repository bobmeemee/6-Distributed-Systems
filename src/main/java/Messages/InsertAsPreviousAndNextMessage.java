package Messages;

public class InsertAsPreviousAndNextMessage extends Message {


    public InsertAsPreviousAndNextMessage(int sender) {
        super(sender);
        super.type = "InsertAsPreviousAndNextMessage";
    }
}
