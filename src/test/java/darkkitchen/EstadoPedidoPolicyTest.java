package darkkitchen;

import model.EstadoPedido;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import service.EstadoPedidoPolicy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TDD (HU20): EstadoPedidoPolicy centraliza el avance/retroceso del tablero
 * Kanban. Cubre el orden de transicion, el estado terminal ENTREGADO (sin
 * avance ni retroceso), los estados que si admiten retroceso, las etiquetas
 * de los botones y el manejo de estado nulo.
 */
class EstadoPedidoPolicyTest {

    private final EstadoPedidoPolicy policy = new EstadoPedidoPolicy();

    @ParameterizedTest
    @CsvSource({
            "RECIBIDO, EN_PREPARACION",
            "EN_PREPARACION, LISTO",
            "LISTO, ENTREGADO"
    })
    void obtenerSiguienteEstadoAvanzaSegunElOrdenDelNegocio(EstadoPedido actual, EstadoPedido siguiente) {
        assertEquals(siguiente, policy.obtenerSiguienteEstado(actual));
    }

    @Test
    void obtenerSiguienteEstadoLanzaExcepcionSiEsTerminal() {
        assertThrows(IllegalStateException.class, () -> policy.obtenerSiguienteEstado(EstadoPedido.ENTREGADO));
    }

    @Test
    void obtenerSiguienteEstadoLanzaExcepcionSiEsNulo() {
        assertThrows(IllegalArgumentException.class, () -> policy.obtenerSiguienteEstado(null));
    }

    @ParameterizedTest
    @EnumSource(value = EstadoPedido.class, names = "ENTREGADO", mode = EnumSource.Mode.EXCLUDE)
    void puedeAvanzarEsVerdaderoParaTodosMenosElTerminal(EstadoPedido actual) {
        assertTrue(policy.puedeAvanzar(actual));
    }

    @Test
    void puedeAvanzarEsFalsoParaEntregadoYParaNulo() {
        assertFalse(policy.puedeAvanzar(EstadoPedido.ENTREGADO));
        assertFalse(policy.puedeAvanzar(null));
    }

    @ParameterizedTest
    @EnumSource(value = EstadoPedido.class, names = {"EN_PREPARACION", "LISTO"})
    void puedeRetrocederEsVerdaderoSoloEnPreparacionOListo(EstadoPedido actual) {
        assertTrue(policy.puedeRetroceder(actual));
    }

    @ParameterizedTest
    @EnumSource(value = EstadoPedido.class, names = {"RECIBIDO", "ENTREGADO"})
    void puedeRetrocederEsFalsoEnRecibidoOEntregado(EstadoPedido actual) {
        assertFalse(policy.puedeRetroceder(actual));
    }

    @Test
    void puedeRetrocederEsFalsoParaNulo() {
        assertFalse(policy.puedeRetroceder(null));
    }

    @ParameterizedTest
    @CsvSource({
            "RECIBIDO, 'Pasar a preparacion'",
            "EN_PREPARACION, 'Marcar listo'",
            "LISTO, 'Marcar entregado'",
            "ENTREGADO, Completado"
    })
    void etiquetaSiguienteAccionDevuelveElTextoDelBotonSegunElEstado(EstadoPedido actual, String etiqueta) {
        assertEquals(etiqueta, policy.etiquetaSiguienteAccion(actual));
    }

    @Test
    void etiquetaSiguienteAccionEsCompletadoParaNulo() {
        assertEquals("Completado", policy.etiquetaSiguienteAccion(null));
    }

    @ParameterizedTest
    @EnumSource(EstadoPedido.class)
    void etiquetaRetrocesoEsSiempreElMismoTexto(EstadoPedido actual) {
        assertEquals("Retroceder", policy.etiquetaRetroceso(actual));
    }

    @Test
    void etiquetaRetrocesoFuncionaTambienConNulo() {
        assertEquals("Retroceder", policy.etiquetaRetroceso(null));
    }
}
