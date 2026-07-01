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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD: al avanzar un pedido de RECIBIDO a EN_PREPARACION, ademas de validar
 * el stock, se debe descontar automaticamente 1 unidad de cada insumo que
 * compone el plato del pedido y persistir el insumo actualizado.
 */
class DescontarStockAlPrepararTest {

    @Test
    void descuentaUnaUnidadDeCadaInsumoDelPlatoAlPasarAPreparacion() {
        PedidoDao pedidoDao = mock(PedidoDao.class);
        PlatoDao platoDao = mock(PlatoDao.class);
        InsumoDao insumoDao = mock(InsumoDao.class);
        PedidoService service = new PedidoService(pedidoDao, platoDao, insumoDao);

        Pedido pedido = new Pedido(1, "Hamburguesa Clasica - Napoli", "Napoli",
                EstadoPedido.RECIBIDO, 2);
        Plato plato = new Plato(2, "Hamburguesa Clasica", List.of(6, 3, 5));
        Insumo pan = new Insumo(6, "Pan de hamburguesa", "unidades", 120.0, 0.35);
        Insumo pollo = new Insumo(3, "Pechuga de pollo", "kg", 25.0, 4.50);
        Insumo tomate = new Insumo(5, "Tomate", "kg", 22.0, 1.30);

        when(pedidoDao.buscarPorId(1)).thenReturn(pedido);
        when(platoDao.buscarPorId(2)).thenReturn(plato);
        when(insumoDao.buscarPorId(6)).thenReturn(pan);
        when(insumoDao.buscarPorId(3)).thenReturn(pollo);
        when(insumoDao.buscarPorId(5)).thenReturn(tomate);

        Pedido resultado = service.avanzarEstado(1);

        assertEquals(EstadoPedido.EN_PREPARACION, resultado.getEstado());
        assertEquals(119.0, pan.getStock(), 0.0001);
        assertEquals(24.0, pollo.getStock(), 0.0001);
        assertEquals(21.0, tomate.getStock(), 0.0001);
        verify(insumoDao, times(1)).actualizar(pan);
        verify(insumoDao, times(1)).actualizar(pollo);
        verify(insumoDao, times(1)).actualizar(tomate);
        verify(pedidoDao, times(1)).actualizar(pedido);
    }
}
