package service;

import dao.InsumoDao;
import dao.PedidoDao;
import dao.PlatoDao;
import dao.RestauranteDao;
import model.EstadoPedido;
import model.IngredientePlato;
import model.Insumo;
import model.MetricaInsumo;
import model.MetricaPlato;
import model.MetricaRestaurantePedidos;
import model.Pedido;
import model.Plato;
import model.Restaurante;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Logica de negocio de las metricas de rendimiento por restaurante (F5.1,
 * HU35-HU38). Cruza los pedidos en estado ENTREGADO con los platos y sus
 * recetas para calcular ventas por plato, consumo de insumos y totales
 * operativos, filtrados por restaurante o agregados para toda la dark
 * kitchen cuando {@code restauranteId == 0}.
 */
public class MetricasService {

    /** Valor de restauranteId que representa la vista consolidada ("Todos los restaurantes"). */
    public static final int TODOS_LOS_RESTAURANTES = 0;

    private final PedidoDao pedidoDao;
    private final PlatoDao platoDao;
    private final InsumoDao insumoDao;
    private final RestauranteDao restauranteDao;
    private final ConversionUnidades conversionUnidades;

    public MetricasService() {
        this(new PedidoDao(), new PlatoDao(), new InsumoDao(), new RestauranteDao());
    }

    public MetricasService(PedidoDao pedidoDao, PlatoDao platoDao, InsumoDao insumoDao, RestauranteDao restauranteDao) {
        this.pedidoDao = pedidoDao;
        this.platoDao = platoDao;
        this.insumoDao = insumoDao;
        this.restauranteDao = restauranteDao;
        this.conversionUnidades = new ConversionUnidades();
    }

    // ---------- HU35: platos mas y menos vendidos ----------

    public List<MetricaPlato> obtenerPlatosMasVendidosPorRestaurante(int restauranteId) {
        List<MetricaPlato> metricas = calcularConteoPlatos(restauranteId);
        metricas.sort(Comparator.comparingLong(MetricaPlato::getPedidosEntregados).reversed()
                .thenComparing(MetricaPlato::getNombre));
        return metricas;
    }

    public List<MetricaPlato> obtenerPlatosMenosVendidosPorRestaurante(int restauranteId) {
        List<MetricaPlato> metricas = calcularConteoPlatos(restauranteId);
        metricas.sort(Comparator.comparingLong(MetricaPlato::getPedidosEntregados)
                .thenComparing(MetricaPlato::getNombre));
        return metricas;
    }

    public List<MetricaPlato> obtenerMetricasGeneralesPlatos() {
        return obtenerPlatosMasVendidosPorRestaurante(TODOS_LOS_RESTAURANTES);
    }

    private List<MetricaPlato> calcularConteoPlatos(int restauranteId) {
        Map<Integer, Long> conteoPorPlato = new LinkedHashMap<>();
        for (Pedido pedido : pedidosEntregadosDelRestaurante(restauranteId)) {
            conteoPorPlato.merge(pedido.getPlatoId(), 1L, Long::sum);
        }
        List<MetricaPlato> resultado = new ArrayList<>();
        for (Map.Entry<Integer, Long> entrada : conteoPorPlato.entrySet()) {
            Plato plato = platoDao.buscarPorId(entrada.getKey());
            if (plato == null) {
                continue;
            }
            resultado.add(new MetricaPlato(plato.getId(), plato.getNombre(), entrada.getValue()));
        }
        return resultado;
    }

    // ---------- HU36: insumos mas y menos utilizados ----------

    public List<MetricaInsumo> obtenerInsumosMasUtilizadosPorRestaurante(int restauranteId) {
        List<MetricaInsumo> metricas = calcularConsumoInsumos(restauranteId);
        metricas.sort(Comparator.comparingDouble(MetricaInsumo::getCantidadTotal).reversed()
                .thenComparing(MetricaInsumo::getNombre));
        return metricas;
    }

    public List<MetricaInsumo> obtenerInsumosMenosUtilizadosPorRestaurante(int restauranteId) {
        List<MetricaInsumo> metricas = calcularConsumoInsumos(restauranteId);
        metricas.sort(Comparator.comparingDouble(MetricaInsumo::getCantidadTotal)
                .thenComparing(MetricaInsumo::getNombre));
        return metricas;
    }

    public List<MetricaInsumo> obtenerMetricasGeneralesInsumos() {
        return obtenerInsumosMasUtilizadosPorRestaurante(TODOS_LOS_RESTAURANTES);
    }

    private List<MetricaInsumo> calcularConsumoInsumos(int restauranteId) {
        Map<Integer, Double> cantidadPorInsumo = new LinkedHashMap<>();
        for (Pedido pedido : pedidosEntregadosDelRestaurante(restauranteId)) {
            Plato plato = platoDao.buscarPorId(pedido.getPlatoId());
            if (plato == null) {
                continue;
            }
            for (IngredientePlato ingrediente : plato.getIngredientes()) {
                Insumo insumo = insumoDao.buscarPorId(ingrediente.getInsumoId());
                if (insumo == null) {
                    continue;
                }
                double cantidadEnUnidadBase = convertirALaUnidadDelInsumo(ingrediente, insumo);
                cantidadPorInsumo.merge(insumo.getId(), cantidadEnUnidadBase, Double::sum);
            }
        }
        List<MetricaInsumo> resultado = new ArrayList<>();
        for (Map.Entry<Integer, Double> entrada : cantidadPorInsumo.entrySet()) {
            Insumo insumo = insumoDao.buscarPorId(entrada.getKey());
            if (insumo == null) {
                continue;
            }
            resultado.add(new MetricaInsumo(insumo.getId(), insumo.getNombre(), entrada.getValue(), insumo.getUnidad()));
        }
        return resultado;
    }

    /** Convierte la cantidad de la receta a la unidad base en la que se almacena el insumo. */
    private double convertirALaUnidadDelInsumo(IngredientePlato ingrediente, Insumo insumo) {
        String unidadReceta = ingrediente.getUnidadReceta();
        if (unidadReceta == null || unidadReceta.equals(insumo.getUnidad())) {
            return ingrediente.getCantidad();
        }
        return conversionUnidades.convertir(ingrediente.getCantidad(), unidadReceta, insumo.getUnidad());
    }

    // ---------- HU37: total de pedidos completados ----------

    public long obtenerTotalPedidosCompletadosPorRestaurante(int restauranteId) {
        return pedidosEntregadosDelRestaurante(restauranteId).size();
    }

    /** Una fila por restaurante con su total de pedidos ENTREGADO (vista "Todos los restaurantes"). */
    public List<MetricaRestaurantePedidos> obtenerTotalesPorTodosLosRestaurantes() {
        List<MetricaRestaurantePedidos> resultado = new ArrayList<>();
        for (Restaurante restaurante : restauranteDao.listarTodos()) {
            long total = obtenerTotalPedidosCompletadosPorRestaurante(restaurante.getId());
            resultado.add(new MetricaRestaurantePedidos(restaurante.getId(), restaurante.getNombre(), total));
        }
        return resultado;
    }

    // ---------- helper comun ----------

    /**
     * Pedidos en estado ENTREGADO cuyo plato pertenece al restaurante indicado.
     * Si {@code restauranteId == TODOS_LOS_RESTAURANTES}, incluye los de todos
     * los restaurantes (vista consolidada).
     */
    private List<Pedido> pedidosEntregadosDelRestaurante(int restauranteId) {
        List<Pedido> resultado = new ArrayList<>();
        for (Pedido pedido : pedidoDao.listarTodos()) {
            if (pedido.getEstado() != EstadoPedido.ENTREGADO) {
                continue;
            }
            if (restauranteId == TODOS_LOS_RESTAURANTES) {
                resultado.add(pedido);
                continue;
            }
            Plato plato = platoDao.buscarPorId(pedido.getPlatoId());
            if (plato != null && plato.getRestauranteId() == restauranteId) {
                resultado.add(pedido);
            }
        }
        return resultado;
    }
}
