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

@ExtendWith(MockitoExtension.class)
class EditarInsumoTest {

    @Mock
    private InsumoDao insumoDao;

    @InjectMocks
    private InsumoService insumoService;

    @Test
    void actualizaNombreYUnidad() {
        Insumo insumo = new Insumo(1, "Arina", "unidades", 0.0, 0.0);
        when(insumoDao.buscarPorId(1)).thenReturn(insumo);
        when(insumoDao.buscarPorNombre("Harina")).thenReturn(null);

        Insumo actualizado = insumoService.editarInsumo(1, "Harina", "kg");

        assertEquals("Harina", actualizado.getNombre());
        assertEquals("kg", actualizado.getUnidad());
        verify(insumoDao).actualizar(insumo);
    }

    @Test
    void rechazaCambiarAUnidadesSiElStockTieneDecimales() {
        Insumo insumo = new Insumo(2, "Queso", "kg", 1.5, 6.20);
        when(insumoDao.buscarPorId(2)).thenReturn(insumo);
        when(insumoDao.buscarPorNombre("Queso")).thenReturn(insumo);

        assertThrows(IllegalArgumentException.class,
                () -> insumoService.editarInsumo(2, "Queso", "unidades"));
    }
}
