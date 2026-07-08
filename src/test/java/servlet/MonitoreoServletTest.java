package servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD (HU9): MonitoreoServlet reune los insumos criticos (registrando su
 * alerta en el historial, HU11) y el estado del menu, y reenvia al panel de
 * monitoreo. Como el servlet crea sus propios servicios (sin inyeccion de
 * dependencias), la prueba corre contra el inventario semilla real y solo
 * mockea la API de Servlet.
 */
@ExtendWith(MockitoExtension.class)
class MonitoreoServletTest {

    private final MonitoreoServlet servlet = new MonitoreoServlet();

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher dispatcher;

    @Test
    void doGetExponeInsumosCriticosYMenuYReenviaAlPanel() throws Exception {
        when(request.getRequestDispatcher("/views/cu9-panel-monitoreo.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("criticos"), notNull());
        verify(request).setAttribute(eq("menu"), notNull());
        verify(dispatcher).forward(request, response);
    }
}
