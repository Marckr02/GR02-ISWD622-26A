package service;

import dao.PedidoDao;
import model.EstadoPedido;
import model.Pedido;

import java.util.List;

/**
 * Logica de negocio del tablero Kanban de pedidos: consulta por estado
 * para alimentar las columnas y avance/retroceso del ciclo de vida.
 */
public class PedidoService {

    private final PedidoDao pedidoDao;

    public PedidoService() {
        this(new PedidoDao());
    }

    public PedidoService(PedidoDao pedidoDao) {
        this.pedidoDao = pedidoDao;
    }

    /** Pedidos de un estado concreto, usado para pintar cada columna del tablero. */
    public List<Pedido> listarPorEstado(EstadoPedido estado) {
        return pedidoDao.buscarPorEstado(estado);
    }

    /** Columna inicial del tablero. */
    public List<Pedido> listarPedidosRecibidos() {
        return listarPorEstado(EstadoPedido.RECIBIDO);
    }

    public List<Pedido> listarTodos() {
        return pedidoDao.listarTodos();
    }

    public Pedido buscar(int id) {
        return pedidoDao.buscarPorId(id);
    }

    /**
     * Avanza el pedido al siguiente estado del flujo y persiste el cambio.
     * @throws IllegalArgumentException si el pedido no existe.
     * @throws IllegalStateException    si el pedido ya esta ENTREGADO.
     */
    public Pedido avanzarEstado(int pedidoId) {
        Pedido pedido = pedidoDao.buscarPorId(pedidoId);
        if (pedido == null) {
            throw new IllegalArgumentException("No existe el pedido " + pedidoId);
        }
        pedido.setEstado(pedido.getEstado().siguiente());
        pedidoDao.actualizar(pedido);
        return pedido;
    }

    /** Alias de negocio usado por la accion "mover" del tablero. */
    public Pedido moverPedido(int pedidoId) {
        return avanzarEstado(pedidoId);
    }

    /**
     * Retrocede el pedido al estado anterior cuando se avanzo por error (HU20)
     * y persiste el cambio.
     * @throws IllegalArgumentException si el pedido no existe ("Pedido no encontrado").
     * @throws IllegalStateException    si esta en RECIBIDO o ENTREGADO.
     */
    public Pedido retrocederEstado(int pedidoId) {
        Pedido pedido = pedidoDao.buscarPorId(pedidoId);
        if (pedido == null) {
            throw new IllegalArgumentException("Pedido no encontrado");
        }
        pedido.setEstado(pedido.getEstado().anterior());
        pedidoDao.actualizar(pedido);
        return pedido;
    }
}
