# Exemplo
main:
    addi $t0, $zero, 10    # Tipo I: Inicializa contador
    addi $t1, $zero, 0     # Tipo I: Inicializa soma
LOOP:
    add $t1, $t1, $t0      # Tipo R: soma = soma + contador
    addi $t0, $t0, -1      # Tipo I: decrementa contador
    bne $t0, $zero, LOOP   # Tipo I: salto condicional (se suportado)
    sw $t1, 0($sp)         # Tipo I: salva na memória
    j FIM                  # Tipo J: salto absoluto
FIM:
    jr $ra                 # Tipo R especial: retorno