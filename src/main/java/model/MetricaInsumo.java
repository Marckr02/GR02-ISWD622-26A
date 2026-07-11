package model;

/**
 * Fila de metrica de un insumo: cantidad total consumida (ya normalizada a
 * la unidad base del insumo) en pedidos ENTREGADO (F5.1, HU36/HU38).
 */
public class MetricaInsumo {

    private final int insumoId;
    private final String nombre;
    private final double cantidadTotal;
    private final String unidad;

    public MetricaInsumo(int insumoId, String nombre, double cantidadTotal, String unidad) {
        this.insumoId = insumoId;
        this.nombre = nombre;
        this.cantidadTotal = cantidadTotal;
        this.unidad = unidad;
    }

    public int getInsumoId() {
        return insumoId;
    }

    public String getNombre() {
        return nombre;
    }

    public double getCantidadTotal() {
        return cantidadTotal;
    }

    public String getUnidad() {
        return unidad;
    }
}
