#!/usr/bin/env sh
# Compila, roda os testes unitários e compara teste.asm com o gabarito.
set -eu
cd "$(dirname "$0")"

rm -rf out
javac -Xlint:all -Werror -d out $(find src test -name '*.java')
java -cp out mips.AssemblerTests

cp teste.asm out/teste.asm
saida=$(java -cp out mips.Main out/teste.asm -h)
diff out/teste.hex testes/teste.esperado.hex
echo "$saida" | grep -q "CPI médio: 2,1"
echo "gabarito: OK (teste.asm -> testes/teste.esperado.hex, CPI 2,1)"
