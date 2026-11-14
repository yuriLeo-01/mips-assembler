package mips;

import static mips.Register.ZERO;

/** Ordem dos operandos de uma instrução e como eles ocupam os campos R, I ou J. */
enum Syntax {
    RD_RS_RT(3) {
        int encode(Instruction i, Operands o, Context c) {
            return rType(i, o.register(1), o.register(2), o.register(0), 0);
        }
    },
    RD_RT_SHAMT(3) {
        int encode(Instruction i, Operands o, Context c) {
            return rType(i, ZERO, o.register(1), o.register(0), o.immediate(2, 0, 31));
        }
    },
    RS_RT(2) {
        int encode(Instruction i, Operands o, Context c) {
            return rType(i, o.register(0), o.register(1), ZERO, 0);
        }
    },
    RD(1) {
        int encode(Instruction i, Operands o, Context c) {
            return rType(i, ZERO, ZERO, o.register(0), 0);
        }
    },
    RS(1) {
        int encode(Instruction i, Operands o, Context c) {
            return rType(i, o.register(0), ZERO, ZERO, 0);
        }
    },
    NONE(0) {
        int encode(Instruction i, Operands o, Context c) {
            return rType(i, ZERO, ZERO, ZERO, 0);
        }
    },
    RT_RS_IMM(3) {
        int encode(Instruction i, Operands o, Context c) {
            return iType(i, o.register(1), o.register(0), o.immediate(2, Short.MIN_VALUE, Short.MAX_VALUE));
        }
    },
    RT_RS_IMM_UNSIGNED(3) {
        int encode(Instruction i, Operands o, Context c) {
            return iType(i, o.register(1), o.register(0), o.immediate(2, 0, MAX_UNSIGNED_IMMEDIATE));
        }
    },
    RT_IMM(2) {
        int encode(Instruction i, Operands o, Context c) {
            return iType(i, ZERO, o.register(0), o.immediate(1, 0, MAX_UNSIGNED_IMMEDIATE));
        }
    },
    RT_MEM(2) {
        int encode(Instruction i, Operands o, Context c) {
            Operands.MemoryAddress memory = o.memory(1);
            return iType(i, memory.base(), o.register(0), memory.offset());
        }
    },
    RS_RT_LABEL(3) {
        int encode(Instruction i, Operands o, Context c) {
            int target = c.symbols().addressOf(o.label(2));
            int offset = (target - (c.address() + 4)) / 4;
            if (offset < Short.MIN_VALUE || offset > Short.MAX_VALUE) {
                throw new AssemblyError("label '" + o.label(2) + "' fora do alcance do desvio");
            }
            return iType(i, o.register(0), o.register(1), offset);
        }
    },
    LABEL(1) {
        int encode(Instruction i, Operands o, Context c) {
            int target = c.symbols().addressOf(o.label(0));
            return i.opcode() << 26 | (target >>> 2 & 0x3FFFFFF);
        }
    };

    private static final int MAX_UNSIGNED_IMMEDIATE = 0xFFFF;

    private final int operandCount;

    Syntax(int operandCount) {
        this.operandCount = operandCount;
    }

    int operandCount() {
        return operandCount;
    }

    abstract int encode(Instruction instruction, Operands operands, Context context);

    private static int rType(Instruction i, Register rs, Register rt, Register rd, int shamt) {
        return i.opcode() << 26 | rs.number() << 21 | rt.number() << 16 | rd.number() << 11 | shamt << 6 | i.funct();
    }

    private static int iType(Instruction i, Register rs, Register rt, int immediate) {
        return i.opcode() << 26 | rs.number() << 21 | rt.number() << 16 | immediate & 0xFFFF;
    }

    /** Tabela de símbolos e endereço da instrução sendo codificada. */
    record Context(SymbolTable symbols, int address) {
    }
}
