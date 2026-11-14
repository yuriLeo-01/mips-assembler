package mips;

/** Erro em uma linha do programa fonte. A linha é 0 enquanto ainda não for conhecida. */
public final class ErroMontagem extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final int linha;

    public ErroMontagem(String mensagem) {
        this(0, mensagem);
    }

    public ErroMontagem(int linha, String mensagem) {
        super(mensagem);
        this.linha = linha;
    }

    public int linha() {
        return linha;
    }

    /** Devolve este erro associado à linha informada, sem sobrescrever uma linha já definida. */
    public ErroMontagem naLinha(int numeroLinha) {
        return linha > 0 ? this : new ErroMontagem(numeroLinha, getMessage());
    }

    @Override
    public String toString() {
        return linha > 0 ? "linha " + linha + ": " + getMessage() : getMessage();
    }
}
