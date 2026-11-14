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
public enum OutputFormat {
    BINARY("-b", ".bin") {
        String format(int code) {
            return String.format("%32s", Integer.toBinaryString(code)).replace(' ', '0');
        }
    },
    /** Formato de imagem de memória do Logisim. */
    HEX("-h", ".hex") {
        String format(int code) {
            return String.format("%08x", code);
        }

        List<String> header() {
            return List.of("v2.0 raw");
        }
    };

    private final String option;
    private final String extension;

    OutputFormat(String option, String extension) {
        this.option = option;
        this.extension = extension;
    }

    public static Optional<OutputFormat> fromOption(String option) {
        return Arrays.stream(values()).filter(f -> f.option.equals(option)).findFirst();
    }

    abstract String format(int code);

    List<String> header() {
        return List.of();
    }

    /** {@code dir/prog.asm} vira {@code dir/prog.hex}; só a extensão final é trocada. */
    public Path outputPathFor(Path source) {
        String name = source.getFileName().toString();
        int dot = name.lastIndexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        return source.resolveSibling(base + extension);
    }

    public void write(List<Integer> codes, Path target) throws IOException {
        List<String> lines = new ArrayList<>(header());
        codes.forEach(code -> lines.add(format(code)));
        Files.write(target, lines, StandardCharsets.UTF_8);
    }
}
