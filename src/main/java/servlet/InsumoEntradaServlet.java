package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.InsumoService;

import java.io.IOException;

/**
 * Controlador del modulo de inventario. En GET lista los insumos y reenvia
 * a la vista; en POST procesa las acciones "registrar" (entrada de lote) y
 * "reducir" (merma o desperdicio), dejando el mensaje resultante en sesion.
 */
@WebServlet("/insumos")
public class InsumoEntradaServlet extends HttpServlet {

    private final InsumoService insumoService = new InsumoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("insumos", insumoService.listarInsumos());
        request.getRequestDispatcher("/views/cu3-insumos-entrada.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String accion = request.getParameter("accion");
        try {
            if ("registrar".equals(accion)) {
                int insumoId = Integer.parseInt(request.getParameter("insumoId"));
                double cantidad = Double.parseDouble(request.getParameter("cantidad"));
                double costo = Double.parseDouble(request.getParameter("costo"));
                String ordenCompra = request.getParameter("ordenCompra");
                String factura = request.getParameter("factura");
                insumoService.registrarEntradaInsumos(insumoId, cantidad, costo, ordenCompra, factura);
                request.getSession().setAttribute("mensaje", "Entrada registrada correctamente");
            } else if ("reducir".equals(accion)) {
                int insumoId = Integer.parseInt(request.getParameter("insumoId"));
                double cantidad = Double.parseDouble(request.getParameter("cantidad"));
                insumoService.reducirStock(insumoId, cantidad);
                request.getSession().setAttribute("mensaje", "Stock reducido correctamente");
            }
        } catch (NumberFormatException ex) {
            request.getSession().setAttribute("error", "Revisa los valores numericos ingresados");
        } catch (RuntimeException ex) {
            request.getSession().setAttribute("error", ex.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/insumos");
    }
}
