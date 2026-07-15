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
 * TDD (F4.1 / F4.2 proveedores): ProveedorServlet lista proveedores y procesa
 * "guardar"/"actualizar"/"eliminar" contra el ProveedorService real (el
 * servlet no admite inyeccion de dependencias); el alta y la confirmacion de
 * eliminacion viven en modales in-page del listado, no en paginas propias.
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
    void doGetListaProveedoresYReenviaAlListado() throws Exception {
        // El alta y la confirmacion de eliminacion ahora viven solo en modales
        // in-page del listado: ya no existen paginas propias (cu12/cu25), asi
        // que doGet ni siquiera lee "accion".
        when(request.getRequestDispatcher("/views/listado-proveedores.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("proveedores"), notNull());
        verify(dispatcher).forward(request, response);
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
        verify(response).sendRedirect("/proveedores");
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
