package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Restaurante;
import service.RestauranteService;

import java.io.IOException;

/**
 * Controlador de restaurantes (F6.1). GET sin parametros (o con accion=nueva
 * / accion=editar, que ya no tienen una pagina propia: alta y edicion viven
 * en el modal de cu27) lista los restaurantes (cu27); GET con
 * accion=confirmarEliminar&id= muestra la confirmacion (cu29).
 * POST procesa "guardar", "actualizar" y "eliminar".
 */
@WebServlet("/restaurantes")
public class RestauranteServlet extends HttpServlet {

    private final RestauranteService restauranteService = new RestauranteService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String accion = request.getParameter("accion");
        if ("confirmarEliminar".equals(accion)) {
            Restaurante restaurante = restauranteService.buscar(parsearId(request.getParameter("id")));
            if (restaurante == null) {
                response.sendRedirect(url(request, ""));
                return;
            }
            request.setAttribute("restaurante", restaurante);
            request.getRequestDispatcher("/views/cu29-confirmar-eliminar-restaurante.jsp").forward(request, response);
            return;
        }
        request.setAttribute("restaurantes", restauranteService.listarRestaurantes());
        request.getRequestDispatcher("/views/cu27-listado-restaurantes.jsp").forward(request, response);
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
            // El alta y la edicion viven en el modal de cu27 (con selector de color incluido):
            // ante un error siempre se vuelve al listado, que muestra el mensaje via toast.
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
