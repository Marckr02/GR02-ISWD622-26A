package darkkitchen;

import dao.PedidoDao;
import model.EstadoPedido;
import model.Pedido;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.PedidoService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD: avanzarEstado() con una transicion valida
 * (RECIBIDO -> EN_PREPARACION) debe mover el pedido un paso en el flujo.
 */
@ExtendWith(MockitoExtension.class)
class AvanzarEstadoValidoTest {

    @Mock
    private PedidoDao pedidoDao;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void avanzaDeRecibidoAEnPreparacion() {
        Pedido pedido = new Pedido(1, "Pizza margarita - Napoli", "Napoli", EstadoPedido.RECIBIDO);
        when(pedidoDao.buscarPorId(1)).thenReturn(pedido);

        Pedido resultado = pedidoService.avanzarEstado(1);

        assertEquals(EstadoPedido.EN_PREPARACION, resultado.getEstado());
        verify(pedidoDao, times(1)).actualizar(pedido);
    }
}
