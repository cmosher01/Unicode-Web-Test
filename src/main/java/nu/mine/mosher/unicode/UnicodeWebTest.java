package nu.mine.mosher.unicode;

import com.google.common.math.LongMath;
import com.google.common.primitives.Longs;
import fi.iki.elonen.NanoHTTPD;
import lombok.val;
import org.stringtemplate.v4.STGroupFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static fi.iki.elonen.NanoHTTPD.SOCKET_READ_TIMEOUT;
import static java.lang.Runtime.getRuntime;
import static java.math.RoundingMode.CEILING;
import static java.util.Collections.emptyList;

public class UnicodeWebTest {
    public static void main(final String... args) throws IOException {
        val unicodeMgr = UnicodeManager.create();

        val stg = new STGroupFile("st/UnicodeWebTest.stg");

        val server = new NanoHTTPD(8080) {
            @SuppressWarnings("UnstableApiUsage")
            @Override
            public Response serve(final IHTTPSession session) {
                try {
                    if (session.getUri().endsWith(".css")) {
                        return newFixedLengthResponse(Response.Status.OK, mimeTypes().get("css"), getStyle());
                    }
                    val start = longParam(session, "start", 0L);
                    val compact = booleanParam(session, "compact", false);
                    val rowlen = (int)1L << Longs.constrainToRange(longParam(session, "rowlen", 0x5L), 0L, LongMath.log2(Pager.PAGE_SIZE, CEILING));
                    val invalid = booleanParam(session, "invalid", true);
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



    @SuppressWarnings("ConstantConditions")
    private static String getStyle() throws IOException {
        return new String(UnicodeWebTest.class.getResourceAsStream("style.css").readAllBytes(), StandardCharsets.US_ASCII);
    }

    private static boolean booleanParam(final NanoHTTPD.IHTTPSession session, final String param, final boolean def) {
        var value = def;
        val values = session.getParameters().getOrDefault(param, emptyList());
        if (!values.isEmpty()) {
            try {
                value = Boolean.parseBoolean(values.get(0));
            } catch (final Throwable ignore) {
            }
        }
        return value;
    }

    private static long longParam(final NanoHTTPD.IHTTPSession session, final String param, final long def) {
        var value = def;
        val values = session.getParameters().getOrDefault(param, emptyList());
        if (!values.isEmpty()) {
            try {
                value = Long.parseLong(values.get(0), 0x10);
            } catch (final Throwable ignore) {
            }
        }
        return value;
    }

    private static String fixedPage(final STGroupFile stg, final UnicodeManager unicodeMgr, final long start, final Pager pager) {
        val rlen = pager.rowlen;
        var cp = start;
        val ctab = new ArrayList<>(Pager.PAGE_SIZE / rlen);
        for (int r = 0; r < Pager.PAGE_SIZE / rlen; ++r) {
            val cols = new ArrayList<>(rlen);
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
