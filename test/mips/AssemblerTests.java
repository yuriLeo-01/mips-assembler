package mips;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Testes sem dependências externas. Executar: {@code java -cp out mips.AssemblerTests}. */
public final class AssemblerTests {
    private final List<String> failures = new ArrayList<>();
    private int run;

    public static void main(String[] args) {
        AssemblerTests tests = new AssemblerTests();
        tests.runAll();
        tests.failures.forEach(f -> System.out.println("FALHOU " + f));
        System.out.printf("%d teste(s), %d falha(s)%n", tests.run, tests.failures.size());
        System.exit(tests.failures.isEmpty() ? 0 : 1);
    }

    private void runAll() {
        testEncoding();
        testSyntax();
        testErrors();
        testReportAndOutput();
    }

    private void testEncoding() {
        encodes("add $1, $2, $3", 0x00430820);
        encodes("sub $t0, $s1, $s2", 0x02324022);
        encodes("xor $1, $2, $3", 0x00430826);
        encodes("nor $1, $2, $3", 0x00430827);
        encodes("sltu $1, $2, $3", 0x0043082B);
        encodes("mul $1, $2, $3", 0x70430802);
        encodes("sll $1, $2, 10", 0x00020A80);
        encodes("sra $1, $2, 31", 0x00020FC3);
        encodes("mult $2, $3", 0x00430018);
        encodes("multu $2, $3", 0x00430019);
        encodes("div $2, $3", 0x0043001A);
        encodes("divu $2, $3", 0x0043001B);
        encodes("mfhi $2", 0x00001010);
        encodes("mflo $2", 0x00001012);
        encodes("jr $ra", 0x03E00008);
        encodes("nop", 0x00000000);
        encodes("syscall", 0x0000000C);
        encodes("addi $sp, $sp, -4", 0x23BDFFFC);
        encodes("addiu $1, $2, 100", 0x24410064);
        encodes("sltiu $1, $2, 1", 0x2C410001);
        encodes("andi $1, $2, 0xFFFF", 0x3041FFFF);
        encodes("xori $1, $2, 1", 0x38410001);
        encodes("lui $at, 0x1001", 0x3C011001);
        encodes("lw $1, 100 ($2)", 0x8C410064);
        encodes("sw $ra, -8($sp)", 0xAFBFFFF8);
        encodes("lw $t0, ($a0)", 0x8C880000);
    }

    private void testSyntax() {
        test("labels, comentários e desvios", () -> {
            List<Integer> codes = assemble(
                    "# comentário isolado",
                    "inicio:",
                    "  beq $t0, $zero, fim   # para frente",
                    "laco: bne $1, $2, inicio",
                    "a: b: j laco",
                    "fim: jal inicio").codes();
            assertEquals(List.of(0x11000002, 0x1422FFFE, 0x08100001, 0x0C100000), codes);
        });
        test("mnemônico e registrador sem diferenciar maiúsculas", () ->
                assertEquals(0x00430820, assemble("ADD $1, $V0, $3").codes().get(0)));
        test("operandos separados só por espaço", () ->
                assertEquals(0x00430820, assemble("add $1 $2 $3").codes().get(0)));
        test("registradores por número e nome", () -> {
            assertEquals(Register.T0, Register.parse("$8"));
            assertEquals(Register.RA, Register.parse("$31"));
            assertEquals(Register.SP, Register.parse("$sp"));
        });
    }

    private void testErrors() {
        failsAtLine("instrução desconhecida 'foo'", 2, "nop", "foo $1");
        failsAtLine("registrador inválido '$32' (use $0 a $31)", 1, "add $32, $1, $2");
        failsAtLine("registrador inválido '$xy'", 1, "add $xy, $1, $2");
        failsAtLine("esperado(s) 3 operando(s), encontrado(s) 2", 1, "add $1, $2");
        failsAtLine("label 'nada' não definido", 1, "j nada");
        failsAtLine("label 'x' definido mais de uma vez", 2, "x: nop", "x: nop");
        failsAtLine("valor 40000 fora do intervalo [-32768, 32767]", 1, "addi $1, $2, 40000");
        failsAtLine("valor -1 fora do intervalo [0, 65535]", 1, "ori $1, $2, -1");
        failsAtLine("valor 32 fora do intervalo [0, 31]", 1, "sll $1, $2, 32");
        failsAtLine("número inválido 'abc'", 1, "addi $1, $2, abc");
        test("reporta todos os erros, não só o primeiro", () -> {
            try {
                assemble("foo", "nop", "bar");
                throw new AssertionError("deveria falhar");
            } catch (AssemblyException e) {
                assertEquals(List.of(1, 3), e.errors().stream().map(AssemblyError::line).toList());
            }
        });
    }

    private void testReportAndOutput() {
        test("CPI usa ciclos.csv ignorando o cabeçalho", () -> {
            Path csv = Files.createTempFile("ciclos", ".csv");
            Files.writeString(csv, "Instrucao,Ciclos\nmul,32\n");
            Report report = Report.of(assemble("mul $1, $2, $3", "add $1, $2, $3"), CycleTable.load(csv));
            Files.delete(csv);
            assertEquals(16.5, report.averageCpi());
            assertEquals(true, report.format().endsWith("CPI médio: 16,5"));
        });
        test("ciclos.csv com linha inválida é rejeitado", () -> {
            Path csv = Files.createTempFile("ciclos", ".csv");
            Files.writeString(csv, "Instrucao,Ciclos\nmul,muitos\n");
            try {
                CycleTable.load(csv);
                throw new AssertionError("deveria falhar");
            } catch (IOException expected) {
                assertEquals(true, expected.getMessage().contains("linha inválida"));
            } finally {
                Files.delete(csv);
            }
        });
        test("destino troca só a extensão final", () ->
                assertEquals(Path.of("dir.asm", "prog.hex"), OutputFormat.HEX.outputPathFor(Path.of("dir.asm", "prog.asm"))));
        test("erro de montagem não gera arquivo", () -> {
            Path dir = Files.createTempDirectory("montador");
            Path source = Files.writeString(dir.resolve("ruim.asm"), "add $1, $2, $3\nfoo\n");
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            int code = Main.run(new String[] {source.toString(), "-b"}, discard(), new PrintStream(err, true, StandardCharsets.UTF_8));
            assertEquals(Main.FAILURE, code);
            assertEquals(false, Files.exists(dir.resolve("ruim.bin")));
            assertEquals(true, err.toString(StandardCharsets.UTF_8).contains("ruim.asm:2: instrução desconhecida 'foo'"));
            Files.delete(source);
            Files.delete(dir);
        });
        test("opção inválida mostra uso", () ->
                assertEquals(Main.USAGE_ERROR, Main.run(new String[] {"x.asm", "-x"}, discard(), discard())));
    }

    // --- utilitários ---

    private interface Body {
        void run() throws Exception;
    }

    private void test(String name, Body body) {
        run++;
        try {
            body.run();
        } catch (Exception | AssertionError e) {
            failures.add(name + ": " + e);
        }
    }

    private void encodes(String instruction, int expected) {
        test(instruction, () -> assertEquals(String.format("%08x", expected),
                String.format("%08x", assemble(instruction).codes().get(0))));
    }

    private void failsAtLine(String message, int line, String... source) {
        test("erro: " + message, () -> {
            try {
                assemble(source);
                throw new AssertionError("deveria falhar");
            } catch (AssemblyException e) {
                AssemblyError error = e.errors().get(0);
                assertEquals(message, error.getMessage());
                assertEquals(line, error.line());
            }
        });
    }

    private static AssembledProgram assemble(String... lines) throws AssemblyException {
        return new Assembler().assemble(List.of(lines));
    }

    private static PrintStream discard() {
        return new PrintStream(PrintStream.nullOutputStream());
    }

    private static void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("esperado <" + expected + "> mas foi <" + actual + ">");
        }
    }
}
