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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD (HU31/HU32/HU33): listarPlatos()/buscar()/restauranteDe()/insumoDe()
 * delegan en los daos correspondientes, actualizarPlato() falla si el plato
 * no existe, y validarIngredientes() rechaza listas vacias, unidades
 * invalidas e insumos inexistentes.
 */
@ExtendWith(MockitoExtension.class)
class PlatoServiceActualizarEliminarTest {

    @Mock
    private PlatoDao platoDao;

    @Mock
    private RestauranteDao restauranteDao;

    @Mock
    private InsumoDao insumoDao;

    @InjectMocks
    private PlatoService platoService;

    @Test
    void listarPlatosOrdenaAlfabeticamente() {
        Plato b = new Plato(1, "Bruschetta", 1, List.of(new IngredientePlato(1, 1, "g")));
        Plato a = new Plato(2, "Arroz", 1, List.of(new IngredientePlato(1, 1, "g")));
        when(platoDao.listarTodos()).thenReturn(List.of(b, a));

        List<Plato> ordenado = platoService.listarPlatos();

        assertEquals("Arroz", ordenado.get(0).getNombre());
        assertEquals("Bruschetta", ordenado.get(1).getNombre());
    }

    @Test
    void buscarDelegaEnElDao() {
        Plato plato = new Plato(3, "Pizza", 1, List.of(new IngredientePlato(1, 1, "g")));
        when(platoDao.buscarPorId(3)).thenReturn(plato);

        assertEquals(plato, platoService.buscar(3));
    }

    @Test
    void restauranteDeDelegaEnElRestauranteDao() {
        Restaurante restaurante = new Restaurante(1, "Napoli", "Desc");
        Plato plato = new Plato(4, "Pizza", 1, List.of(new IngredientePlato(1, 1, "g")));
        when(restauranteDao.buscarPorId(1)).thenReturn(restaurante);

        assertEquals(restaurante, platoService.restauranteDe(plato));
    }

    @Test
    void insumoDeDelegaEnElInsumoDao() {
        Insumo insumo = new Insumo(5, "Harina", "kg", 10.0, 1.0);
        when(insumoDao.buscarPorId(5)).thenReturn(insumo);

        assertEquals(insumo, platoService.insumoDe(5));
    }

    @Test
    void listarRestaurantesDisponiblesDelegaEnElDao() {
        List<Restaurante> lista = List.of(new Restaurante(1, "Napoli", "Desc"));
        when(restauranteDao.listarTodos()).thenReturn(lista);

        assertEquals(lista, platoService.listarRestaurantesDisponibles());
    }

    @Test
    void listarInsumosDisponiblesDelegaEnElDao() {
        List<Insumo> lista = List.of(new Insumo(1, "Harina", "kg", 10.0, 1.0));
        when(insumoDao.listarTodos()).thenReturn(lista);

        assertEquals(lista, platoService.listarInsumosDisponibles());
    }

    @Test
    void actualizarPlatoLanzaExcepcionSiNoExiste() {
        when(platoDao.buscarPorId(404)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> platoService.actualizarPlato(404, "Nombre", 1, List.of(new IngredientePlato(1, 1, "g"))));
    }

    @Test
    void actualizarPlatoModificaNombreRestauranteEIngredientes() {
        Plato existente = new Plato(6, "Nombre Viejo", 1, List.of(new IngredientePlato(1, 1, "g")));
        when(platoDao.buscarPorId(6)).thenReturn(existente);
        when(platoDao.buscarPorNombre("Nombre Nuevo Plato")).thenReturn(null);
        when(restauranteDao.buscarPorId(2)).thenReturn(new Restaurante(2, "Sakura", "Desc"));
        when(insumoDao.buscarPorId(3)).thenReturn(new Insumo(3, "Pollo", "kg", 5.0, 1.0));

        Plato actualizado = platoService.actualizarPlato(6, "Nombre Nuevo Plato", 2,
                List.of(new IngredientePlato(3, 2.0, "kg")));

        assertEquals("Nombre Nuevo Plato", actualizado.getNombre());
        assertEquals(2, actualizado.getRestauranteId());
        verify(platoDao).actualizar(existente);
    }

    @Test
    void eliminarPlatoLanzaExcepcionSiNoExiste() {
        when(platoDao.buscarPorId(500)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> platoService.eliminarPlato(500));
    }

    @Test
    void eliminarPlatoEliminaCuandoExiste() {
        Plato plato = new Plato(7, "Para Eliminar", 1, List.of(new IngredientePlato(1, 1, "g")));
        when(platoDao.buscarPorId(7)).thenReturn(plato);

        platoService.eliminarPlato(7);

        verify(platoDao).eliminar(7);
    }

    @Test
    void registrarPlatoLanzaExcepcionSiIngredientesEsVacio() {
        when(restauranteDao.buscarPorId(1)).thenReturn(new Restaurante(1, "Napoli", "Desc"));

        assertThrows(IllegalArgumentException.class,
                () -> platoService.registrarPlato("Plato Vacio", 1, List.of()));
    }

    @Test
    void registrarPlatoLanzaExcepcionSiUnidadDeIngredienteEsInvalida() {
        when(restauranteDao.buscarPorId(1)).thenReturn(new Restaurante(1, "Napoli", "Desc"));

        assertThrows(IllegalArgumentException.class, () -> platoService.registrarPlato(
                "Plato Unidad Invalida", 1, List.of(new IngredientePlato(1, 5.0, "libras"))));
    }

    @Test
    void registrarPlatoLanzaExcepcionSiElInsumoNoExiste() {
        when(restauranteDao.buscarPorId(1)).thenReturn(new Restaurante(1, "Napoli", "Desc"));
        when(insumoDao.buscarPorId(999)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> platoService.registrarPlato(
                "Plato Insumo Inexistente", 1, List.of(new IngredientePlato(999, 5.0, "g"))));
    }
}
