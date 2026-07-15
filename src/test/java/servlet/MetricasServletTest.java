package servlet;

import dao.PedidoDao;
import dao.PlatoDao;
import dao.RestauranteDao;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.EstadoPedido;
import model.IngredientePlato;
import model.Pedido;
import model.Plato;
import model.Restaurante;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD (F5.1, HU35-HU38): MetricasServlet resuelve el flujo de la vista de
 * metricas contra el MetricasService/RestauranteService reales (el servlet
 * los crea internamente, sin inyeccion posible). Cada prueba que necesita
 * un restaurante con datos reales crea el suyo propio con nombre unico,
 * para no depender de los datos de ejemplo (que pueden cambiar).
 */
@ExtendWith(MockitoExtension.class)
class MetricasServletTest {

    private final MetricasServlet servlet = new MetricasServlet();

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private RequestDispatcher dispatcher;

    @Test
    void doGetSinParametrosSoloMuestraElSelectorSinMensajeDeError() throws Exception {
        when(request.getParameter("restauranteId")).thenReturn(null);
        when(request.getParameter("buscar")).thenReturn(null);
        when(request.getRequestDispatcher("/views/metricas-restaurante.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("restaurantes"), notNull());
        verify(request, never()).setAttribute(eq("sinSeleccion"), notNull());
        verify(dispatcher).forward(request, response);
    }

    @Test
    void doGetConBuscarYSinRestauranteIdMarcaSinSeleccion() throws Exception {
        when(request.getParameter("restauranteId")).thenReturn(null);
        when(request.getParameter("buscar")).thenReturn("1");
        when(request.getRequestDispatcher("/views/metricas-restaurante.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("sinSeleccion", Boolean.TRUE);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void doGetConRestauranteIdInvalidoMarcaSinSeleccion() throws Exception {
        when(request.getParameter("restauranteId")).thenReturn("no-es-un-numero");
        when(request.getParameter("buscar")).thenReturn(null);
        when(request.getRequestDispatcher("/views/metricas-restaurante.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("sinSeleccion", Boolean.TRUE);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void doGetConRestauranteIdEspecificoCalculaSusMetricas() throws Exception {
        Restaurante restaurante = new RestauranteDao().guardar(
                new Restaurante(0, "Restaurante metricas test " + System.nanoTime(), ""));
        Plato plato = new PlatoDao().guardar(new Plato(0, "Plato metricas test " + System.nanoTime(),
                restaurante.getId(), List.of(new IngredientePlato(1, 100, "g"))));
        new PedidoDao().guardar(new Pedido(0, "Pedido metricas test", restaurante.getNombre(),
                EstadoPedido.ENTREGADO, plato.getId()));

        when(request.getParameter("restauranteId")).thenReturn(String.valueOf(restaurante.getId()));
        when(request.getParameter("buscar")).thenReturn(null);
        when(request.getRequestDispatcher("/views/metricas-restaurante.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("restauranteIdSeleccionado", restaurante.getId());
        verify(request).setAttribute("vistaGeneral", false);
        verify(request).setAttribute("nombreRestauranteSeleccionado", restaurante.getNombre());
        verify(request).setAttribute(eq("platosMasVendidos"), notNull());
        verify(request).setAttribute(eq("insumosMasUtilizados"), notNull());
        verify(request).setAttribute(eq("totalPedidos"), notNull());
        verify(dispatcher).forward(request, response);
    }

    @Test
    void doGetConRestauranteIdCeroMuestraLaVistaGeneral() throws Exception {
        when(request.getParameter("restauranteId")).thenReturn("0");
        when(request.getParameter("buscar")).thenReturn(null);
        when(request.getRequestDispatcher("/views/metricas-restaurante.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("vistaGeneral", true);
        verify(request).setAttribute(eq("totalesPorRestaurante"), notNull());
        verify(dispatcher).forward(request, response);
    }
}
