package nu.mine.mosher.unicode;

import com.google.common.math.LongMath;
import com.google.common.primitives.Longs;
import fi.iki.elonen.NanoHTTPD;
import org.stringtemplate.v4.STGroupFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static fi.iki.elonen.NanoHTTPD.SOCKET_READ_TIMEOUT;
import static java.lang.Runtime.getRuntime;
import static java.math.RoundingMode.CEILING;
import static java.util.Collections.emptyList;

public class UnicodeWebTest {
    private static final int PAGE_SIZE = 0x1000;

    public static void main(final String... args) throws IOException {
        final UnicodeManager unicodeMgr = UnicodeManager.create();

        final STGroupFile stg = new STGroupFile("st/UnicodeWebTest.stg");

        final NanoHTTPD server = new NanoHTTPD(8080) {
            @Override
            public Response serve(final IHTTPSession session) {
                try {
                    if (session.getUri().endsWith(".css")) {
                        return newFixedLengthResponse(Response.Status.OK, mimeTypes().get("css"), getStyle());
                    }
                    final long start = longParam(session, "start", 0L);
                    final boolean compact = booleanParam(session, "compact", false);
                    final int rowlen = (int)1L << Longs.constrainToRange(longParam(session, "rowlen", 0x5L), 0L, LongMath.log2(PAGE_SIZE, CEILING));
                    final boolean invalid = booleanParam(session, "invalid", true);
                    return newFixedLengthResponse(
                            Response.Status.OK, MIME_HTML,
                            fixedPage(stg, unicodeMgr, start, new Pager(start, unicodeMgr.getMaxCodepoint(), compact, rowlen, invalid)));
                } catch (final Throwable e) {
                    throw new IllegalStateException(e);
                }
            }
        };

        getRuntime().addShutdownHook(new Thread(server::stop));
        server.start(SOCKET_READ_TIMEOUT, false);

        System.out.flush();
        System.err.flush();
    }

    private static String getStyle() throws IOException {
        return new String(UnicodeWebTest.class.getResourceAsStream("style.css").readAllBytes(), StandardCharsets.US_ASCII);
    }

    private static boolean booleanParam(final NanoHTTPD.IHTTPSession session, final String param, boolean def) {
        boolean value = def;
        final List<String> values = session.getParameters().getOrDefault(param, emptyList());
        if (!values.isEmpty()) {
            try {
                value = Boolean.parseBoolean(values.get(0));
            } catch (final Throwable ignore) {
            }
        }
        return value;
    }

    private static long longParam(final NanoHTTPD.IHTTPSession session, final String param, long def) {
        long value = def;
        final List<String> values = session.getParameters().getOrDefault(param, emptyList());
        if (!values.isEmpty()) {
            try {
                value = Long.parseLong(values.get(0), 0x10);
            } catch (final Throwable ignore) {
            }
        }
        return value;
    }

    public static class Pager {
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
            return String.format("U+%03X\u00d7\u00d7\u00d7", this.start/PAGE_SIZE);
        }

        public int getRowlen() {
            return 31-Integer.numberOfLeadingZeros(this.rowlen);
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

    private static String fixedPage(final STGroupFile stg, final UnicodeManager unicodeMgr, final long start, final Pager pager) {
        final int rlen = pager.rowlen;
        long cp = start;
        final List<List<UnicodeCharacter>> ctab = new ArrayList<>(PAGE_SIZE / rlen);
        for (int r = 0; r < PAGE_SIZE / rlen; ++r) {
            final List<UnicodeCharacter> cols = new ArrayList<>(rlen);
            ctab.add(cols);
            for (int c = 0; c < rlen; ++c) {
                cols.add(unicodeMgr.charFor(cp++));
            }
        }
        return stg
                .getInstanceOf(pager.compact ? "compactPage" : "fixedPage")
                .add("ctab", ctab)
                .add("pager", pager)
                .render();
    }
}
