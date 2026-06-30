package darkkitchen;

import dao.InsumoDao;
import dao.PlatoDao;
import model.DisponibilidadPlato;
import model.EstadoPlato;
import model.Insumo;
import model.Plato;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.MenuService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * TDD (HU7): sincronizarMenuConInventario() habilita un plato (lo deja
 * DISPONIBLE) cuando se recupera el stock de su ultimo ingrediente faltante.
 */
@ExtendWith(MockitoExtension.class)
class SincronizarMenuTest {

    @Mock
    private PlatoDao platoDao;

    @Mock
    private InsumoDao insumoDao;

    @InjectMocks
    private MenuService menuService;

    @Test
    void habilitaPlatoAlRecuperarStockDelUltimoIngredienteFaltante() {
        Plato plato = new Plato(1, "Pizza Margarita", Arrays.asList(1, 2));
        when(platoDao.listarTodos()).thenReturn(Collections.singletonList(plato));
        // El ingrediente que estaba agotado ya recupero stock.
        when(insumoDao.buscarPorId(1)).thenReturn(new Insumo(1, "Harina de trigo", "kg", 12.0, 0.85));
        when(insumoDao.buscarPorId(2)).thenReturn(new Insumo(2, "Albahaca fresca", "kg", 4.0, 3.00));

        List<DisponibilidadPlato> menu = menuService.sincronizarMenuConInventario();

        assertEquals(1, menu.size());
        assertEquals(EstadoPlato.DISPONIBLE, menu.get(0).getEstado());
    }
}
