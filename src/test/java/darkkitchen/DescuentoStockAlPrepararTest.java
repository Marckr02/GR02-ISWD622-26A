package darkkitchen;

import dao.InsumoDao;
import dao.PedidoDao;
import dao.PlatoDao;
import model.EstadoPedido;
import model.IngredientePlato;
import model.Insumo;
import model.Pedido;
import model.Plato;
import org.junit.jupiter.api.Test;
import service.PedidoService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD (tarea tecnica F6.2 + HU30): al enviar un pedido a preparacion, el
 * sistema descuenta del inventario la cantidad real de la receta,
 * convirtiendo la unidad de la receta a la unidad en que se almacena el
 * insumo (g -> kg en este caso).
 */
class DescuentoStockAlPrepararTest {

    @Test
    void descuentaStockConvirtiendoLaUnidadDeLaReceta() {
        PedidoDao pedidoDao = mock(PedidoDao.class);
        PlatoDao platoDao = mock(PlatoDao.class);
        InsumoDao insumoDao = mock(InsumoDao.class);
        PedidoService service = new PedidoService(pedidoDao, platoDao, insumoDao);

        Pedido pedido = new Pedido(1, "Pizza Margarita - Napoli", "Napoli", EstadoPedido.RECIBIDO, 7);
        Plato plato = new Plato(7, "Pizza Margarita", 1,
                List.of(new IngredientePlato(1, 250.0, "g")));
        Insumo harina = new Insumo(1, "Harina de trigo", "kg", 10.0, 0.85);

        when(pedidoDao.buscarPorId(1)).thenReturn(pedido);
        when(platoDao.buscarPorId(7)).thenReturn(plato);
        when(insumoDao.buscarPorId(1)).thenReturn(harina);

        service.avanzarEstado(1);

        assertEquals(9.75, harina.getStock(), 0.0001);
        assertEquals(EstadoPedido.EN_PREPARACION, pedido.getEstado());
        verify(insumoDao).actualizar(harina);
    }
}
