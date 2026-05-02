import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

public class Assembler {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: java Assembler <arquivo.asm> <-b ou -h>");
            return;
        }

        String nomeArquivoEntrada = args[0];
        String modoSaida = args[1];
        HashMap<String, Integer> tabelaSimbolos = new HashMap<>();
        ArrayList<String> instrucoesLimpas = new ArrayList<>();
        Map<String, Integer> contadorInstrucoes = new TreeMap<>();
        Map<String, Integer> tabelaCiclos = carregarCiclos("ciclos.csv");

        try {
            // --- PRIMEIRA PASSADA: Labels e Endereços ---
            Scanner leitor = new Scanner(new File(nomeArquivoEntrada));
            int enderecoAtual = 0x00400000;

            while (leitor.hasNextLine()) {
                String linha = leitor.nextLine().trim();
                if (linha.isEmpty() || linha.startsWith("#")) continue;
                if (linha.contains("#")) linha = linha.split("#")[0].trim();

                if (linha.contains(":")) {
                    String[] partes = linha.split(":");
                    tabelaSimbolos.put(partes[0].trim(), enderecoAtual);
                    if (partes.length > 1 && !partes[1].trim().isEmpty()) {
                        instrucoesLimpas.add(partes[1].trim());
                        enderecoAtual += 4;
                    }
                } else {
                    instrucoesLimpas.add(linha);
                    enderecoAtual += 4;
                }
            }
            leitor.close();

            // --- SEGUNDA PASSADA: Tradução e Estatísticas ---
            String extensao = modoSaida.equals("-h") ? ".hex" : ".bin";
            String nomeSaida = nomeArquivoEntrada.replace(".asm", "") + extensao;
            long somaCiclosTotal = 0;

            try (PrintWriter escritor = new PrintWriter(new FileWriter(nomeSaida))) {
                if (modoSaida.equals("-h")) escritor.println("v2.0 raw");

                for (int i = 0; i < instrucoesLimpas.size(); i++) {
                    String linha = instrucoesLimpas.get(i);
                    String op = linha.split("\\s+")[0].toLowerCase();

                    contadorInstrucoes.put(op, contadorInstrucoes.getOrDefault(op, 0) + 1);
                    somaCiclosTotal += tabelaCiclos.getOrDefault(op, 1);

                    String binario = traduzirInstrucao(linha, tabelaSimbolos, i);
                    if (modoSaida.equals("-h")) {
                        long decimal = Long.parseUnsignedLong(binario, 2);
                        escritor.println(String.format("%08x", decimal));
                    } else {
                        escritor.println(binario);
                    }
                }
                System.out.println("Sucesso! Arquivo gerado: " + nomeSaida);
            }

            // --- EXIBIÇÃO DO RELATÓRIO ---
            System.out.println("\nQuantidades por tipo de instruções:");
            for (Map.Entry<String, Integer> entry : contadorInstrucoes.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
            double cpiMedio = (double) somaCiclosTotal / instrucoesLimpas.size();
            System.out.printf("CPI médio: %.1f\n", cpiMedio);

        } catch (Exception e) {
            System.out.println("Erro ao processar: " + e.getMessage());
        }
    }

    private static Map<String, Integer> carregarCiclos(String path) {
        Map<String, Integer> mapa = new HashMap<>();
        File file = new File(path);
        if (!file.exists()) return mapa;
        try (Scanner s = new Scanner(file)) {
            while (s.hasNextLine()) {
                String[] p = s.nextLine().split(",");
                if (p.length == 2) mapa.put(p[0].trim().toLowerCase(), Integer.parseInt(p[1].trim()));
            }
        } catch (Exception e) { }
        return mapa;
    }

    public static String getReg(String reg) {
        HashMap<String, String> regs = new HashMap<>();
        regs.put("$zero", "00000"); regs.put("$0", "00000");
        regs.put("$at", "00001");   regs.put("$1", "00001");
        regs.put("$v0", "00010");   regs.put("$2", "00010");
        for(int i=0; i<4; i++) { // t0-t3
            regs.put("$t" + i, String.format("%5s", Integer.toBinaryString(8 + i)).replace(' ', '0'));
        }
        for(int i=0; i<8; i++) { // s0-s7
            regs.put("$s" + i, String.format("%5s", Integer.toBinaryString(16 + i)).replace(' ', '0'));
        }
        String limpo = reg.replace(",", "").replace("$", "").trim();
        try {
            int numReg = Integer.parseInt(limpo);
            return String.format("%5s", Integer.toBinaryString(numReg)).replace(' ', '0');
        } catch (Exception e) {
            return regs.getOrDefault("$" + limpo, "00000");
        }
    }

    public static String traduzirInstrucao(String instrucao, HashMap<String, Integer> tabela, int indiceAtual) {
        String[] partes = instrucao.replace(",", "").split("\\s+");
        String op = partes[0].toLowerCase();

        // TIPO R: Padrão (3 registradores)
        if (op.equals("add") || op.equals("sub") || op.equals("and") || op.equals("or") || op.equals("slt")) {
            String rd = getReg(partes[1]);
            String rs = getReg(partes[2]);
            String rt = getReg(partes[3]);

            String funct = "";
            switch (op) {
                case "add": funct = "100000"; break; // 32
                case "sub": funct = "100010"; break; // 34
                case "and": funct = "100100"; break; // 36
                case "or":  funct = "100101"; break; // 37
                case "slt": funct = "101010"; break; // 42
            }
            return "000000" + rs + rt + rd + "00000" + funct;
        }

        // TIPO R: jr
        else if (op.equals("jr")) {
            String rs = getReg(partes[1]);
            // Opcode(0) + rs + rt(0) + rd(0) + shamt(0) + funct(8)
            return "000000" + rs + "000000000000000001000";
        }

        // TIPO R: mult e div
        else if (op.equals("mult") || op.equals("div")) {
            String rs = getReg(partes[1]);
            String rt = getReg(partes[2]);
            String funct = op.equals("mult") ? "011000" : "011010"; // mult=24, div=26
            // Opcode(0) + rs + rt + rd(0) + shamt(0) + funct
            return "000000" + rs + rt + "0000000000" + funct;
        }

        // TIPO R: mfhi e mflo
        else if (op.equals("mfhi") || op.equals("mflo")) {
            String rd = getReg(partes[1]);
            String funct = op.equals("mfhi") ? "010000" : "010010"; // mfhi=16, mflo=18
            // Opcode(0) + rs(0) + rt(0) + rd + shamt(0) + funct
            return "0000000000000000" + rd + "00000" + funct;
        }

        // TIPO I: addi
        if (op.equals("addi")) {
            String rt = getReg(partes[1]);
            String rs = getReg(partes[2]);
            int imediato = Integer.parseInt(partes[3]);
            String binImed = String.format("%16s", Integer.toBinaryString(imediato & 0xFFFF)).replace(' ', '0');
            return "001000" + rs + rt + binImed;
        }

        // TIPO I: beq
        if (op.equals("beq")) {
            String rs = getReg(partes[1]);
            String rt = getReg(partes[2]);
            int enderecoLabel = tabela.get(partes[3]);
            int enderecoPCProximo = 0x00400000 + (indiceAtual + 1) * 4;
            int deslocamento = (enderecoLabel - enderecoPCProximo) / 4;
            String binDesloc = String.format("%16s", Integer.toBinaryString(deslocamento & 0xFFFF)).replace(' ', '0');
            return "000100" + rs + rt + binDesloc;
        }

        // TIPO J: j
        if (op.equals("j")) {
            int enderecoAlvo = tabela.get(partes[1]) / 4;
            String binAddr = String.format("%26s", Integer.toBinaryString(enderecoAlvo)).replace(' ', '0');
            return "000010" + binAddr;
        }
        // TIPO I: (lw e sw)
        if (op.equals("lw") || op.equals("sw")) {
            String rt = getReg(partes[1]);
            // Aqui tratamos a sintaxe "4($t1)"
            String[] extra = partes[2].replace(")", "").split("\\(");
            int imediato = Integer.parseInt(extra[0]);
            String rs = getReg(extra[1]);

            // lw = 100011 (35) | sw = 101011 (43)
            String opcode = op.equals("lw") ? "100011" : "101011";
            String binImed = String.format("%16s", Integer.toBinaryString(imediato & 0xFFFF)).replace(' ', '0');

            return opcode + rs + rt + binImed;
        }

        return "00000000000000000000000000000000";
    }
}