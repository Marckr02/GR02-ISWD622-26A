package darkkitchen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import service.ConversionUnidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * TDD (tarea tecnica F6.2): convertir() es insensible a mayusculas y a
 * espacios en las unidades, devuelve el mismo valor cuando origen y
 * destino son iguales (incluso con distinta grafia), y lanza excepcion si
 * alguna de las unidades es null.
 */
class ConversionUnidadesCasosAdicionalesTest {

    private final ConversionUnidades conversion = new ConversionUnidades();

    @ParameterizedTest
    @CsvSource({
            "1000.0, G, KG, 1.0",
            "1.0, ' Kg ', ' g ', 1000.0",
            "1000.0, ML, L, 1.0",
            "1.0, ' L ', ' ml ', 1000.0"
    })
    void convertirEsInsensibleAMayusculasYEspacios(double valor, String origen, String destino, double esperado) {
        assertEquals(esperado, conversion.convertir(valor, origen, destino), 0.0001);
    }

    @Test
    void devuelveElMismoValorCuandoUnidadesSonIgualesConDistintaGrafia() {
        assertEquals(42.0, conversion.convertir(42.0, "KG", "kg"), 0.0001);
    }

    @Test
    void lanzaExcepcionSiUnidadOrigenEsNull() {
        assertThrows(IllegalArgumentException.class, () -> conversion.convertir(1.0, null, "kg"));
    }

    @Test
    void lanzaExcepcionSiUnidadDestinoEsNull() {
        assertThrows(IllegalArgumentException.class, () -> conversion.convertir(1.0, "kg", null));
    }
}
