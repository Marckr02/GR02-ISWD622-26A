package darkkitchen;

import model.DetalleEntradaInsumo;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * TDD (tarea tecnica model): DetalleEntradaInsumo asigna la fecha actual en
 * el constructor con datos, y expone getters/setters para todos sus campos
 * (trazabilidad de orden de compra y factura).
 */
class DetalleEntradaInsumoTest {

    @Test
    void constructorConDatosAsignaFechaActual() {
        DetalleEntradaInsumo detalle = new DetalleEntradaInsumo(3, 10.0, 5.5, "OC-001", "F-001");

        assertEquals(3, detalle.getInsumoId());
        assertEquals(10.0, detalle.getCantidad(), 0.0001);
        assertEquals(5.5, detalle.getCosto(), 0.0001);
        assertEquals("OC-001", detalle.getOrdenCompra());
        assertEquals("F-001", detalle.getFactura());
        assertEquals(LocalDate.now(), detalle.getFecha());
    }

    @Test
    void constructorVacioPermiteUsarSetters() {
        DetalleEntradaInsumo detalle = new DetalleEntradaInsumo();
        LocalDate fecha = LocalDate.of(2023, 5, 20);

        detalle.setInsumoId(7);
        detalle.setCantidad(20.0);
        detalle.setCosto(2.5);
        detalle.setOrdenCompra("OC-002");
        detalle.setFactura("F-002");
        detalle.setFecha(fecha);

        assertEquals(7, detalle.getInsumoId());
        assertEquals(20.0, detalle.getCantidad(), 0.0001);
        assertEquals(2.5, detalle.getCosto(), 0.0001);
        assertEquals("OC-002", detalle.getOrdenCompra());
        assertEquals("F-002", detalle.getFactura());
        assertEquals(fecha, detalle.getFecha());
        assertNotNull(detalle.getFecha());
    }
}
