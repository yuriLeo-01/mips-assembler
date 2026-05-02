import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Assembler {
    public static void main(String[] args) {
        // Requisito: Receber arquivo .asm e parâmetro de formato (-b ou -h)
        if (args.length < 2) {
            System.out.println("Uso: java Assembler <arquivo.asm> <-b ou -h>");
            return;
        }

        String nomeArquivoEntrada = args[0];
        String modoSaida = args[1];
        File arquivo = new File(nomeArquivoEntrada);

        HashMap<String, Integer> tabelaSimbolos = new HashMap<>();
        ArrayList<String> instrucoesLimpas = new ArrayList<>();

        try {
            // --- PRIMEIRA PASSADA ---
            Scanner leitor = new Scanner(arquivo);
            int enderecoInicial = 0x00400000; // Requisito: Endereço de memória inicial
            int enderecoAtual = enderecoInicial;

            while (leitor.hasNextLine()) {
                String linha = leitor.nextLine().trim();

                // Requisito: Ignorar linhas vazias e comentários
                if (linha.isEmpty() || linha.startsWith("#")) continue;
                if (linha.contains("#")) linha = linha.split("#")[0].trim();

                // Requisito: Identificar labels e guardar o endereço correspondente[cite: 1]
                if (linha.endsWith(":")) {
                    String nomeLabel = linha.substring(0, linha.length() - 1);
                    tabelaSimbolos.put(nomeLabel, enderecoAtual);
                    continue;
                }

                instrucoesLimpas.add(linha);
                enderecoAtual += 4; // Cada instrução ocupa 4 bytes[cite: 1]
            }
            leitor.close();

            // --- SEGUNDA PASSADA E GERAÇÃO DE ARQUIVO ---
            String nomeBase = nomeArquivoEntrada.replace(".asm", "");
            String extensao = modoSaida.equals("-h") ? ".hex" : ".bin";
            String nomeSaida = nomeBase + extensao;

            try (PrintWriter escritor = new PrintWriter(new FileWriter(nomeSaida))) {
                // Requisito: Cabeçalho para formato hexadecimal (Logisim)[cite: 1]
                if (modoSaida.equals("-h")) {
                    escritor.println("v2.0 raw");
                }

                for (int i = 0; i < instrucoesLimpas.size(); i++) {
                    String instrucao = instrucoesLimpas.get(i);
                    // Passamos o índice atual 'i' para calcular o PC relativo corretamente
                    String binario = traduzirInstrucao(instrucao, tabelaSimbolos, i);

                    if (modoSaida.equals("-h")) {
                        // Converte binário para hexadecimal de 8 caracteres[cite: 1]
                        long decimal = Long.parseLong(binario, 2);
                        escritor.println(String.format("%08x", decimal));
                    } else {
                        escritor.println(binario);
                    }
                }
                System.out.println("Sucesso! Arquivo gerado: " + nomeSaida);
            }

        } catch (IOException e) {
            System.out.println("Erro ao processar arquivos: " + e.getMessage());
        }
    }

    public static String getReg(String reg) {
        HashMap<String, String> regs = new HashMap<>();
        // Requisito: Aceitar registradores por nome ou número ($s0 ou $16)[cite: 1]
        regs.put("$zero", "00000"); regs.put("$0", "00000");
        regs.put("$at", "00001");   regs.put("$1", "00001");
        regs.put("$v0", "00010");   regs.put("$2", "00010");
        regs.put("$a0", "00100");   regs.put("$4", "00100");
        regs.put("$t0", "01000");   regs.put("$8", "01000");
        regs.put("$t1", "01001");   regs.put("$9", "01001");

        // Adicionando mapeamento completo de $s0-$s3 conforme sua imagem[cite: 1]
        for(int i=0; i<=7; i++) {
            String bin = String.format("%5s", Integer.toBinaryString(16 + i)).replace(' ', '0');
            regs.put("$s" + i, bin);
            regs.put("$" + (16 + i), bin);
        }

        String limpo = reg.replace(",", "").trim();
        return regs.getOrDefault(limpo, "00000");
    }

    public static String traduzirInstrucao(String instrucao, HashMap<String, Integer> tabela, int indiceAtual) {
        String[] partes = instrucao.replace(",", "").split("\\s+");
        String op = partes[0];

        // --- TIPO R ---[cite: 1]
        if (op.equals("add") || op.equals("sub")) {
            String rd = getReg(partes[1]);
            String rs = getReg(partes[2]);
            String rt = getReg(partes[3]);
            String funct = op.equals("add") ? "100000" : "100010";
            return "000000" + rs + rt + rd + "00000" + funct;
        }

        // --- TIPO I: addi ---[cite: 1]
        if (op.equals("addi")) {
            String rt = getReg(partes[1]);
            String rs = getReg(partes[2]);
            int imediato = Integer.parseInt(partes[3]);
            String binImed = String.format("%16s", Integer.toBinaryString(imediato & 0xFFFF)).replace(' ', '0');
            return "001000" + rs + rt + binImed;
        }

        // --- TIPO I: beq (Salto Relativo) ---[cite: 1]
        if (op.equals("beq")) {
            String rs = getReg(partes[1]);
            String rt = getReg(partes[2]);
            int enderecoLabel = tabela.get(partes[3]);
            // Requisito: PC aponta para a próxima instrução (+4 bytes)[cite: 1]
            int enderecoPCProximo = 0x00400000 + (indiceAtual + 1) * 4;
            int deslocamento = (enderecoLabel - enderecoPCProximo) / 4;
            String binDesloc = String.format("%16s", Integer.toBinaryString(deslocamento & 0xFFFF)).replace(' ', '0');
            return "000100" + rs + rt + binDesloc;
        }

        // --- TIPO J: j (Salto Absoluto) ---[cite: 1]
        if (op.equals("j")) {
            int enderecoAlvo = tabela.get(partes[1]) / 4;
            String binAddr = String.format("%26s", Integer.toBinaryString(enderecoAlvo)).replace(' ', '0');
            return "000010" + binAddr;
        }

        return "00000000000000000000000000000000";
    }
}