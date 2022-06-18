package Messages;

public class FailureMessage extends Message{
    final int failedNext;
    final int failedPrev;

    public FailureMessage(int sender, int failedNode, int failedPrevious, int failedNext) {
        super(sender);
        super.content = failedNode;
        super.type= "FailureMessage";
        this.failedNext = failedNext;
        this.failedPrev = failedPrevious;
    }


    public int getFailedNext() {
        return failedNext;
    }

    public int getFailedPrev() {
        return failedPrev;
    }
}
