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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD (HU11): registrarAlerta() persiste una alerta con el insumo, el stock
 * al momento y la marca de tiempo.
 */
@ExtendWith(MockitoExtension.class)
class RegistrarAlertaTest {

    @Mock
    private AlertaStockDao alertaStockDao;

    @InjectMocks
    private AlertaStockService alertaStockService;

    @Test
    void persisteAlertaConInsumoStockYTimestamp() {
        Insumo insumo = new Insumo(7, "Albahaca fresca", "kg", 0.0, 3.0, 2.0);
        when(alertaStockDao.buscarUltimaPorInsumo(7)).thenReturn(null);
        when(alertaStockDao.guardar(any(AlertaStock.class))).thenAnswer(inv -> inv.getArgument(0));

        AlertaStock alerta = alertaStockService.registrarAlerta(insumo);

        assertNotNull(alerta.getTimestamp());
        assertEquals(7, alerta.getInsumoId());
        assertEquals("Albahaca fresca", alerta.getInsumoNombre());
        assertEquals(0.0, alerta.getStockAlMomento(), 0.0001);
        verify(alertaStockDao).guardar(any(AlertaStock.class));
    }
}
