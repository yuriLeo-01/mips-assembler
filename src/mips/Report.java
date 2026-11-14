package mips;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/** Quantidade de cada instrução e CPI médio do programa. */
public record Report(Map<String, Integer> counts, double averageCpi) {
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    public Report {
        counts = Map.copyOf(counts);
    }

    public static Report of(AssembledProgram program, CycleTable table) {
        Map<String, Integer> counts = new TreeMap<>();
        long totalCycles = 0;
        for (AssembledInstruction assembled : program.instructions()) {
            counts.merge(assembled.instruction().mnemonic(), 1, Integer::sum);
            totalCycles += table.cyclesOf(assembled.instruction());
        }
        int total = program.instructions().size();
        return new Report(counts, total == 0 ? 0 : (double) totalCycles / total);
    }

    /** Texto com as contagens em ordem alfabética e o CPI com vírgula decimal. */
    public String format() {
        StringBuilder text = new StringBuilder("Quantidades por tipo de instrução:\n");
        new TreeMap<>(counts).forEach((mnemonic, count) ->
                text.append(mnemonic).append(": ").append(count).append('\n'));
        return text.append(String.format(PT_BR, "CPI médio: %.1f", averageCpi)).toString();
    }
}
