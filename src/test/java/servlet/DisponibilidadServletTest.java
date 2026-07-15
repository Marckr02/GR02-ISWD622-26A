package servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.DisponibilidadPlato;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD (HU10): DisponibilidadServlet sincroniza el menu con el inventario real
 * (sin mocks de servicio, ya que el servlet no admite inyeccion de
 * dependencias) y reenvia a la vista del cocinero con el menu como atributo.
 * Se ubica en el paquete servlet porque doGet es protected.
 */
@ExtendWith(MockitoExtension.class)
class DisponibilidadServletTest {

    private final DisponibilidadServlet servlet = new DisponibilidadServlet();

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher dispatcher;

    @Test
    void doGetCalculaElMenuYReenviaALaVistaDelCocinero() throws Exception {
        when(request.getRequestDispatcher("/views/disponibilidad-cocinero.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(request).setAttribute(org.mockito.ArgumentMatchers.eq("menu"), captor.capture());
        @SuppressWarnings("unchecked")
        List<DisponibilidadPlato> menu = (List<DisponibilidadPlato>) captor.getValue();
        assertFalse(menu.isEmpty());
        verify(dispatcher).forward(request, response);
    }
}
