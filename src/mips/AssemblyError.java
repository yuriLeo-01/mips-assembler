package mips;

/** Erro em uma linha do programa fonte. A linha é 0 enquanto ainda não for conhecida. */
public final class AssemblyError extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final int line;

    public AssemblyError(String message) {
        this(0, message);
    }

    public AssemblyError(int line, String message) {
        super(message);
        this.line = line;
    }

    public int line() {
        return line;
    }

    /** Devolve este erro associado à linha informada, sem sobrescrever uma linha já definida. */
    public AssemblyError atLine(int lineNumber) {
        return line > 0 ? this : new AssemblyError(lineNumber, getMessage());
    }

    @Override
    public String toString() {
        return line > 0 ? "linha " + line + ": " + getMessage() : getMessage();
    }
}
