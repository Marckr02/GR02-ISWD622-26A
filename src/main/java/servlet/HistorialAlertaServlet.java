package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.AlertaStockService;

import java.io.IOException;

/**
 * Historial de alertas de stock critico (HU11). Solo lista lo ya
 * registrado; el registro ocurre cuando el panel de monitoreo detecta
 * insumos criticos (ver MonitoreoServlet). Acceso restringido a
 * ADMINISTRADOR por AuthFilter.
 */
@WebServlet("/alertas")
public class HistorialAlertaServlet extends HttpServlet {

    private final AlertaStockService alertaStockService = new AlertaStockService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("alertas", alertaStockService.listarHistorial());
        request.getRequestDispatcher("/views/cu11-historial-alertas.jsp").forward(request, response);
    }
}
