package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.AlertaStockService;
import service.InsumoService;
import service.MenuService;

import java.io.IOException;

/**
 * Panel de monitoreo de disponibilidad (HU9). Reune los insumos criticos
 * (stock por debajo del minimo) y el estado actual de todos los platos del
 * menu, y reenvia a la vista. El acceso lo restringe AuthFilter (solo
 * ADMIN_BODEGA y ADMINISTRADOR). Cada insumo critico detectado aqui queda
 * registrado en el historial de alertas (HU11).
 */
@WebServlet("/monitoreo")
public class MonitoreoServlet extends HttpServlet {

    private final InsumoService insumoService = new InsumoService();
    private final MenuService menuService = new MenuService();
    private final AlertaStockService alertaStockService = new AlertaStockService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("criticos", insumoService.listarInsumosCriticos(alertaStockService));
        request.setAttribute("menu", menuService.sincronizarMenuConInventario());
        request.getRequestDispatcher("/views/cu9-panel-monitoreo.jsp").forward(request, response);
    }
}
