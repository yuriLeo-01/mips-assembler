package mips;

/** Instrução já codificada e o endereço que ocupa na memória. */
public record InstrucaoMontada(Instrucao instrucao, int endereco, int codigo) {
}
