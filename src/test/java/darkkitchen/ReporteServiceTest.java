package darkkitchen;

import dao.RestauranteDao;
import model.MetricaInsumo;
import model.MetricaPlato;
import model.MetricaRestaurantePedidos;
import model.Restaurante;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.MetricasService;
import service.ReporteService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Pruebas de ReporteService.generarReportePDF (F5.2, HU39). MetricasService
 * se mockea para aislar la construccion del PDF de los calculos de metricas.
 */
@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private MetricasService metricasService;
    @Mock
    private RestauranteDao restauranteDao;
    @InjectMocks
    private ReporteService reporteService;

    @Test
    void generarReportePDF_conDatosDeUnRestaurante_retornaBytesNoVacios() {
        Restaurante napoli = new Restaurante(1, "Napoli", "Pizzeria");
        when(restauranteDao.buscarPorId(1)).thenReturn(napoli);
        when(metricasService.obtenerTotalPedidosCompletadosPorRestaurante(1)).thenReturn(5L);
        when(metricasService.obtenerPlatosMasVendidosPorRestaurante(1))
                .thenReturn(List.of(new MetricaPlato(1, "Pizza Margarita", 5)));
        when(metricasService.obtenerPlatosMenosVendidosPorRestaurante(1))
                .thenReturn(List.of(new MetricaPlato(1, "Pizza Margarita", 5)));
        when(metricasService.obtenerInsumosMasUtilizadosPorRestaurante(1))
                .thenReturn(List.of(new MetricaInsumo(1, "Harina de trigo", 1.5, "kg")));
        when(metricasService.obtenerInsumosMenosUtilizadosPorRestaurante(1))
                .thenReturn(List.of(new MetricaInsumo(1, "Harina de trigo", 1.5, "kg")));
        when(metricasService.obtenerResumenInsumosConsumidos(1))
                .thenReturn(List.of(new MetricaInsumo(1, "Harina de trigo", 1.5, "kg")));

        byte[] pdf = reporteService.generarReportePDF(1);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void generarReportePDF_vistaGeneral_retornaBytesNoVacios() {
        when(metricasService.obtenerTotalesPorTodosLosRestaurantes())
                .thenReturn(List.of(new MetricaRestaurantePedidos(1, "Napoli", 3)));
        when(metricasService.obtenerMetricasGeneralesPlatos())
                .thenReturn(List.of(new MetricaPlato(1, "Pizza Margarita", 3)));
        when(metricasService.obtenerMetricasGeneralesInsumos())
                .thenReturn(List.of(new MetricaInsumo(1, "Harina de trigo", 0.9, "kg")));
        when(metricasService.obtenerResumenGeneralInsumos())
                .thenReturn(List.of(new MetricaInsumo(1, "Harina de trigo", 0.9, "kg")));

        byte[] pdf = reporteService.generarReportePDF(MetricasService.TODOS_LOS_RESTAURANTES);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -5})
    void generarReportePDF_sinRestauranteSeleccionado_lanzaIllegalArgumentException(int restauranteId) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reporteService.generarReportePDF(restauranteId));

        assertEquals("Debe seleccionar un restaurante y ver sus métricas antes de exportar", ex.getMessage());
    }

    @Test
    void generarReportePDF_restauranteSinPedidosEntregados_lanzaIllegalArgumentException() {
        Restaurante napoli = new Restaurante(1, "Napoli", "Pizzeria");
        when(restauranteDao.buscarPorId(1)).thenReturn(napoli);
        when(metricasService.obtenerTotalPedidosCompletadosPorRestaurante(1)).thenReturn(0L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reporteService.generarReportePDF(1));

        assertEquals("No hay datos suficientes para generar el reporte de este restaurante", ex.getMessage());
    }

    @Test
    void generarReportePDF_restauranteInexistente_lanzaIllegalArgumentException() {
        when(restauranteDao.buscarPorId(99)).thenReturn(null);
        when(metricasService.obtenerTotalPedidosCompletadosPorRestaurante(99)).thenReturn(0L);

        assertThrows(IllegalArgumentException.class, () -> reporteService.generarReportePDF(99));
    }
}
