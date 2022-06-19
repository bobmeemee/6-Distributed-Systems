package Messages;

public class GetFileOwnerMessage extends Message {

    public GetFileOwnerMessage(int sender, int fileID) {
        super(sender);
        super.type = "GetFileOwnerMessage";
        super.content = fileID;
    }
}
