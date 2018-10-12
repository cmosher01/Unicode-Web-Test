package nu.mine.mosher.unicode;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnicodeManager {
    private final Map<String, Range<Long>> ranges = new HashMap<>(10, 0.01f);
    private final Map<Long, UnicodeCharacter> chars = new HashMap<>(0x10000);

    private long codepointMax;

    private UnicodeManager() {
    }

    public static UnicodeManager create() throws IOException {
        final UnicodeManager unicodeMgr = new UnicodeManager();
        unicodeMgr.addFromResource("/ucd/UnicodeData.txt");
        unicodeMgr.addFromResource("/ucsur/UnicodeData.txt");
        return unicodeMgr;
    }

    public UnicodeCharacter charFor(final long codepoint) {
        UnicodeCharacter c = this.chars.get(codepoint);
        if (Objects.isNull(c)) {
            c = lookUpInRanges(codepoint);
        }
        if (Objects.isNull(c)) {
            c = UnicodeCharacter.non(codepoint);
        }
        return c;
    }

    public long getMaxCodepoint() {
        return this.codepointMax;
    }

    private UnicodeCharacter lookUpInRanges(final long codepoint) {
        return this.ranges
                .entrySet()
                .stream()
                .filter(e -> e.getValue().contains(codepoint))
                .findAny()
                .map(Map.Entry::getKey)
                .map(name -> asUnicodeChar(codepoint, name))
                .orElse(null);
    }

    private static UnicodeCharacter asUnicodeChar(long codepoint, String name) {
        return new UnicodeCharacter(codepoint, name, false);
    }

    private void addFromResource(final String resourceName) throws IOException {
        try (final BufferedReader linesUnicodeCharacters = new BufferedReader(new InputStreamReader(UnicodeWebTest.class.getResourceAsStream(resourceName)))) {
            linesUnicodeCharacters
                    .lines()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .filter(s -> !s.startsWith("#"))
                    .map(s -> s.split(";"))
                    .forEach(this::handleLine);
        }
    }

    private static final Pattern SPECIAL = Pattern.compile("<(.*,.*)>");

    private void handleLine(String[] r) {
        final long codepoint = parseHex(r[0]);
        final String name = r[1].trim().toLowerCase();
        final boolean combining = parseCombining(r[3]);

        final Matcher matcher = SPECIAL.matcher(name);
        if (matcher.matches()) {
            handleSpecial(codepoint, matcher.group(1));
        } else {
            this.chars.put(codepoint, new UnicodeCharacter(codepoint, name, combining));
        }

        if (this.codepointMax < codepoint) {
            this.codepointMax = codepoint;
        }
    }

    private enum BoundCommand {
        first, last
    }

    private void handleSpecial(final long codepoint, String name) {
        final String[] parsedName = name.split(",", 2);
        final String classRange = parsedName[0].trim();
        final BoundCommand boundRange = BoundCommand.valueOf(parsedName[1].trim());

        if (classRange.contains("surrogate")) {
            // surrogates aren't characters
            return;
        }

        final Range<Long> rangeHalf =
                boundRange == BoundCommand.first ?
                        Range.downTo(codepoint, BoundType.CLOSED) :
                        Range.upTo(codepoint, BoundType.CLOSED);
        Range<Long> r = this.ranges.get(classRange);
        if (Objects.isNull(r)) {
            r = rangeHalf;
        } else {
            r = r.intersection(rangeHalf);
        }
        this.ranges.put(classRange, r);
    }

    private boolean parseCombining(String s) {
        return Integer.parseInt(s) > 0;
    }

    private static long parseHex(final String sHex) {
        return Long.parseLong(sHex, 0x10);
    }
}
