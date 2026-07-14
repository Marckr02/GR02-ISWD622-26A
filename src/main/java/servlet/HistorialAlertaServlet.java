package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.AlertaStockService;
import service.InsumoService;

import java.io.IOException;

/**
 * Historial de alertas de stock critico (HU11). Solo lista lo ya
 * registrado; el registro ocurre en tiempo real dentro de InsumoService y
 * PedidoService, justo cuando el stock de un insumo cambia y cruza el
 * umbral critico (y al arrancar la aplicacion, ver AlertaStockInicializador).
 * Acceso restringido a ADMINISTRADOR por AuthFilter.
 */
@WebServlet("/alertas")
public class HistorialAlertaServlet extends HttpServlet {

    private final AlertaStockService alertaStockService = new AlertaStockService();
    private final InsumoService insumoService = new InsumoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("alertas", alertaStockService.listarHistorial());
        request.setAttribute("criticosActuales", insumoService.listarInsumosCriticos());
        request.getRequestDispatcher("/views/cu11-historial-alertas.jsp").forward(request, response);
    }
}
