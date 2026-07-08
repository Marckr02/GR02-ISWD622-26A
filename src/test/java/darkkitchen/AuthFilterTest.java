package darkkitchen;

import config.AuthFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD (control de acceso por rol): AuthFilter permite el paso solo al rol
 * dueño de cada seccion sensible, resuelve el rol vigente desde el parametro
 * "rol" o, en su defecto, desde la sesion, y reenvia a acceso-denegado.jsp
 * cuando el rol no tiene permiso o no hay sesion.
 */
@ExtendWith(MockitoExtension.class)
class AuthFilterTest {

    private final AuthFilter filtro = new AuthFilter();

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Mock
    private HttpSession session;

    @Mock
    private RequestDispatcher dispatcher;

    @ParameterizedTest
    @CsvSource({
            "/insumos, ADMIN_BODEGA",
            "/insumos/crear, ADMIN_BODEGA",
            "/menu, ADMIN_BODEGA",
            "/monitoreo, ADMIN_BODEGA",
            "/proveedores, ADMIN_BODEGA",
            "/disponibilidad, COCINERO",
            "/restaurantes, ADMINISTRADOR",
            "/platos, ADMINISTRADOR",
            "/alertas, ADMINISTRADOR"
    })
    void permiteElPasoCuandoElRolDelParametroCorrespondeALaRuta(String ruta, String rol) throws Exception {
        when(request.getServletPath()).thenReturn(ruta);
        when(request.getParameter("rol")).thenReturn(rol);
        when(request.getSession()).thenReturn(session);

        filtro.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(session).setAttribute("rol", rol);
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @ParameterizedTest
    @CsvSource({
            "/insumos, COCINERO",
            "/disponibilidad, ADMINISTRADOR",
            "/restaurantes, ADMIN_BODEGA"
    })
    void reenviaAAccesoDenegadoCuandoElRolNoTienePermisoSobreLaRuta(String ruta, String rol) throws Exception {
        when(request.getServletPath()).thenReturn(ruta);
        when(request.getParameter("rol")).thenReturn(rol);
        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher("/views/acceso-denegado.jsp")).thenReturn(dispatcher);

        filtro.doFilter(request, response, chain);

        verify(request).setAttribute("mensajeAcceso", "No tienes permisos para acceder a esta seccion");
        verify(dispatcher).forward(request, response);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void resuelveElRolDesdeLaSesionCuandoNoHayParametroRol() throws Exception {
        when(request.getServletPath()).thenReturn("/monitoreo");
        when(request.getParameter("rol")).thenReturn(null);
        when(request.getSession(false)).thenReturn(session);
        lenient().when(request.getSession()).thenReturn(session);
        when(session.getAttribute("rol")).thenReturn("ADMIN_BODEGA");

        filtro.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void sinSesionYSinParametroSeConsideraSinPermisoYSeReenviaADenegado() throws Exception {
        when(request.getServletPath()).thenReturn("/menu");
        when(request.getParameter("rol")).thenReturn(null);
        when(request.getSession(false)).thenReturn(null);
        when(request.getRequestDispatcher("/views/acceso-denegado.jsp")).thenReturn(dispatcher);

        filtro.doFilter(request, response, chain);

        verify(dispatcher).forward(request, response);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void unaRutaNoProtegidaComoElTableroDePedidosAdmiteCualquierRol() throws Exception {
        when(request.getServletPath()).thenReturn("/pedidos");
        when(request.getParameter("rol")).thenReturn("COCINERO");
        when(request.getSession()).thenReturn(session);

        filtro.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}
