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
 * proveedor asociado, si tiene) y reenvia a la vista de cuadricula; en POST
 * procesa las acciones "registrar" (entrada simplificada), "reducir" (merma),
 * "crear" (alta de insumo, con nivel minimo y proveedor opcionales),
 * "actualizarInsumo" (edicion unificada de nombre/unidad/minimo/proveedor)
 * y "eliminar" (baja definitiva), dejando el mensaje resultante en sesion.
 */
@WebServlet("/insumos")
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

        request.getRequestDispatcher("/views/insumos-entrada.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String accion = request.getParameter("accion");
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
                Insumo creado = insumoService.crearInsumo(
                        request.getParameter("nombre"), request.getParameter("unidad"));
                aplicarMinimoSiPresente(creado.getId(), request.getParameter("stockMinimo"));
                aplicarProveedorSiPresente(creado.getId(), request.getParameter("proveedorId"));
                request.getSession().setAttribute("mensaje", "Insumo creado correctamente");
            } else if ("actualizarInsumo".equals(accion)) {
                int insumoId = parsearInsumo(request.getParameter("insumoId"));
                insumoService.editarInsumo(
                        insumoId, request.getParameter("nombre"), request.getParameter("unidad"));
                aplicarMinimoSiPresente(insumoId, request.getParameter("stockMinimo"));
                aplicarProveedorSiPresente(insumoId, request.getParameter("proveedorId"));
                request.getSession().setAttribute("mensaje", "Insumo actualizado correctamente");
            } else if ("eliminar".equals(accion)) {
                int insumoId = parsearInsumo(request.getParameter("insumoId"));
                insumoService.eliminarInsumo(insumoId);
                request.getSession().setAttribute("mensaje", "Insumo eliminado correctamente");
            }
        } catch (NumberFormatException ex) {
            boolean esFormularioInsumo = "crear".equals(accion) || "actualizarInsumo".equals(accion);
            String mensaje = esFormularioInsumo
                    ? "Debe seleccionar un proveedor de la lista"
                    : "Ingrese un numero valido";
            request.getSession().setAttribute("error", mensaje);
        } catch (RuntimeException ex) {
            request.getSession().setAttribute("error", ex.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/insumos");
    }

    /** Aplica el nuevo nivel minimo solo si el campo vino informado (crear/editar). */
    private void aplicarMinimoSiPresente(int insumoId, String stockMinimoTexto) {
        if (stockMinimoTexto == null || stockMinimoTexto.trim().isEmpty()) {
            return;
        }
        insumoService.actualizarStockMinimo(insumoId, stockMinimoTexto);
    }

    /** Vincula o desvincula el proveedor segun el valor recibido ("0" = sin proveedor). */
    private void aplicarProveedorSiPresente(int insumoId, String proveedorIdTexto) {
        if (proveedorIdTexto == null || proveedorIdTexto.trim().isEmpty()) {
            return;
        }
        int proveedorId = Integer.parseInt(proveedorIdTexto.trim());
        if (proveedorId <= 0) {
            proveedorService.desvincularDeInsumo(insumoId);
        } else {
            proveedorService.vincularAInsumo(insumoId, proveedorId);
        }
    }

    /** Convierte el id del insumo seleccionado; si falta, lo trata como no seleccionado. */
    private int parsearInsumo(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar un insumo de la lista");
        }
        return Integer.parseInt(valor.trim());
    }
}