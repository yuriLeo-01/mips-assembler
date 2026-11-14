package mips;

import java.util.HashMap;
import java.util.Map;

/** Endereço de cada label definido no programa. */
public final class SymbolTable {
    private final Map<String, Integer> addresses = new HashMap<>();

    public void define(String label, int address) {
        if (addresses.putIfAbsent(label, address) != null) {
            throw new AssemblyError("label '" + label + "' definido mais de uma vez");
        }
    }

    public int addressOf(String label) {
        Integer address = addresses.get(label);
        if (address == null) {
            throw new AssemblyError("label '" + label + "' não definido");
        }
        return address;
    }
}
