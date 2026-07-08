package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.IngredientePlato;
import model.Plato;
import service.PlatoService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de platos y recetas (F6.2). GET sin parametros lista los
 * platos (cu31); GET con accion=nueva muestra el alta (cu30); GET con
 * accion=editar&id= muestra la edicion (cu32); GET con
 * accion=confirmarEliminar&id= muestra la confirmacion (cu33).
 * POST procesa "guardar", "actualizar" y "eliminar". Los ingredientes de
 * la receta llegan como parametros repetidos insumoId[], cantidad[] y
 * unidad[] (una posicion por fila del formulario dinamico).
 */
@WebServlet("/platos")
public class PlatoServlet extends HttpServlet {

    private final PlatoService platoService = new PlatoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String accion = request.getParameter("accion");
        if ("nueva".equals(accion)) {
            cargarCatalogos(request);
            request.getRequestDispatcher("/views/cu30-registrar-plato.jsp").forward(request, response);
            return;
        }
        if ("editar".equals(accion)) {
            Plato plato = platoService.buscar(parsearId(request.getParameter("id")));
            if (plato == null) {
                response.sendRedirect(url(request, ""));
                return;
            }
            cargarCatalogos(request);
            request.setAttribute("plato", plato);
            request.getRequestDispatcher("/views/cu32-editar-plato.jsp").forward(request, response);
            return;
        }
        if ("confirmarEliminar".equals(accion)) {
            Plato plato = platoService.buscar(parsearId(request.getParameter("id")));
            if (plato == null) {
                response.sendRedirect(url(request, ""));
                return;
            }
            request.setAttribute("plato", plato);
            request.setAttribute("restaurante", platoService.restauranteDe(plato));
            request.getRequestDispatcher("/views/cu33-confirmar-eliminar-plato.jsp").forward(request, response);
            return;
        }
        request.setAttribute("platos", platoService.listarPlatos());
        request.setAttribute("platoService", platoService);
        request.getRequestDispatcher("/views/cu31-listado-platos.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String accion = request.getParameter("accion");
        String destino = url(request, "");
        try {
            List<IngredientePlato> ingredientes = leerIngredientes(request);
            Integer restauranteId = parsearRestauranteId(request.getParameter("restauranteId"));
            if ("guardar".equals(accion)) {
                platoService.registrarPlato(request.getParameter("nombre"), restauranteId, ingredientes);
                request.getSession().setAttribute("mensaje", "Plato registrado correctamente");
            } else if ("actualizar".equals(accion)) {
                int id = parsearId(request.getParameter("id"));
                platoService.actualizarPlato(id, request.getParameter("nombre"), restauranteId, ingredientes);
                request.getSession().setAttribute("mensaje", "Plato actualizado correctamente");
            } else if ("eliminar".equals(accion)) {
                int id = parsearId(request.getParameter("id"));
                platoService.eliminarPlato(id);
                request.getSession().setAttribute("mensaje", "Plato eliminado correctamente");
            }
        } catch (RuntimeException ex) {
            request.getSession().setAttribute("error", ex.getMessage());
            if ("guardar".equals(accion)) {
                destino = url(request, "accion=nueva");
            } else if ("actualizar".equals(accion)) {
                destino = url(request, "accion=editar&id=" + request.getParameter("id"));
            }
        }
        response.sendRedirect(destino);
    }

    /** Reconstruye la lista de ingredientes desde los parametros repetidos del formulario. */
    private List<IngredientePlato> leerIngredientes(HttpServletRequest request) {
        String[] insumoIds = request.getParameterValues("insumoId[]");
        String[] cantidades = request.getParameterValues("cantidad[]");
        String[] unidades = request.getParameterValues("unidad[]");
        List<IngredientePlato> ingredientes = new ArrayList<>();
        if (insumoIds == null) {
            return ingredientes;
        }
        for (int i = 0; i < insumoIds.length; i++) {
            if (insumoIds[i] == null || insumoIds[i].trim().isEmpty()) {
                continue;
            }
            int insumoId = parsearId(insumoIds[i]);
            double cantidad = parsearCantidad((cantidades != null && i < cantidades.length) ? cantidades[i] : null);
            String unidad = (unidades != null && i < unidades.length) ? unidades[i] : null;
            ingredientes.add(new IngredientePlato(insumoId, cantidad, unidad));
        }
        return ingredientes;
    }

    private double parsearCantidad(String valor) {
        try {
            return Double.parseDouble(valor.trim());
        } catch (RuntimeException ex) {
            return -1;
        }
    }

    private Integer parsearRestauranteId(String valor) {
        try {
            return Integer.parseInt(valor.trim());
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private int parsearId(String valor) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException | NullPointerException ex) {
            return -1;
        }
    }

    private void cargarCatalogos(HttpServletRequest request) {
        request.setAttribute("restaurantes", platoService.listarRestaurantesDisponibles());
        request.setAttribute("insumos", platoService.listarInsumosDisponibles());
    }

    private String url(HttpServletRequest request, String extraParam) {
        String rol = request.getParameter("rol");
        StringBuilder sb = new StringBuilder(request.getContextPath()).append("/platos");
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
