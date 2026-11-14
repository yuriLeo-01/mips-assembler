package mips;

import java.util.Locale;

/** Os 32 registradores de uso geral do MIPS; o número é a posição no enum. */
public enum Registrador {
    ZERO, AT, V0, V1, A0, A1, A2, A3,
    T0, T1, T2, T3, T4, T5, T6, T7,
    S0, S1, S2, S3, S4, S5, S6, S7,
    T8, T9, K0, K1, GP, SP, FP, RA;

    private static final Registrador[] TODOS = values();

    public int numero() {
        return ordinal();
    }

    /** Converte {@code $8}, {@code $t0}, {@code $zero} etc. */
    public static Registrador de(String token) {
        if (!token.startsWith("$") || token.length() == 1) {
            throw new ErroMontagem("registrador inválido '" + token + "'");
        }
        String nome = token.substring(1);
        return Character.isDigit(nome.charAt(0)) ? porNumero(nome, token) : porNome(nome, token);
    }

    private static Registrador porNumero(String numero, String token) {
        try {
            int indice = Integer.parseInt(numero);
            if (indice >= 0 && indice < TODOS.length) {
                return TODOS[indice];
            }
        } catch (NumberFormatException ignorado) {
            // tratado abaixo
        }
        throw new ErroMontagem("registrador inválido '" + token + "' (use $0 a $31)");
    }

    private static Registrador porNome(String nome, String token) {
        try {
            return valueOf(nome.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ErroMontagem("registrador inválido '" + token + "'");
        }
    }
}
