package Messages;

import Utils.HashFunction;

public class Message {
    protected String type;
    protected int sender;
    protected int content;

    public Message(int sender) {
        this.sender = sender;
        this.type = "message";
    }

    public Message(String name) {
        this.sender = HashFunction.hash(name);
    }



    public String getType() {
        return type;
    }

    public int getSender() {
        return sender;
    }


    public int getContent() {
        return content;
    }
}
