package servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Proveedor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.ProveedorService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD (F4.1 / F4.2 proveedores): ProveedorServlet lista, da de alta, muestra
 * confirmacion de eliminacion y procesa "guardar"/"eliminar" contra el
 * ProveedorService real (el servlet no admite inyeccion de dependencias).
 * Cada prueba usa nombres unicos para no chocar con otras pruebas que
 * comparten el mismo almacen estatico en memoria.
 */
@ExtendWith(MockitoExtension.class)
class ProveedorServletTest {

    private final ProveedorServlet servlet = new ProveedorServlet();
    private final ProveedorService proveedorService = new ProveedorService();

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher dispatcher;

    @Mock
    private HttpSession session;

    @Test
    void doGetSinAccionListaProveedoresYReenviaAlListado() throws Exception {
        when(request.getParameter("accion")).thenReturn(null);
        when(request.getRequestDispatcher("/views/cu13-listado-proveedores.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("proveedores"), notNull());
        verify(dispatcher).forward(request, response);
    }

    @Test
    void doGetConAccionNuevaReenviaAlFormularioDeAlta() throws Exception {
        when(request.getParameter("accion")).thenReturn("nueva");
        when(request.getRequestDispatcher("/views/cu12-registrar-proveedor.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(dispatcher).forward(request, response);
    }

    @Test
    void doGetConfirmarEliminarConProveedorExistenteMuestraConfirmacion() throws Exception {
        Proveedor proveedor = proveedorService.registrarProveedor(
                "Proveedor prueba " + System.nanoTime(), "0999999999", "prueba@correo.com");
        when(request.getParameter("accion")).thenReturn("confirmarEliminar");
        when(request.getParameter("id")).thenReturn(String.valueOf(proveedor.getId()));
        when(request.getRequestDispatcher("/views/cu25-confirmar-eliminar-proveedor.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("proveedor", proveedor);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void doGetConfirmarEliminarConIdInexistenteRedirigeAlListado() throws Exception {
        when(request.getParameter("accion")).thenReturn("confirmarEliminar");
        when(request.getParameter("id")).thenReturn("999999");
        when(request.getParameter("rol")).thenReturn(null);
        when(request.getContextPath()).thenReturn("");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/proveedores");
    }

    @Test
    void doPostGuardarProveedorValidoDejaMensajeYRedirigeAlListado() throws Exception {
        when(request.getParameter("accion")).thenReturn("guardar");
        when(request.getParameter("nombre")).thenReturn("Proveedor nuevo " + System.nanoTime());
        when(request.getParameter("telefono")).thenReturn("0987001122");
        when(request.getParameter("correo")).thenReturn("nuevo@correo.com");
        when(request.getParameter("rol")).thenReturn(null);
        when(request.getContextPath()).thenReturn("");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("mensaje", "Proveedor registrado correctamente");
        verify(response).sendRedirect("/proveedores");
    }

    @Test
    void doPostGuardarConTelefonoInvalidoDejaErrorYRedirigeAlFormularioDeAlta() throws Exception {
        when(request.getParameter("accion")).thenReturn("guardar");
        when(request.getParameter("nombre")).thenReturn("Proveedor invalido " + System.nanoTime());
        when(request.getParameter("telefono")).thenReturn("abc");
        when(request.getParameter("correo")).thenReturn("correo@valido.com");
        when(request.getParameter("rol")).thenReturn(null);
        when(request.getContextPath()).thenReturn("");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("error",
                "El telefono debe contener solo numeros, entre 7 y 15 digitos");
        verify(response).sendRedirect("/proveedores?accion=nueva");
    }

    @Test
    void doPostEliminarProveedorSinVinculosDejaMensajeYRedirige() throws Exception {
        Proveedor proveedor = proveedorService.registrarProveedor(
                "Proveedor a eliminar " + System.nanoTime(), "0988001122", "eliminar@correo.com");
        when(request.getParameter("accion")).thenReturn("eliminar");
        when(request.getParameter("id")).thenReturn(String.valueOf(proveedor.getId()));
        when(request.getParameter("rol")).thenReturn(null);
        when(request.getContextPath()).thenReturn("");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("mensaje", "Proveedor eliminado correctamente");
        verify(response).sendRedirect("/proveedores");
        org.junit.jupiter.api.Assertions.assertNull(proveedorService.buscar(proveedor.getId()));
    }

    @Test
    void doPostConAccionDesconocidaSoloRedirigeConservandoElRol() throws Exception {
        when(request.getParameter("accion")).thenReturn("otra");
        when(request.getParameter("rol")).thenReturn("administrador");
        when(request.getContextPath()).thenReturn("");

        servlet.doPost(request, response);

        verify(response).sendRedirect("/proveedores?rol=administrador");
    }
}
