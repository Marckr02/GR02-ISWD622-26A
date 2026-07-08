package darkkitchen;

import dao.RestauranteDao;
import model.Restaurante;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TDD (tarea tecnica dao): RestauranteDao asigna id al guardar, permite
 * busqueda por id y por nombre (ignorando mayusculas), lista ordenado
 * alfabeticamente, y soporta actualizar/eliminar.
 */
class RestauranteDaoTest {

    private final RestauranteDao dao = new RestauranteDao();

    @Test
    void guardarAsignaIdYPermiteBuscarPorId() {
        Restaurante guardado = dao.guardar(new Restaurante(0, "Cocina Zeta Dao", "Descripcion"));

        assertTrue(guardado.getId() > 0);
        assertEquals("Cocina Zeta Dao", dao.buscarPorId(guardado.getId()).getNombre());
    }

    @Test
    void buscarPorIdDevuelveNullSiNoExiste() {
        assertNull(dao.buscarPorId(-999));
    }

    @Test
    void buscarPorNombreDevuelveNullSiNombreEsNull() {
        assertNull(dao.buscarPorNombre(null));
    }

    @Test
    void buscarPorNombreEsInsensibleAMayusculas() {
        dao.guardar(new Restaurante(0, "Cocina Omega Dao", "Desc"));

        Restaurante encontrado = dao.buscarPorNombre("cocina OMEGA dao");

        assertEquals("Cocina Omega Dao", encontrado.getNombre());
    }

    @Test
    void buscarPorNombreDevuelveNullSiNoExiste() {
        assertNull(dao.buscarPorNombre("Restaurante Que No Existe Dao XYZ"));
    }

    @Test
    void listarTodosIncluyeElRegistradoYQuedaOrdenado() {
        Restaurante nuevo = dao.guardar(new Restaurante(0, "Alfa Restaurante Dao", "Desc"));

        List<Restaurante> lista = dao.listarTodos();

        assertTrue(lista.stream().anyMatch(r -> r.getId() == nuevo.getId()));
        for (int i = 0; i < lista.size() - 1; i++) {
            assertTrue(lista.get(i).getNombre().compareToIgnoreCase(lista.get(i + 1).getNombre()) <= 0);
        }
    }

    @Test
    void actualizarModificaLosDatosPersistidos() {
        Restaurante creado = dao.guardar(new Restaurante(0, "Restaurante Para Actualizar Dao", "Vieja"));
        creado.setDescripcion("Nueva descripcion");

        dao.actualizar(creado);

        assertEquals("Nueva descripcion", dao.buscarPorId(creado.getId()).getDescripcion());
    }

    @Test
    void eliminarRemueveElRestauranteDelAlmacen() {
        Restaurante creado = dao.guardar(new Restaurante(0, "Restaurante Para Eliminar Dao", "Desc"));

        dao.eliminar(creado.getId());

        assertNull(dao.buscarPorId(creado.getId()));
    }
}
