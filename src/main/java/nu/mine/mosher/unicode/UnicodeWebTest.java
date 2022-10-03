package nu.mine.mosher.unicode;

import com.google.common.math.LongMath;
import com.google.common.primitives.Longs;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import lombok.*;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static java.math.RoundingMode.CEILING;

@WebServlet("/")
public class UnicodeWebTest extends HttpServlet {
    private static final PageBuilder BUILDER = new PageBuilder();

    @Override
    @SneakyThrows
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) {
        val start = hexParam(request, "start", 0L);
        val compact = booleanParam(request, "compact", false);
        val rowlen = (int)1L << Longs.constrainToRange(hexParam(request, "rowlen", 0x5L), 0L, LongMath.log2(Pager.PAGE_SIZE, CEILING));
        val invalid = booleanParam(request, "invalid", true);

        val page = BUILDER.build(new Pager(start, BUILDER.maxPoint(), compact, rowlen, invalid));

        response.setContentType("text/html");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(page);
    }

    private static boolean booleanParam(final HttpServletRequest request, final String param, final boolean def) {
        var value = def;
        val p = Optional.ofNullable(request.getParameter(param));
        if (p.isPresent()) {
            try {
                value = Boolean.parseBoolean(p.get());
            } catch (final Throwable ignore) {
            }
        }
        return value;
    }

    private static long hexParam(final HttpServletRequest request, final String param, final long def) {
        var value = def;
        val p = Optional.ofNullable(request.getParameter(param));
        if (p.isPresent()) {
            try {
                value = Long.parseLong(p.get(), 0x10);
            } catch (final Throwable ignore) {
            }
        }
        return value;
    }
}
