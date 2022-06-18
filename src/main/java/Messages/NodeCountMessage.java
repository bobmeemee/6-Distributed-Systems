package Messages;

public class NodeCountMessage extends Message {

    public NodeCountMessage(int sender, int nodeCount) {
        super(sender);
        super.type = "NodeCountMessage";
        this.content = nodeCount;
    }

    @Override
    public int getContent() {
        return this.content;
    }
}
