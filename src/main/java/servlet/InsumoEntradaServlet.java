package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Insumo;
import model.Proveedor;
import service.InsumoService;
import service.ProveedorService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador del modulo de inventario. En GET lista los insumos (con su
 * proveedor asociado, si tiene) y reenvia a la vista; en POST procesa las
 * acciones "registrar" (entrada simplificada), "reducir" (merma), "crear"
 * (alta de insumo), "editar" (nombre/unidad + nivel minimo, HU23/HU34) y
 * "vincularProveedor" (HU6), dejando el mensaje resultante en sesion.
 */
@WebServlet({"/insumos", "/insumos/crear"})
public class InsumoEntradaServlet extends HttpServlet {

    private final InsumoService insumoService = new InsumoService();
    private final ProveedorService proveedorService = new ProveedorService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Insumo> insumos = insumoService.listarInsumos();
        request.setAttribute("insumos", insumos);
        request.setAttribute("proveedores", proveedorService.listarProveedores());

        Map<Integer, Proveedor> proveedorPorInsumo = new HashMap<>();
        for (Insumo insumo : insumos) {
            Proveedor proveedor = proveedorService.obtenerProveedorDe(insumo.getId());
            if (proveedor != null) {
                proveedorPorInsumo.put(insumo.getId(), proveedor);
            }
        }
        request.setAttribute("proveedorPorInsumo", proveedorPorInsumo);

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
                insumoService.crearInsumo(request.getParameter("nombre"), request.getParameter("unidad"));
                request.getSession().setAttribute("mensaje", "Insumo creado correctamente");
                destino = request.getContextPath() + "/insumos/crear";
            } else if ("editar".equals(accion)) {
                int insumoId = parsearInsumo(request.getParameter("insumoId"));
                insumoService.editarInsumo(
                        insumoId, request.getParameter("nombre"), request.getParameter("unidad"));
                request.getSession().setAttribute("mensaje", "Insumo actualizado correctamente");
                destino = request.getContextPath() + "/insumos/crear";
            } else if ("actualizarMinimo".equals(accion)) {
                int insumoId = parsearInsumo(request.getParameter("insumoId"));
                insumoService.actualizarStockMinimo(insumoId, request.getParameter("stockMinimo"));
                request.getSession().setAttribute("mensaje", "Nivel minimo actualizado correctamente");
            } else if ("vincularProveedor".equals(accion)) {
                int insumoId = parsearInsumo(request.getParameter("insumoId"));
                int proveedorId = Integer.parseInt(request.getParameter("proveedorId"));
                if (proveedorId <= 0) {
                    proveedorService.desvincularDeInsumo(insumoId);
                    request.getSession().setAttribute("mensaje", "Insumo sin proveedor asignado");
                } else {
                    proveedorService.vincularAInsumo(insumoId, proveedorId);
                    request.getSession().setAttribute("mensaje", "Proveedor asociado correctamente");
                }
            }
        } catch (NumberFormatException ex) {
            String mensaje = "vincularProveedor".equals(accion)
                    ? "Debe seleccionar un proveedor de la lista"
                    : "Ingrese un numero valido";
            request.getSession().setAttribute("error", mensaje);
            if ("crear".equals(accion) || "editar".equals(accion)) {
                destino = request.getContextPath() + "/insumos/crear";
            }
        } catch (RuntimeException ex) {
            request.getSession().setAttribute("error", ex.getMessage());
            if ("crear".equals(accion) || "editar".equals(accion)) {
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
