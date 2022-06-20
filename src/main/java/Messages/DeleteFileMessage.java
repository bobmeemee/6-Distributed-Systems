package Messages;

public class DeleteFileMessage extends Message {

    public DeleteFileMessage(int sender, int fileID) {
        super(sender);
        this.type = "DeleteFileMessage";
        this.content = fileID;
    }
}
