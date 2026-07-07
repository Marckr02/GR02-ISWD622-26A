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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD (HU34): actualizarStockMinimo() actualiza correctamente el nivel
 * minimo y lanza excepcion para valor negativo e insumo inexistente.
 */
@ExtendWith(MockitoExtension.class)
class ActualizarStockMinimoTest {

    @Mock
    private InsumoDao insumoDao;

    @InjectMocks
    private InsumoService insumoService;

    @Test
    void actualizaElNivelMinimoCorrectamente() {
        Insumo insumo = new Insumo(1, "Tomate", "kg", 20.0, 1.30, 6.0);
        when(insumoDao.buscarPorId(1)).thenReturn(insumo);

        Insumo actualizado = insumoService.actualizarStockMinimo(1, "10");

        assertEquals(10.0, actualizado.getStockMinimo(), 0.0001);
        verify(insumoDao).actualizar(insumo);
    }

    @Test
    void lanzaExcepcionConValorNegativo() {
        Insumo insumo = new Insumo(1, "Tomate", "kg", 20.0, 1.30, 6.0);
        when(insumoDao.buscarPorId(1)).thenReturn(insumo);

        assertThrows(IllegalArgumentException.class,
                () -> insumoService.actualizarStockMinimo(1, "-3"));
    }

    @Test
    void lanzaExcepcionSiElInsumoNoExiste() {
        when(insumoDao.buscarPorId(99)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> insumoService.actualizarStockMinimo(99, "5"));
    }
}
