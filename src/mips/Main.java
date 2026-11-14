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
    static final int SUCCESS = 0;
    static final int USAGE_ERROR = 1;
    static final int FAILURE = 2;

    private static final String CYCLES_FILE = "ciclos.csv";

    private Main() {
    }

    public static void main(String[] args) {
        System.exit(run(args, System.out, System.err));
    }

    static int run(String[] args, PrintStream out, PrintStream err) {
        Optional<OutputFormat> format = args.length == 2 ? OutputFormat.fromOption(args[1]) : Optional.empty();
        if (format.isEmpty()) {
            err.println("Uso: java -cp out mips.Main <arquivo.asm> <-b | -h>");
            err.println("  -b  binário (.bin)    -h  hexadecimal Logisim (.hex)");
            return USAGE_ERROR;
        }
        Path source = Path.of(args[0]);
        try {
            AssembledProgram program = new Assembler().assemble(Files.readAllLines(source, StandardCharsets.UTF_8));
            Path target = format.get().outputPathFor(source);
            format.get().write(program.codes(), target);
            out.println("Sucesso! Arquivo gerado: " + target);
            out.println();
            out.println(Report.of(program, loadCycles(source, err)).format());
            return SUCCESS;
        } catch (AssemblyException failure) {
            failure.errors().forEach(e -> err.println(source + ":" + e.line() + ": " + e.getMessage()));
            err.println(failure.getMessage() + "; nenhum arquivo foi gerado.");
        } catch (IOException e) {
            err.println("Erro de arquivo: " + e.getMessage());
        }
        return FAILURE;
    }

    /** Procura o CSV no diretório atual e depois ao lado do .asm; sem ele, cada instrução vale 1 ciclo. */
    private static CycleTable loadCycles(Path source, PrintStream err) throws IOException {
        Path sourceDir = source.toAbsolutePath().getParent();
        for (Path candidate : List.of(Path.of(CYCLES_FILE), sourceDir.resolve(CYCLES_FILE))) {
            if (Files.isRegularFile(candidate)) {
                return CycleTable.load(candidate);
            }
        }
        err.println("Aviso: " + CYCLES_FILE + " não encontrado; usando 1 ciclo por instrução.");
        return CycleTable.defaults();
    }
}
