package mips;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Ciclos por instrução, lidos de um CSV {@code instrucao,ciclos} com cabeçalho. */
public final class TabelaCiclos {
    private static final int CICLOS_PADRAO = 1;

    private final Map<Instrucao, Integer> ciclos;

    private TabelaCiclos(Map<Instrucao, Integer> ciclos) {
        this.ciclos = Map.copyOf(ciclos);
    }

    /** Tabela em que toda instrução leva {@value #CICLOS_PADRAO} ciclo. */
    public static TabelaCiclos padrao() {
        return new TabelaCiclos(Map.of());
    }

    public static TabelaCiclos carregar(Path arquivo) throws IOException {
        List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
        Map<Instrucao, Integer> ciclos = new HashMap<>();
        for (int i = 1; i < linhas.size(); i++) {
            String linha = linhas.get(i).strip();
            if (!linha.isEmpty()) {
                lerLinha(linha, i + 1, arquivo, ciclos);
            }
        }
        return new TabelaCiclos(ciclos);
    }

    private static void lerLinha(String linha, int numero, Path arquivo, Map<Instrucao, Integer> ciclos)
            throws IOException {
        String[] campos = linha.split(",");
        Instrucao instrucao = campos.length == 2 ? Instrucao.porMnemonico(campos[0].strip()).orElse(null) : null;
        int quantidade = campos.length == 2 ? converterCiclos(campos[1].strip()) : 0;
        if (instrucao == null || quantidade <= 0) {
            throw new IOException(arquivo + ":" + numero + ": linha inválida '" + linha + "'");
        }
        ciclos.put(instrucao, quantidade);
    }

    private static int converterCiclos(String texto) {
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int ciclos(Instrucao instrucao) {
        return ciclos.getOrDefault(instrucao, CICLOS_PADRAO);
    }
}
