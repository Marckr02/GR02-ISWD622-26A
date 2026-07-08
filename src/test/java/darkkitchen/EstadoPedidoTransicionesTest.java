package darkkitchen;

import model.EstadoPedido;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TDD (tarea tecnica model): EstadoPedido.siguiente()/anterior() recorren
 * el flujo RECIBIDO -> EN_PREPARACION -> LISTO -> ENTREGADO en ambos
 * sentidos y lanzan IllegalStateException en los extremos; esFinal() solo
 * es verdadero para ENTREGADO.
 */
class EstadoPedidoTransicionesTest {

    @ParameterizedTest
    @CsvSource({
            "RECIBIDO, EN_PREPARACION",
            "EN_PREPARACION, LISTO",
            "LISTO, ENTREGADO"
    })
    void siguienteAvanzaAlEstadoEsperado(EstadoPedido origen, EstadoPedido esperado) {
        assertEquals(esperado, origen.siguiente());
    }

    @Test
    void siguienteLanzaExcepcionSiYaEstaEntregado() {
        assertThrows(IllegalStateException.class, EstadoPedido.ENTREGADO::siguiente);
    }

    @ParameterizedTest
    @CsvSource({
            "EN_PREPARACION, RECIBIDO",
            "LISTO, EN_PREPARACION"
    })
    void anteriorRetrocedeAlEstadoEsperado(EstadoPedido origen, EstadoPedido esperado) {
        assertEquals(esperado, origen.anterior());
    }

    @Test
    void anteriorLanzaExcepcionSiEstaEnRecibido() {
        assertThrows(IllegalStateException.class, EstadoPedido.RECIBIDO::anterior);
    }

    @Test
    void anteriorLanzaExcepcionSiEstaEnEntregado() {
        assertThrows(IllegalStateException.class, EstadoPedido.ENTREGADO::anterior);
    }

    @ParameterizedTest
    @EnumSource(value = EstadoPedido.class, names = {"ENTREGADO"})
    void esFinalEsVerdaderoSoloParaEntregado(EstadoPedido estado) {
        assertTrue(estado.esFinal());
    }

    @ParameterizedTest
    @EnumSource(value = EstadoPedido.class, names = {"RECIBIDO", "EN_PREPARACION", "LISTO"})
    void esFinalEsFalsoParaLosDemasEstados(EstadoPedido estado) {
        assertFalse(estado.esFinal());
    }
}
