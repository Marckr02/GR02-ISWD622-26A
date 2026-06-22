package model;

/**
 * Estados validos de un pedido en la Dark Kitchen y su orden de avance:
 * RECIBIDO -> EN_PREPARACION -> LISTO -> ENTREGADO.
 */
public enum EstadoPedido {

    RECIBIDO("Recibido"),
    EN_PREPARACION("En preparacion"),
    LISTO("Listo"),
    ENTREGADO("Entregado");

    private final String etiqueta;

    EstadoPedido(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** Indica si el estado es terminal (no admite mas avances). */
    public boolean esFinal() {
        return this == ENTREGADO;
    }

    /**
     * Devuelve el siguiente estado segun el flujo del negocio.
     * @throws IllegalStateException si el pedido ya esta ENTREGADO.
     */
    public EstadoPedido siguiente() {
        switch (this) {
            case RECIBIDO:
                return EN_PREPARACION;
            case EN_PREPARACION:
                return LISTO;
            case LISTO:
                return ENTREGADO;
            default:
                throw new IllegalStateException(
                        "El pedido ya fue entregado y no admite mas avances");
        }
    }
}
