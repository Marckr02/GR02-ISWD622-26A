package darkkitchen;

import dao.AlertaStockDao;
import dao.InsumoDao;
import model.Insumo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.AlertaStockService;
import service.InsumoService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD (HU9 / HU11): listarInsumos()/buscar() delegan en el dao,
 * listarInsumosCriticos() filtra solo los insumos por debajo de su minimo,
 * y la sobrecarga con AlertaStockService registra una alerta por cada
 * insumo critico encontrado (y no falla si se invoca con null).
 */
@ExtendWith(MockitoExtension.class)
class InsumoServiceConsultasTest {

    @Mock
    private InsumoDao insumoDao;

    @InjectMocks
    private InsumoService insumoService;

    @Test
    void listarInsumosDelegaEnElDao() {
        List<Insumo> insumos = List.of(new Insumo(1, "Harina", "kg", 10.0, 1.0, 2.0));
        when(insumoDao.listarTodos()).thenReturn(insumos);

        assertEquals(insumos, insumoService.listarInsumos());
    }

    @Test
    void buscarDelegaEnElDao() {
        Insumo insumo = new Insumo(2, "Queso", "kg", 5.0, 1.0, 2.0);
        when(insumoDao.buscarPorId(2)).thenReturn(insumo);

        assertEquals(insumo, insumoService.buscar(2));
    }

    @Test
    void listarInsumosCriticosFiltraSoloLosPorDebajoDelMinimo() {
        Insumo critico = new Insumo(3, "Albahaca", "kg", 0.0, 3.0, 2.0);
        Insumo normal = new Insumo(4, "Pan", "unidades", 100.0, 0.35, 20.0);
        when(insumoDao.listarTodos()).thenReturn(List.of(critico, normal));

        List<Insumo> criticos = insumoService.listarInsumosCriticos();

        assertEquals(1, criticos.size());
        assertEquals(3, criticos.get(0).getId());
    }

    @Test
    void listarInsumosCriticosConAlertaServiceNullNoFalla() {
        Insumo critico = new Insumo(5, "Salsa", "l", 1.0, 1.8, 5.0);
        when(insumoDao.listarTodos()).thenReturn(List.of(critico));

        List<Insumo> criticos = insumoService.listarInsumosCriticos(null);

        assertEquals(1, criticos.size());
    }

    @Test
    void listarInsumosCriticosConAlertaServiceRegistraUnaAlertaPorCadaCritico() {
        AlertaStockDao alertaStockDao = org.mockito.Mockito.mock(AlertaStockDao.class);
        AlertaStockService alertaStockService = org.mockito.Mockito.mock(AlertaStockService.class);
        Insumo critico1 = new Insumo(6, "Albahaca", "kg", 0.0, 3.0, 2.0);
        Insumo critico2 = new Insumo(7, "Salsa", "l", 1.0, 1.8, 5.0);
        when(insumoDao.listarTodos()).thenReturn(List.of(critico1, critico2));

        List<Insumo> criticos = insumoService.listarInsumosCriticos(alertaStockService);

        assertEquals(2, criticos.size());
        verify(alertaStockService, times(1)).registrarAlerta(critico1);
        verify(alertaStockService, times(1)).registrarAlerta(critico2);
    }
}
