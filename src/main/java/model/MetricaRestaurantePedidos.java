package model;

/**
 * Fila de la vista general: total de pedidos ENTREGADO de un restaurante
 * (F5.1, HU37).
 */
public class MetricaRestaurantePedidos {

    private final int restauranteId;
    private final String nombreRestaurante;
    private final long totalPedidos;

    public MetricaRestaurantePedidos(int restauranteId, String nombreRestaurante, long totalPedidos) {
        this.restauranteId = restauranteId;
        this.nombreRestaurante = nombreRestaurante;
        this.totalPedidos = totalPedidos;
    }

    public int getRestauranteId() {
        return restauranteId;
    }

    public String getNombreRestaurante() {
        return nombreRestaurante;
    }

    public long getTotalPedidos() {
        return totalPedidos;
    }
}
