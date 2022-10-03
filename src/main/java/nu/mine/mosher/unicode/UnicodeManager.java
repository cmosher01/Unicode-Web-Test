package nu.mine.mosher.unicode;

import com.google.common.collect.*;
import lombok.*;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class UnicodeManager {
    private final Map<String, Range<Long>> ranges = new HashMap<>(10, 0.01f);
    private final Map<Long, UnicodeCharacter> chars = new HashMap<>(0x10000);

    private long codepointMax;

    private UnicodeManager() {
    }



    public static UnicodeManager create() {
        val unicodeMgr = new UnicodeManager();
        unicodeMgr.addFromResource("/ucd/UnicodeData.txt");
        unicodeMgr.addFromResource("/ucsur/UnicodeData.txt");
        return unicodeMgr;
    }



    public UnicodeCharacter charFor(final long codepoint) {
        var c = this.chars.get(codepoint);
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
            .map(name -> new UnicodeCharacter(codepoint, name, false))
            .orElse(null);
    }

    @SneakyThrows
    private void addFromResource(final String resourceName) {
        val resource = UnicodeWebTest.class.getResourceAsStream(resourceName);
        if (Objects.isNull(resource)) {
            System.err.println("Warning: cannot find Unicode Database text file. Codepoint names will not be shown.");
        } else {
            try (val linesUnicodeCharacters = new BufferedReader(new InputStreamReader(resource))) {
                linesUnicodeCharacters
                    .lines()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .filter(s -> !s.startsWith("#"))
                    .map(s -> s.split(";"))
                    .forEach(this::handleLine);
            }
        }
    }

    private static final Pattern SPECIAL = Pattern.compile("<(.*,.*)>");

    private void handleLine(String[] r) {
        val codepoint = parseHex(r[0]);
        val name = r[1].trim().toLowerCase();
        val combining = parseCombining(r[3]);

        val matcher = SPECIAL.matcher(name);
        if (matcher.matches()) {
            handleSpecial(codepoint, matcher.group(1));
        } else {
            this.chars.put(codepoint, new UnicodeCharacter(codepoint, name, combining));
        }

        if (this.codepointMax < codepoint) {
            this.codepointMax = codepoint;
        }
    }

    @SuppressWarnings("unused")
    private enum BoundCommand {
        first, last
    }

    private void handleSpecial(final long codepoint, String name) {
        val parsedName = name.split(",", 2);
        val classRange = parsedName[0].trim();

        if (classRange.contains("surrogate")) {
            // surrogates aren't characters
            return;
        }

        val boundRange = BoundCommand.valueOf(parsedName[1].trim());
        val rangeHalf =
            boundRange == BoundCommand.first ?
                Range.downTo(codepoint, BoundType.CLOSED) :
                Range.upTo(codepoint, BoundType.CLOSED);

        var r = this.ranges.get(classRange);
        if (Objects.isNull(r)) {
            r = rangeHalf;
        } else {
            r = r.intersection(rangeHalf);
        }
        this.ranges.put(classRange, r);
    }

    private static boolean parseCombining(String s) {
        return Integer.parseInt(s) > 0;
    }

    private static long parseHex(final String sHex) {
        return Long.parseLong(sHex, 0x10);
    }
}
