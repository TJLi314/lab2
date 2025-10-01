public class ParserResult {
    private final InterRep head;
    private final boolean success;
    private final int opCount;

    public ParserResult(InterRep head, boolean success, int opCount) {
        this.head = head;
        this.success = success;
        this.opCount = opCount;
    }

    public InterRep getHead() {
        return head;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getOpCount() {
        return opCount;
    }
}