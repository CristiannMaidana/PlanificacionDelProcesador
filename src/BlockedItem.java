public class BlockedItem {
    Process p;
    int ioRemaining;

    BlockedItem(Process p, int ioRemaining) {
        this.p = p;
        this.ioRemaining = ioRemaining;
    }
}
