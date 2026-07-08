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
 * TDD (F6.2 platos y recetas): PlatoServlet lista, da de alta, edita y
 * elimina platos con su receta (ingredientes insumoId[]/cantidad[]/unidad[])
 * contra el PlatoService real (el servlet crea sus propios servicios, sin
 * inyeccion de dependencias). Usa el restaurante semilla "Napoli" (id 1) y
 * el insumo semilla "Harina de trigo" (id 1) para las recetas de prueba, y
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
    void doGetSinAccionListaPlatosYReenviaAlListado() throws Exception {
        when(request.getParameter("accion")).thenReturn(null);
        when(request.getRequestDispatcher("/views/cu31-listado-platos.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("platos"), notNull());
        verify(request).setAttribute(eq("platoService"), notNull());
        verify(dispatcher).forward(request, response);
    }

    @Test
    void doGetConAccionNuevaCargaCatalogosYReenviaAlAlta() throws Exception {
        when(request.getParameter("accion")).thenReturn("nueva");
        when(request.getRequestDispatcher("/views/cu30-registrar-plato.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("restaurantes"), notNull());
        verify(request).setAttribute(eq("insumos"), notNull());
        verify(dispatcher).forward(request, response);
    }

    @Test
    void doGetEditarConPlatoExistenteCargaCatalogosYElPlato() throws Exception {
        Plato plato = platoService.buscar(1);
        when(request.getParameter("accion")).thenReturn("editar");
        when(request.getParameter("id")).thenReturn("1");
        when(request.getRequestDispatcher("/views/cu32-editar-plato.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("plato", plato);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void doGetEditarConIdInexistenteRedirigeAlListado() throws Exception {
        when(request.getParameter("accion")).thenReturn("editar");
        when(request.getParameter("id")).thenReturn("999999");
        when(request.getParameter("rol")).thenReturn(null);
        when(request.getContextPath()).thenReturn("");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/platos");
    }

    @Test
    void doGetConfirmarEliminarMuestraElPlatoYSuRestaurante() throws Exception {
        when(request.getParameter("accion")).thenReturn("confirmarEliminar");
        when(request.getParameter("id")).thenReturn("2");
        when(request.getRequestDispatcher("/views/cu33-confirmar-eliminar-plato.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("plato"), notNull());
        verify(request).setAttribute(eq("restaurante"), notNull());
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
    void doPostGuardarSinIngredientesDejaErrorYRedirigeAlAlta() throws Exception {
        when(request.getParameter("accion")).thenReturn("guardar");
        when(request.getParameter("nombre")).thenReturn("Plato sin ingredientes " + System.nanoTime());
        when(request.getParameter("restauranteId")).thenReturn("1");
        when(request.getParameterValues("insumoId[]")).thenReturn(null);
        when(request.getParameter("rol")).thenReturn(null);
        when(request.getContextPath()).thenReturn("");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("error", "Debe agregar al menos un insumo con su cantidad y unidad");
        verify(response).sendRedirect("/platos?accion=nueva");
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
