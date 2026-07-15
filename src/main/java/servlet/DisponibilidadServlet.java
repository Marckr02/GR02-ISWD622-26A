package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.MenuService;
import service.RestauranteService;

import java.io.IOException;

/**
 * Vista de consulta de disponibilidad para el cocinero (HU10): lista los
 * platos con su estado, la marca a la que pertenecen y, para los
 * bloqueados, el motivo. El acceso lo restringe AuthFilter (solo COCINERO
 * y ADMINISTRADOR).
 */
@WebServlet("/disponibilidad")
public class DisponibilidadServlet extends HttpServlet {

    private final MenuService menuService = new MenuService();
    private final RestauranteService restauranteService = new RestauranteService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("menu", menuService.sincronizarMenuConInventario());
        request.setAttribute("restauranteService", restauranteService);
        request.getRequestDispatcher("/views/disponibilidad-cocinero.jsp").forward(request, response);
    }
}
