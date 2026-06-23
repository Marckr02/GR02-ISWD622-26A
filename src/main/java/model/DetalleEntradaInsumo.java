package model;

import java.time.LocalDate;

/**
 * Detalle de un lote de entrada de insumos al inventario. Conserva la
 * trazabilidad de la compra (orden de compra y factura) y la fecha de ingreso.
 */
public class DetalleEntradaInsumo {

    private int insumoId;
    private double cantidad;
    private double costo;
    private String ordenCompra;
    private String factura;
    private LocalDate fecha;

    public DetalleEntradaInsumo() {
    }

    public DetalleEntradaInsumo(int insumoId, double cantidad, double costo,
                                String ordenCompra, String factura) {
        this.insumoId = insumoId;
        this.cantidad = cantidad;
        this.costo = costo;
        this.ordenCompra = ordenCompra;
        this.factura = factura;
        this.fecha = LocalDate.now();
    }

    public int getInsumoId() {
        return insumoId;
    }

    public void setInsumoId(int insumoId) {
        this.insumoId = insumoId;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public double getCosto() {
        return costo;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }

    public String getOrdenCompra() {
        return ordenCompra;
    }

    public void setOrdenCompra(String ordenCompra) {
        this.ordenCompra = ordenCompra;
    }

    public String getFactura() {
        return factura;
    }

    public void setFactura(String factura) {
        this.factura = factura;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
}
