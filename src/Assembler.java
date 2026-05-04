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
                // Ignora comentários e linhas vazias
                if (linha.isEmpty() || linha.startsWith("#")) continue;
                if (linha.contains("#")) linha = linha.split("#")[0].trim();

                if (linha.contains(":")) {
                    String[] partes = linha.split(":");
                    tabelaSimbolos.put(partes[0].trim(), enderecoAtual);
                    // Se tiver instrução na mesma linha do label: "L1: add $1, $2, $3"
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

            // --- SEGUNDA PASSADA: Tradução ---
            String extensao = modoSaida.equals("-h") ? ".hex" : ".bin";
            String nomeSaida = nomeArquivoEntrada.replace(".asm", "") + extensao;
            long somaCiclosTotal = 0;

            try (PrintWriter escritor = new PrintWriter(new FileWriter(nomeSaida))) {
                if (modoSaida.equals("-h")) escritor.println("v2.0 raw");

                for (int i = 0; i < instrucoesLimpas.size(); i++) {
                    String linhaOriginal = instrucoesLimpas.get(i).trim();
                    if (linhaOriginal.isEmpty()) continue;

                    // Divide para pegar o opcode (ex: add, sub)
                    String[] partesEspaco = linhaOriginal.split("\\s+");
                    String op = partesEspaco[0].toLowerCase();

                    contadorInstrucoes.put(op, contadorInstrucoes.getOrDefault(op, 0) + 1);
                    somaCiclosTotal += tabelaCiclos.getOrDefault(op, 1);

                    String binario = traduzirInstrucao(linhaOriginal, tabelaSimbolos, i);

                    if (modoSaida.equals("-h")) {
                        long decimal = Long.parseUnsignedLong(binario, 2);
                        escritor.println(String.format("%08x", decimal));
                    } else {
                        escritor.println(binario);
                    }
                }
                System.out.println("Sucesso! Arquivo gerado: " + nomeSaida);
            }

            // Relatório
            System.out.println("\nQuantidades por tipo de instruções:");
            for (Map.Entry<String, Integer> entry : contadorInstrucoes.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
            if (!instrucoesLimpas.isEmpty()) {
                double cpiMedio = (double) somaCiclosTotal / instrucoesLimpas.size();
                System.out.printf("CPI médio: %.1f\n", cpiMedio);
            }

        } catch (Exception e) {
            System.out.println("Erro ao processar: " + e.getMessage());
            e.printStackTrace(); // Isso ajuda você a ver a linha exata do erro
        }
    }

    private static Map<String, Integer> carregarCiclos(String path) {
        Map<String, Integer> mapa = new HashMap<>();
        File file = new File(path);
        if (!file.exists()) return mapa;
        try (Scanner s = new Scanner(file)) {
            while (s.hasNextLine()) {
                String l = s.nextLine().trim();
                if (l.isEmpty() || !l.contains(",")) continue;
                String[] p = l.split(",");
                mapa.put(p[0].trim().toLowerCase(), Integer.parseInt(p[1].trim()));
            }
        } catch (Exception e) { }
        return mapa;
    }

    public static String getReg(String reg) {
        // Limpeza agressiva: remove $, vírgulas e parênteses
        String limpo = reg.replaceAll("[$,() ]", "").trim();

        HashMap<String, String> regs = new HashMap<>();
        regs.put("zero", "00000"); regs.put("0", "00000");
        regs.put("at", "00001");   regs.put("1", "00001");
        regs.put("v0", "00010");   regs.put("2", "00010");

        // s0-s7 (16-23)
        for(int i=0; i<8; i++) {
            regs.put("s" + i, String.format("%5s", Integer.toBinaryString(16 + i)).replace(' ', '0'));
        }

        if (regs.containsKey(limpo)) return regs.get(limpo);

        try {
            int numReg = Integer.parseInt(limpo);
            return String.format("%5s", Integer.toBinaryString(numReg)).replace(' ', '0');
        } catch (Exception e) {
            return "00000"; // Default para evitar crash
        }
    }

    public static String traduzirInstrucao(String instrucao, HashMap<String, Integer> tabela, int indiceAtual) {
        // 1. Limpeza e Validação Inicial
        String[] partes = instrucao.replace(",", " ").trim().split("\\s+");

        // SEGUNDA TRAVA DE SEGURANÇA: Se a linha tiver menos de 2 partes (Opcode + algo)
        // e não for uma das instruções que aceitam apenas 1 parâmetro (j, jal, jr, mfhi, mflo)
        // nós ignoramos para não dar o erro de Index Out of Bounds.
        if (partes.length < 2 && !partes[0].toLowerCase().matches("jr|mfhi|mflo")) {
            return "00000000000000000000000000000000";
        }

        String op = partes[0].toLowerCase();

        // TIPO R padrão (3 registradores) - EXIGE partes[1], [2] e [3]
        if (op.matches("add|addu|sub|subu|and|or|slt|mul")) {
            if (partes.length < 4) return "00000000000000000000000000000000"; // Segurança
            String rd = getReg(partes[1]);
            String rs = getReg(partes[2]);
            String rt = getReg(partes[3]);
            String funct = "";
            String opcode = "000000";

            switch (op) {
                case "add":  funct = "100000"; break;
                case "addu": funct = "100001"; break;
                case "sub":  funct = "100010"; break;
                case "subu": funct = "100011"; break;
                case "and":  funct = "100100"; break;
                case "or":   funct = "100101"; break;
                case "slt":  funct = "101010"; break;
                case "mul":  opcode = "011100"; funct = "000010"; break;
            }
            return opcode + rs + rt + rd + "00000" + funct;
        }

        // TIPO R: Shifts (sll, srl) - EXIGE partes[1], [2] e [3]
        if (op.equals("sll") || op.equals("srl")) {
            if (partes.length < 4) return "00000000000000000000000000000000";
            String rd = getReg(partes[1]);
            String rt = getReg(partes[2]);
            int shamt = Integer.parseInt(partes[3]);
            String binShamt = String.format("%5s", Integer.toBinaryString(shamt & 0x1F)).replace(' ', '0');
            String funct = op.equals("sll") ? "000000" : "000010";
            return "00000000000" + rt + rd + binShamt + funct;
        }

        // TIPO I: Imediatos - EXIGE partes[1], [2] e [3]
        if (op.matches("addi|addiu|andi|ori|slti")) {
            if (partes.length < 4) return "00000000000000000000000000000000";
            String rt = getReg(partes[1]);
            String rs = getReg(partes[2]);
            int imediato = Integer.parseInt(partes[3]);
            String binImed = String.format("%16s", Integer.toBinaryString(imediato & 0xFFFF)).replace(' ', '0');
            String opcode = "";
            switch(op){
                case "addi":  opcode = "001000"; break;
                case "addiu": opcode = "001001"; break;
                case "andi":  opcode = "001100"; break;
                case "ori":   opcode = "001101"; break;
                case "slti":  opcode = "001010"; break;
            }
            return opcode + rs + rt + binImed;
        }

        // TIPO I: beq, bne - EXIGE partes[1], [2] e [3]
        if (op.equals("beq") || op.equals("bne")) {
            if (partes.length < 4) return "00000000000000000000000000000000";
            String rs = getReg(partes[1]);
            String rt = getReg(partes[2]);
            int enderecoLabel = tabela.getOrDefault(partes[3], 0);
            int enderecoPCProximo = 0x00400000 + (indiceAtual + 1) * 4;
            int deslocamento = (enderecoLabel - enderecoPCProximo) / 4;
            String binDesloc = String.format("%16s", Integer.toBinaryString(deslocamento & 0xFFFF)).replace(' ', '0');
            String opcode = op.equals("beq") ? "000100" : "000101";
            return opcode + rs + rt + binDesloc;
        }

        // TIPO I: lw, sw
        if (op.equals("lw") || op.equals("sw")) {
            if (partes.length < 3) return "00000000000000000000000000000000";
            String rt = getReg(partes[1]);
            String[] extra = partes[2].replace(")", "").split("\\(");
            if (extra.length < 2) return "00000000000000000000000000000000";
            int imediato = Integer.parseInt(extra[0]);
            String rs = getReg(extra[1]);
            String opcode = op.equals("lw") ? "100011" : "101011";
            String binImed = String.format("%16s", Integer.toBinaryString(imediato & 0xFFFF)).replace(' ', '0');
            return opcode + rs + rt + binImed;
        }

        // TIPO I: lui
        if (op.equals("lui")) {
            if (partes.length < 3) return "00000000000000000000000000000000";
            String rt = getReg(partes[1]);
            int imediato = Integer.parseInt(partes[2]);
            String binImed = String.format("%16s", Integer.toBinaryString(imediato & 0xFFFF)).replace(' ', '0');
            return "00111100000" + rt + binImed;
        }

        // TIPO J: j, jal
        if (op.equals("j") || op.equals("jal")) {
            if (partes.length < 2) return "00000000000000000000000000000000";
            int enderecoAlvo = tabela.getOrDefault(partes[1], 0x00400000) / 4;
            String binAddr = String.format("%26s", Integer.toBinaryString(enderecoAlvo & 0x3FFFFFF)).replace(' ', '0');
            String opcode = op.equals("j") ? "000010" : "000011";
            return opcode + binAddr;
        }

        // TIPO R: jr
        if (op.equals("jr")) {
            if (partes.length < 2) return "00000000000000000000000000000000";
            String rs = getReg(partes[1]);
            return "000000" + rs + "000000000000000001000";
        }

        // Outros R: mult, div, mfhi, mflo
        if (op.matches("mult|div")) {
            if (partes.length < 3) return "00000000000000000000000000000000";
            String rs = getReg(partes[1]);
            String rt = getReg(partes[2]);
            String funct = op.equals("mult") ? "011000" : "011010";
            return "000000" + rs + rt + "0000000000" + funct;
        }
        if (op.matches("mfhi|mflo")) {
            if (partes.length < 2) return "00000000000000000000000000000000";
            String rd = getReg(partes[1]);
            String funct = op.equals("mfhi") ? "010000" : "010010";
            return "0000000000000000" + rd + "00000" + funct;
        }

        return "00000000000000000000000000000000";
    }
}