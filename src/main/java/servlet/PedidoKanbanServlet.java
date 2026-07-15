package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.EstadoPedido;
import model.Rol;
import service.EstadoPedidoPolicy;
import service.PedidoService;
import service.PlatoService;
import service.RestauranteService;
import service.StockInsuficienteException;

import java.io.IOException;

/**
 * Controlador del tablero Kanban. En GET arma las columnas por estado y
 * reenvia a la vista; en POST procesa las acciones "mover" (avanzar),
 * "retroceder" (HU20) y "crear" (simulador de pedidos) invocando al
 * PedidoService.
 */
@WebServlet("/pedidos")
public class PedidoKanbanServlet extends HttpServlet {

    private final PedidoService pedidoService = new PedidoService();
    private final EstadoPedidoPolicy policy = new EstadoPedidoPolicy();
    private final RestauranteService restauranteService = new RestauranteService();
    private final PlatoService platoService = new PlatoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Rol rol = Rol.desde(request.getParameter("rol"));
        if (rol != null) {
            request.getSession().setAttribute("rol", rol.name());
        }
        for (EstadoPedido estado : EstadoPedido.values()) {
            if (estado == EstadoPedido.ENTREGADO) {
                request.setAttribute("col_ENTREGADO", pedidoService.listarEntregadosRecientes(10));
            } else {
                request.setAttribute("col_" + estado.name(), pedidoService.listarPorEstado(estado));
            }
        }
        request.setAttribute("historialEntregados", pedidoService.listarHistorialEntregados());
        request.setAttribute("policy", policy);
        request.setAttribute("restaurantes", restauranteService.listarRestaurantes());
        request.setAttribute("platos", platoService.listarPlatos());
        request.getRequestDispatcher("/views/pedidos-kanban.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String accion = request.getParameter("accion");
        try {
            if ("crear".equals(accion)) {
                int platoId = Integer.parseInt(request.getParameter("platoId"));
                pedidoService.crearPedidoManual(platoId);
            } else {
                int pedidoId = Integer.parseInt(request.getParameter("pedidoId"));
                if ("mover".equals(accion)) {
                    pedidoService.avanzarEstado(pedidoId);
                } else if ("retroceder".equals(accion)) {
                    pedidoService.retrocederEstado(pedidoId);
                }
            }
        } catch (NumberFormatException ex) {
            request.getSession().setAttribute("error", "Identificador de pedido invalido");
        } catch (StockInsuficienteException ex) {
            request.getSession().setAttribute("error", ex.getMessage());
            request.getSession().setAttribute("stockFaltantes", ex.getFaltantes());
            request.getSession().setAttribute("stockPlato", ex.getPlato());
        } catch (RuntimeException ex) {
            request.getSession().setAttribute("error", ex.getMessage());
        }
        Rol rol = Rol.desde(request.getParameter("rol"));
        if (rol != null) {
            request.getSession().setAttribute("rol", rol.name());
        }
        String destino = request.getContextPath() + "/pedidos";
        response.sendRedirect(rol == null ? destino : destino + "?rol=" + rol.name());
    }
}