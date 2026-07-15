package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ProveedorService;

import java.io.IOException;

/**
 * Controlador de proveedores (F4.1). GET siempre lista los proveedores
 * (listado-proveedores.jsp); el alta y la confirmacion de eliminacion viven
 * en modales in-page de esa misma vista, no en paginas propias. POST procesa
 * "guardar", "actualizar" y "eliminar".
 */
@WebServlet("/proveedores")
public class ProveedorServlet extends HttpServlet {

    private final ProveedorService proveedorService = new ProveedorService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("proveedores", proveedorService.listarProveedores());
        request.getRequestDispatcher("/views/listado-proveedores.jsp").forward(request, response);
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
            } else if ("actualizar".equals(accion)) {
                int id = parsearId(request.getParameter("id"));
                proveedorService.actualizarProveedor(
                        id,
                        request.getParameter("nombre"),
                        request.getParameter("telefono"),
                        request.getParameter("correo"));
                request.getSession().setAttribute("mensaje", "Proveedor actualizado correctamente");
            } else if ("eliminar".equals(accion)) {
                int id = parsearId(request.getParameter("id"));
                proveedorService.eliminarProveedor(id);
                request.getSession().setAttribute("mensaje", "Proveedor eliminado correctamente");
            }
        } catch (RuntimeException ex) {
            // El alta y la confirmacion de eliminacion viven en modales in-page del
            // listado: ante un error siempre se vuelve al listado, que muestra el
            // mensaje via toast (igual que Restaurante y Plato).
            request.getSession().setAttribute("error", ex.getMessage());
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
