package darkkitchen;

import dao.InsumoDao;
import dao.PedidoDao;
import dao.PlatoDao;
import dao.RestauranteDao;
import model.EstadoPedido;
import model.IngredientePlato;
import model.Insumo;
import model.Pedido;
import model.Plato;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.AlertaStockService;
import service.PedidoService;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD (fix F3.3/HU11): al descontar stock para preparar un pedido,
 * PedidoService debe evaluar y registrar la alerta de stock critico en el
 * mismo momento del descuento, no solo cuando alguien visita el panel de
 * monitoreo.
 */
@ExtendWith(MockitoExtension.class)
class PedidoServiceAlertaTest {

    @Mock
    private PedidoDao pedidoDao;
    @Mock
    private PlatoDao platoDao;
    @Mock
    private InsumoDao insumoDao;
    @Mock
    private AlertaStockService alertaStockService;

    @Test
    void avanzarEstadoEvaluaLaAlertaParaCadaInsumoDescontado() {
        PedidoService service = new PedidoService(
                pedidoDao, platoDao, insumoDao, mock(RestauranteDao.class), alertaStockService);

        Pedido pedido = new Pedido(1, "Albahaca fresca - Napoli", "Napoli", EstadoPedido.RECIBIDO, 7);
        Plato plato = new Plato(7, "Pizza Margarita", 1,
                List.of(new IngredientePlato(1, 10.0, "g")));
        Insumo albahaca = new Insumo(1, "Albahaca fresca", "kg", 0.02, 3.0, 2.0);

        when(pedidoDao.buscarPorId(1)).thenReturn(pedido);
        when(platoDao.buscarPorId(7)).thenReturn(plato);
        when(insumoDao.buscarPorId(1)).thenReturn(albahaca);

        service.avanzarEstado(1);

        verify(alertaStockService).evaluarYRegistrar(albahaca);
    }
}
