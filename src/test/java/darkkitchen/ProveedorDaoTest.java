package darkkitchen;

import dao.ProveedorDao;
import model.Proveedor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TDD (tarea tecnica dao): ProveedorDao asigna id al guardar, permite
 * buscar por id y por nombre (ignorando mayusculas), listar ordenado
 * alfabeticamente, eliminar (y limpiar sus vinculos) y gestionar la
 * vinculacion insumo-proveedor (HU6/HU25).
 */
class ProveedorDaoTest {

    private final ProveedorDao dao = new ProveedorDao();

    @Test
    void guardarAsignaIdCuandoEsCero() {
        Proveedor guardado = dao.guardar(new Proveedor(0, "Proveedor Dao Test", "0991112233", "a@dao.com"));

        assertTrue(guardado.getId() > 0);
    }

    @Test
    void buscarPorIdDevuelveNullSiNoExiste() {
        assertNull(dao.buscarPorId(-222));
    }

    @Test
    void buscarPorNombreDevuelveNullSiNombreEsNull() {
        assertNull(dao.buscarPorNombre(null));
    }

    @Test
    void buscarPorNombreEsInsensibleAMayusculas() {
        dao.guardar(new Proveedor(0, "Proveedor Especial Dao", "0991112244", "b@dao.com"));

        Proveedor encontrado = dao.buscarPorNombre("proveedor ESPECIAL dao");

        assertEquals("Proveedor Especial Dao", encontrado.getNombre());
    }

    @Test
    void buscarPorNombreDevuelveNullSiNoExiste() {
        assertNull(dao.buscarPorNombre("Proveedor Que No Existe XYZ Dao"));
    }

    @Test
    void listarTodosIncluyeElProveedorGuardadoYQuedaOrdenado() {
        Proveedor guardado = dao.guardar(new Proveedor(0, "Aaaa Proveedor Listar Dao", "0991112255", "c@dao.com"));

        List<Proveedor> lista = dao.listarTodos();

        assertTrue(lista.stream().anyMatch(p -> p.getId() == guardado.getId()));
        for (int i = 0; i < lista.size() - 1; i++) {
            assertTrue(lista.get(i).getNombre().compareToIgnoreCase(lista.get(i + 1).getNombre()) <= 0);
        }
    }

    @Test
    void eliminarRemueveElProveedorDelAlmacen() {
        Proveedor creado = dao.guardar(new Proveedor(0, "Proveedor Para Eliminar Dao", "0991112266", "d@dao.com"));

        dao.eliminar(creado.getId());

        assertNull(dao.buscarPorId(creado.getId()));
    }

    @Test
    void vincularYConsultarInsumoDevuelveElProveedorAsociado() {
        int insumoId = 654321;
        Proveedor proveedor = dao.guardar(new Proveedor(0, "Proveedor Vinculado Dao", "0991112277", "e@dao.com"));

        dao.vincularInsumo(insumoId, proveedor.getId());

        assertEquals(proveedor.getId(), dao.obtenerProveedorIdDeInsumo(insumoId));
        assertTrue(dao.tieneInsumosVinculados(proveedor.getId()));
    }

    @Test
    void desvincularInsumoEliminaLaAsociacion() {
        int insumoId = 654322;
        Proveedor proveedor = dao.guardar(new Proveedor(0, "Proveedor Desvinculado Dao", "0991112288", "f@dao.com"));
        dao.vincularInsumo(insumoId, proveedor.getId());

        dao.desvincularInsumo(insumoId);

        assertNull(dao.obtenerProveedorIdDeInsumo(insumoId));
    }

    @Test
    void tieneInsumosVinculadosEsFalsoParaProveedorSinVinculos() {
        assertFalse(dao.tieneInsumosVinculados(-999888));
    }

    @Test
    void eliminarProveedorLimpiaSusVinculos() {
        int insumoId = 654323;
        Proveedor proveedor = dao.guardar(new Proveedor(0, "Proveedor A Eliminar Con Vinculo Dao", "0991112299", "g@dao.com"));
        dao.vincularInsumo(insumoId, proveedor.getId());

        dao.eliminar(proveedor.getId());

        assertFalse(dao.tieneInsumosVinculados(proveedor.getId()));
    }
}
