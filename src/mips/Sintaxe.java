package mips;

import static mips.Registrador.ZERO;

/** Ordem dos operandos de uma instrução e como eles ocupam os campos R, I ou J. */
enum Sintaxe {
    RD_RS_RT(3) {
        int codificar(Instrucao i, Operandos o, Contexto c) {
            return tipoR(i, o.registrador(1), o.registrador(2), o.registrador(0), 0);
        }
    },
    RD_RT_SHAMT(3) {
        int codificar(Instrucao i, Operandos o, Contexto c) {
            return tipoR(i, ZERO, o.registrador(1), o.registrador(0), o.imediato(2, 0, 31));
        }
    },
    RS_RT(2) {
        int codificar(Instrucao i, Operandos o, Contexto c) {
            return tipoR(i, o.registrador(0), o.registrador(1), ZERO, 0);
        }
    },
    RD(1) {
        int codificar(Instrucao i, Operandos o, Contexto c) {
            return tipoR(i, ZERO, ZERO, o.registrador(0), 0);
        }
    },
    RS(1) {
        int codificar(Instrucao i, Operandos o, Contexto c) {
            return tipoR(i, o.registrador(0), ZERO, ZERO, 0);
        }
    },
    NENHUM(0) {
        int codificar(Instrucao i, Operandos o, Contexto c) {
            return tipoR(i, ZERO, ZERO, ZERO, 0);
        }
    },
    RT_RS_IMEDIATO(3) {
        int codificar(Instrucao i, Operandos o, Contexto c) {
            return tipoI(i, o.registrador(1), o.registrador(0), o.imediato(2, Short.MIN_VALUE, Short.MAX_VALUE));
        }
    },
    RT_RS_IMEDIATO_LOGICO(3) {
        int codificar(Instrucao i, Operandos o, Contexto c) {
            return tipoI(i, o.registrador(1), o.registrador(0), o.imediato(2, 0, MAIOR_IMEDIATO_SEM_SINAL));
        }
    },
    RT_IMEDIATO(2) {
        int codificar(Instrucao i, Operandos o, Contexto c) {
            return tipoI(i, ZERO, o.registrador(0), o.imediato(1, 0, MAIOR_IMEDIATO_SEM_SINAL));
        }
    },
    RT_MEMORIA(2) {
        int codificar(Instrucao i, Operandos o, Contexto c) {
            Operandos.EnderecoMemoria memoria = o.memoria(1);
            return tipoI(i, memoria.base(), o.registrador(0), memoria.deslocamento());
        }
    },
    RS_RT_LABEL(3) {
        int codificar(Instrucao i, Operandos o, Contexto c) {
            int alvo = c.simbolos().endereco(o.label(2));
            int deslocamento = (alvo - (c.endereco() + 4)) / 4;
            if (deslocamento < Short.MIN_VALUE || deslocamento > Short.MAX_VALUE) {
                throw new ErroMontagem("label '" + o.label(2) + "' fora do alcance do desvio");
            }
            return tipoI(i, o.registrador(0), o.registrador(1), deslocamento);
        }
    },
    LABEL(1) {
        int codificar(Instrucao i, Operandos o, Contexto c) {
            int alvo = c.simbolos().endereco(o.label(0));
            return i.opcode() << 26 | (alvo >>> 2 & 0x3FFFFFF);
        }
    };

    private static final int MAIOR_IMEDIATO_SEM_SINAL = 0xFFFF;

    private final int quantidadeOperandos;

    Sintaxe(int quantidadeOperandos) {
        this.quantidadeOperandos = quantidadeOperandos;
    }

    int quantidadeOperandos() {
        return quantidadeOperandos;
    }

    abstract int codificar(Instrucao instrucao, Operandos operandos, Contexto contexto);

    private static int tipoR(Instrucao i, Registrador rs, Registrador rt, Registrador rd, int shamt) {
        return i.opcode() << 26 | rs.numero() << 21 | rt.numero() << 16 | rd.numero() << 11 | shamt << 6 | i.funct();
    }

    private static int tipoI(Instrucao i, Registrador rs, Registrador rt, int imediato) {
        return i.opcode() << 26 | rs.numero() << 21 | rt.numero() << 16 | imediato & 0xFFFF;
    }

    /** Tabela de símbolos e endereço da instrução sendo codificada. */
    record Contexto(TabelaSimbolos simbolos, int endereco) {
    }
}
