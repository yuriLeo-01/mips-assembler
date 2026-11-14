package mips;

import static mips.Syntax.LABEL;
import static mips.Syntax.NONE;
import static mips.Syntax.RD;
import static mips.Syntax.RD_RS_RT;
import static mips.Syntax.RD_RT_SHAMT;
import static mips.Syntax.RS;
import static mips.Syntax.RS_RT;
import static mips.Syntax.RS_RT_LABEL;
import static mips.Syntax.RT_IMM;
import static mips.Syntax.RT_MEM;
import static mips.Syntax.RT_RS_IMM;
import static mips.Syntax.RT_RS_IMM_UNSIGNED;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Instruções suportadas, com opcode e funct (este só vale para o tipo R). */
public enum Instruction {
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
    NOP(NONE, 0x00, 0x00), SYSCALL(NONE, 0x00, 0x0C),
    // Tipo I
    ADDI(RT_RS_IMM, 0x08), ADDIU(RT_RS_IMM, 0x09),
    SLTI(RT_RS_IMM, 0x0A), SLTIU(RT_RS_IMM, 0x0B),
    ANDI(RT_RS_IMM_UNSIGNED, 0x0C), ORI(RT_RS_IMM_UNSIGNED, 0x0D), XORI(RT_RS_IMM_UNSIGNED, 0x0E),
    LUI(RT_IMM, 0x0F),
    LW(RT_MEM, 0x23), SW(RT_MEM, 0x2B),
    BEQ(RS_RT_LABEL, 0x04), BNE(RS_RT_LABEL, 0x05),
    // Tipo J
    J(LABEL, 0x02), JAL(LABEL, 0x03);

    private static final Map<String, Instruction> BY_MNEMONIC = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(Instruction::mnemonic, Function.identity()));

    private final Syntax syntax;
    private final int opcode;
    private final int funct;

    Instruction(Syntax syntax, int opcode, int funct) {
        this.syntax = syntax;
        this.opcode = opcode;
        this.funct = funct;
    }

    Instruction(Syntax syntax, int opcode) {
        this(syntax, opcode, 0);
    }

    public static Optional<Instruction> fromMnemonic(String mnemonic) {
        return Optional.ofNullable(BY_MNEMONIC.get(mnemonic.toLowerCase(Locale.ROOT)));
    }

    public String mnemonic() {
        return name().toLowerCase(Locale.ROOT);
    }

    int opcode() {
        return opcode;
    }

    int funct() {
        return funct;
    }

    /** Gera a palavra de 32 bits da instrução que está em {@code address}. */
    public int encode(List<String> operands, SymbolTable symbols, int address) {
        Operands validated = new Operands(operands, syntax.operandCount());
        return syntax.encode(this, validated, new Syntax.Context(symbols, address));
    }
}
