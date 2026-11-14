package mips;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Ciclos por instrução, lidos de um CSV {@code instrucao,ciclos} com cabeçalho. */
public final class CycleTable {
    private static final int DEFAULT_CYCLES = 1;

    private final Map<Instruction, Integer> cycles;

    private CycleTable(Map<Instruction, Integer> cycles) {
        this.cycles = Map.copyOf(cycles);
    }

    /** Tabela em que toda instrução leva {@value #DEFAULT_CYCLES} ciclo. */
    public static CycleTable defaults() {
        return new CycleTable(Map.of());
    }

    public static CycleTable load(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        Map<Instruction, Integer> cycles = new HashMap<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).strip();
            if (!line.isEmpty()) {
                parseLine(line, i + 1, file, cycles);
            }
        }
        return new CycleTable(cycles);
    }

    private static void parseLine(String line, int number, Path file, Map<Instruction, Integer> cycles)
            throws IOException {
        String[] fields = line.split(",");
        Instruction instruction = fields.length == 2 ? Instruction.fromMnemonic(fields[0].strip()).orElse(null) : null;
        int count = fields.length == 2 ? parseCount(fields[1].strip()) : 0;
        if (instruction == null || count <= 0) {
            throw new IOException(file + ":" + number + ": linha inválida '" + line + "'");
        }
        cycles.put(instruction, count);
    }

    private static int parseCount(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int cyclesOf(Instruction instruction) {
        return cycles.getOrDefault(instruction, DEFAULT_CYCLES);
    }
}
