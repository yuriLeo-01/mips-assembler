package mips;

import java.util.List;

/** Reúne todos os erros encontrados ao montar um programa. */
public final class AssemblyException extends Exception {
    private static final long serialVersionUID = 1L;

    private final List<AssemblyError> errors;

    public AssemblyException(List<AssemblyError> errors) {
        super(errors.size() + " erro(s) de montagem");
        this.errors = List.copyOf(errors);
    }

    public List<AssemblyError> errors() {
        return errors;
    }
}
