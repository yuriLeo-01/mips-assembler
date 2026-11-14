package mips;

import java.util.HashMap;
import java.util.Map;

/** Endereço de cada label definido no programa. */
public final class TabelaSimbolos {
    private final Map<String, Integer> enderecos = new HashMap<>();

    public void definir(String label, int endereco) {
        if (enderecos.putIfAbsent(label, endereco) != null) {
            throw new ErroMontagem("label '" + label + "' definido mais de uma vez");
        }
    }

    public int endereco(String label) {
        Integer endereco = enderecos.get(label);
        if (endereco == null) {
            throw new ErroMontagem("label '" + label + "' não definido");
        }
        return endereco;
    }
}
