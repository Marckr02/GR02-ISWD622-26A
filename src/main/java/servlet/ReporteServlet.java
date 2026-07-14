package servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Restaurante;
import service.MetricasService;
import service.ReporteService;
import service.RestauranteService;

import java.io.IOException;
import java.io.OutputStream;
import java.text.Normalizer;

/**
 * Controlador de la exportacion de metricas en PDF (F5.2, HU39). POST con
 * "restauranteId": genera y descarga el reporte. Si no hay un restaurante
 * valido seleccionado o no hay datos suficientes, redirige de vuelta a
 * /metricas con un mensaje de error en sesion (igual que el resto de
 * formularios del sistema). El acceso lo restringe AuthFilter (solo ADMINISTRADOR).
 */
@WebServlet("/reporte")
public class ReporteServlet extends HttpServlet {

    private final ReporteService reporteService = new ReporteService();
    private final RestauranteService restauranteService = new RestauranteService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String restauranteIdParam = request.getParameter("restauranteId");
        Integer restauranteId = parsearId(restauranteIdParam);

        try {
            if (restauranteId == null) {
                throw new IllegalArgumentException("Debe seleccionar un restaurante y ver sus métricas antes de exportar");
            }
            byte[] pdf = reporteService.generarReportePDF(restauranteId);
            String nombreArchivo = nombreArchivo(restauranteId);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + nombreArchivo + "\"");
            response.setContentLength(pdf.length);
            try (OutputStream salida = response.getOutputStream()) {
                salida.write(pdf);
            }
        } catch (IllegalArgumentException ex) {
            request.getSession().setAttribute("error", ex.getMessage());
            response.sendRedirect(urlMetricas(request, restauranteIdParam));
        }
    }

    private String nombreArchivo(int restauranteId) {
        if (restauranteId == MetricasService.TODOS_LOS_RESTAURANTES) {
            return "reporte-metricas-general.pdf";
        }
        Restaurante restaurante = restauranteService.buscar(restauranteId);
        String nombre = (restaurante != null) ? restaurante.getNombre() : "restaurante";
        return "reporte-metricas-" + slug(nombre) + ".pdf";
    }

    /** Convierte el nombre del restaurante a un slug seguro para nombre de archivo. */
    private String slug(String texto) {
        String sinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        String slug = sinAcentos.toLowerCase().trim()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+)|(-+$)", "");
        return slug.isEmpty() ? "restaurante" : slug;
    }

    private Integer parsearId(String valor) {
        try {
            int id = Integer.parseInt(valor.trim());
            return id < 0 ? null : id;
        } catch (NumberFormatException | NullPointerException ex) {
            return null;
        }
    }

    private String urlMetricas(HttpServletRequest request, String restauranteIdParam) {
        String rol = request.getParameter("rol");
        StringBuilder sb = new StringBuilder(request.getContextPath()).append("/metricas");
        StringBuilder qs = new StringBuilder();
        if (rol != null && !rol.isEmpty()) {
            qs.append("rol=").append(rol);
        }
        if (restauranteIdParam != null && !restauranteIdParam.isEmpty()) {
            if (qs.length() > 0) {
                qs.append("&");
            }
            qs.append("restauranteId=").append(restauranteIdParam);
        }
        if (qs.length() > 0) {
            sb.append("?").append(qs);
        }
        return sb.toString();
    }
}
