package service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StockInsuficienteException extends IllegalStateException {

    private final String plato;
    private final List<String> faltantes;

    public StockInsuficienteException(String plato, List<String> faltantes) {
        super("No se puede enviar a preparacion. Falta stock para " + plato);
        this.plato = plato;
        this.faltantes = Collections.unmodifiableList(new ArrayList<>(faltantes));
    }

    public String getPlato() {
        return plato;
    }

    public List<String> getFaltantes() {
        return faltantes;
    }
}
