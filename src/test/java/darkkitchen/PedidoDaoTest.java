package darkkitchen;

import dao.PedidoDao;
import model.EstadoPedido;
import model.Pedido;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TDD (tarea tecnica dao): PedidoDao asigna id al guardar, permite buscar
 * por id, listar todos, filtrar por estado (ordenado por id) y actualizar
 * el estado persistido.
 */
class PedidoDaoTest {

    private final PedidoDao dao = new PedidoDao();

    @Test
    void guardarAsignaIdCuandoEsCero() {
        Pedido guardado = dao.guardar(new Pedido(0, "Pedido Dao Test", "Marca X", EstadoPedido.RECIBIDO, 0));

        assertTrue(guardado.getId() > 0);
        assertEquals(guardado.getId(), dao.buscarPorId(guardado.getId()).getId());
    }

    @Test
    void buscarPorIdDevuelveNullSiNoExiste() {
        assertNull(dao.buscarPorId(-555));
    }

    @Test
    void listarTodosIncluyeElPedidoGuardado() {
        Pedido guardado = dao.guardar(new Pedido(0, "Pedido Dao Listar", "Marca Y", EstadoPedido.RECIBIDO, 0));

        List<Pedido> todos = dao.listarTodos();

        assertTrue(todos.stream().anyMatch(p -> p.getId() == guardado.getId()));
    }

    @Test
    void buscarPorEstadoFiltraYOrdenaPorId() {
        Pedido p1 = dao.guardar(new Pedido(0, "Pedido Listo Uno", "Marca Z", EstadoPedido.LISTO, 0));
        Pedido p2 = dao.guardar(new Pedido(0, "Pedido Listo Dos", "Marca Z", EstadoPedido.LISTO, 0));

        List<Pedido> listos = dao.buscarPorEstado(EstadoPedido.LISTO);

        assertTrue(listos.stream().allMatch(p -> p.getEstado() == EstadoPedido.LISTO));
        int idx1 = listos.indexOf(p1);
        int idx2 = listos.indexOf(p2);
        assertTrue(idx1 >= 0 && idx2 >= 0);
        assertTrue(idx1 < idx2);
    }

    @Test
    void actualizarPersisteElNuevoEstado() {
        Pedido creado = dao.guardar(new Pedido(0, "Pedido Para Actualizar", "Marca W", EstadoPedido.RECIBIDO, 0));
        creado.setEstado(EstadoPedido.EN_PREPARACION);

        dao.actualizar(creado);

        assertEquals(EstadoPedido.EN_PREPARACION, dao.buscarPorId(creado.getId()).getEstado());
    }
}
