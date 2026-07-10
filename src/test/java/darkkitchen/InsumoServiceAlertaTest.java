package darkkitchen;

import dao.InsumoDao;
import model.Insumo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.AlertaStockService;
import service.InsumoService;
import service.ValidadorEntradaInsumo;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD (fix F3.3/HU11): InsumoService debe evaluar y registrar la alerta de
 * stock critico en tiempo real, en el mismo momento en que cambia el stock
 * o el nivel minimo de un insumo, en vez de depender de que alguien visite
 * el panel de monitoreo.
 */
@ExtendWith(MockitoExtension.class)
class InsumoServiceAlertaTest {

    @Mock
    private InsumoDao insumoDao;
    @Mock
    private ValidadorEntradaInsumo validador;
    @Mock
    private AlertaStockService alertaStockService;
    @InjectMocks
    private InsumoService insumoService;

    @Test
    void reducirStockEvaluaLaAlertaConElInsumoActualizado() {
        Insumo insumo = new Insumo(1, "Albahaca fresca", "kg", 2.0, 3.0, 2.0);
        when(insumoDao.buscarPorId(1)).thenReturn(insumo);

        insumoService.reducirStock(1, 2.0);

        verify(alertaStockService).evaluarYRegistrar(insumo);
    }

    @Test
    void registrarEntradaSimplificadaEvaluaLaAlertaConElInsumoActualizado() {
        Insumo insumo = new Insumo(2, "Salsa de tomate", "l", 3.0, 1.8, 5.0);
        when(insumoDao.buscarPorId(2)).thenReturn(insumo);

        insumoService.registrarEntradaSimplificada(2, "1");

        verify(alertaStockService).evaluarYRegistrar(insumo);
    }

    @Test
    void actualizarStockMinimoEvaluaLaAlertaConElInsumoActualizado() {
        Insumo insumo = new Insumo(3, "Queso mozzarella", "kg", 10.0, 6.2, 5.0);
        when(insumoDao.buscarPorId(3)).thenReturn(insumo);

        insumoService.actualizarStockMinimo(3, "12");

        verify(alertaStockService).evaluarYRegistrar(insumo);
    }
}
