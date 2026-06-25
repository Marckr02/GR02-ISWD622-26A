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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * TDD (HU7): verificarDisponibilidadPlato() devuelve DISPONIBLE cuando todos
 * los ingredientes del plato tienen stock mayor a cero.
 */
@ExtendWith(MockitoExtension.class)
class DisponibilidadPlatoDisponibleTest {

    @Mock
    private PlatoDao platoDao;

    @Mock
    private InsumoDao insumoDao;

    @InjectMocks
    private MenuService menuService;

    @Test
    void devuelveDisponibleCuandoTodosLosIngredientesTienenStock() {
        Plato plato = new Plato(1, "Pizza Margarita", Arrays.asList(1, 2));
        when(insumoDao.buscarPorId(1)).thenReturn(new Insumo(1, "Harina de trigo", "kg", 12.0, 0.85));
        when(insumoDao.buscarPorId(2)).thenReturn(new Insumo(2, "Queso mozzarella", "kg", 5.0, 6.20));

        DisponibilidadPlato resultado = menuService.verificarDisponibilidadPlato(plato);

        assertEquals(EstadoPlato.DISPONIBLE, resultado.getEstado());
    }
}
