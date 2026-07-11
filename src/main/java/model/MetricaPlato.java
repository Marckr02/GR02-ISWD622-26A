package model;

/**
 * Fila de metrica de un plato: cuantos pedidos ENTREGADO tuvo (F5.1, HU35).
 */
public class MetricaPlato {

    private final int platoId;
    private final String nombre;
    private final long pedidosEntregados;

    public MetricaPlato(int platoId, String nombre, long pedidosEntregados) {
        this.platoId = platoId;
        this.nombre = nombre;
        this.pedidosEntregados = pedidosEntregados;
    }

    public int getPlatoId() {
        return platoId;
    }

    public String getNombre() {
        return nombre;
    }

    public long getPedidosEntregados() {
        return pedidosEntregados;
    }
}
