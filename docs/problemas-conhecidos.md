# Limitações conhecidas

Não são bugs, e sim recursos fora do escopo atual. Ao implementar um deles, remova o item daqui.

1. **Sem diretivas**: `.data`, `.text`, `.word`, `.asciiz` etc. não são reconhecidas. Todo o arquivo é tratado como segmento de texto, começando em `0x00400000`.
2. **Sem pseudo-instruções**: `li`, `la`, `move`, `blt`, `bgt` etc. (exceto `nop`).
3. **Instruções de memória**: só `lw`/`sw`; faltam `lb`, `lbu`, `lh`, `lhu`, `sb`, `sh`.
4. **Desvios**: só `beq`/`bne`; faltam `bgez`, `bltz`, `blez`, `bgtz` e `jalr`.
5. **Labels como imediato**: `addi $1, $0, label` não é aceito; imediatos são só números.
6. **`ciclos.csv`**: instruções ausentes valem 1 ciclo. Linha mal formatada ou mnemônico desconhecido interrompe a execução com erro.

## Bugs corrigidos na refatoração (histórico)

| Bug da versão original | Correção |
|---|---|
| CPI sempre 1.0 (cabeçalho do CSV abortava a leitura) | `TabelaCiclos` pula o cabeçalho e valida cada linha |
| `lw $1, 100 ($2)` gerava `00000000` | `AnalisadorSintatico` normaliza o espaço antes de `(` |
| `multu` gerava `00000000`; `divu` não existia | adicionadas ao enum `Instrucao` |
| Só `$zero`, `$at`, `$v0`, `$s0–$s7` eram reconhecidos | enum `Registrador` com os 32 |
| Erros viravam `nop` em silêncio | `ErroMontagem` com número da linha; nenhum arquivo é gerado |
| Imediato hex (`0x10`) abortava com stack trace | `Operandos` aceita decimal e hexadecimal e valida a faixa |
| CPI contava instruções inválidas | só instruções montadas entram no relatório |
| `replace(".asm", "")` alterava o caminho inteiro | `FormatoSaida.destinoPara` troca só a extensão |
