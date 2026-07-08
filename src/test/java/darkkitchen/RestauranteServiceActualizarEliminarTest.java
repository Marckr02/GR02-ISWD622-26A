package darkkitchen;

import dao.PlatoDao;
import dao.RestauranteDao;
import model.Restaurante;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.RestauranteService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD (HU27/HU28/HU29): listarRestaurantes()/buscar() delegan en el dao,
 * actualizarRestaurante() modifica un restaurante existente y falla si no
 * existe, y eliminarRestaurante() bloquea la eliminacion si el restaurante
 * tiene platos asociados.
 */
@ExtendWith(MockitoExtension.class)
class RestauranteServiceActualizarEliminarTest {

    @Mock
    private RestauranteDao restauranteDao;

    @Mock
    private PlatoDao platoDao;

    @InjectMocks
    private RestauranteService restauranteService;

    @Test
    void listarRestaurantesDelegaEnElDao() {
        List<Restaurante> lista = List.of(new Restaurante(1, "Napoli", "Desc"));
        when(restauranteDao.listarTodos()).thenReturn(lista);

        assertEquals(lista, restauranteService.listarRestaurantes());
    }

    @Test
    void buscarDelegaEnElDao() {
        Restaurante restaurante = new Restaurante(2, "Sakura", "Desc");
        when(restauranteDao.buscarPorId(2)).thenReturn(restaurante);

        assertEquals(restaurante, restauranteService.buscar(2));
    }

    @Test
    void actualizarRestauranteLanzaExcepcionSiNoExiste() {
        when(restauranteDao.buscarPorId(404)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> restauranteService.actualizarRestaurante(404, "Nombre", "Desc"));
    }

    @Test
    void actualizarRestauranteModificaNombreYDescripcion() {
        Restaurante existente = new Restaurante(3, "Nombre Viejo", "Desc Vieja");
        when(restauranteDao.buscarPorId(3)).thenReturn(existente);
        when(restauranteDao.buscarPorNombre("Nombre Nuevo Rest")).thenReturn(null);

        Restaurante actualizado = restauranteService.actualizarRestaurante(3, "Nombre Nuevo Rest", "Desc Nueva");

        assertEquals("Nombre Nuevo Rest", actualizado.getNombre());
        assertEquals("Desc Nueva", actualizado.getDescripcion());
        verify(restauranteDao).actualizar(existente);
    }

    @Test
    void eliminarRestauranteLanzaExcepcionSiNoExiste() {
        when(restauranteDao.buscarPorId(500)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> restauranteService.eliminarRestaurante(500));
    }

    @Test
    void eliminarRestauranteLanzaExcepcionSiTienePlatosAsociados() {
        Restaurante restaurante = new Restaurante(6, "Con Platos", "Desc");
        when(restauranteDao.buscarPorId(6)).thenReturn(restaurante);
        when(platoDao.existePlatoConRestaurante(6)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> restauranteService.eliminarRestaurante(6));
    }

    @Test
    void eliminarRestauranteEliminaSiNoTienePlatosAsociados() {
        Restaurante restaurante = new Restaurante(7, "Sin Platos", "Desc");
        when(restauranteDao.buscarPorId(7)).thenReturn(restaurante);
        when(platoDao.existePlatoConRestaurante(7)).thenReturn(false);

        restauranteService.eliminarRestaurante(7);

        verify(restauranteDao).eliminar(7);
    }
}
