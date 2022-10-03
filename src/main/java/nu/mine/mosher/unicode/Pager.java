package nu.mine.mosher.unicode;

import lombok.RequiredArgsConstructor;

@SuppressWarnings("unused")
@RequiredArgsConstructor
public class Pager {
    public static final int PAGE_SIZE = 0x1000;

    public final long start;
    public final long maxCodepoint;
    public final boolean compact;
    public final int rowlen;
    public final boolean invalid;

    public String getStart() {
        return String.format("U+%03X\u00d7\u00d7\u00d7", this.start / PAGE_SIZE);
    }

    public int getRowlen() {
        return 31 - Integer.numberOfLeadingZeros(this.rowlen);
    }

    public String getPrev() {
        if (this.start - PAGE_SIZE < 0) {
            return f(this.maxCodepoint / PAGE_SIZE * PAGE_SIZE);
        }
        return f(this.start - PAGE_SIZE);
    }

    public String getNext() {
        if (this.maxCodepoint < this.start + PAGE_SIZE) {
            return f(0);
        }
        return f(this.start + PAGE_SIZE);
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
