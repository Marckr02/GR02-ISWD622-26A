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
 * TDD (HU20): retrocederEstado() lanza excepcion cuando el pedido esta en
 * RECIBIDO, ya que es el estado inicial y no tiene estado anterior; ademas no
 * debe persistir ningun cambio.
 */
@ExtendWith(MockitoExtension.class)
class RetrocederEstadoInicialTest {

    @Mock
    private PedidoDao pedidoDao;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void lanzaExcepcionAlRetrocederUnPedidoEnRecibido() {
        Pedido pedido = new Pedido(1, "Combo familiar - Napoli", "Napoli", EstadoPedido.RECIBIDO);
        when(pedidoDao.buscarPorId(1)).thenReturn(pedido);

        assertThrows(IllegalStateException.class, () -> pedidoService.retrocederEstado(1));
        verify(pedidoDao, never()).actualizar(pedido);
    }
}
