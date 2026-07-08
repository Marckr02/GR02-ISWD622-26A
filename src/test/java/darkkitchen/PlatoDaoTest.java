package darkkitchen;

import dao.PlatoDao;
import model.IngredientePlato;
import model.Plato;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TDD (tarea tecnica dao): PlatoDao asigna id al guardar, permite buscar
 * por id y por nombre (ignorando mayusculas), listar ordenado por id,
 * actualizar/eliminar y detectar si un restaurante tiene platos asociados.
 */
class PlatoDaoTest {

    private final PlatoDao dao = new PlatoDao();

    @Test
    void guardarAsignaIdCuandoEsCero() {
        Plato guardado = dao.guardar(new Plato(0, "Plato Dao Test", 1,
                List.of(new IngredientePlato(1, 100, "g"))));

        assertTrue(guardado.getId() > 0);
    }

    @Test
    void buscarPorIdDevuelveNullSiNoExiste() {
        assertNull(dao.buscarPorId(-444));
    }

    @Test
    void buscarPorNombreDevuelveNullSiNombreEsNull() {
        assertNull(dao.buscarPorNombre(null));
    }

    @Test
    void buscarPorNombreEsInsensibleAMayusculas() {
        dao.guardar(new Plato(0, "Plato Especial Dao", 1, List.of(new IngredientePlato(1, 100, "g"))));

        Plato encontrado = dao.buscarPorNombre("plato ESPECIAL dao");

        assertEquals("Plato Especial Dao", encontrado.getNombre());
    }

    @Test
    void buscarPorNombreDevuelveNullSiNoExiste() {
        assertNull(dao.buscarPorNombre("Plato Inexistente XYZ Dao"));
    }

    @Test
    void listarTodosIncluyeElPlatoGuardadoOrdenadoPorId() {
        Plato guardado = dao.guardar(new Plato(0, "Plato Listar Dao", 1,
                List.of(new IngredientePlato(1, 100, "g"))));

        List<Plato> lista = dao.listarTodos();

        assertTrue(lista.stream().anyMatch(p -> p.getId() == guardado.getId()));
        for (int i = 0; i < lista.size() - 1; i++) {
            assertTrue(lista.get(i).getId() <= lista.get(i + 1).getId());
        }
    }

    @Test
    void actualizarModificaElNombrePersistido() {
        Plato creado = dao.guardar(new Plato(0, "Plato Original Dao", 1,
                List.of(new IngredientePlato(1, 100, "g"))));
        creado.setNombre("Plato Modificado Dao");

        dao.actualizar(creado);

        assertEquals("Plato Modificado Dao", dao.buscarPorId(creado.getId()).getNombre());
    }

    @Test
    void eliminarRemueveElPlatoDelAlmacen() {
        Plato creado = dao.guardar(new Plato(0, "Plato Para Eliminar Dao", 1,
                List.of(new IngredientePlato(1, 100, "g"))));

        dao.eliminar(creado.getId());

        assertNull(dao.buscarPorId(creado.getId()));
    }

    @Test
    void existePlatoConRestauranteEsFalsoParaRestauranteSinPlatos() {
        assertFalse(dao.existePlatoConRestaurante(-321));
    }

    @Test
    void existePlatoConRestauranteEsVerdaderoTrasGuardarUno() {
        int restauranteId = 987654;
        dao.guardar(new Plato(0, "Plato De Restaurante Dao", restauranteId,
                List.of(new IngredientePlato(1, 100, "g"))));

        assertTrue(dao.existePlatoConRestaurante(restauranteId));
    }
}