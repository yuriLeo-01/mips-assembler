package mips;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Separa comentário, labels, mnemônico e operandos de uma linha de assembly. */
public final class AnalisadorSintatico {
    private static final Pattern LABEL = Pattern.compile("^\\s*([A-Za-z_.][\\w.]*)\\s*:");
    private static final Pattern ESPACO_ANTES_PARENTESE = Pattern.compile("\\s*\\(\\s*");
    private static final Pattern ESPACO_ANTES_FECHA = Pattern.compile("\\s*\\)");
    private static final Pattern SEPARADOR = Pattern.compile("[,\\s]+");

    private AnalisadorSintatico() {
    }

    public static LinhaFonte analisar(String texto, int numero) {
        String codigo = removerComentario(texto);
        List<String> labels = new ArrayList<>();
        for (Matcher m = LABEL.matcher(codigo); m.find(); m = LABEL.matcher(codigo)) {
            labels.add(m.group(1));
            codigo = codigo.substring(m.end());
        }
        codigo = codigo.strip();
        if (codigo.isEmpty()) {
            return new LinhaFonte(numero, labels, null, List.of());
        }
        String[] partes = codigo.split("\\s+", 2);
        String mnemonico = partes[0].toLowerCase(Locale.ROOT);
        List<String> operandos = partes.length > 1 ? separarOperandos(partes[1]) : List.of();
        return new LinhaFonte(numero, labels, mnemonico, operandos);
    }

    private static String removerComentario(String texto) {
        int inicio = texto.indexOf('#');
        return inicio < 0 ? texto : texto.substring(0, inicio);
    }

    /** Junta {@code 100 ($2)} em {@code 100($2)} antes de separar por vírgulas/espaços. */
    private static List<String> separarOperandos(String texto) {
        String normalizado = ESPACO_ANTES_PARENTESE.matcher(texto).replaceAll("(");
        normalizado = ESPACO_ANTES_FECHA.matcher(normalizado).replaceAll(")");
        return Arrays.stream(SEPARADOR.split(normalizado.strip()))
                .filter(operando -> !operando.isEmpty())
                .toList();
    }
}
