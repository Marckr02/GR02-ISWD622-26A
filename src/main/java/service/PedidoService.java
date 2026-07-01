package service;

import dao.InsumoDao;
import dao.PedidoDao;
import dao.PlatoDao;
import model.EstadoPedido;
import model.Insumo;
import model.Pedido;
import model.Plato;

import java.util.ArrayList;
import java.util.List;

/**
 * Logica de negocio del tablero Kanban de pedidos: consulta por estado
 * para alimentar las columnas y avance/retroceso del ciclo de vida.
 */
public class PedidoService {

    private final PedidoDao pedidoDao;
    private final PlatoDao platoDao;
    private final InsumoDao insumoDao;

    public PedidoService() {
        this(new PedidoDao());
    }

    public PedidoService(PedidoDao pedidoDao) {
        this(pedidoDao, new PlatoDao(), new InsumoDao());
    }

    public PedidoService(PedidoDao pedidoDao, PlatoDao platoDao, InsumoDao insumoDao) {
        this.pedidoDao = pedidoDao;
        this.platoDao = platoDao;
        this.insumoDao = insumoDao;
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
        validarYDescontarStockParaPreparacion(pedido);
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

    /**
     * Valida el stock necesario para pasar el pedido a preparacion y, si es
     * suficiente, descuenta automaticamente 1 unidad de cada insumo que
     * compone el plato (HU: descuento de stock al iniciar preparacion).
     * Si falta stock de uno o mas insumos no se descuenta nada y se lanza
     * StockInsuficienteException con el detalle de lo que falta.
     */
    private void validarYDescontarStockParaPreparacion(Pedido pedido) {
        if (pedido.getEstado() != EstadoPedido.RECIBIDO || pedido.getPlatoId() <= 0) {
            return;
        }
        Plato plato = platoDao.buscarPorId(pedido.getPlatoId());
        if (plato == null) {
            return;
        }
        List<String> faltantes = new ArrayList<>();
        List<Insumo> insumosADescontar = new ArrayList<>();
        for (int insumoId : plato.getInsumoIds()) {
            Insumo insumo = insumoDao.buscarPorId(insumoId);
            if (insumo == null) {
                faltantes.add("insumo #" + insumoId);
            } else if (insumo.getStock() < 1) {
                faltantes.add(insumo.getNombre() + " (disponible: "
                        + formatear(insumo.getStock()) + " " + insumo.getUnidad()
                        + ", requerido: 1 " + insumo.getUnidad() + ")");
            } else {
                insumosADescontar.add(insumo);
            }
        }
        if (!faltantes.isEmpty()) {
            throw new StockInsuficienteException(plato.getNombre(), faltantes);
        }
        for (Insumo insumo : insumosADescontar) {
            insumo.setStock(insumo.getStock() - 1);
            insumoDao.actualizar(insumo);
        }
    }

    private String formatear(double valor) {
        if (valor == Math.rint(valor)) {
            return String.valueOf((long) valor);
        }
        return String.format(java.util.Locale.US, "%.2f", valor);
    }
}