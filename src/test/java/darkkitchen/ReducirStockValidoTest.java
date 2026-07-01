package darkkitchen;

import dao.InsumoDao;
import model.Insumo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.InsumoService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD: reducirStock() con una cantidad valida dentro del stock disponible
 * debe descontar la cantidad y persistir el insumo actualizado.
 */
@ExtendWith(MockitoExtension.class)
class ReducirStockValidoTest {

    @Mock
    private InsumoDao insumoDao;

    @InjectMocks
    private InsumoService insumoService;

    @Test
    void reduceStockDentroDelDisponible() {
        Insumo insumo = new Insumo(1, "Queso mozzarella", "kg", 10.0, 6.20);
        when(insumoDao.buscarPorId(1)).thenReturn(insumo);

        Insumo resultado = insumoService.reducirStock(1, 4.0);

        assertEquals(6.0, resultado.getStock(), 0.0001);
        verify(insumoDao, times(1)).actualizar(insumo);
    }
}
