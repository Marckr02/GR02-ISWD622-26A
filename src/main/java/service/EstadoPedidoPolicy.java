package service;

import model.EstadoPedido;

/**
 * Reglas de transicion de estados expuestas a la capa de presentacion.
 * Centraliza el orden correcto del flujo y las etiquetas de las acciones
 * para que el servlet y el tablero solo avancen pedidos de forma valida.
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
}
