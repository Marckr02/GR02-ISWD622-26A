package servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Plato;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.PlatoService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD (F6.2 platos y recetas): PlatoServlet lista platos y procesa
 * "guardar"/"actualizar"/"eliminar" con su receta (ingredientes
 * insumoId[]/cantidad[]/unidad[]) contra el PlatoService real (el servlet
 * crea sus propios servicios, sin inyeccion de dependencias); el alta, la
 * edicion y la confirmacion de eliminacion viven en modales in-page del
 * listado, no en paginas propias. Usa el restaurante semilla "Napoli" (id 1)
 * y el insumo semilla "Harina de trigo" (id 1) para las recetas de prueba, y
 * nombres de plato unicos para no chocar con la semilla ni con otras pruebas.
 */
@ExtendWith(MockitoExtension.class)
class PlatoServletTest {

    private final PlatoServlet servlet = new PlatoServlet();
    private final PlatoService platoService = new PlatoService();

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher dispatcher;

    @Mock
    private HttpSession session;

    @Test
    void doGetListaPlatosCargaCatalogosYReenviaAlListado() throws Exception {
        // El alta, la edicion y la confirmacion de eliminacion viven solo en
        // modales in-page del listado: ya no existen paginas propias (cu30/cu32/
        // cu33 fueron retiradas), asi que doGet ni siquiera lee "accion" ni "id".
        when(request.getRequestDispatcher("/views/listado-platos.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("platos"), notNull());
        verify(request).setAttribute(eq("platoService"), notNull());
        verify(request).setAttribute(eq("restaurantes"), notNull());
        verify(request).setAttribute(eq("insumos"), notNull());
        verify(dispatcher).forward(request, response);
    }

    @Test
    void doPostGuardarPlatoValidoConUnIngredienteDejaMensajeYRedirige() throws Exception {
        when(request.getParameter("accion")).thenReturn("guardar");
        when(request.getParameter("nombre")).thenReturn("Plato prueba " + System.nanoTime());
        when(request.getParameter("restauranteId")).thenReturn("1");
        when(request.getParameterValues("insumoId[]")).thenReturn(new String[]{"1"});
        when(request.getParameterValues("cantidad[]")).thenReturn(new String[]{"100"});
        when(request.getParameterValues("unidad[]")).thenReturn(new String[]{"g"});
        when(request.getParameter("rol")).thenReturn(null);
        when(request.getContextPath()).thenReturn("");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("mensaje", "Plato registrado correctamente");
        verify(response).sendRedirect("/platos");
    }

    @Test
    void doPostGuardarSinIngredientesDejaErrorYRedirigeAlListado() throws Exception {
        when(request.getParameter("accion")).thenReturn("guardar");
        when(request.getParameter("nombre")).thenReturn("Plato sin ingredientes " + System.nanoTime());
        when(request.getParameter("restauranteId")).thenReturn("1");
        when(request.getParameterValues("insumoId[]")).thenReturn(null);
        when(request.getParameter("rol")).thenReturn(null);
        when(request.getContextPath()).thenReturn("");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("error", "Debe agregar al menos un insumo con su cantidad y unidad");
        verify(response).sendRedirect("/platos");
    }

    @Test
    void doPostActualizarPlatoExistenteDejaMensajeYRedirige() throws Exception {
        Plato existente = platoService.registrarPlato("Plato a actualizar " + System.nanoTime(), 1,
                java.util.List.of(new model.IngredientePlato(1, 50, "g")));

        when(request.getParameter("accion")).thenReturn("actualizar");
        when(request.getParameter("id")).thenReturn(String.valueOf(existente.getId()));
        when(request.getParameter("nombre")).thenReturn("Plato actualizado " + System.nanoTime());
        when(request.getParameter("restauranteId")).thenReturn("1");
        when(request.getParameterValues("insumoId[]")).thenReturn(new String[]{"1"});
        when(request.getParameterValues("cantidad[]")).thenReturn(new String[]{"200"});
        when(request.getParameterValues("unidad[]")).thenReturn(new String[]{"g"});
        when(request.getParameter("rol")).thenReturn(null);
        when(request.getContextPath()).thenReturn("");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("mensaje", "Plato actualizado correctamente");
        verify(response).sendRedirect("/platos");
    }

    @Test
    void doPostEliminarPlatoExistenteDejaMensajeYRedirige() throws Exception {
        Plato existente = platoService.registrarPlato("Plato a eliminar " + System.nanoTime(), 1,
                java.util.List.of(new model.IngredientePlato(1, 20, "g")));

        when(request.getParameter("accion")).thenReturn("eliminar");
        when(request.getParameter("id")).thenReturn(String.valueOf(existente.getId()));
        when(request.getParameterValues("insumoId[]")).thenReturn(null);
        when(request.getParameter("restauranteId")).thenReturn(null);
        when(request.getParameter("rol")).thenReturn(null);
        when(request.getContextPath()).thenReturn("");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("mensaje", "Plato eliminado correctamente");
        verify(response).sendRedirect("/platos");
        org.junit.jupiter.api.Assertions.assertNull(platoService.buscar(existente.getId()));
    }
}
