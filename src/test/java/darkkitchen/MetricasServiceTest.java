package darkkitchen;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.MetricasService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Pruebas de MetricasService (F5.1, HU35-HU38). Los DAOs se mockean para
 * aislar la logica de calculo de metricas; los datos de pedidos, platos e
 * insumos se arman a mano en cada escenario.
 */
@ExtendWith(MockitoExtension.class)
class MetricasServiceTest {

    @Mock
    private PedidoDao pedidoDao;
    @Mock
    private PlatoDao platoDao;
    @Mock
    private InsumoDao insumoDao;
    @Mock
    private RestauranteDao restauranteDao;
    @InjectMocks
    private MetricasService metricasService;

    private Plato pizza;
    private Plato hamburguesa;
    private Plato sushi;
    private Insumo harina;
    private Insumo queso;

    @BeforeEach
    void configurarCatalogoBase() {
        pizza = new Plato(1, "Pizza Margarita", 1,
                List.of(new IngredientePlato(10, 300, "g"), new IngredientePlato(11, 200, "g")));
        hamburguesa = new Plato(2, "Hamburguesa Clasica", 1,
                List.of(new IngredientePlato(10, 150, "g")));
        sushi = new Plato(3, "Sushi Roll", 3, List.of(new IngredientePlato(11, 1, "kg")));

        harina = new Insumo(10, "Harina de trigo", "kg", 40.0, 0.85, 10.0);
        queso = new Insumo(11, "Queso mozzarella", "kg", 18.5, 6.20, 5.0);
    }

    // ---------- HU35: platos mas y menos vendidos ----------

    @Test
    void obtenerPlatosMasVendidosPorRestaurante_ordenaDeMayorAMenorCantidadEntregada() {
        when(pedidoDao.listarTodos()).thenReturn(List.of(
                pedido(1, EstadoPedido.ENTREGADO, 1),
                pedido(2, EstadoPedido.ENTREGADO, 1),
                pedido(3, EstadoPedido.ENTREGADO, 1),
                pedido(4, EstadoPedido.ENTREGADO, 2),
                pedido(5, EstadoPedido.EN_PREPARACION, 1), // no cuenta: no esta ENTREGADO
                pedido(6, EstadoPedido.ENTREGADO, 3)       // no cuenta: es de otro restaurante
        ));
        when(platoDao.buscarPorId(1)).thenReturn(pizza);
        when(platoDao.buscarPorId(2)).thenReturn(hamburguesa);

        List<MetricaPlato> resultado = metricasService.obtenerPlatosMasVendidosPorRestaurante(1);

        assertEquals(2, resultado.size());
        assertEquals("Pizza Margarita", resultado.get(0).getNombre());
        assertEquals(3, resultado.get(0).getPedidosEntregados());
        assertEquals("Hamburguesa Clasica", resultado.get(1).getNombre());
        assertEquals(1, resultado.get(1).getPedidosEntregados());
    }

    @Test
    void obtenerPlatosMenosVendidosPorRestaurante_ordenaDeMenorAMayorCantidadEntregada() {
        when(pedidoDao.listarTodos()).thenReturn(List.of(
                pedido(1, EstadoPedido.ENTREGADO, 1),
                pedido(2, EstadoPedido.ENTREGADO, 1),
                pedido(3, EstadoPedido.ENTREGADO, 2)
        ));
        when(platoDao.buscarPorId(1)).thenReturn(pizza);
        when(platoDao.buscarPorId(2)).thenReturn(hamburguesa);

        List<MetricaPlato> resultado = metricasService.obtenerPlatosMenosVendidosPorRestaurante(1);

        assertEquals("Hamburguesa Clasica", resultado.get(0).getNombre());
        assertEquals("Pizza Margarita", resultado.get(1).getNombre());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 99})
    void obtenerPlatosMasVendidosPorRestaurante_sinPedidosEntregados_retornaListaVacia(int restauranteId) {
        when(pedidoDao.listarTodos()).thenReturn(List.of(
                pedido(1, EstadoPedido.RECIBIDO, 1),
                pedido(2, EstadoPedido.LISTO, 1)
        ));

        List<MetricaPlato> resultado = metricasService.obtenerPlatosMasVendidosPorRestaurante(restauranteId);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void obtenerMetricasGeneralesPlatos_agregaTodosLosRestaurantes() {
        when(pedidoDao.listarTodos()).thenReturn(List.of(
                pedido(1, EstadoPedido.ENTREGADO, 1),
                pedido(2, EstadoPedido.ENTREGADO, 3)
        ));
        when(platoDao.buscarPorId(1)).thenReturn(pizza);
        when(platoDao.buscarPorId(3)).thenReturn(sushi);

        List<MetricaPlato> resultado = metricasService.obtenerMetricasGeneralesPlatos();

        assertEquals(2, resultado.size());
    }

    // ---------- HU36: insumos mas y menos utilizados ----------

    @Test
    void obtenerInsumosMasUtilizadosPorRestaurante_sumaCantidadesYNormalizaUnidades() {
        // hamburguesa consume 150g de harina; pizza consume 300g de harina y 200g de queso.
        when(pedidoDao.listarTodos()).thenReturn(List.of(
                pedido(1, EstadoPedido.ENTREGADO, 1), // pizza
                pedido(2, EstadoPedido.ENTREGADO, 2)  // hamburguesa
        ));
        when(platoDao.buscarPorId(1)).thenReturn(pizza);
        when(platoDao.buscarPorId(2)).thenReturn(hamburguesa);
        when(insumoDao.buscarPorId(10)).thenReturn(harina); // unidad base: kg
        when(insumoDao.buscarPorId(11)).thenReturn(queso);  // unidad base: kg

        List<MetricaInsumo> resultado = metricasService.obtenerInsumosMasUtilizadosPorRestaurante(1);

        assertEquals(2, resultado.size());
        MetricaInsumo masUsado = resultado.get(0);
        assertEquals("Harina de trigo", masUsado.getNombre());
        assertEquals(0.45, masUsado.getCantidadTotal(), 0.0001); // 300g + 150g = 450g = 0.45kg
        assertEquals("kg", masUsado.getUnidad());
    }

    @Test
    void obtenerInsumosMenosUtilizadosPorRestaurante_ordenaAscendente() {
        when(pedidoDao.listarTodos()).thenReturn(List.of(pedido(1, EstadoPedido.ENTREGADO, 1)));
        when(platoDao.buscarPorId(1)).thenReturn(pizza);
        when(insumoDao.buscarPorId(10)).thenReturn(harina);
        when(insumoDao.buscarPorId(11)).thenReturn(queso);

        List<MetricaInsumo> resultado = metricasService.obtenerInsumosMenosUtilizadosPorRestaurante(1);

        assertEquals("Queso mozzarella", resultado.get(0).getNombre()); // 0.2kg < 0.3kg
        assertEquals("Harina de trigo", resultado.get(1).getNombre());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void obtenerInsumosMasUtilizadosPorRestaurante_sinConsumoRegistrado_retornaListaVacia(int restauranteId) {
        when(pedidoDao.listarTodos()).thenReturn(List.of());

        List<MetricaInsumo> resultado = metricasService.obtenerInsumosMasUtilizadosPorRestaurante(restauranteId);

        assertTrue(resultado.isEmpty());
    }

    // ---------- HU37: total de pedidos completados ----------

    @ParameterizedTest
    @CsvSource({
            "1, 2",
            "2, 0",
            "99, 0"
    })
    void obtenerTotalPedidosCompletadosPorRestaurante_cuentaSoloEntregadosDelRestaurante(
            int restauranteId, long esperado) {
        when(pedidoDao.listarTodos()).thenReturn(List.of(
                pedido(1, EstadoPedido.ENTREGADO, 1),
                pedido(2, EstadoPedido.ENTREGADO, 2), // plato del restaurante 3 en realidad
                pedido(3, EstadoPedido.ENTREGADO, 1),
                pedido(4, EstadoPedido.RECIBIDO, 1)
        ));
        when(platoDao.buscarPorId(1)).thenReturn(pizza);      // restaurante 1
        when(platoDao.buscarPorId(2)).thenReturn(sushi);      // restaurante 3 (no 2)

        long total = metricasService.obtenerTotalPedidosCompletadosPorRestaurante(restauranteId);

        assertEquals(esperado, total);
    }

    @Test
    void obtenerTotalesPorTodosLosRestaurantes_retornaUnaFilaPorRestaurante() {
        Restaurante napoli = new Restaurante(1, "Napoli", "");
        Restaurante burgerLab = new Restaurante(2, "Burger Lab", "");
        when(restauranteDao.listarTodos()).thenReturn(List.of(napoli, burgerLab));
        when(pedidoDao.listarTodos()).thenReturn(List.of(
                pedido(1, EstadoPedido.ENTREGADO, 1),
                pedido(2, EstadoPedido.ENTREGADO, 1)
        ));
        when(platoDao.buscarPorId(1)).thenReturn(pizza);

        List<MetricaRestaurantePedidos> resultado = metricasService.obtenerTotalesPorTodosLosRestaurantes();

        assertEquals(2, resultado.size());
        assertEquals(2, resultado.stream()
                .filter(fila -> fila.getRestauranteId() == 1)
                .findFirst().orElseThrow().getTotalPedidos());
        assertEquals(0, resultado.stream()
                .filter(fila -> fila.getRestauranteId() == 2)
                .findFirst().orElseThrow().getTotalPedidos());
    }

    // ---------- helpers ----------

    private Pedido pedido(int id, EstadoPedido estado, int platoId) {
        return new Pedido(id, "Pedido " + id, "Marca", estado, platoId);
    }
}
