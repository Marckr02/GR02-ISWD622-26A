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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD: avanzarEstado() sobre un pedido ENTREGADO (estado terminal)
 * debe lanzar excepcion y no persistir ningun cambio.
 */
@ExtendWith(MockitoExtension.class)
class AvanzarEstadoInvalidoTest {

    @Mock
    private PedidoDao pedidoDao;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void entregadoNoPuedeAvanzar() {
        Pedido pedido = new Pedido(9, "Tacos al pastor - El Fogon", "El Fogon", EstadoPedido.ENTREGADO);
        when(pedidoDao.buscarPorId(9)).thenReturn(pedido);

        assertThrows(IllegalStateException.class, () -> pedidoService.avanzarEstado(9));

        verify(pedidoDao, never()).actualizar(pedido);
    }
}
