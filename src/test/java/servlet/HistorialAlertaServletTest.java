package servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.AlertaStock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD (HU11): HistorialAlertaServlet solo lista el historial ya registrado
 * (no registra alertas nuevas, eso lo hace MonitoreoServlet) y reenvia a la
 * vista historial-alertas.jsp. Va en el paquete servlet porque doGet es
 * protected y el servlet no admite mockear el servicio real.
 */
@ExtendWith(MockitoExtension.class)
class HistorialAlertaServletTest {

    private final HistorialAlertaServlet servlet = new HistorialAlertaServlet();

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher dispatcher;

    @Test
    void doGetExponeElHistorialDeAlertasYReenviaALaVista() throws Exception {
        when(request.getRequestDispatcher("/views/historial-alertas.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(request).setAttribute(org.mockito.ArgumentMatchers.eq("alertas"), captor.capture());
        @SuppressWarnings("unchecked")
        List<AlertaStock> alertas = (List<AlertaStock>) captor.getValue();
        assertNotNull(alertas);
        verify(dispatcher).forward(request, response);
    }
}
