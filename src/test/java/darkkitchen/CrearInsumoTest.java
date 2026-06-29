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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD (HU23): crearInsumo() persiste el insumo con stock inicial en cero y
 * lanza excepcion si el nombre esta duplicado, vacio o con caracteres
 * especiales.
 */
@ExtendWith(MockitoExtension.class)
class CrearInsumoTest {

    @Mock
    private InsumoDao insumoDao;

    @InjectMocks
    private InsumoService insumoService;

    @Test
    void persisteNuevoInsumoConStockCero() {
        when(insumoDao.buscarPorNombre("Cilantro fresco")).thenReturn(null);
        when(insumoDao.guardar(any(Insumo.class))).thenAnswer(inv -> inv.getArgument(0));

        Insumo creado = insumoService.crearInsumo("Cilantro fresco");

        assertEquals(0.0, creado.getStock(), 0.0001);
        verify(insumoDao).guardar(any(Insumo.class));
    }

    @Test
    void lanzaExcepcionSiNombreDuplicado() {
        when(insumoDao.buscarPorNombre("Tomate")).thenReturn(new Insumo(5, "Tomate", "kg", 3.0, 1.30));

        assertThrows(IllegalArgumentException.class, () -> insumoService.crearInsumo("Tomate"));
    }

    @Test
    void lanzaExcepcionSiNombreVacio() {
        assertThrows(IllegalArgumentException.class, () -> insumoService.crearInsumo("   "));
    }

    @Test
    void lanzaExcepcionSiNombreTieneCaracteresEspeciales() {
        assertThrows(IllegalArgumentException.class, () -> insumoService.crearInsumo("Sal <>{}"));
    }
}
