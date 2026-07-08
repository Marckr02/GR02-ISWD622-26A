package darkkitchen;

import dao.InsumoDao;
import model.Insumo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TDD (tarea tecnica dao): InsumoDao asigna id al guardar, permite buscar
 * por id y por nombre (ignorando mayusculas), listar ordenado por id y
 * actualizar los datos persistidos.
 */
class InsumoDaoTest {

    private final InsumoDao dao = new InsumoDao();

    @Test
    void guardarAsignaIdCuandoEsCero() {
        Insumo guardado = dao.guardar(new Insumo(0, "Insumo Dao Test", "kg", 10.0, 1.0, 1.0));

        assertTrue(guardado.getId() > 0);
    }

    @Test
    void buscarPorIdDevuelveNullSiNoExiste() {
        assertNull(dao.buscarPorId(-333));
    }

    @Test
    void buscarPorNombreDevuelveNullSiNombreEsNull() {
        assertNull(dao.buscarPorNombre(null));
    }

    @Test
    void buscarPorNombreEsInsensibleAMayusculas() {
        dao.guardar(new Insumo(0, "Insumo Especial Dao", "kg", 5.0, 1.0, 1.0));

        Insumo encontrado = dao.buscarPorNombre("insumo ESPECIAL dao");

        assertEquals("Insumo Especial Dao", encontrado.getNombre());
    }

    @Test
    void buscarPorNombreDevuelveNullSiNoExiste() {
        assertNull(dao.buscarPorNombre("Insumo Que No Existe XYZ Dao"));
    }

    @Test
    void actualizarModificaElStockPersistido() {
        Insumo creado = dao.guardar(new Insumo(0, "Insumo Para Actualizar Dao", "kg", 3.0, 1.0, 1.0));
        creado.setStock(9.0);

        dao.actualizar(creado);

        assertEquals(9.0, dao.buscarPorId(creado.getId()).getStock(), 0.0001);
    }
}