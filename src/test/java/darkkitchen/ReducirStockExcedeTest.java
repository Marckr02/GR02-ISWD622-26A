package darkkitchen;

import dao.InsumoDao;
import model.Insumo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.InsumoService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD: reducirStock() debe lanzar excepcion cuando la cantidad solicitada
 * excede el stock actual, sin alterar la persistencia.
 */
@ExtendWith(MockitoExtension.class)
class ReducirStockExcedeTest {

    @Mock
    private InsumoDao insumoDao;

    @InjectMocks
    private InsumoService insumoService;

    @Test
    void lanzaExcepcionCuandoCantidadExcedeStock() {
        Insumo insumo = new Insumo(2, "Pechuga de pollo", "kg", 5.0, 4.50);
        when(insumoDao.buscarPorId(2)).thenReturn(insumo);

        assertThrows(IllegalArgumentException.class,
                () -> insumoService.reducirStock(2, 8.0));

        verify(insumoDao, never()).actualizar(insumo);
    }
}
