package service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import dao.RestauranteDao;
import model.MetricaInsumo;
import model.MetricaPlato;
import model.MetricaRestaurantePedidos;
import model.Restaurante;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Genera el reporte PDF de metricas de un restaurante o de toda la dark
 * kitchen (F5.2, HU39), reutilizando los calculos de {@link MetricasService}.
 * El diseno reproduce el mismo lenguaje visual del panel web (banda de
 * marca, tarjetas de resumen, tablas con fila resaltada para el extremo
 * mas/menos destacado) para que el documento se entienda sin contexto
 * adicional.
 */
public class ReporteService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Color COLOR_MARCA = new Color(0x0F, 0x17, 0x24);
    private static final Color COLOR_ACENTO = new Color(0x10, 0xB9, 0x81);
    private static final Color COLOR_ACENTO_SUAVE = new Color(0xD1, 0xFA, 0xE5);
    private static final Color COLOR_TEXTO = new Color(0x1F, 0x29, 0x37);
    private static final Color COLOR_MUTED = new Color(0x6B, 0x72, 0x80);
    private static final Color COLOR_BORDE = new Color(0xE2, 0xE8, 0xF0);
    private static final Color COLOR_ZEBRA = new Color(0xF8, 0xFA, 0xFC);
    private static final Color COLOR_TOP = new Color(0xD1, 0xFA, 0xE5);
    private static final Color COLOR_TOP_TEXTO = new Color(0x03, 0x66, 0x41);
    private static final Color COLOR_BOTTOM = new Color(0xFE, 0xE2, 0xE2);
    private static final Color COLOR_BOTTOM_TEXTO = new Color(0x99, 0x1B, 0x1B);

    private static final Font FUENTE_MARCA = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
    private static final Font FUENTE_TITULO = new Font(Font.HELVETICA, 20, Font.BOLD, Color.WHITE);
    private static final Font FUENTE_SUBTITULO_BANNER = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(0xC9, 0xF7, 0xE7));
    private static final Font FUENTE_KPI_LABEL = new Font(Font.HELVETICA, 8, Font.BOLD, COLOR_MUTED);
    private static final Font FUENTE_KPI_VALOR = new Font(Font.HELVETICA, 15, Font.BOLD, COLOR_TEXTO);
    private static final Font FUENTE_SECCION = new Font(Font.HELVETICA, 13, Font.BOLD, COLOR_TEXTO);
    private static final Font FUENTE_ENCABEZADO_TABLA = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
    private static final Font FUENTE_CELDA = new Font(Font.HELVETICA, 9.5f, Font.NORMAL, COLOR_TEXTO);
    private static final Font FUENTE_CELDA_DESTACADA = new Font(Font.HELVETICA, 9.5f, Font.BOLD, COLOR_TEXTO);
    private static final Font FUENTE_ETIQUETA_TOP = new Font(Font.HELVETICA, 7.5f, Font.BOLD, COLOR_TOP_TEXTO);
    private static final Font FUENTE_ETIQUETA_BOTTOM = new Font(Font.HELVETICA, 7.5f, Font.BOLD, COLOR_BOTTOM_TEXTO);
    private static final Font FUENTE_VACIO = new Font(Font.HELVETICA, 10, Font.ITALIC, COLOR_MUTED);

    private final MetricasService metricasService;
    private final RestauranteDao restauranteDao;

    public ReporteService() {
        this(new MetricasService(), new RestauranteDao());
    }

    public ReporteService(MetricasService metricasService, RestauranteDao restauranteDao) {
        this.metricasService = metricasService;
        this.restauranteDao = restauranteDao;
    }

    /**
     * @throws IllegalArgumentException si no se selecciono un restaurante valido
     *         (restauranteId negativo) o si no hay datos suficientes para armar el reporte.
     */
    public byte[] generarReportePDF(int restauranteId) {
        if (restauranteId < 0) {
            throw new IllegalArgumentException("Debe seleccionar un restaurante y ver sus métricas antes de exportar");
        }
        if (restauranteId == MetricasService.TODOS_LOS_RESTAURANTES) {
            return generarReporteGeneral();
        }
        return generarReporteRestaurante(restauranteId);
    }

    private byte[] generarReporteRestaurante(int restauranteId) {
        Restaurante restaurante = restauranteDao.buscarPorId(restauranteId);
        long total = metricasService.obtenerTotalPedidosCompletadosPorRestaurante(restauranteId);
        if (restaurante == null || total == 0) {
            throw new IllegalArgumentException("No hay datos suficientes para generar el reporte de este restaurante");
        }
        List<MetricaPlato> masVendidos = metricasService.obtenerPlatosMasVendidosPorRestaurante(restauranteId);
        List<MetricaPlato> menosVendidos = metricasService.obtenerPlatosMenosVendidosPorRestaurante(restauranteId);
        List<MetricaInsumo> masUtilizados = metricasService.obtenerInsumosMasUtilizadosPorRestaurante(restauranteId);
        List<MetricaInsumo> menosUtilizados = metricasService.obtenerInsumosMenosUtilizadosPorRestaurante(restauranteId);

        return construirPdf(restaurante.getNombre(), total, masVendidos, menosVendidos,
                masUtilizados, menosUtilizados, null);
    }

    private byte[] generarReporteGeneral() {
        List<MetricaRestaurantePedidos> totalesPorRestaurante = metricasService.obtenerTotalesPorTodosLosRestaurantes();
        long total = totalesPorRestaurante.stream().mapToLong(MetricaRestaurantePedidos::getTotalPedidos).sum();
        if (total == 0) {
            throw new IllegalArgumentException("No hay datos suficientes para generar el reporte de este restaurante");
        }
        List<MetricaPlato> platos = metricasService.obtenerMetricasGeneralesPlatos();
        List<MetricaInsumo> insumos = metricasService.obtenerMetricasGeneralesInsumos();

        return construirPdf("Todos los restaurantes", total, platos, invertidaPlatos(platos),
                insumos, invertidaInsumos(insumos), totalesPorRestaurante);
    }

    private byte[] construirPdf(String nombreRestaurante, long totalPedidos,
                                 List<MetricaPlato> masVendidos, List<MetricaPlato> menosVendidos,
                                 List<MetricaInsumo> masUtilizados, List<MetricaInsumo> menosUtilizados,
                                 List<MetricaRestaurantePedidos> totalesPorRestaurante) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 34, 34, 34, 46);
        try {
            PdfWriter writer = PdfWriter.getInstance(documento, salida);
            writer.setPageEvent(new PiePagina());
            documento.open();

            escribirBanner(documento, nombreRestaurante);
            escribirTarjetasResumen(documento, totalPedidos, masVendidos, masUtilizados);
            if (totalesPorRestaurante != null && !totalesPorRestaurante.isEmpty()) {
                escribirTotalesPorRestaurante(documento, totalesPorRestaurante, totalPedidos);
            }
            escribirSeccionPlatos(documento, masVendidos, menosVendidos);
            escribirSeccionInsumos(documento, masUtilizados, menosUtilizados);

            documento.close();
        } catch (DocumentException ex) {
            throw new IllegalStateException("No se pudo generar el reporte PDF", ex);
        }
        return salida.toByteArray();
    }

    // ---------- Banda de encabezado con marca ----------

    private void escribirBanner(Document documento, String nombreRestaurante) throws DocumentException {
        PdfPTable banner = new PdfPTable(1);
        banner.setWidthPercentage(100);

        PdfPCell fondo = new PdfPCell();
        fondo.setBackgroundColor(COLOR_MARCA);
        fondo.setBorder(Rectangle.NO_BORDER);
        fondo.setPadding(16);

        Paragraph marca = new Paragraph("DARK KITCHEN · MÉTRICAS DE RENDIMIENTO", FUENTE_MARCA);
        marca.setSpacingAfter(6);

        Paragraph titulo = new Paragraph(nombreRestaurante, FUENTE_TITULO);
        titulo.setSpacingAfter(4);

        Paragraph subtitulo = new Paragraph(
                "Generado el " + LocalDateTime.now().format(FORMATO_FECHA), FUENTE_SUBTITULO_BANNER);

        fondo.addElement(marca);
        fondo.addElement(titulo);
        fondo.addElement(subtitulo);
        banner.addCell(fondo);
        documento.add(banner);
        documento.add(espacio(14));
    }

    // ---------- Tarjetas de resumen (KPIs) ----------

    private void escribirTarjetasResumen(Document documento, long totalPedidos,
                                          List<MetricaPlato> masVendidos, List<MetricaInsumo> masUtilizados)
            throws DocumentException {
        PdfPTable fila = new PdfPTable(3);
        fila.setWidthPercentage(100);
        fila.setSpacingAfter(18);
        fila.setWidths(new float[]{1f, 1f, 1f});

        fila.addCell(tarjetaKpi("TOTAL DE PEDIDOS COMPLETADOS", String.valueOf(totalPedidos)));
        fila.addCell(tarjetaKpi("PLATO MÁS VENDIDO",
                masVendidos.isEmpty() ? "Sin datos" : masVendidos.get(0).getNombre()));
        fila.addCell(tarjetaKpi("INSUMO MÁS UTILIZADO",
                masUtilizados.isEmpty() ? "Sin datos" : masUtilizados.get(0).getNombre()));

        documento.add(fila);
    }

    private PdfPCell tarjetaKpi(String etiqueta, String valor) {
        PdfPCell celda = new PdfPCell();
        celda.setBorder(Rectangle.BOX);
        celda.setBorderColor(COLOR_BORDE);
        celda.setBorderWidthTop(3f);
        celda.setBorderColorTop(COLOR_ACENTO);
        celda.setPadding(10);
        celda.setPaddingBottom(12);

        Paragraph pLabel = new Paragraph(etiqueta, FUENTE_KPI_LABEL);
        pLabel.setSpacingAfter(5);
        Paragraph pValor = new Paragraph(valor, FUENTE_KPI_VALOR);

        celda.addElement(pLabel);
        celda.addElement(pValor);
        return celda;
    }

    // ---------- Tabla de totales por restaurante (vista general) ----------

    private void escribirTotalesPorRestaurante(Document documento, List<MetricaRestaurantePedidos> totales, long total)
            throws DocumentException {
        documento.add(tituloSeccion("Pedidos completados por restaurante"));
        PdfPTable tabla = nuevaTabla(new float[]{2.4f, 1f}, "Restaurante", "Total de pedidos completados");
        boolean par = false;
        for (MetricaRestaurantePedidos fila : totales) {
            agregarFila(tabla, par, false, false, fila.getNombreRestaurante(), String.valueOf(fila.getTotalPedidos()));
            par = !par;
        }
        agregarFilaTotal(tabla, "Total", String.valueOf(total));
        documento.add(tabla);
        documento.add(espacio(16));
    }

    // ---------- HU35: platos mas y menos vendidos ----------

    private void escribirSeccionPlatos(Document documento, List<MetricaPlato> masVendidos, List<MetricaPlato> menosVendidos)
            throws DocumentException {
        documento.add(tituloSeccion("Platos más y menos vendidos"));
        if (masVendidos.isEmpty()) {
            documento.add(new Paragraph("No hay pedidos entregados para este restaurante", FUENTE_VACIO));
            documento.add(espacio(16));
            return;
        }
        String nombreMenos = menosVendidos.isEmpty() ? null : menosVendidos.get(0).getNombre();

        PdfPTable tabla = nuevaTabla(new float[]{2.6f, 1f, 1f}, "Plato", "Pedidos entregados", "");
        boolean par = false;
        for (int i = 0; i < masVendidos.size(); i++) {
            MetricaPlato fila = masVendidos.get(i);
            boolean esTop = i == 0;
            boolean esBottom = masVendidos.size() > 1 && fila.getNombre().equals(nombreMenos) && !esTop;
            agregarFila(tabla, par, esTop, esBottom, fila.getNombre(), String.valueOf(fila.getPedidosEntregados()),
                    esTop ? "MÁS VENDIDO" : (esBottom ? "MENOS VENDIDO" : ""));
            par = !par;
        }
        documento.add(tabla);
        documento.add(espacio(16));
    }

    // ---------- HU36: insumos mas y menos utilizados ----------

    private void escribirSeccionInsumos(Document documento, List<MetricaInsumo> masUtilizados, List<MetricaInsumo> menosUtilizados)
            throws DocumentException {
        documento.add(tituloSeccion("Insumos más y menos utilizados"));
        if (masUtilizados.isEmpty()) {
            documento.add(new Paragraph("No hay consumo de insumos registrado para este restaurante", FUENTE_VACIO));
            return;
        }
        String nombreMenos = menosUtilizados.isEmpty() ? null : menosUtilizados.get(0).getNombre();

        PdfPTable tabla = nuevaTabla(new float[]{2.3f, 1f, .8f, 1.1f}, "Insumo", "Cantidad total", "Unidad", "");
        boolean par = false;
        for (int i = 0; i < masUtilizados.size(); i++) {
            MetricaInsumo fila = masUtilizados.get(i);
            boolean esTop = i == 0;
            boolean esBottom = masUtilizados.size() > 1 && fila.getNombre().equals(nombreMenos) && !esTop;
            agregarFila(tabla, par, esTop, esBottom, fila.getNombre(), formatear(fila.getCantidadTotal()), fila.getUnidad(),
                    esTop ? "MÁS UTILIZADO" : (esBottom ? "MENOS UTILIZADO" : ""));
            par = !par;
        }
        documento.add(tabla);
    }

    // ---------- helpers de construccion visual ----------

    private Paragraph tituloSeccion(String texto) {
        Paragraph parrafo = new Paragraph(texto, FUENTE_SECCION);
        parrafo.setSpacingBefore(2);
        parrafo.setSpacingAfter(8);
        return parrafo;
    }

    private Paragraph espacio(float alto) {
        Paragraph p = new Paragraph(" ");
        p.setSpacingAfter(alto);
        return p;
    }

    private PdfPTable nuevaTabla(float[] anchos, String... encabezados) {
        PdfPTable tabla = new PdfPTable(anchos.length);
        tabla.setWidthPercentage(100);
        try {
            tabla.setWidths(anchos);
        } catch (DocumentException ignored) {
            // anchos.length siempre coincide con la cantidad de columnas declarada arriba.
        }
        for (String encabezado : encabezados) {
            PdfPCell celda = new PdfPCell(new Phrase(encabezado, FUENTE_ENCABEZADO_TABLA));
            celda.setBackgroundColor(COLOR_MARCA);
            celda.setPadding(7);
            celda.setBorder(Rectangle.NO_BORDER);
            celda.setHorizontalAlignment(Element.ALIGN_LEFT);
            tabla.addCell(celda);
        }
        return tabla;
    }

    /** Agrega una fila con zebra striping y resaltado de extremo (top/bottom). La ultima columna, si viene, es la etiqueta. */
    private void agregarFila(PdfPTable tabla, boolean parImpar, boolean esTop, boolean esBottom, String... valores) {
        Color fondo = esTop ? COLOR_TOP : (esBottom ? COLOR_BOTTOM : (parImpar ? COLOR_ZEBRA : Color.WHITE));
        int ultimaColumna = valores.length - 1;
        for (int i = 0; i < valores.length; i++) {
            String valor = valores[i];
            PdfPCell celda;
            if (i == ultimaColumna && !valor.isEmpty()) {
                Font fuenteEtiqueta = esTop ? FUENTE_ETIQUETA_TOP : FUENTE_ETIQUETA_BOTTOM;
                celda = new PdfPCell(new Phrase(valor, fuenteEtiqueta));
                celda.setHorizontalAlignment(Element.ALIGN_RIGHT);
            } else {
                Font fuente = (esTop || esBottom) ? FUENTE_CELDA_DESTACADA : FUENTE_CELDA;
                celda = new PdfPCell(new Phrase(valor, fuente));
            }
            celda.setBackgroundColor(fondo);
            celda.setPadding(6.5f);
            celda.setBorder(Rectangle.BOTTOM);
            celda.setBorderColor(COLOR_BORDE);
            tabla.addCell(celda);
        }
    }

    private void agregarFilaTotal(PdfPTable tabla, String... valores) {
        for (String valor : valores) {
            PdfPCell celda = new PdfPCell(new Phrase(valor, FUENTE_CELDA_DESTACADA));
            celda.setBackgroundColor(COLOR_ACENTO_SUAVE);
            celda.setPadding(7);
            celda.setBorder(Rectangle.TOP);
            celda.setBorderWidth(1.4f);
            celda.setBorderColor(COLOR_ACENTO);
            tabla.addCell(celda);
        }
    }

    private String formatear(double valor) {
        if (valor == Math.rint(valor)) {
            return String.valueOf((long) valor);
        }
        return String.format(Locale.US, "%.3f", valor);
    }

    private List<MetricaPlato> invertidaPlatos(List<MetricaPlato> platos) {
        List<MetricaPlato> copia = new java.util.ArrayList<>(platos);
        java.util.Collections.reverse(copia);
        return copia;
    }

    private List<MetricaInsumo> invertidaInsumos(List<MetricaInsumo> insumos) {
        List<MetricaInsumo> copia = new java.util.ArrayList<>(insumos);
        java.util.Collections.reverse(copia);
        return copia;
    }

    /** Pie de pagina con numero de pagina y marca, en todas las paginas del documento. */
    private static final class PiePagina extends PdfPageEventHelper {
        private static final Font FUENTE_PIE = new Font(Font.HELVETICA, 8, Font.NORMAL, COLOR_MUTED);

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Phrase pie = new Phrase("Dark Kitchen — Reporte de métricas   ·   Página "
                    + writer.getPageNumber(), FUENTE_PIE);
            com.lowagie.text.pdf.ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, pie,
                    (document.right() + document.left()) / 2, document.bottom() - 20, 0);
        }
    }
}
