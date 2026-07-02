package darkkitchen;

import org.junit.jupiter.api.Test;
import service.ConversionUnidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * TDD (tarea tecnica F6.2): convertir() soporta los pares g<->kg y ml<->l,
 * y lanza excepcion para pares de unidades incompatibles.
 */
class ConversionUnidadesTest {

    private final ConversionUnidades conversion = new ConversionUnidades();

    @Test
    void convierteGramosAKilogramos() {
        assertEquals(0.25, conversion.convertir(250.0, "g", "kg"), 0.0001);
    }

    @Test
    void convierteMililitrosALitros() {
        assertEquals(0.5, conversion.convertir(500.0, "ml", "l"), 0.0001);
    }

    @Test
    void convierteKilogramosAGramosYLitrosAMililitros() {
        assertEquals(1500.0, conversion.convertir(1.5, "kg", "g"), 0.0001);
        assertEquals(750.0, conversion.convertir(0.75, "l", "ml"), 0.0001);
    }

    @Test
    void lanzaExcepcionParaUnidadesIncompatibles() {
        assertThrows(IllegalArgumentException.class, () -> conversion.convertir(10.0, "g", "l"));
        assertThrows(IllegalArgumentException.class, () -> conversion.convertir(10.0, "unidades", "kg"));
    }
}
