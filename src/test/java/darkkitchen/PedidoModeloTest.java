package darkkitchen;

import model.EstadoPedido;
import model.Pedido;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * TDD (tarea tecnica model): Pedido inicializa estado RECIBIDO y creadoEn
 * en el constructor vacio, el constructor de 4 argumentos usa platoId 0
 * por defecto, y todos los getters/setters funcionan correctamente.
 */
class PedidoModeloTest {

    @Test
    void constructorVacioInicializaEstadoRecibidoYFechaDeCreacion() {
        Pedido pedido = new Pedido();

        assertEquals(EstadoPedido.RECIBIDO, pedido.getEstado());
        assertNotNull(pedido.getCreadoEn());
    }

    @Test
    void constructorDeCuatroArgumentosUsaPlatoIdCero() {
        Pedido pedido = new Pedido(1, "Descripcion", "Marca", EstadoPedido.LISTO);

        assertEquals(0, pedido.getPlatoId());
        assertEquals(EstadoPedido.LISTO, pedido.getEstado());
    }

    @Test
    void constructorDeCincoArgumentosAsignaTodosLosCampos() {
        Pedido pedido = new Pedido(2, "Otra Descripcion", "Otra Marca", EstadoPedido.EN_PREPARACION, 9);

        assertEquals(2, pedido.getId());
        assertEquals("Otra Descripcion", pedido.getDescripcion());
        assertEquals("Otra Marca", pedido.getMarca());
        assertEquals(EstadoPedido.EN_PREPARACION, pedido.getEstado());
        assertEquals(9, pedido.getPlatoId());
    }

    @Test
    void settersModificanTodosLosCampos() {
        Pedido pedido = new Pedido();
        LocalDateTime fecha = LocalDateTime.of(2024, 1, 1, 10, 0);

        pedido.setId(11);
        pedido.setDescripcion("Nueva desc");
        pedido.setMarca("Nueva marca");
        pedido.setPlatoId(3);
        pedido.setEstado(EstadoPedido.ENTREGADO);
        pedido.setCreadoEn(fecha);

        assertEquals(11, pedido.getId());
        assertEquals("Nueva desc", pedido.getDescripcion());
        assertEquals("Nueva marca", pedido.getMarca());
        assertEquals(3, pedido.getPlatoId());
        assertEquals(EstadoPedido.ENTREGADO, pedido.getEstado());
        assertEquals(fecha, pedido.getCreadoEn());
    }
}
