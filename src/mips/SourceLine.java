package mips;

import java.util.List;

/**
 * Linha do programa já separada em partes.
 *
 * @param mnemonic em minúsculas, ou {@code null} se a linha só tiver labels/comentários
 */
public record SourceLine(int number, List<String> labels, String mnemonic, List<String> operands) {

    public SourceLine {
        labels = List.copyOf(labels);
        operands = List.copyOf(operands);
    }

    public boolean hasInstruction() {
        return mnemonic != null;
    }
}
