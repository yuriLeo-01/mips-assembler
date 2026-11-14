# Referência de codificação MIPS32

Formatos (bits do mais significativo para o menos):

```
R: opcode(6) | rs(5) | rt(5) | rd(5) | shamt(5) | funct(6)
I: opcode(6) | rs(5) | rt(5) | imediato(16)
J: opcode(6) | endereço(26)
```

## Instruções suportadas

A coluna **Syntax** é a constante de `src/mips/Syntax.java`. Opcode e funct estão em hexadecimal.

| Instrução | Uso | Syntax | opcode | funct |
|---|---|---|---|---|
| add / addu | `add rd, rs, rt` | `RD_RS_RT` | 00 | 20 / 21 |
| sub / subu | `sub rd, rs, rt` | `RD_RS_RT` | 00 | 22 / 23 |
| and / or / xor / nor | `and rd, rs, rt` | `RD_RS_RT` | 00 | 24 / 25 / 26 / 27 |
| slt / sltu | `slt rd, rs, rt` | `RD_RS_RT` | 00 | 2A / 2B |
| mul | `mul rd, rs, rt` | `RD_RS_RT` | 1C (SPECIAL2) | 02 |
| sll / srl / sra | `sll rd, rt, shamt` (0–31) | `RD_RT_SHAMT` | 00 | 00 / 02 / 03 |
| mult / multu | `mult rs, rt` | `RS_RT` | 00 | 18 / 19 |
| div / divu | `div rs, rt` | `RS_RT` | 00 | 1A / 1B |
| mfhi / mflo | `mfhi rd` | `RD` | 00 | 10 / 12 |
| jr | `jr rs` | `RS` | 00 | 08 |
| nop / syscall | `nop` | `NONE` | 00 | 00 / 0C |
| addi / addiu | `addi rt, rs, imm` (−32768..32767) | `RT_RS_IMM` | 08 / 09 | — |
| slti / sltiu | `slti rt, rs, imm` (−32768..32767) | `RT_RS_IMM` | 0A / 0B | — |
| andi / ori / xori | `andi rt, rs, imm` (0..65535) | `RT_RS_IMM_UNSIGNED` | 0C / 0D / 0E | — |
| lui | `lui rt, imm` (0..65535) | `RT_IMM` | 0F | — |
| lw / sw | `lw rt, desl(rs)` | `RT_MEM` | 23 / 2B | — |
| beq / bne | `beq rs, rt, label` | `RS_RT_LABEL` | 04 / 05 | — |
| j / jal | `j label` | `LABEL` | 02 / 03 | — |

- Imediatos podem ser decimais ou hexadecimais (`0x1F`), com sinal opcional.
- Branch: `deslocamento = (alvo − (PC + 4)) / 4`, com 16 bits de sinal.
- Jump: `alvo >>> 2`, nos 26 bits menos significativos.

## Registradores

| Nº | Nome | Nº | Nome |
|---|---|---|---|
| 0 | `$zero` | 16–23 | `$s0`–`$s7` |
| 1 | `$at` | 24–25 | `$t8`–`$t9` |
| 2–3 | `$v0`–`$v1` | 26–27 | `$k0`–`$k1` |
| 4–7 | `$a0`–`$a3` | 28 | `$gp` |
| 8–15 | `$t0`–`$t7` | 29 | `$sp` |
| | | 30 | `$fp` |
| | | 31 | `$ra` |

Também aceita a forma numérica `$0`–`$31`.

## Exemplos conferidos

| Instrução | Hex |
|---|---|
| `add $1, $2, $3` | `00430820` |
| `mul $1, $2, $3` | `70430802` |
| `multu $2, $3` | `00430019` |
| `sll $1, $2, 10` | `00020a80` |
| `addi $sp, $sp, -4` | `23bdfffc` |
| `lw $1, 100 ($2)` | `8c410064` |
| `sw $ra, -8($sp)` | `afbffff8` |
| `jr $ra` | `03e00008` |
| `beq $1, $2, L1` (índice 21, L1 = índice 0) | `1022ffea` |
| `jal L3` (L3 = 0x00400054) | `0c100015` |
