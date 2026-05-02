# Teste do Montador MIPS
addi $s0, $zero, 10    # Carrega 10 em $s0
addi $s1, $zero, 5     # Carrega 5 em $s1
beq $s0, $s1, fim      # Se s0 == s1, pula para fim (não deve pular)
add $s2, $s0, $s1      # s2 = 10 + 5
j final                # Pulo absoluto para final
fim:
sub $s2, $s0, $s1      # s2 = 10 - 5
final:
addi $t0, $s2, 1       # t0 = s2 + 1