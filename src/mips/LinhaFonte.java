package mips;

import java.util.List;

/**
 * Linha do programa já separada em partes.
 *
 * @param mnemonico em minúsculas, ou {@code null} se a linha só tiver labels/comentários
 */
public record LinhaFonte(int numero, List<String> labels, String mnemonico, List<String> operandos) {

    public LinhaFonte {
        labels = List.copyOf(labels);
        operandos = List.copyOf(operandos);
    }

    public boolean temInstrucao() {
        return mnemonico != null;
    }
}
