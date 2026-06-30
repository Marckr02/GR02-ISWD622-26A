package service;

import model.EstadoPedido;

/**
 * Reglas de transicion de estados expuestas a la capa de presentacion.
 * Centraliza el orden del flujo y las etiquetas de las acciones para que el
 * servlet y el tablero solo avancen o retrocedan pedidos de forma valida.
 */
public class EstadoPedidoPolicy {

    /**
     * Siguiente estado segun el orden del negocio.
     * @throws IllegalArgumentException si el estado actual es nulo.
     * @throws IllegalStateException    si el estado actual es terminal.
     */
    public EstadoPedido obtenerSiguienteEstado(EstadoPedido actual) {
        if (actual == null) {
            throw new IllegalArgumentException("El estado actual es obligatorio");
        }
        return actual.siguiente();
    }

    /** True si el pedido todavia puede avanzar (no esta entregado). */
    public boolean puedeAvanzar(EstadoPedido actual) {
        return actual != null && !actual.esFinal();
    }

    /**
     * True si el pedido puede retroceder: solo desde EN_PREPARACION o LISTO.
     * RECIBIDO (inicial) y ENTREGADO (terminal) no admiten retroceso (HU20).
     */
    public boolean puedeRetroceder(EstadoPedido actual) {
        return actual == EstadoPedido.EN_PREPARACION || actual == EstadoPedido.LISTO;
    }

    /** Texto del boton para avanzar desde el estado indicado. */
    public String etiquetaSiguienteAccion(EstadoPedido actual) {
        if (!puedeAvanzar(actual)) {
            return "Completado";
        }
        switch (actual) {
            case RECIBIDO:
                return "Pasar a preparacion";
            case EN_PREPARACION:
                return "Marcar listo";
            case LISTO:
                return "Marcar entregado";
            default:
                return "Completado";
        }
    }

    /** Texto del boton para retroceder un pedido. */
    public String etiquetaRetroceso(EstadoPedido actual) {
        return "Retroceder";
    }
}
