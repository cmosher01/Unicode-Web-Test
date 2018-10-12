package nu.mine.mosher.unicode;

import fi.iki.elonen.NanoHTTPD;
import org.stringtemplate.v4.STGroupFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static fi.iki.elonen.NanoHTTPD.SOCKET_READ_TIMEOUT;
import static java.lang.Runtime.getRuntime;
import static java.util.Collections.emptyList;

public class UnicodeWebTest {
    private static final int ROW_LENGTH = 0x20;
    private static final int ROW_COUNT = 0x80;

    public static void main(final String... args) throws IOException {
        final UnicodeManager unicodeMgr = UnicodeManager.create();

        final STGroupFile stg = new STGroupFile("st/UnicodeWebTest.stg");

        final NanoHTTPD server = new NanoHTTPD(8080) {
            @Override
            public Response serve(final IHTTPSession session) {
                try {
                    final List<String> paramStart = session.getParameters().getOrDefault("start", emptyList());
                    long start = 0;
                    if (!paramStart.isEmpty()) {
                        try {
                            start = Long.parseLong(paramStart.get(0), 0x10);
                        } catch (final Throwable ignore) {
                        }
                    }
                    return newFixedLengthResponse(Response.Status.OK, MIME_HTML, fixedPage(stg, unicodeMgr, start, new Pager(start, unicodeMgr.getMaxCodepoint())));
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

    public static class Pager {
        public final long start;
        public final long prev;
        public final long next;
        public final long maxCodepoint;

        Pager(long start, long maxCodepoint) {
            this.start = start;
            this.prev = start - ROW_COUNT * ROW_LENGTH;
            this.next = start + ROW_COUNT * ROW_LENGTH;
            this.maxCodepoint = maxCodepoint;
        }

        public String getStart() {
            return String.format("U+%03X\u00d7\u00d7\u00d7", this.start/0x1000);
        }

        public String getPrev() {
            if (this.prev < 0) {
                return f(this.maxCodepoint / 0x1000 * 0x1000);
            }
            return f(this.prev);
        }

        public String getNext() {
            if (this.maxCodepoint < this.next) {
                return f(0);
            }
            return f(this.next);
        }

        private String f(long cp) {
            return String.format("%06X", cp);
        }
    }

    private static String fixedPage(final STGroupFile stg, final UnicodeManager unicodeMgr, final long start, final Pager pager) {
        long cp = start;
        final List<List<UnicodeCharacter>> ctab = new ArrayList<>(ROW_COUNT);
        for (int r = 0; r < ROW_COUNT; ++r) {
            final List<UnicodeCharacter> cols = new ArrayList<>(ROW_LENGTH);
            ctab.add(cols);
            for (int c = 0; c < ROW_LENGTH; ++c) {
                cols.add(unicodeMgr.charFor(cp++));
            }
        }
        return stg
                .getInstanceOf("fixedPage")
                .add("ctab", ctab)
                .add("pager", pager)
                .render();
    }
}
