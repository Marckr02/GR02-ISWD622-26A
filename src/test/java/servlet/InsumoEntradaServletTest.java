package servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Insumo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.InsumoService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD (F3 inventario / HU21 / HU23 / HU34 / HU6): InsumoEntradaServlet lista
 * el inventario real en GET y en POST procesa "registrar", "reducir",
 * "crear", "editar", "actualizarMinimo" y "vincularProveedor", dejando
 * mensaje o error en sesion. Como el servlet crea su propia instancia de
 * InsumoService (sin inyeccion de dependencias), la prueba corre contra el
 * inventario semilla real, creando insumos propios con nombres unicos para
 * no interferir con otras pruebas.
 */
@ExtendWith(MockitoExtension.class)
class InsumoEntradaServletTest {

    private final InsumoEntradaServlet servlet = new InsumoEntradaServlet();
    private final InsumoService insumoService = new InsumoService();

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher dispatcher;

    @Mock
    private HttpSession session;

    @Test
    void doGetListaInsumosYProveedoresYReenviaAlListado() throws Exception {
        when(request.getServletPath()).thenReturn("/insumos");
        when(request.getRequestDispatcher("/views/cu3-insumos-entrada.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(request).setAttribute(eq("insumos"), captor.capture());
        @SuppressWarnings("unchecked")
        List<Insumo> insumos = (List<Insumo>) captor.getValue();
        assertTrue(insumos.size() >= 8);
        verify(request).setAttribute(eq("proveedores"), notNull());
        verify(request).setAttribute(eq("proveedorPorInsumo"), notNull());
        verify(dispatcher).forward(request, response);
    }

    @Test
    void doGetConAccionCrearReenviaAlFormularioDeAlta() throws Exception {
        when(request.getServletPath()).thenReturn("/insumos/crear");
        when(request.getRequestDispatcher("/views/cu23-crear-insumo.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(dispatcher).forward(request, response);
    }

    @Test
    void doPostCrearInsumoDejaMensajeDeExitoYRedirigeAlFormularioDeAlta() throws Exception {
        when(request.getParameter("accion")).thenReturn("crear");
        when(request.getParameter("nombre")).thenReturn("Insumo prueba crear " + System.nanoTime());
        when(request.getParameter("unidad")).thenReturn("kg");
        when(request.getContextPath()).thenReturn("");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("mensaje", "Insumo creado correctamente");
        verify(response).sendRedirect("/insumos/crear");
    }

    @Test
    void doPostCrearInsumoConNombreDuplicadoDejaErrorYRedirigeAlFormulario() throws Exception {
        String nombre = "Insumo duplicado " + System.nanoTime();
        insumoService.crearInsumo(nombre, "kg");

        when(request.getParameter("accion")).thenReturn("crear");
        when(request.getParameter("nombre")).thenReturn(nombre);
        when(request.getParameter("unidad")).thenReturn("kg");
        when(request.getContextPath()).thenReturn("");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("error", "Ya existe un insumo con ese nombre");
        verify(response).sendRedirect("/insumos/crear");
    }

    @Test
    void doPostRegistrarEntradaSimplificadaDejaMensajeYRedirigeAInsumos() throws Exception {
        Insumo insumo = insumoService.crearInsumo("Insumo prueba registrar " + System.nanoTime(), "kg");

        when(request.getParameter("accion")).thenReturn("registrar");
        when(request.getParameter("insumoId")).thenReturn(String.valueOf(insumo.getId()));
        when(request.getParameter("cantidad")).thenReturn("5");
        when(request.getContextPath()).thenReturn("");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("mensaje", "Entrada registrada correctamente");
        verify(response).sendRedirect("/insumos");
        assertEquals(5.0, insumoService.buscar(insumo.getId()).getStock(), 0.0001);
    }

    @Test
    void doPostReducirStockSinSeleccionarInsumoDejaErrorDeSeleccion() throws Exception {
        when(request.getParameter("accion")).thenReturn("reducir");
        when(request.getParameter("insumoId")).thenReturn("");
        when(request.getContextPath()).thenReturn("");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("error", "Debe seleccionar un insumo de la lista");
        verify(response).sendRedirect("/insumos");
    }

    @Test
    void doPostVincularProveedorConProveedorNoNumericoDejaMensajeDeSeleccion() throws Exception {
        Insumo insumo = insumoService.crearInsumo("Insumo prueba vincular " + System.nanoTime(), "kg");

        when(request.getParameter("accion")).thenReturn("vincularProveedor");
        when(request.getParameter("insumoId")).thenReturn(String.valueOf(insumo.getId()));
        when(request.getParameter("proveedorId")).thenReturn("no-numero");
        when(request.getContextPath()).thenReturn("");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("error", "Debe seleccionar un proveedor de la lista");
        verify(response).sendRedirect("/insumos");
    }

    @Test
    void doPostActualizarMinimoConValorInvalidoDejaMensajeGenerico() throws Exception {
        Insumo insumo = insumoService.crearInsumo("Insumo prueba minimo " + System.nanoTime(), "kg");

        when(request.getParameter("accion")).thenReturn("actualizarMinimo");
        when(request.getParameter("insumoId")).thenReturn(String.valueOf(insumo.getId()));
        when(request.getParameter("stockMinimo")).thenReturn("-3");
        when(request.getContextPath()).thenReturn("");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute(eq("error"), anyString());
        verify(response).sendRedirect("/insumos");
    }
}
