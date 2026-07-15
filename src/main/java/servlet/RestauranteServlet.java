package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.RestauranteService;

import java.io.IOException;

/**
 * Controlador de restaurantes (F6.1). GET siempre lista los restaurantes
 * (listado-restaurantes.jsp) sin importar el valor de "accion": el alta, la
 * edicion y la confirmacion de eliminacion viven en modales in-page de esa
 * misma vista, no en paginas propias.
 * POST procesa "guardar", "actualizar" y "eliminar".
 */
@WebServlet("/restaurantes")
public class RestauranteServlet extends HttpServlet {

    private final RestauranteService restauranteService = new RestauranteService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("restaurantes", restauranteService.listarRestaurantes());
        request.getRequestDispatcher("/views/listado-restaurantes.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String accion = request.getParameter("accion");
        String destino = url(request, "");
        try {
            if ("guardar".equals(accion)) {
                restauranteService.registrarRestaurante(
                        request.getParameter("nombre"), request.getParameter("descripcion"), request.getParameter("color"));
                request.getSession().setAttribute("mensaje", "Restaurante registrado correctamente");
            } else if ("actualizar".equals(accion)) {
                int id = parsearId(request.getParameter("id"));
                restauranteService.actualizarRestaurante(
                        id, request.getParameter("nombre"), request.getParameter("descripcion"), request.getParameter("color"));
                request.getSession().setAttribute("mensaje", "Restaurante actualizado correctamente");
            } else if ("eliminar".equals(accion)) {
                int id = parsearId(request.getParameter("id"));
                restauranteService.eliminarRestaurante(id);
                request.getSession().setAttribute("mensaje", "Restaurante eliminado correctamente");
            }
        } catch (RuntimeException ex) {
            // El alta y la edicion viven en el modal del listado (con selector de color
            // incluido): ante un error siempre se vuelve al listado, que muestra el
            // mensaje via toast.
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

    private String url(HttpServletRequest request, String extraParam) {
        String rol = request.getParameter("rol");
        StringBuilder sb = new StringBuilder(request.getContextPath()).append("/restaurantes");
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
