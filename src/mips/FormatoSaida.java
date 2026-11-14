package mips;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** Formatos de arquivo de saída, escolhidos pela opção da linha de comando. */
public enum FormatoSaida {
    BINARIO("-b", ".bin") {
        String formatar(int codigo) {
            return String.format("%32s", Integer.toBinaryString(codigo)).replace(' ', '0');
        }
    },
    /** Formato de imagem de memória do Logisim. */
    HEXADECIMAL("-h", ".hex") {
        String formatar(int codigo) {
            return String.format("%08x", codigo);
        }

        List<String> cabecalho() {
            return List.of("v2.0 raw");
        }
    };

    private final String opcao;
    private final String extensao;

    FormatoSaida(String opcao, String extensao) {
        this.opcao = opcao;
        this.extensao = extensao;
    }

    public static Optional<FormatoSaida> porOpcao(String opcao) {
        return Arrays.stream(values()).filter(f -> f.opcao.equals(opcao)).findFirst();
    }

    abstract String formatar(int codigo);

    List<String> cabecalho() {
        return List.of();
    }

    /** {@code dir/prog.asm} vira {@code dir/prog.hex}; só a extensão final é trocada. */
    public Path destinoPara(Path fonte) {
        String nome = fonte.getFileName().toString();
        int ponto = nome.lastIndexOf('.');
        String base = ponto > 0 ? nome.substring(0, ponto) : nome;
        return fonte.resolveSibling(base + extensao);
    }

    public void escrever(List<Integer> codigos, Path destino) throws IOException {
        List<String> linhas = new ArrayList<>(cabecalho());
        codigos.forEach(codigo -> linhas.add(formatar(codigo)));
        Files.write(destino, linhas, StandardCharsets.UTF_8);
    }
}
