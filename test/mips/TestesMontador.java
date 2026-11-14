package mips;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Testes sem dependências externas. Executar: {@code java -cp out mips.TestesMontador}. */
public final class TestesMontador {
    private final List<String> falhas = new ArrayList<>();
    private int executados;

    public static void main(String[] args) {
        TestesMontador testes = new TestesMontador();
        testes.executarTodos();
        testes.falhas.forEach(f -> System.out.println("FALHOU " + f));
        System.out.printf("%d teste(s), %d falha(s)%n", testes.executados, testes.falhas.size());
        System.exit(testes.falhas.isEmpty() ? 0 : 1);
    }

    private void executarTodos() {
        testarCodificacao();
        testarSintaxe();
        testarErros();
        testarRelatorioESaida();
    }

    private void testarCodificacao() {
        codifica("add $1, $2, $3", 0x00430820);
        codifica("sub $t0, $s1, $s2", 0x02324022);
        codifica("xor $1, $2, $3", 0x00430826);
        codifica("nor $1, $2, $3", 0x00430827);
        codifica("sltu $1, $2, $3", 0x0043082B);
        codifica("mul $1, $2, $3", 0x70430802);
        codifica("sll $1, $2, 10", 0x00020A80);
        codifica("sra $1, $2, 31", 0x00020FC3);
        codifica("mult $2, $3", 0x00430018);
        codifica("multu $2, $3", 0x00430019);
        codifica("div $2, $3", 0x0043001A);
        codifica("divu $2, $3", 0x0043001B);
        codifica("mfhi $2", 0x00001010);
        codifica("mflo $2", 0x00001012);
        codifica("jr $ra", 0x03E00008);
        codifica("nop", 0x00000000);
        codifica("syscall", 0x0000000C);
        codifica("addi $sp, $sp, -4", 0x23BDFFFC);
        codifica("addiu $1, $2, 100", 0x24410064);
        codifica("sltiu $1, $2, 1", 0x2C410001);
        codifica("andi $1, $2, 0xFFFF", 0x3041FFFF);
        codifica("xori $1, $2, 1", 0x38410001);
        codifica("lui $at, 0x1001", 0x3C011001);
        codifica("lw $1, 100 ($2)", 0x8C410064);
        codifica("sw $ra, -8($sp)", 0xAFBFFFF8);
        codifica("lw $t0, ($a0)", 0x8C880000);
    }

    private void testarSintaxe() {
        teste("labels, comentários e desvios", () -> {
            List<Integer> codigos = montar(
                    "# comentário isolado",
                    "inicio:",
                    "  beq $t0, $zero, fim   # para frente",
                    "laco: bne $1, $2, inicio",
                    "a: b: j laco",
                    "fim: jal inicio").codigos();
            igual(List.of(0x11000002, 0x1422FFFE, 0x08100001, 0x0C100000), codigos);
        });
        teste("mnemônico e registrador sem diferenciar maiúsculas", () ->
                igual(0x00430820, montar("ADD $1, $V0, $3").codigos().get(0)));
        teste("operandos separados só por espaço", () ->
                igual(0x00430820, montar("add $1 $2 $3").codigos().get(0)));
        teste("registradores por número e nome", () -> {
            igual(Registrador.T0, Registrador.de("$8"));
            igual(Registrador.RA, Registrador.de("$31"));
            igual(Registrador.SP, Registrador.de("$sp"));
        });
    }

    private void testarErros() {
        falhaNaLinha("instrução desconhecida 'foo'", 2, "nop", "foo $1");
        falhaNaLinha("registrador inválido '$32' (use $0 a $31)", 1, "add $32, $1, $2");
        falhaNaLinha("registrador inválido '$xy'", 1, "add $xy, $1, $2");
        falhaNaLinha("esperado(s) 3 operando(s), encontrado(s) 2", 1, "add $1, $2");
        falhaNaLinha("label 'nada' não definido", 1, "j nada");
        falhaNaLinha("label 'x' definido mais de uma vez", 2, "x: nop", "x: nop");
        falhaNaLinha("valor 40000 fora do intervalo [-32768, 32767]", 1, "addi $1, $2, 40000");
        falhaNaLinha("valor -1 fora do intervalo [0, 65535]", 1, "ori $1, $2, -1");
        falhaNaLinha("valor 32 fora do intervalo [0, 31]", 1, "sll $1, $2, 32");
        falhaNaLinha("número inválido 'abc'", 1, "addi $1, $2, abc");
        teste("reporta todos os erros, não só o primeiro", () -> {
            try {
                montar("foo", "nop", "bar");
                throw new AssertionError("deveria falhar");
            } catch (FalhaMontagem e) {
                igual(List.of(1, 3), e.erros().stream().map(ErroMontagem::linha).toList());
            }
        });
    }

    private void testarRelatorioESaida() {
        teste("CPI usa ciclos.csv ignorando o cabeçalho", () -> {
            Path csv = Files.createTempFile("ciclos", ".csv");
            Files.writeString(csv, "Instrucao,Ciclos\nmul,32\n");
            Relatorio relatorio = Relatorio.de(montar("mul $1, $2, $3", "add $1, $2, $3"), TabelaCiclos.carregar(csv));
            Files.delete(csv);
            igual(16.5, relatorio.cpiMedio());
            igual(true, relatorio.formatar().endsWith("CPI médio: 16,5"));
        });
        teste("ciclos.csv com linha inválida é rejeitado", () -> {
            Path csv = Files.createTempFile("ciclos", ".csv");
            Files.writeString(csv, "Instrucao,Ciclos\nmul,muitos\n");
            try {
                TabelaCiclos.carregar(csv);
                throw new AssertionError("deveria falhar");
            } catch (IOException esperado) {
                igual(true, esperado.getMessage().contains("linha inválida"));
            } finally {
                Files.delete(csv);
            }
        });
        teste("destino troca só a extensão final", () ->
                igual(Path.of("dir.asm", "prog.hex"), FormatoSaida.HEXADECIMAL.destinoPara(Path.of("dir.asm", "prog.asm"))));
        teste("erro de montagem não gera arquivo", () -> {
            Path dir = Files.createTempDirectory("montador");
            Path fonte = Files.writeString(dir.resolve("ruim.asm"), "add $1, $2, $3\nfoo\n");
            ByteArrayOutputStream erro = new ByteArrayOutputStream();
            int codigo = Main.executar(new String[] {fonte.toString(), "-b"}, nulo(), new PrintStream(erro, true, StandardCharsets.UTF_8));
            igual(Main.FALHA, codigo);
            igual(false, Files.exists(dir.resolve("ruim.bin")));
            igual(true, erro.toString(StandardCharsets.UTF_8).contains("ruim.asm:2: instrução desconhecida 'foo'"));
            Files.delete(fonte);
            Files.delete(dir);
        });
        teste("opção inválida mostra uso", () ->
                igual(Main.USO_INCORRETO, Main.executar(new String[] {"x.asm", "-x"}, nulo(), nulo())));
    }

    // --- utilitários ---

    private interface Corpo {
        void executar() throws Exception;
    }

    private void teste(String nome, Corpo corpo) {
        executados++;
        try {
            corpo.executar();
        } catch (Exception | AssertionError e) {
            falhas.add(nome + ": " + e);
        }
    }

    private void codifica(String instrucao, int esperado) {
        teste(instrucao, () -> igual(String.format("%08x", esperado),
                String.format("%08x", montar(instrucao).codigos().get(0))));
    }

    private void falhaNaLinha(String mensagem, int linha, String... fonte) {
        teste("erro: " + mensagem, () -> {
            try {
                montar(fonte);
                throw new AssertionError("deveria falhar");
            } catch (FalhaMontagem e) {
                ErroMontagem erro = e.erros().get(0);
                igual(mensagem, erro.getMessage());
                igual(linha, erro.linha());
            }
        });
    }

    private static ProgramaMontado montar(String... linhas) throws FalhaMontagem {
        return new Montador().montar(List.of(linhas));
    }

    private static PrintStream nulo() {
        return new PrintStream(PrintStream.nullOutputStream());
    }

    private static void igual(Object esperado, Object obtido) {
        if (!esperado.equals(obtido)) {
            throw new AssertionError("esperado <" + esperado + "> mas foi <" + obtido + ">");
        }
    }
}
