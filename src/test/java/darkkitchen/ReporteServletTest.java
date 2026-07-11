package servlet;

import dao.PedidoDao;
import dao.PlatoDao;
import dao.RestauranteDao;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.EstadoPedido;
import model.IngredientePlato;
import model.Pedido;
import model.Plato;
import model.Restaurante;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD (F5.2, HU39): ReporteServlet contra ReporteService/RestauranteService
 * reales (el servlet los crea internamente, sin inyeccion posible). Cada
 * prueba crea su propio restaurante con nombre unico, para no depender de
 * los datos de ejemplo (que pueden cambiar).
 */
@ExtendWith(MockitoExtension.class)
class ReporteServletTest {

    private final ReporteServlet servlet = new ReporteServlet();

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;

    @Test
    void doPostSinRestauranteIdRedirigeConMensajeDeError() throws Exception {
        when(request.getParameter("restauranteId")).thenReturn(null);
        when(request.getParameter("rol")).thenReturn(null);
        when(request.getContextPath()).thenReturn("");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("error", "Debe seleccionar un restaurante y ver sus métricas antes de exportar");
        verify(response).sendRedirect("/metricas");
    }

    @Test
    void doPostRestauranteSinPedidosEntregadosRedirigeConMensajeDeError() throws Exception {
        // Restaurante propio, recien creado: nunca tuvo pedidos ENTREGADO.
        Restaurante restaurante = new RestauranteDao().guardar(
                new Restaurante(0, "Restaurante sin pedidos test " + System.nanoTime(), ""));

        when(request.getParameter("restauranteId")).thenReturn(String.valueOf(restaurante.getId()));
        when(request.getParameter("rol")).thenReturn(null);
        when(request.getContextPath()).thenReturn("");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("error", "No hay datos suficientes para generar el reporte de este restaurante");
        verify(response).sendRedirect("/metricas?restauranteId=" + restaurante.getId());
    }

    @Test
    void doPostConDatosSuficientesDescargaElPdf() throws Exception {
        Restaurante restaurante = new RestauranteDao().guardar(
                new Restaurante(0, "Restaurante reporte test " + System.nanoTime(), ""));
        Plato plato = new PlatoDao().guardar(new Plato(0, "Plato reporte test " + System.nanoTime(),
                restaurante.getId(), List.of(new IngredientePlato(1, 100, "g"))));
        new PedidoDao().guardar(new Pedido(0, "Pedido reporte test", restaurante.getNombre(),
                EstadoPedido.ENTREGADO, plato.getId()));

        when(request.getParameter("restauranteId")).thenReturn(String.valueOf(restaurante.getId()));
        ByteArrayOutputStream salidaCapturada = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(streamQueEscribeEn(salidaCapturada));

        servlet.doPost(request, response);

        verify(response).setContentType("application/pdf");
        verify(response).setHeader(org.mockito.ArgumentMatchers.eq("Content-Disposition"),
                org.mockito.ArgumentMatchers.contains("reporte-metricas-"));
        assertTrue(salidaCapturada.size() > 0);
    }

    private ServletOutputStream streamQueEscribeEn(ByteArrayOutputStream destino) {
        return new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
            }

            @Override
            public void write(int b) throws IOException {
                destino.write(b);
            }
        };
    }
}
