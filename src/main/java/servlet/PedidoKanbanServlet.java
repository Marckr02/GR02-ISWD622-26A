package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.EstadoPedido;
import service.EstadoPedidoPolicy;
import service.PedidoService;

import java.io.IOException;

/**
 * Controlador del tablero Kanban. En GET arma las columnas por estado y
 * reenvia a la vista; en POST procesa las acciones "mover" (avanzar) y
 * "retroceder" (HU20) invocando al PedidoService.
 */
@WebServlet("/pedidos")
public class PedidoKanbanServlet extends HttpServlet {

    private final PedidoService pedidoService = new PedidoService();
    private final EstadoPedidoPolicy policy = new EstadoPedidoPolicy();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        for (EstadoPedido estado : EstadoPedido.values()) {
            request.setAttribute("col_" + estado.name(), pedidoService.listarPorEstado(estado));
        }
        request.setAttribute("policy", policy);
        request.getRequestDispatcher("/views/cu2-pedidos-kanban.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String accion = request.getParameter("accion");
        try {
            int pedidoId = Integer.parseInt(request.getParameter("pedidoId"));
            if ("mover".equals(accion)) {
                pedidoService.avanzarEstado(pedidoId);
            } else if ("retroceder".equals(accion)) {
                pedidoService.retrocederEstado(pedidoId);
            }
        } catch (NumberFormatException ex) {
            request.getSession().setAttribute("error", "Identificador de pedido invalido");
        } catch (RuntimeException ex) {
            request.getSession().setAttribute("error", ex.getMessage());
        }
        String rol = request.getParameter("rol");
        String destino = request.getContextPath() + "/pedidos";
        response.sendRedirect(rol == null ? destino : destino + "?rol=" + rol);
    }
}
