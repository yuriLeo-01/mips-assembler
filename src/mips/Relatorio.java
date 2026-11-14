package mips;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/** Quantidade de cada instrução e CPI médio do programa. */
public record Relatorio(Map<String, Integer> quantidades, double cpiMedio) {
    private static final Locale PORTUGUES = Locale.forLanguageTag("pt-BR");

    public Relatorio {
        quantidades = Map.copyOf(quantidades);
    }

    public static Relatorio de(ProgramaMontado programa, TabelaCiclos tabela) {
        Map<String, Integer> quantidades = new TreeMap<>();
        long ciclosTotais = 0;
        for (InstrucaoMontada montada : programa.instrucoes()) {
            quantidades.merge(montada.instrucao().mnemonico(), 1, Integer::sum);
            ciclosTotais += tabela.ciclos(montada.instrucao());
        }
        int total = programa.instrucoes().size();
        return new Relatorio(quantidades, total == 0 ? 0 : (double) ciclosTotais / total);
    }

    /** Texto com as contagens em ordem alfabética e o CPI com vírgula decimal. */
    public String formatar() {
        StringBuilder texto = new StringBuilder("Quantidades por tipo de instrução:\n");
        new TreeMap<>(quantidades).forEach((mnemonico, quantidade) ->
                texto.append(mnemonico).append(": ").append(quantidade).append('\n'));
        return texto.append(String.format(PORTUGUES, "CPI médio: %.1f", cpiMedio)).toString();
    }
}
