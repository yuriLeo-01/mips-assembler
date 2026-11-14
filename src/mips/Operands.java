package mips;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Acesso tipado e validado aos operandos de uma instrução. */
final class Operands {
    private static final Pattern MEMORY = Pattern.compile("(.*)\\((.+)\\)");

    private final List<String> values;

    Operands(List<String> values, int expected) {
        if (values.size() != expected) {
            throw new AssemblyError("esperado(s) " + expected + " operando(s), encontrado(s) " + values.size());
        }
        this.values = values;
    }

    Register register(int index) {
        return Register.parse(values.get(index));
    }

    String label(int index) {
        return values.get(index);
    }

    int immediate(int index, int min, int max) {
        return numberInRange(values.get(index), min, max);
    }

    /** Operando no formato {@code deslocamento(base)}; deslocamento vazio vale 0. */
    MemoryAddress memory(int index) {
        String text = values.get(index);
        Matcher m = MEMORY.matcher(text);
        if (!m.matches()) {
            throw new AssemblyError("endereço de memória inválido '" + text + "' (use deslocamento($reg))");
        }
        String offset = m.group(1);
        int value = offset.isEmpty() ? 0 : numberInRange(offset, Short.MIN_VALUE, Short.MAX_VALUE);
        return new MemoryAddress(value, Register.parse(m.group(2)));
    }

    private static int numberInRange(String text, int min, int max) {
        long value = parseNumber(text);
        if (value < min || value > max) {
            throw new AssemblyError("valor " + text + " fora do intervalo [" + min + ", " + max + "]");
        }
        return (int) value;
    }

    /** Aceita decimal e hexadecimal ({@code 0x}), com sinal opcional. */
    private static long parseNumber(String text) {
        boolean negative = text.startsWith("-");
        String digits = negative || text.startsWith("+") ? text.substring(1) : text;
        boolean hex = digits.regionMatches(true, 0, "0x", 0, 2);
        try {
            long value = hex ? Long.parseLong(digits.substring(2), 16) : Long.parseLong(digits);
            return negative ? -value : value;
        } catch (NumberFormatException e) {
            throw new AssemblyError("número inválido '" + text + "'");
        }
    }

    record MemoryAddress(int offset, Register base) {
    }
}
