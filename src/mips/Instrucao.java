package mips;

import static mips.Sintaxe.LABEL;
import static mips.Sintaxe.NENHUM;
import static mips.Sintaxe.RD;
import static mips.Sintaxe.RD_RS_RT;
import static mips.Sintaxe.RD_RT_SHAMT;
import static mips.Sintaxe.RS;
import static mips.Sintaxe.RS_RT;
import static mips.Sintaxe.RS_RT_LABEL;
import static mips.Sintaxe.RT_IMEDIATO;
import static mips.Sintaxe.RT_MEMORIA;
import static mips.Sintaxe.RT_RS_IMEDIATO;
import static mips.Sintaxe.RT_RS_IMEDIATO_LOGICO;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Instruções suportadas, com opcode e funct (este só vale para o tipo R). */
public enum Instrucao {
    // Tipo R
    ADD(RD_RS_RT, 0x00, 0x20), ADDU(RD_RS_RT, 0x00, 0x21),
    SUB(RD_RS_RT, 0x00, 0x22), SUBU(RD_RS_RT, 0x00, 0x23),
    AND(RD_RS_RT, 0x00, 0x24), OR(RD_RS_RT, 0x00, 0x25),
    XOR(RD_RS_RT, 0x00, 0x26), NOR(RD_RS_RT, 0x00, 0x27),
    SLT(RD_RS_RT, 0x00, 0x2A), SLTU(RD_RS_RT, 0x00, 0x2B),
    MUL(RD_RS_RT, 0x1C, 0x02),
    SLL(RD_RT_SHAMT, 0x00, 0x00), SRL(RD_RT_SHAMT, 0x00, 0x02), SRA(RD_RT_SHAMT, 0x00, 0x03),
    MULT(RS_RT, 0x00, 0x18), MULTU(RS_RT, 0x00, 0x19),
    DIV(RS_RT, 0x00, 0x1A), DIVU(RS_RT, 0x00, 0x1B),
    MFHI(RD, 0x00, 0x10), MFLO(RD, 0x00, 0x12),
    JR(RS, 0x00, 0x08),
    NOP(NENHUM, 0x00, 0x00), SYSCALL(NENHUM, 0x00, 0x0C),
    // Tipo I
    ADDI(RT_RS_IMEDIATO, 0x08), ADDIU(RT_RS_IMEDIATO, 0x09),
    SLTI(RT_RS_IMEDIATO, 0x0A), SLTIU(RT_RS_IMEDIATO, 0x0B),
    ANDI(RT_RS_IMEDIATO_LOGICO, 0x0C), ORI(RT_RS_IMEDIATO_LOGICO, 0x0D), XORI(RT_RS_IMEDIATO_LOGICO, 0x0E),
    LUI(RT_IMEDIATO, 0x0F),
    LW(RT_MEMORIA, 0x23), SW(RT_MEMORIA, 0x2B),
    BEQ(RS_RT_LABEL, 0x04), BNE(RS_RT_LABEL, 0x05),
    // Tipo J
    J(LABEL, 0x02), JAL(LABEL, 0x03);

    private static final Map<String, Instrucao> POR_MNEMONICO = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(Instrucao::mnemonico, Function.identity()));

    private final Sintaxe sintaxe;
    private final int opcode;
    private final int funct;

    Instrucao(Sintaxe sintaxe, int opcode, int funct) {
        this.sintaxe = sintaxe;
        this.opcode = opcode;
        this.funct = funct;
    }

    Instrucao(Sintaxe sintaxe, int opcode) {
        this(sintaxe, opcode, 0);
    }

    public static Optional<Instrucao> porMnemonico(String mnemonico) {
        return Optional.ofNullable(POR_MNEMONICO.get(mnemonico.toLowerCase(Locale.ROOT)));
    }

    public String mnemonico() {
        return name().toLowerCase(Locale.ROOT);
    }

    int opcode() {
        return opcode;
    }

    int funct() {
        return funct;
    }

    /** Gera a palavra de 32 bits da instrução que está em {@code endereco}. */
    public int codificar(List<String> operandos, TabelaSimbolos simbolos, int endereco) {
        Operandos validados = new Operandos(operandos, sintaxe.quantidadeOperandos());
        return sintaxe.codificar(this, validados, new Sintaxe.Contexto(simbolos, endereco));
    }
}
