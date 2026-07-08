package darkkitchen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import service.ValidadorEntradaInsumo;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * TDD (F.entrada-insumo): ValidadorEntradaInsumo exige orden de compra y
 * factura no vacias, cantidad positiva y costo no negativo; con todos los
 * datos validos no lanza ninguna excepcion.
 */
class ValidadorEntradaInsumoTest {

    private final ValidadorEntradaInsumo validador = new ValidadorEntradaInsumo();

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void lanzaExcepcionSiOrdenCompraEsVaciaOBlanco(String ordenCompra) {
        assertThrows(IllegalArgumentException.class,
                () -> validador.validar(ordenCompra, "F-001", 10.0, 5.0));
    }

    @Test
    void lanzaExcepcionSiOrdenCompraEsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> validador.validar(null, "F-001", 10.0, 5.0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void lanzaExcepcionSiFacturaEsVaciaOBlanco(String factura) {
        assertThrows(IllegalArgumentException.class,
                () -> validador.validar("OC-001", factura, 10.0, 5.0));
    }

    @Test
    void lanzaExcepcionSiFacturaEsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> validador.validar("OC-001", null, 10.0, 5.0));
    }

    @ParameterizedTest
    @CsvSource({"0.0", "-5.0"})
    void lanzaExcepcionSiCantidadNoEsPositiva(double cantidad) {
        assertThrows(IllegalArgumentException.class,
                () -> validador.validar("OC-001", "F-001", cantidad, 5.0));
    }

    @Test
    void lanzaExcepcionSiCostoEsNegativo() {
        assertThrows(IllegalArgumentException.class,
                () -> validador.validar("OC-001", "F-001", 10.0, -1.0));
    }

    @Test
    void noLanzaExcepcionConDatosCompletamenteValidos() {
        assertDoesNotThrow(() -> validador.validar("OC-001", "F-001", 10.0, 0.0));
    }
}
