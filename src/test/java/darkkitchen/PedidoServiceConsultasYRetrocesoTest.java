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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.PedidoService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD (HU20 / Kanban): las consultas de PedidoService delegan en el dao,
 * retrocederEstado() mueve el pedido al estado anterior y persiste el
 * cambio, avanzarEstado()/retrocederEstado() lanzan excepcion si el pedido
 * no existe, y avanzarEstado() no valida stock cuando el pedido no esta
 * RECIBIDO o no tiene plato asociado.
 */
@ExtendWith(MockitoExtension.class)
class PedidoServiceConsultasYRetrocesoTest {

    @Mock
    private PedidoDao pedidoDao;

    @Mock
    private PlatoDao platoDao;

    @Mock
    private InsumoDao insumoDao;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void listarPorEstadoDelegaEnElDao() {
        List<Pedido> pedidos = List.of(new Pedido(1, "Desc", "Marca", EstadoPedido.LISTO, 0));
        when(pedidoDao.buscarPorEstado(EstadoPedido.LISTO)).thenReturn(pedidos);

        assertEquals(pedidos, pedidoService.listarPorEstado(EstadoPedido.LISTO));
    }

    @Test
    void listarPedidosRecibidosUsaElEstadoRecibido() {
        List<Pedido> pedidos = List.of(new Pedido(2, "Desc", "Marca", EstadoPedido.RECIBIDO, 0));
        when(pedidoDao.buscarPorEstado(EstadoPedido.RECIBIDO)).thenReturn(pedidos);

        assertEquals(pedidos, pedidoService.listarPedidosRecibidos());
    }

    @Test
    void listarTodosDelegaEnElDao() {
        List<Pedido> pedidos = List.of(new Pedido(3, "Desc", "Marca", EstadoPedido.ENTREGADO, 0));
        when(pedidoDao.listarTodos()).thenReturn(pedidos);

        assertEquals(pedidos, pedidoService.listarTodos());
    }

    @Test
    void buscarDelegaEnElDao() {
        Pedido pedido = new Pedido(4, "Desc", "Marca", EstadoPedido.RECIBIDO, 0);
        when(pedidoDao.buscarPorId(4)).thenReturn(pedido);

        assertEquals(pedido, pedidoService.buscar(4));
    }

    @Test
    void avanzarEstadoLanzaExcepcionSiElPedidoNoExiste() {
        when(pedidoDao.buscarPorId(999)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> pedidoService.avanzarEstado(999));
    }

    @Test
    void avanzarEstadoNoValidaStockSiElPedidoNoEstaRecibido() {
        Pedido pedido = new Pedido(5, "Desc", "Marca", EstadoPedido.EN_PREPARACION, 7);
        when(pedidoDao.buscarPorId(5)).thenReturn(pedido);

        Pedido actualizado = pedidoService.avanzarEstado(5);

        assertEquals(EstadoPedido.LISTO, actualizado.getEstado());
        verify(platoDao, org.mockito.Mockito.never()).buscarPorId(org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void avanzarEstadoNoValidaStockSiElPedidoNoTienePlatoAsociado() {
        Pedido pedido = new Pedido(6, "Desc", "Marca", EstadoPedido.RECIBIDO, 0);
        when(pedidoDao.buscarPorId(6)).thenReturn(pedido);

        Pedido actualizado = pedidoService.avanzarEstado(6);

        assertEquals(EstadoPedido.EN_PREPARACION, actualizado.getEstado());
    }

    @Test
    void avanzarEstadoNoHaceNadaSiElPlatoNoExiste() {
        Pedido pedido = new Pedido(7, "Desc", "Marca", EstadoPedido.RECIBIDO, 55);
        when(pedidoDao.buscarPorId(7)).thenReturn(pedido);
        when(platoDao.buscarPorId(55)).thenReturn(null);

        Pedido actualizado = pedidoService.avanzarEstado(7);

        assertEquals(EstadoPedido.EN_PREPARACION, actualizado.getEstado());
    }

    @Test
    void avanzarEstadoAgregaFaltanteConNombreGenericoSiElInsumoNoExiste() {
        Pedido pedido = new Pedido(8, "Desc", "Marca", EstadoPedido.RECIBIDO, 20);
        Plato plato = new Plato(20, "Plato Fantasma", 1, List.of(new IngredientePlato(999, 1, "g")));
        when(pedidoDao.buscarPorId(8)).thenReturn(pedido);
        when(platoDao.buscarPorId(20)).thenReturn(plato);
        when(insumoDao.buscarPorId(999)).thenReturn(null);

        service.StockInsuficienteException excepcion = assertThrows(
                service.StockInsuficienteException.class, () -> pedidoService.avanzarEstado(8));

        assertEquals(1, excepcion.getFaltantes().size());
        assertEquals("insumo #999", excepcion.getFaltantes().get(0));
    }

    @Test
    void moverPedidoEsUnAliasDeAvanzarEstado() {
        Pedido pedido = new Pedido(9, "Desc", "Marca", EstadoPedido.LISTO, 0);
        when(pedidoDao.buscarPorId(9)).thenReturn(pedido);

        Pedido actualizado = pedidoService.moverPedido(9);

        assertEquals(EstadoPedido.ENTREGADO, actualizado.getEstado());
    }

    @Test
    void retrocederEstadoLanzaExcepcionSiElPedidoNoExiste() {
        when(pedidoDao.buscarPorId(111)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> pedidoService.retrocederEstado(111));
    }

    @Test
    void retrocederEstadoMueveDeListoAEnPreparacionYPersiste() {
        Pedido pedido = new Pedido(10, "Desc", "Marca", EstadoPedido.LISTO, 0);
        when(pedidoDao.buscarPorId(10)).thenReturn(pedido);

        Pedido actualizado = pedidoService.retrocederEstado(10);

        assertEquals(EstadoPedido.EN_PREPARACION, actualizado.getEstado());
        verify(pedidoDao).actualizar(pedido);
    }
}
