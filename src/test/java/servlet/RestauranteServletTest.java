package servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Restaurante;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.RestauranteService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD (F6.1 restaurantes): RestauranteServlet lista, da de alta, edita y
 * elimina restaurantes contra el RestauranteService real (el servlet crea
 * sus propios servicios, sin inyeccion de dependencias posible). Los
 * restaurantes creados aqui usan nombres unicos para no interferir con la
 * semilla compartida (Napoli, Burger Lab, Sakura, El Fogon) ni con otras
 * pruebas que corren en el mismo almacen estatico.
 */
@ExtendWith(MockitoExtension.class)
class RestauranteServletTest {

    private final RestauranteServlet servlet = new RestauranteServlet();
    private final RestauranteService restauranteService = new RestauranteService();

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher dispatcher;

    @Mock
    private HttpSession session;

    @Test
    void doGetListaRestaurantesYReenviaAlListado() throws Exception {
        // El alta, la edicion y la confirmacion de eliminacion viven solo en
        // modales in-page del listado: ya no existen paginas propias (cu12/cu25/
        // cu29 fueron retiradas), asi que doGet ni siquiera lee "accion".
        when(request.getRequestDispatcher("/views/listado-restaurantes.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("restaurantes"), notNull());
        verify(dispatcher).forward(request, response);
    }

    @Test
    void doPostGuardarRestauranteValidoDejaMensajeYRedirige() throws Exception {
        when(request.getParameter("accion")).thenReturn("guardar");
        when(request.getParameter("nombre")).thenReturn("Restaurante nuevo " + System.nanoTime());
        when(request.getParameter("descripcion")).thenReturn("Una descripcion cualquiera");
        when(request.getParameter("rol")).thenReturn(null);
        when(request.getContextPath()).thenReturn("");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("mensaje", "Restaurante registrado correctamente");
        verify(response).sendRedirect("/restaurantes");
    }

    @Test
    void doPostGuardarConNombreVacioDejaErrorYRedirigeAlListado() throws Exception {
        when(request.getParameter("accion")).thenReturn("guardar");
        when(request.getParameter("nombre")).thenReturn("   ");
        when(request.getParameter("descripcion")).thenReturn("");
        when(request.getParameter("rol")).thenReturn(null);
        when(request.getContextPath()).thenReturn("");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("error", "El nombre no puede estar vacio");
        verify(response).sendRedirect("/restaurantes");
    }

    @Test
    void doPostActualizarRestauranteExistenteDejaMensajeYRedirige() throws Exception {
        Restaurante restaurante = restauranteService.registrarRestaurante(
                "Restaurante a actualizar " + System.nanoTime(), "");
        when(request.getParameter("accion")).thenReturn("actualizar");
        when(request.getParameter("id")).thenReturn(String.valueOf(restaurante.getId()));
        when(request.getParameter("nombre")).thenReturn("Restaurante actualizado " + System.nanoTime());
        when(request.getParameter("descripcion")).thenReturn("Nueva descripcion");
        when(request.getParameter("rol")).thenReturn(null);
        when(request.getContextPath()).thenReturn("");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("mensaje", "Restaurante actualizado correctamente");
        verify(response).sendRedirect("/restaurantes");
    }

    @Test
    void doPostEliminarRestauranteSinPlatosAsociadosDejaMensajeYRedirige() throws Exception {
        Restaurante restaurante = restauranteService.registrarRestaurante(
                "Restaurante a eliminar " + System.nanoTime(), "");
        when(request.getParameter("accion")).thenReturn("eliminar");
        when(request.getParameter("id")).thenReturn(String.valueOf(restaurante.getId()));
        when(request.getParameter("rol")).thenReturn(null);
        when(request.getContextPath()).thenReturn("");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("mensaje", "Restaurante eliminado correctamente");
        verify(response).sendRedirect("/restaurantes");
        org.junit.jupiter.api.Assertions.assertNull(restauranteService.buscar(restaurante.getId()));
    }
}
