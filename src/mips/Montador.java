package mips;

import java.util.ArrayList;
import java.util.List;

/** Monta um programa em duas passadas: endereços dos labels e depois a codificação. */
public final class Montador {
    public static final int ENDERECO_INICIAL = 0x00400000;
    private static final int TAMANHO_INSTRUCAO = 4;

    /** @throws FalhaMontagem com todos os erros do programa, nunca só o primeiro */
    public ProgramaMontado montar(List<String> fonte) throws FalhaMontagem {
        List<ErroMontagem> erros = new ArrayList<>();
        List<LinhaFonte> linhas = analisar(fonte);
        TabelaSimbolos simbolos = registrarLabels(linhas, erros);
        List<InstrucaoMontada> instrucoes = codificar(linhas, simbolos, erros);
        if (!erros.isEmpty()) {
            throw new FalhaMontagem(erros);
        }
        return new ProgramaMontado(instrucoes);
    }

    private static List<LinhaFonte> analisar(List<String> fonte) {
        List<LinhaFonte> linhas = new ArrayList<>();
        for (int i = 0; i < fonte.size(); i++) {
            linhas.add(AnalisadorSintatico.analisar(fonte.get(i), i + 1));
        }
        return linhas;
    }

    private static TabelaSimbolos registrarLabels(List<LinhaFonte> linhas, List<ErroMontagem> erros) {
        TabelaSimbolos simbolos = new TabelaSimbolos();
        int endereco = ENDERECO_INICIAL;
        for (LinhaFonte linha : linhas) {
            for (String label : linha.labels()) {
                try {
                    simbolos.definir(label, endereco);
                } catch (ErroMontagem e) {
                    erros.add(e.naLinha(linha.numero()));
                }
            }
            if (linha.temInstrucao()) {
                endereco += TAMANHO_INSTRUCAO;
            }
        }
        return simbolos;
    }

    private static List<InstrucaoMontada> codificar(
            List<LinhaFonte> linhas, TabelaSimbolos simbolos, List<ErroMontagem> erros) {
        List<InstrucaoMontada> instrucoes = new ArrayList<>();
        int endereco = ENDERECO_INICIAL;
        for (LinhaFonte linha : linhas) {
            if (!linha.temInstrucao()) {
                continue;
            }
            try {
                Instrucao instrucao = Instrucao.porMnemonico(linha.mnemonico())
                        .orElseThrow(() -> new ErroMontagem("instrução desconhecida '" + linha.mnemonico() + "'"));
                int codigo = instrucao.codificar(linha.operandos(), simbolos, endereco);
                instrucoes.add(new InstrucaoMontada(instrucao, endereco, codigo));
            } catch (ErroMontagem e) {
                erros.add(e.naLinha(linha.numero()));
            }
            endereco += TAMANHO_INSTRUCAO;
        }
        return instrucoes;
    }
}
