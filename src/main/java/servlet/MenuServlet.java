package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.MenuService;

import java.io.IOException;

/**
 * Endpoint de sincronizacion del menu con el inventario (HU7). En GET
 * recalcula el estado de todos los platos y reenvia a la vista; el POST
 * con accion "sincronizar" fuerza el recalculo y vuelve al menu.
 */
@WebServlet("/menu")
public class MenuServlet extends HttpServlet {

    private final MenuService menuService = new MenuService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("menu", menuService.sincronizarMenuConInventario());
        request.getRequestDispatcher("/views/cu7-menu-disponibilidad.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // La sincronizacion se recalcula siempre en el GET; aqui solo se
        // redirige para refrescar la vista tras una entrada de stock.
        response.sendRedirect(request.getContextPath() + "/menu");
    }
}
