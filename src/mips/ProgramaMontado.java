package mips;

import java.util.List;

/** Resultado de uma montagem bem-sucedida, na ordem do programa. */
public record ProgramaMontado(List<InstrucaoMontada> instrucoes) {

    public ProgramaMontado {
        instrucoes = List.copyOf(instrucoes);
    }

    public List<Integer> codigos() {
        return instrucoes.stream().map(InstrucaoMontada::codigo).toList();
    }
}
