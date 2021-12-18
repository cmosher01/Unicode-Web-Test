package nu.mine.mosher.unicode;

import lombok.RequiredArgsConstructor;

@SuppressWarnings("unused")
@RequiredArgsConstructor
public class UnicodeCharacter {
    private static final char NBSP = 0x00a0;

    private final long codepoint;
    private final String name;
    private final boolean combining;

    public static UnicodeCharacter non(final long codepoint) {
        return new UnicodeCharacter(codepoint, "not a valid Unicode codepoint", false) {
            @Override
            public String getCssClass() {
                return "invalid";
            }

            @Override
            public boolean isValid() {
                return false;
            }
        };
    }

    public String getCssClass() {
        return "uchar";
    }

    public boolean isValid() {
        return true;
    }

    public long codepoint() {
        return this.codepoint;
    }

    public String getCodepointDisplay() {
        return String.format("U+%06X: %s", this.codepoint, this.name);
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.combining ? NBSP+asChar() : asChar();
    }



    private String asChar() {
        try {
            return new String(Character.toChars((int) this.codepoint));
        } catch (final Throwable ignore) {
            return "";
        }
    }

    private static boolean parseZC(String cat) {
        return cat.startsWith("Z") || cat.startsWith("C");
    }
}
