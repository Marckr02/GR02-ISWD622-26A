package service;

/**
 * Valida los datos de una entrada de insumos antes de afectar el stock:
 * la orden de compra y la factura deben venir informadas, la cantidad debe
 * ser positiva y el costo no puede ser negativo.
 */
public class ValidadorEntradaInsumo {

    public void validar(String ordenCompra, String factura, double cantidad, double costo) {
        if (ordenCompra == null || ordenCompra.trim().isEmpty()) {
            throw new IllegalArgumentException("La orden de compra es obligatoria");
        }
        if (factura == null || factura.trim().isEmpty()) {
            throw new IllegalArgumentException("El numero de factura es obligatorio");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        if (costo < 0) {
            throw new IllegalArgumentException("El costo no puede ser negativo");
        }
    }
}
