package darkkitchen;

import dao.InsumoDao;
import dao.PedidoDao;
import dao.PlatoDao;
import model.EstadoPedido;
import model.Insumo;
import model.Pedido;
import model.Plato;
import org.junit.jupiter.api.Test;
import service.PedidoService;
import service.StockInsuficienteException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AvanzarPedidoSinStockTest {

    @Test
    void noAvanzaDeRecibidoAPreparacionSiFaltaStock() {
        PedidoDao pedidoDao = mock(PedidoDao.class);
        PlatoDao platoDao = mock(PlatoDao.class);
        InsumoDao insumoDao = mock(InsumoDao.class);
        PedidoService service = new PedidoService(pedidoDao, platoDao, insumoDao);

        Pedido pedido = new Pedido(1, "Pizza Margarita - Napoli", "Napoli", EstadoPedido.RECIBIDO, 7);
        Plato plato = new Plato(7, "Pizza Margarita", List.of(3));
        Insumo agotado = new Insumo(3, "Albahaca fresca", "kg", 0.0, 3.0);

        when(pedidoDao.buscarPorId(1)).thenReturn(pedido);
        when(platoDao.buscarPorId(7)).thenReturn(plato);
        when(insumoDao.buscarPorId(3)).thenReturn(agotado);

        StockInsuficienteException ex = assertThrows(
                StockInsuficienteException.class, () -> service.avanzarEstado(1));

        assertEquals(List.of("Albahaca fresca (disponible: 0 kg, requerido: 1 kg)"), ex.getFaltantes());
        assertEquals(EstadoPedido.RECIBIDO, pedido.getEstado());
        verify(pedidoDao, never()).actualizar(pedido);
    }
}
