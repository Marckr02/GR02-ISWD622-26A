package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.MetricaInsumo;
import model.MetricaPlato;
import model.MetricaRestaurantePedidos;
import model.Restaurante;
import service.MetricasService;
import service.RestauranteService;

import java.io.IOException;
import java.util.List;

/**
 * Controlador de la vista de metricas por restaurante (F5.1, HU35-HU38).
 * GET sin "restauranteId": solo muestra el selector, sin resultados.
 * GET con "buscar=1" y sin "restauranteId": HU35 escenario negativo 2.
 * GET con "restauranteId=0": vista consolidada ("Todos los restaurantes").
 * GET con "restauranteId=&lt;id&gt;": metricas de ese restaurante.
 * El acceso lo restringe AuthFilter (solo ADMINISTRADOR).
 */
@WebServlet("/metricas")
public class MetricasServlet extends HttpServlet {

    private final MetricasService metricasService = new MetricasService();
    private final RestauranteService restauranteService = new RestauranteService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Restaurante> restaurantes = restauranteService.listarRestaurantes();
        request.setAttribute("restaurantes", restaurantes);

        String buscar = request.getParameter("buscar");
        String restauranteIdParam = request.getParameter("restauranteId");

        if (restauranteIdParam == null || restauranteIdParam.isBlank()) {
            if ("1".equals(buscar)) {
                request.setAttribute("sinSeleccion", Boolean.TRUE);
            }
            request.getRequestDispatcher("/views/metricas-restaurante.jsp").forward(request, response);
            return;
        }

        Integer restauranteId = parsearId(restauranteIdParam);
        if (restauranteId == null || restauranteId < 0) {
            request.setAttribute("sinSeleccion", Boolean.TRUE);
            request.getRequestDispatcher("/views/metricas-restaurante.jsp").forward(request, response);
            return;
        }

        request.setAttribute("restauranteIdSeleccionado", restauranteId);
        boolean vistaGeneral = restauranteId == MetricasService.TODOS_LOS_RESTAURANTES;
        request.setAttribute("vistaGeneral", vistaGeneral);

        if (vistaGeneral) {
            List<MetricaPlato> platos = metricasService.obtenerMetricasGeneralesPlatos();
            List<MetricaInsumo> insumos = metricasService.obtenerMetricasGeneralesInsumos();
            List<MetricaRestaurantePedidos> totalesPorRestaurante = metricasService.obtenerTotalesPorTodosLosRestaurantes();
            long totalPedidos = totalesPorRestaurante.stream().mapToLong(MetricaRestaurantePedidos::getTotalPedidos).sum();

            request.setAttribute("platosMasVendidos", platos);
            request.setAttribute("insumosMasUtilizados", insumos);
            request.setAttribute("totalesPorRestaurante", totalesPorRestaurante);
            request.setAttribute("totalPedidos", totalPedidos);
            request.setAttribute("nombreRestauranteSeleccionado", "Todos los restaurantes");
        } else {
            Restaurante restaurante = restauranteService.buscar(restauranteId);
            request.setAttribute("nombreRestauranteSeleccionado",
                    restaurante != null ? restaurante.getNombre() : null);
            request.setAttribute("platosMasVendidos",
                    metricasService.obtenerPlatosMasVendidosPorRestaurante(restauranteId));
            request.setAttribute("insumosMasUtilizados",
                    metricasService.obtenerInsumosMasUtilizadosPorRestaurante(restauranteId));
            request.setAttribute("totalPedidos",
                    metricasService.obtenerTotalPedidosCompletadosPorRestaurante(restauranteId));
        }

        request.getRequestDispatcher("/views/metricas-restaurante.jsp").forward(request, response);
    }

    private Integer parsearId(String valor) {
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
