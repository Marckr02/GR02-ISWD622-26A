package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.InsumoService;

import java.io.IOException;

/**
 * Controlador del modulo de inventario. En GET lista los insumos y reenvia a
 * la vista; en POST procesa las acciones "registrar" (entrada simplificada,
 * HU21), "reducir" (merma) y "crear" (alta de insumo, HU23), dejando el
 * mensaje resultante en sesion.
 */
@WebServlet({"/insumos", "/insumos/crear"})
public class InsumoEntradaServlet extends HttpServlet {

    private final InsumoService insumoService = new InsumoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("insumos", insumoService.listarInsumos());
        String destino = request.getServletPath().endsWith("/crear")
                ? "/views/cu23-crear-insumo.jsp"
                : "/views/cu3-insumos-entrada.jsp";
        request.getRequestDispatcher(destino).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String accion = request.getParameter("accion");
        String destino = request.getContextPath() + "/insumos";
        try {
            if ("registrar".equals(accion)) {
                int insumoId = parsearInsumo(request.getParameter("insumoId"));
                insumoService.registrarEntradaSimplificada(insumoId, request.getParameter("cantidad"));
                request.getSession().setAttribute("mensaje", "Entrada registrada correctamente");
            } else if ("reducir".equals(accion)) {
                int insumoId = parsearInsumo(request.getParameter("insumoId"));
                double cantidad = Double.parseDouble(request.getParameter("cantidad"));
                insumoService.reducirStock(insumoId, cantidad);
                request.getSession().setAttribute("mensaje", "Stock reducido correctamente");
            } else if ("crear".equals(accion)) {
                insumoService.crearInsumo(request.getParameter("nombre"));
                request.getSession().setAttribute("mensaje", "Insumo creado correctamente");
                destino = request.getContextPath() + "/insumos/crear";
            }
        } catch (NumberFormatException ex) {
            request.getSession().setAttribute("error", "Ingrese un numero valido");
            if ("crear".equals(accion)) {
                destino = request.getContextPath() + "/insumos/crear";
            }
        } catch (RuntimeException ex) {
            request.getSession().setAttribute("error", ex.getMessage());
            if ("crear".equals(accion)) {
                destino = request.getContextPath() + "/insumos/crear";
            }
        }
        response.sendRedirect(destino);
    }

    /** Convierte el id del insumo seleccionado; si falta, lo trata como no seleccionado. */
    private int parsearInsumo(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar un insumo de la lista");
        }
        return Integer.parseInt(valor.trim());
    }
}
