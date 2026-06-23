package darkkitchen;

import dao.InsumoDao;
import model.Insumo;
import org.junit.jupiter.api.Test;
import service.InsumoService;
import service.ValidadorEntradaInsumo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Prueba de integracion del flujo de registro de entrada de insumos usando
 * DAO real en memoria: tras registrar un lote, el stock debe incrementarse
 * exactamente en la cantidad recibida.
 */
class IngresoInsumoFlujoTest {

    @Test
    void registrarEntradaIncrementaElStock() {
        InsumoDao dao = new InsumoDao();
        InsumoService service = new InsumoService(dao, new ValidadorEntradaInsumo());

        Insumo insumo = service.listarInsumos().get(0);
        double stockInicial = insumo.getStock();
        double cantidad = 15.0;

        var detalle = service.registrarEntradaInsumos(
                insumo.getId(), cantidad, 0.90, "OC-2026-001", "FAC-5567");

        assertNotNull(detalle);
        Insumo actualizado = service.buscar(insumo.getId());
        assertEquals(stockInicial + cantidad, actualizado.getStock(), 0.0001);
        assertEquals(insumo.getId(), detalle.getInsumoId());
    }
}
