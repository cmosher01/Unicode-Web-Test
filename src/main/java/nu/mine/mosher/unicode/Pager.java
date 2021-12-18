package nu.mine.mosher.unicode;

@SuppressWarnings("unused")
public class Pager {
    public static final int PAGE_SIZE = 0x1000;

    public final long start;
    public final long prev;
    public final long next;
    public final long maxCodepoint;
    public final boolean compact;
    public final int rowlen;
    public final boolean invalid;

    Pager(long start, long maxCodepoint, boolean compact, int rowlen, boolean invalid) {
        this.start = start;
        this.prev = start - PAGE_SIZE;
        this.next = start + PAGE_SIZE;
        this.maxCodepoint = maxCodepoint;
        this.compact = compact;
        this.rowlen = rowlen;
        this.invalid = invalid;
    }

    public String getStart() {
        return String.format("U+%03X\u00d7\u00d7\u00d7", this.start / PAGE_SIZE);
    }

    public int getRowlen() {
        return 31 - Integer.numberOfLeadingZeros(this.rowlen);
    }

    public String getPrev() {
        if (this.prev < 0) {
            return f(this.maxCodepoint / PAGE_SIZE * PAGE_SIZE);
        }
        return f(this.prev);
    }

    public String getNext() {
        if (this.maxCodepoint < this.next) {
            return f(0);
        }
        return f(this.next);
    }

    public boolean isCompact() {
        return this.compact;
    }

    public boolean isShowInvalid() {
        return this.invalid;
    }

    private String f(long cp) {
        return String.format("%06X", cp);
    }
}
