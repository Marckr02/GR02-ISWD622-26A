package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Insumo;
import model.Proveedor;
import service.InsumoService;
import service.MenuService;
import service.ProveedorService;
import service.RestauranteService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel de control operativo (HU7 + HU9 fusionados). Reune los insumos
 * criticos (stock por debajo del minimo), su proveedor asociado para la
 * accion rapida "Contactar proveedor", y el estado de disponibilidad de
 * todos los platos del menu, en una sola vista. El acceso lo restringe
 * AuthFilter (solo ADMIN_BODEGA y ADMINISTRADOR). Cada insumo critico
 * detectado aqui queda registrado en el historial de alertas (HU11).
 */
@WebServlet("/monitoreo")
public class MonitoreoServlet extends HttpServlet {

    private final InsumoService insumoService = new InsumoService();
    private final MenuService menuService = new MenuService();
    private final ProveedorService proveedorService = new ProveedorService();
    private final RestauranteService restauranteService = new RestauranteService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Las alertas ya se registran en el momento real del cambio de stock
        // (InsumoService/PedidoService); aqui solo se leen los criticos actuales.
        List<Insumo> criticos = insumoService.listarInsumosCriticos();
        request.setAttribute("criticos", criticos);
        request.setAttribute("menu", menuService.sincronizarMenuConInventario());
        request.setAttribute("restauranteService", restauranteService);

        Map<Integer, Proveedor> proveedorPorInsumo = new HashMap<>();
        for (Insumo insumo : criticos) {
            Proveedor proveedor = proveedorService.obtenerProveedorDe(insumo.getId());
            if (proveedor != null) {
                proveedorPorInsumo.put(insumo.getId(), proveedor);
            }
        }
        request.setAttribute("proveedorPorInsumo", proveedorPorInsumo);

        request.getRequestDispatcher("/views/panel-monitoreo.jsp").forward(request, response);
    }
}
