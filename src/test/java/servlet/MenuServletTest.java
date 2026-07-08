package servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD (HU7): MenuServlet recalcula la disponibilidad del menu en GET y en
 * POST simplemente redirige de vuelta a /menu para refrescar la vista tras
 * una entrada de stock (la sincronizacion real siempre ocurre en el GET).
 */
@ExtendWith(MockitoExtension.class)
class MenuServletTest {

    private final MenuServlet servlet = new MenuServlet();

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher dispatcher;

    @Test
    void doGetExponeElMenuSincronizadoYReenviaALaVista() throws Exception {
        when(request.getRequestDispatcher("/views/cu7-menu-disponibilidad.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("menu"), org.mockito.ArgumentMatchers.notNull());
        verify(dispatcher).forward(request, response);
    }

    @Test
    void doPostSiempreRedirigeAMenuParaRefrescarLaVista() throws Exception {
        when(request.getContextPath()).thenReturn("/darkkitchen");

        servlet.doPost(request, response);

        verify(response).sendRedirect("/darkkitchen/menu");
    }
}
