package darkkitchen;

import dao.AlertaStockDao;
import model.AlertaStock;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TDD (tarea tecnica dao): AlertaStockDao asigna id autoincremental al
 * guardar, buscarUltimaPorInsumo devuelve la mas reciente (o null si no hay
 * ninguna) y listarOrdenadoDesc devuelve el historial de la mas reciente a
 * la mas antigua.
 */
class AlertaStockDaoTest {

    private final AlertaStockDao dao = new AlertaStockDao();

    @Test
    void guardarAsignaIdCuandoEsCero() {
        AlertaStock alerta = new AlertaStock(0, 9001, "Insumo Prueba Alerta", 1.5, LocalDateTime.now());

        AlertaStock guardada = dao.guardar(alerta);

        assertTrue(guardada.getId() > 0);
    }

    @Test
    void buscarUltimaPorInsumoDevuelveNullSiNuncaSeRegistro() {
        assertNull(dao.buscarUltimaPorInsumo(-777));
    }

    @Test
    void buscarUltimaPorInsumoDevuelveLaMasReciente() {
        int insumoId = 9002;
        dao.guardar(new AlertaStock(0, insumoId, "Insumo Antiguo", 5.0,
                LocalDateTime.now().minusDays(2)));
        AlertaStock masReciente = dao.guardar(new AlertaStock(0, insumoId, "Insumo Reciente", 2.0,
                LocalDateTime.now()));

        AlertaStock ultima = dao.buscarUltimaPorInsumo(insumoId);

        assertEquals(masReciente.getId(), ultima.getId());
        assertEquals(2.0, ultima.getStockAlMomento(), 0.0001);
    }

    @Test
    void listarOrdenadoDescIncluyeLaAlertaRegistradaYQuedaOrdenado() {
        AlertaStock nueva = dao.guardar(new AlertaStock(0, 9003, "Insumo Orden Desc", 3.0,
                LocalDateTime.now()));

        List<AlertaStock> historial = dao.listarOrdenadoDesc();

        assertTrue(historial.stream().anyMatch(a -> a.getId() == nueva.getId()));
        for (int i = 0; i < historial.size() - 1; i++) {
            assertTrue(!historial.get(i).getTimestamp().isBefore(historial.get(i + 1).getTimestamp()));
        }
    }
}
