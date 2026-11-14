package mips;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/** Linha de comando: {@code java -cp out mips.Main <arquivo.asm> <-b|-h>}. */
public final class Main {
    static final int SUCESSO = 0;
    static final int USO_INCORRETO = 1;
    static final int FALHA = 2;

    private static final String ARQUIVO_CICLOS = "ciclos.csv";

    private Main() {
    }

    public static void main(String[] args) {
        System.exit(executar(args, System.out, System.err));
    }

    static int executar(String[] args, PrintStream saida, PrintStream erro) {
        Optional<FormatoSaida> formato = args.length == 2 ? FormatoSaida.porOpcao(args[1]) : Optional.empty();
        if (formato.isEmpty()) {
            erro.println("Uso: java -cp out mips.Main <arquivo.asm> <-b | -h>");
            erro.println("  -b  binário (.bin)    -h  hexadecimal Logisim (.hex)");
            return USO_INCORRETO;
        }
        Path fonte = Path.of(args[0]);
        try {
            ProgramaMontado programa = new Montador().montar(Files.readAllLines(fonte, StandardCharsets.UTF_8));
            Path destino = formato.get().destinoPara(fonte);
            formato.get().escrever(programa.codigos(), destino);
            saida.println("Sucesso! Arquivo gerado: " + destino);
            saida.println();
            saida.println(Relatorio.de(programa, carregarCiclos(fonte, erro)).formatar());
            return SUCESSO;
        } catch (FalhaMontagem falha) {
            falha.erros().forEach(e -> erro.println(fonte + ":" + e.linha() + ": " + e.getMessage()));
            erro.println(falha.getMessage() + "; nenhum arquivo foi gerado.");
        } catch (IOException e) {
            erro.println("Erro de arquivo: " + e.getMessage());
        }
        return FALHA;
    }

    /** Procura o CSV no diretório atual e depois ao lado do .asm; sem ele, cada instrução vale 1 ciclo. */
    private static TabelaCiclos carregarCiclos(Path fonte, PrintStream erro) throws IOException {
        Path diretorioFonte = fonte.toAbsolutePath().getParent();
        for (Path candidato : List.of(Path.of(ARQUIVO_CICLOS), diretorioFonte.resolve(ARQUIVO_CICLOS))) {
            if (Files.isRegularFile(candidato)) {
                return TabelaCiclos.carregar(candidato);
            }
        }
        erro.println("Aviso: " + ARQUIVO_CICLOS + " não encontrado; usando 1 ciclo por instrução.");
        return TabelaCiclos.padrao();
    }
}
