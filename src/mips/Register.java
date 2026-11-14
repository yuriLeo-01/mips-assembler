package mips;

import java.util.Locale;

/** Os 32 registradores de uso geral do MIPS; o número é a posição no enum. */
public enum Register {
    ZERO, AT, V0, V1, A0, A1, A2, A3,
    T0, T1, T2, T3, T4, T5, T6, T7,
    S0, S1, S2, S3, S4, S5, S6, S7,
    T8, T9, K0, K1, GP, SP, FP, RA;

    private static final Register[] ALL = values();

    public int number() {
        return ordinal();
    }

    /** Converte {@code $8}, {@code $t0}, {@code $zero} etc. */
    public static Register parse(String token) {
        if (!token.startsWith("$") || token.length() == 1) {
            throw new AssemblyError("registrador inválido '" + token + "'");
        }
        String name = token.substring(1);
        return Character.isDigit(name.charAt(0)) ? byNumber(name, token) : byName(name, token);
    }

    private static Register byNumber(String number, String token) {
        try {
            int index = Integer.parseInt(number);
            if (index >= 0 && index < ALL.length) {
                return ALL[index];
            }
        } catch (NumberFormatException ignored) {
            // tratado abaixo
        }
        throw new AssemblyError("registrador inválido '" + token + "' (use $0 a $31)");
    }

    private static Register byName(String name, String token) {
        try {
            return valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new AssemblyError("registrador inválido '" + token + "'");
        }
    }
}
