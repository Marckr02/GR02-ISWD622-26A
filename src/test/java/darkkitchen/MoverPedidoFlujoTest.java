package darkkitchen;

import dao.PedidoDao;
import model.EstadoPedido;
import model.Pedido;
import org.junit.jupiter.api.Test;
import service.PedidoService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Prueba de integracion del flujo completo de moverPedido() sobre el
 * almacen en memoria, validando que cada avance persiste y que un pedido
 * entregado ya no admite mas movimientos.
 */
class MoverPedidoFlujoTest {

    @Test
    void recorreTodoElCicloYPersiste() {
        PedidoDao dao = new PedidoDao();
        PedidoService service = new PedidoService(dao);

        Pedido nuevo = dao.guardar(new Pedido(0, "Combo prueba - QA Kitchen", "QA Kitchen", EstadoPedido.RECIBIDO));
        int id = nuevo.getId();

        assertEquals(EstadoPedido.EN_PREPARACION, service.moverPedido(id).getEstado());
        assertEquals(EstadoPedido.LISTO, service.moverPedido(id).getEstado());
        assertEquals(EstadoPedido.ENTREGADO, service.moverPedido(id).getEstado());

        // El cambio quedo persistido en el almacen.
        assertEquals(EstadoPedido.ENTREGADO, dao.buscarPorId(id).getEstado());

        // Un pedido entregado no puede avanzar mas.
        assertThrows(IllegalStateException.class, () -> service.moverPedido(id));
    }
}
