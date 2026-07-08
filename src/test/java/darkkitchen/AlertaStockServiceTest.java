package darkkitchen;

import dao.AlertaStockDao;
import model.AlertaStock;
import model.Insumo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.AlertaStockService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD (F3.3): registrarAlerta() no duplica una alerta cuando el stock del
 * insumo sigue igual al de la ultima registrada, lanza excepcion si el
 * insumo es null, y listarHistorial() delega en el dao.
 */
@ExtendWith(MockitoExtension.class)
class AlertaStockServiceTest {

    @Mock
    private AlertaStockDao alertaStockDao;

    @InjectMocks
    private AlertaStockService alertaStockService;

    @Test
    void noDuplicaAlertaSiElStockNoCambio() {
        Insumo insumo = new Insumo(1, "Tomate", "kg", 2.0, 1.3, 6.0);
        AlertaStock ultima = new AlertaStock(10, 1, "Tomate", 2.0, LocalDateTime.now().minusHours(1));
        when(alertaStockDao.buscarUltimaPorInsumo(1)).thenReturn(ultima);

        AlertaStock resultado = alertaStockService.registrarAlerta(insumo);

        assertSame(ultima, resultado);
        verify(alertaStockDao, never()).guardar(any(AlertaStock.class));
    }

    @Test
    void registraNuevaAlertaSiElStockCambioDesdeLaUltima() {
        Insumo insumo = new Insumo(2, "Queso", "kg", 1.0, 6.2, 5.0);
        AlertaStock ultima = new AlertaStock(11, 2, "Queso", 4.0, LocalDateTime.now().minusHours(2));
        when(alertaStockDao.buscarUltimaPorInsumo(2)).thenReturn(ultima);
        when(alertaStockDao.guardar(any(AlertaStock.class))).thenAnswer(inv -> inv.getArgument(0));

        AlertaStock resultado = alertaStockService.registrarAlerta(insumo);

        assertEquals(1.0, resultado.getStockAlMomento(), 0.0001);
        verify(alertaStockDao, times(1)).guardar(any(AlertaStock.class));
    }

    @Test
    void lanzaExcepcionSiElInsumoEsNull() {
        assertThrows(IllegalArgumentException.class, () -> alertaStockService.registrarAlerta(null));
    }

    @Test
    void listarHistorialDelegaEnElDao() {
        List<AlertaStock> historial = List.of(new AlertaStock(1, 1, "Tomate", 1.0, LocalDateTime.now()));
        when(alertaStockDao.listarOrdenadoDesc()).thenReturn(historial);

        List<AlertaStock> resultado = alertaStockService.listarHistorial();

        assertEquals(historial, resultado);
    }
}
