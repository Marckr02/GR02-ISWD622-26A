package darkkitchen;

import dao.InsumoDao;
import dao.PlatoDao;
import dao.RestauranteDao;
import model.IngredientePlato;
import model.Insumo;
import model.Plato;
import model.Restaurante;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.PlatoService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD (HU30): registrarPlato() persiste el plato con receta, restaurante y
 * unidades validas, y lanza excepcion para nombre duplicado, cantidad
 * invalida y ausencia de restaurante seleccionado.
 */
@ExtendWith(MockitoExtension.class)
class PlatoRegistroTest {

    @Mock
    private PlatoDao platoDao;

    @Mock
    private RestauranteDao restauranteDao;

    @Mock
    private InsumoDao insumoDao;

    @InjectMocks
    private PlatoService platoService;

    @Test
    void persistePlatoConRecetaRestauranteYUnidadesValidas() {
        when(platoDao.buscarPorNombre("Ensalada Cesar")).thenReturn(null);
        when(restauranteDao.buscarPorId(1)).thenReturn(new Restaurante(1, "Napoli", "Pizzeria"));
        when(insumoDao.buscarPorId(5)).thenReturn(new Insumo(5, "Tomate", "kg", 20.0, 1.30));
        when(platoDao.guardar(any(Plato.class))).thenAnswer(inv -> inv.getArgument(0));

        Plato creado = platoService.registrarPlato("Ensalada Cesar", 1,
                List.of(new IngredientePlato(5, 150, "g")));

        assertEquals("Ensalada Cesar", creado.getNombre());
        assertEquals(1, creado.getRestauranteId());
        verify(platoDao).guardar(any(Plato.class));
    }

    @Test
    void lanzaExcepcionSiNombreDuplicado() {
        when(platoDao.buscarPorNombre("Pizza Margarita"))
                .thenReturn(new Plato(9, "Pizza Margarita", List.of(1)));

        assertThrows(IllegalArgumentException.class, () -> platoService.registrarPlato(
                "Pizza Margarita", 1, List.of(new IngredientePlato(1, 100, "g"))));
    }

    @Test
    void lanzaExcepcionConCantidadDeIngredienteInvalida() {
        lenient().when(platoDao.buscarPorNombre("Wrap de pollo")).thenReturn(null);
        lenient().when(restauranteDao.buscarPorId(2)).thenReturn(new Restaurante(2, "Burger Lab", ""));

        assertThrows(IllegalArgumentException.class, () -> platoService.registrarPlato(
                "Wrap de pollo", 2, List.of(new IngredientePlato(3, 0, "g"))));
    }

    @Test
    void lanzaExcepcionSinRestauranteSeleccionado() {
        lenient().when(platoDao.buscarPorNombre("Bowl de quinua")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> platoService.registrarPlato(
                "Bowl de quinua", null, List.of(new IngredientePlato(1, 100, "g"))));
    }
}
