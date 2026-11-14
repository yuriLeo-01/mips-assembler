package mips;

import java.util.List;

/** Resultado de uma montagem bem-sucedida, na ordem do programa. */
public record AssembledProgram(List<AssembledInstruction> instructions) {

    public AssembledProgram {
        instructions = List.copyOf(instructions);
    }

    public List<Integer> codes() {
        return instructions.stream().map(AssembledInstruction::code).toList();
    }
}
