package Messages;

public class ResponseFileDestinationMessage extends Message{


    public ResponseFileDestinationMessage(int sender, int fileID) {
        super(sender);
        super.type="ResponseFileDestinationMessage";
        super.content = fileID;

    }
}
