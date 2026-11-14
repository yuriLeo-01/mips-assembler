package mips;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Acesso tipado e validado aos operandos de uma instrução. */
final class Operandos {
    private static final Pattern MEMORIA = Pattern.compile("(.*)\\((.+)\\)");

    private final List<String> valores;

    Operandos(List<String> valores, int esperados) {
        if (valores.size() != esperados) {
            throw new ErroMontagem("esperado(s) " + esperados + " operando(s), encontrado(s) " + valores.size());
        }
        this.valores = valores;
    }

    Registrador registrador(int indice) {
        return Registrador.de(valores.get(indice));
    }

    String label(int indice) {
        return valores.get(indice);
    }

    int imediato(int indice, int minimo, int maximo) {
        return numeroNoIntervalo(valores.get(indice), minimo, maximo);
    }

    /** Operando no formato {@code deslocamento(base)}; deslocamento vazio vale 0. */
    EnderecoMemoria memoria(int indice) {
        String texto = valores.get(indice);
        Matcher m = MEMORIA.matcher(texto);
        if (!m.matches()) {
            throw new ErroMontagem("endereço de memória inválido '" + texto + "' (use deslocamento($reg))");
        }
        String deslocamento = m.group(1);
        int valor = deslocamento.isEmpty() ? 0 : numeroNoIntervalo(deslocamento, Short.MIN_VALUE, Short.MAX_VALUE);
        return new EnderecoMemoria(valor, Registrador.de(m.group(2)));
    }

    private static int numeroNoIntervalo(String texto, int minimo, int maximo) {
        long valor = converterNumero(texto);
        if (valor < minimo || valor > maximo) {
            throw new ErroMontagem("valor " + texto + " fora do intervalo [" + minimo + ", " + maximo + "]");
        }
        return (int) valor;
    }

    /** Aceita decimal e hexadecimal ({@code 0x}), com sinal opcional. */
    private static long converterNumero(String texto) {
        boolean negativo = texto.startsWith("-");
        String digitos = negativo || texto.startsWith("+") ? texto.substring(1) : texto;
        boolean hexadecimal = digitos.regionMatches(true, 0, "0x", 0, 2);
        try {
            long valor = hexadecimal ? Long.parseLong(digitos.substring(2), 16) : Long.parseLong(digitos);
            return negativo ? -valor : valor;
        } catch (NumberFormatException e) {
            throw new ErroMontagem("número inválido '" + texto + "'");
        }
    }

    record EnderecoMemoria(int deslocamento, Registrador base) {
    }
}
