package mips;

import java.util.List;

/** Reúne todos os erros encontrados ao montar um programa. */
public final class FalhaMontagem extends Exception {
    private static final long serialVersionUID = 1L;

    private final List<ErroMontagem> erros;

    public FalhaMontagem(List<ErroMontagem> erros) {
        super(erros.size() + " erro(s) de montagem");
        this.erros = List.copyOf(erros);
    }

    public List<ErroMontagem> erros() {
        return erros;
    }
}
