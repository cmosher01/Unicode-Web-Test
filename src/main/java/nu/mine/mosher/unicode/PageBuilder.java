package nu.mine.mosher.unicode;

import lombok.*;
import org.stringtemplate.v4.STGroupFile;

import java.util.ArrayList;

public class PageBuilder {
    private final STGroupFile stg = new STGroupFile("st/UnicodeWebTest.stg");
    private final UnicodeManager unicodeMgr = UnicodeManager.create();

    public String build(final Pager pager) {
        val rlen = pager.rowlen;
        var cp = pager.start;
        val ctab = new ArrayList<>(Pager.PAGE_SIZE / rlen);
        for (int r = 0; r < Pager.PAGE_SIZE / rlen; ++r) {
            val cols = new ArrayList<>(rlen);
            ctab.add(cols);
            for (int c = 0; c < rlen; ++c) {
                cols.add(this.unicodeMgr.charFor(cp++));
            }
        }
        return this.stg
            .getInstanceOf(pager.compact ? "compactPage" : "fixedPage")
            .add("ctab", ctab)
            .add("pager", pager)
            .render();
    }

    public long maxPoint() {
        return this.unicodeMgr.getMaxCodepoint();
    }
}
