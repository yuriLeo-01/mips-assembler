package mips;

import java.util.ArrayList;
import java.util.List;

/** Monta um programa em duas passadas: endereços dos labels e depois a codificação. */
public final class Assembler {
    public static final int START_ADDRESS = 0x00400000;
    private static final int INSTRUCTION_SIZE = 4;

    /** @throws AssemblyException com todos os erros do programa, nunca só o primeiro */
    public AssembledProgram assemble(List<String> source) throws AssemblyException {
        List<AssemblyError> errors = new ArrayList<>();
        List<SourceLine> lines = parse(source);
        SymbolTable symbols = collectLabels(lines, errors);
        List<AssembledInstruction> instructions = encode(lines, symbols, errors);
        if (!errors.isEmpty()) {
            throw new AssemblyException(errors);
        }
        return new AssembledProgram(instructions);
    }

    private static List<SourceLine> parse(List<String> source) {
        List<SourceLine> lines = new ArrayList<>();
        for (int i = 0; i < source.size(); i++) {
            lines.add(Parser.parse(source.get(i), i + 1));
        }
        return lines;
    }

    private static SymbolTable collectLabels(List<SourceLine> lines, List<AssemblyError> errors) {
        SymbolTable symbols = new SymbolTable();
        int address = START_ADDRESS;
        for (SourceLine line : lines) {
            for (String label : line.labels()) {
                try {
                    symbols.define(label, address);
                } catch (AssemblyError e) {
                    errors.add(e.atLine(line.number()));
                }
            }
            if (line.hasInstruction()) {
                address += INSTRUCTION_SIZE;
            }
        }
        return symbols;
    }

    private static List<AssembledInstruction> encode(
            List<SourceLine> lines, SymbolTable symbols, List<AssemblyError> errors) {
        List<AssembledInstruction> instructions = new ArrayList<>();
        int address = START_ADDRESS;
        for (SourceLine line : lines) {
            if (!line.hasInstruction()) {
                continue;
            }
            try {
                Instruction instruction = Instruction.fromMnemonic(line.mnemonic())
                        .orElseThrow(() -> new AssemblyError("instrução desconhecida '" + line.mnemonic() + "'"));
                int code = instruction.encode(line.operands(), symbols, address);
                instructions.add(new AssembledInstruction(instruction, address, code));
            } catch (AssemblyError e) {
                errors.add(e.atLine(line.number()));
            }
            address += INSTRUCTION_SIZE;
        }
        return instructions;
    }
}
