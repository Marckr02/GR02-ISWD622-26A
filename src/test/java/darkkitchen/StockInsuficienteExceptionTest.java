package darkkitchen;

import org.junit.jupiter.api.Test;
import service.StockInsuficienteException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TDD (HU30): StockInsuficienteException conserva el nombre del plato y la
 * lista de insumos faltantes, exponiendola como inmutable (copia defensiva)
 * y con un mensaje descriptivo.
 */
class StockInsuficienteExceptionTest {

    @Test
    void exponeElPlatoYLosFaltantesRecibidos() {
        List<String> faltantes = new ArrayList<>(List.of("Harina", "Queso"));

        StockInsuficienteException excepcion = new StockInsuficienteException("Pizza Margarita", faltantes);

        assertEquals("Pizza Margarita", excepcion.getPlato());
        assertEquals(List.of("Harina", "Queso"), excepcion.getFaltantes());
        assertTrue(excepcion.getMessage().contains("Pizza Margarita"));
    }

    @Test
    void laListaDeFaltantesEsInmutable() {
        StockInsuficienteException excepcion = new StockInsuficienteException("Plato X", List.of("Tomate"));

        assertThrows(UnsupportedOperationException.class,
                () -> excepcion.getFaltantes().add("Otro"));
    }

    @Test
    void modificarLaListaOriginalNoAfectaALaExcepcion() {
        List<String> faltantes = new ArrayList<>(List.of("Aceite"));
        StockInsuficienteException excepcion = new StockInsuficienteException("Plato Y", faltantes);

        faltantes.add("Sal");

        assertEquals(1, excepcion.getFaltantes().size());
    }
}
