# MIPS Assembler 💻

Montador para a arquitetura **MIPS32** escrito em Java puro, sem dependências. Traduz programas assembly para código de máquina em binário ou hexadecimal (pronto para carregar na memória do **Logisim**) e gera um relatório de desempenho com o **CPI médio**.

> Desenvolvido na disciplina de **Arquitetura e Organização de Computadores** e depois refatorado para orientação a objetos, com testes automatizados e mensagens de erro precisas.

## Funcionalidades

- 37 instruções dos tipos R, I e J, incluindo multiplicação/divisão, shifts, desvios e saltos.
- Labels, inclusive na mesma linha da instrução ou vários seguidos (`a: b: add ...`).
- Todos os 32 registradores, por número (`$8`) ou nome (`$t0`, `$sp`, `$ra`).
- Imediatos em decimal ou hexadecimal (`-4`, `0xFFFF`), com verificação de faixa.
- Endereçamento `deslocamento(base)`, tolerante a espaços: `100 ($2)`.
- Erros com número da linha. Todos são listados de uma vez, e nenhum arquivo é gerado se houver erro.
- Relatório com a quantidade de cada instrução e o CPI médio, a partir de `ciclos.csv`.

## Como usar

Requisito: JDK 17 ou superior.

```sh
javac -d out $(find src -name '*.java')
java -cp out mips.Main teste.asm -h    # gera teste.hex (Logisim)
java -cp out mips.Main teste.asm -b    # gera teste.bin
```

### Exemplo

```asm
L1: add $1, $2, $3
    mul $1, $2, $3
    lw  $1, 100 ($2)
    beq $1, $2, L1     # volta para L1
```

```
$ java -cp out mips.Main exemplo.asm -h
Sucesso! Arquivo gerado: exemplo.hex

Quantidades por tipo de instrução:
add: 1
beq: 1
lw: 1
mul: 1
CPI médio: 8,8
```

`exemplo.hex`:

```
v2.0 raw
00430820
70430802
8c410064
1022fffc
```

Com erros, nada é gerado:

```
$ java -cp out mips.Main ruim.asm -h
ruim.asm:1: instrução desconhecida 'foo'
ruim.asm:2: registrador inválido '$s9'
ruim.asm:3: label 'nada' não definido
3 erro(s) de montagem; nenhum arquivo foi gerado.
```

### Tabela de ciclos

`ciclos.csv` define os ciclos de cada instrução. As instruções ausentes valem 1 ciclo. O arquivo é procurado no diretório atual e depois na pasta do `.asm`.

```csv
Instrucao,Ciclos
add,1
sub,1
mul,32
```

## Instruções suportadas

| Tipo | Instruções |
|---|---|
| R | `add` `addu` `sub` `subu` `and` `or` `xor` `nor` `slt` `sltu` `mul` `sll` `srl` `sra` `mult` `multu` `div` `divu` `mfhi` `mflo` `jr` `nop` `syscall` |
| I | `addi` `addiu` `slti` `sltiu` `andi` `ori` `xori` `lui` `lw` `sw` `beq` `bne` |
| J | `j` `jal` |

Opcodes e formatos estão em [`docs/referencia-mips.md`](docs/referencia-mips.md), e as limitações em [`docs/problemas-conhecidos.md`](docs/problemas-conhecidos.md).

## Arquitetura

```
src/mips/
├── Main.java                 CLI e localização do ciclos.csv
├── Assembler.java            duas passadas: labels → codificação
├── Parser.java               texto → SourceLine (labels, mnemônico, operandos)
├── Instruction.java          enum: mnemônico → sintaxe + opcode + funct
├── Syntax.java               enum: ordem dos operandos e montagem dos bits R/I/J
├── Operands.java             leitura validada de registradores, imediatos, memória e labels
├── Register.java             enum dos 32 registradores
├── SymbolTable.java          label → endereço
├── CycleTable.java           leitura do ciclos.csv
├── Report.java               contagem e CPI médio
├── OutputFormat.java         escrita .bin / .hex
└── AssemblyError.java, AssemblyException.java, SourceLine.java, AssembledProgram.java, AssembledInstruction.java
```

Adicionar uma instrução que usa uma sintaxe existente exige **uma linha** no enum `Instruction`.

## Testes

```sh
./testar.sh
```

O script compila com `-Xlint:all -Werror`, executa os testes unitários (`test/mips/AssemblerTests.java`) e compara a saída de `teste.asm` com o gabarito `testes/teste.esperado.hex`.
