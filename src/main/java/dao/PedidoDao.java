package dao;

import model.EstadoPedido;
import model.Pedido;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Persistencia en memoria de pedidos. El almacen es estatico para
 * compartir el mismo estado entre peticiones del servlet (simula la BD).
 */
public class PedidoDao {

    private static final Map<Integer, Pedido> ALMACEN = new ConcurrentHashMap<>();
    private static final AtomicInteger SECUENCIA = new AtomicInteger(0);

    static {
        sembrar("Pollo BBQ + papas - Crispy House", EstadoPedido.RECIBIDO);
        sembrar("Bowl vegano mediano - Green Bowl", EstadoPedido.RECIBIDO);
        sembrar("Pizza pepperoni familiar - Napoli", EstadoPedido.EN_PREPARACION);
        sembrar("Hamburguesa doble - Burger Lab", EstadoPedido.EN_PREPARACION);
        sembrar("Sushi roll x12 - Sakura", EstadoPedido.LISTO);
        sembrar("Tacos al pastor x6 - El Fogon", EstadoPedido.ENTREGADO);
    }

    private static void sembrar(String descripcion, EstadoPedido estado) {
        int id = SECUENCIA.incrementAndGet();
        String marca = descripcion.contains("-")
                ? descripcion.substring(descripcion.indexOf('-') + 1).trim()
                : "Generica";
        ALMACEN.put(id, new Pedido(id, descripcion, marca, estado));
    }

    public Pedido guardar(Pedido pedido) {
        if (pedido.getId() == 0) {
            pedido.setId(SECUENCIA.incrementAndGet());
        }
        ALMACEN.put(pedido.getId(), pedido);
        return pedido;
    }

    public Pedido buscarPorId(int id) {
        return ALMACEN.get(id);
    }

    public List<Pedido> listarTodos() {
        return new ArrayList<>(ALMACEN.values());
    }

    public List<Pedido> buscarPorEstado(EstadoPedido estado) {
        return ALMACEN.values().stream()
                .filter(p -> p.getEstado() == estado)
                .sorted(Comparator.comparingInt(Pedido::getId))
                .collect(Collectors.toList());
    }

    public void actualizar(Pedido pedido) {
        ALMACEN.put(pedido.getId(), pedido);
    }
}
