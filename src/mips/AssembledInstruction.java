package mips;

/** Instrução já codificada e o endereço que ocupa na memória. */
public record AssembledInstruction(Instruction instruction, int address, int code) {
}
