package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.MenuService;

import java.io.IOException;

/**
 * Vista de consulta de disponibilidad para el cocinero (HU10): lista los
 * platos con su estado y, para los bloqueados, el motivo. El acceso lo
 * restringe AuthFilter (solo COCINERO y ADMINISTRADOR).
 */
@WebServlet("/disponibilidad")
public class DisponibilidadServlet extends HttpServlet {

    private final MenuService menuService = new MenuService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("menu", menuService.sincronizarMenuConInventario());
        request.getRequestDispatcher("/views/cu10-disponibilidad-cocinero.jsp").forward(request, response);
    }
}
