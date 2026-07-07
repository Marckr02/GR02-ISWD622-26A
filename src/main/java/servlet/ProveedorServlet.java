package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Proveedor;
import service.ProveedorService;

import java.io.IOException;

/**
 * Controlador de proveedores (F4.1). GET sin parametros lista los
 * proveedores (cu13); GET con accion=nueva muestra el formulario de alta
 * (cu12); GET con accion=confirmarEliminar muestra la pantalla de
 * confirmacion (cu25). POST procesa "guardar" y "eliminar".
 */
@WebServlet("/proveedores")
public class ProveedorServlet extends HttpServlet {

    private final ProveedorService proveedorService = new ProveedorService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String accion = request.getParameter("accion");
        if ("nueva".equals(accion)) {
            request.getRequestDispatcher("/views/cu12-registrar-proveedor.jsp").forward(request, response);
            return;
        }
        if ("confirmarEliminar".equals(accion)) {
            int id = parsearId(request.getParameter("id"));
            Proveedor proveedor = proveedorService.buscar(id);
            if (proveedor == null) {
                response.sendRedirect(url(request, ""));
                return;
            }
            request.setAttribute("proveedor", proveedor);
            request.getRequestDispatcher("/views/cu25-confirmar-eliminar-proveedor.jsp").forward(request, response);
            return;
        }
        request.setAttribute("proveedores", proveedorService.listarProveedores());
        request.getRequestDispatcher("/views/cu13-listado-proveedores.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String accion = request.getParameter("accion");
        String destino = url(request, "");
        try {
            if ("guardar".equals(accion)) {
                proveedorService.registrarProveedor(
                        request.getParameter("nombre"),
                        request.getParameter("telefono"),
                        request.getParameter("correo"));
                request.getSession().setAttribute("mensaje", "Proveedor registrado correctamente");
            } else if ("eliminar".equals(accion)) {
                int id = parsearId(request.getParameter("id"));
                proveedorService.eliminarProveedor(id);
                request.getSession().setAttribute("mensaje", "Proveedor eliminado correctamente");
            }
        } catch (RuntimeException ex) {
            request.getSession().setAttribute("error", ex.getMessage());
            if ("guardar".equals(accion)) {
                destino = url(request, "accion=nueva");
            }
        }
        response.sendRedirect(destino);
    }

    private int parsearId(String valor) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException | NullPointerException ex) {
            return -1;
        }
    }

    /** Construye la URL de /proveedores conservando el rol y agregando parametros extra. */
    private String url(HttpServletRequest request, String extraParam) {
        String rol = request.getParameter("rol");
        StringBuilder sb = new StringBuilder(request.getContextPath()).append("/proveedores");
        StringBuilder qs = new StringBuilder();
        if (rol != null && !rol.isEmpty()) {
            qs.append("rol=").append(rol);
        }
        if (!extraParam.isEmpty()) {
            if (qs.length() > 0) {
                qs.append("&");
            }
            qs.append(extraParam);
        }
        if (qs.length() > 0) {
            sb.append("?").append(qs);
        }
        return sb.toString();
    }
}
