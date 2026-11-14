package mips;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Separa comentário, labels, mnemônico e operandos de uma linha de assembly. */
public final class Parser {
    private static final Pattern LABEL = Pattern.compile("^\\s*([A-Za-z_.][\\w.]*)\\s*:");
    private static final Pattern SPACE_AROUND_OPEN_PAREN = Pattern.compile("\\s*\\(\\s*");
    private static final Pattern SPACE_BEFORE_CLOSE_PAREN = Pattern.compile("\\s*\\)");
    private static final Pattern SEPARATOR = Pattern.compile("[,\\s]+");

    private Parser() {
    }

    public static SourceLine parse(String text, int number) {
        String code = stripComment(text);
        List<String> labels = new ArrayList<>();
        for (Matcher m = LABEL.matcher(code); m.find(); m = LABEL.matcher(code)) {
            labels.add(m.group(1));
            code = code.substring(m.end());
        }
        code = code.strip();
        if (code.isEmpty()) {
            return new SourceLine(number, labels, null, List.of());
        }
        String[] parts = code.split("\\s+", 2);
        String mnemonic = parts[0].toLowerCase(Locale.ROOT);
        List<String> operands = parts.length > 1 ? splitOperands(parts[1]) : List.of();
        return new SourceLine(number, labels, mnemonic, operands);
    }

    private static String stripComment(String text) {
        int start = text.indexOf('#');
        return start < 0 ? text : text.substring(0, start);
    }

    /** Junta {@code 100 ($2)} em {@code 100($2)} antes de separar por vírgulas/espaços. */
    private static List<String> splitOperands(String text) {
        String normalized = SPACE_AROUND_OPEN_PAREN.matcher(text).replaceAll("(");
        normalized = SPACE_BEFORE_CLOSE_PAREN.matcher(normalized).replaceAll(")");
        return Arrays.stream(SEPARATOR.split(normalized.strip()))
                .filter(operand -> !operand.isEmpty())
                .toList();
    }
}
