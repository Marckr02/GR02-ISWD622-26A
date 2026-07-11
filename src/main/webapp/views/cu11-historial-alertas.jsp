<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="model.AlertaStock" %>
<%@ page import="model.Insumo" %>
<%
    List<AlertaStock> alertas = (List<AlertaStock>) request.getAttribute("alertas");
    List<Insumo> criticosActuales = (List<Insumo>) request.getAttribute("criticosActuales");
    String ctx = request.getContextPath();
    DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    int totalCriticosActuales = (criticosActuales == null) ? 0 : criticosActuales.size();
    int totalAlertas = (alertas == null) ? 0 : alertas.size();
%>
<%!
    private String attr(String valor) {
        return valor == null ? "" : valor.replace("\"", "&quot;");
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Alertas | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/gestion.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/metricas.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/main.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/toast.css">
</head>
<body>
<jsp:include page="navbar.jsp"><jsp:param name="activo" value="alertas"/></jsp:include>

<main class="gestion-main">
    <section class="panel">
        <div class="metricas-stats" style="margin-bottom:1.2rem;">
            <div class="stat-card">
                <p class="stat-card__label">Insumos críticos ahora mismo</p>
                <p class="stat-card__valor"><%= totalCriticosActuales %></p>
            </div>
            <div class="stat-card">
                <p class="stat-card__label">Alertas registradas en el historial</p>
                <p class="stat-card__valor"><%= totalAlertas %></p>
            </div>
        </div>

        <div class="gestion-toolbar">
            <div class="gestion-toolbar__buscar">
                <div class="buscador">
                    <input type="text" id="buscador-alertas" class="input-filtro"
                           placeholder="Buscar insumo..." aria-label="Buscar alerta por insumo">
                </div>
                <select id="filtro-severidad" aria-label="Filtrar por severidad">
                    <option value="">Todas las severidades</option>
                    <option value="critico">Sin stock</option>
                    <option value="bajo">Bajo mínimo</option>
                </select>
                <label class="filtro-fecha">Desde
                    <input type="date" id="filtro-fecha-desde" aria-label="Fecha desde">
                </label>
                <label class="filtro-fecha">Hasta
                    <input type="date" id="filtro-fecha-hasta" aria-label="Fecha hasta">
                </label>
                <button type="button" id="btn-limpiar-filtros-alertas" class="btn-link">Limpiar filtros</button>
            </div>
        </div>

        <% if (alertas == null || alertas.isEmpty()) { %>
            <p class="vacio">Sin alertas críticas registradas</p>
        <% } else { %>
            <table class="tabla" id="tabla-alertas">
                <thead>
                    <tr>
                        <th>Insumo</th>
                        <th>Severidad</th>
                        <th class="num">Stock al momento</th>
                        <th>Fecha y hora</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (AlertaStock a : alertas) {
                        boolean sinStock = a.getStockAlMomento() <= 0;
                        String claveSeveridad = sinStock ? "critico" : "bajo";
                        String claseSeveridad = sinStock ? "pill--critico" : "pill--bajo";
                        String textoSeveridad = sinStock ? "Sin stock" : "Bajo mínimo";
                    %>
                        <tr data-nombre="<%= attr(a.getInsumoNombre().toLowerCase()) %>"
                            data-severidad="<%= claveSeveridad %>"
                            data-fecha="<%= a.getTimestamp().format(formatoFecha) %>">
                            <td><%= a.getInsumoNombre() %></td>
                            <td><span class="pill <%= claseSeveridad %>"><%= textoSeveridad %></span></td>
                            <td class="num"><%= a.getStockAlMomento() %></td>
                            <td><%= a.getTimestamp().format(formato) %></td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
            <p class="vacio vacio--filtro" id="sin-resultados-alertas" hidden>Ninguna alerta coincide con los filtros aplicados.</p>
        <% } %>
    </section>
</main>
<script src="<%= ctx %>/resources/js/toast.js"></script>
<script src="<%= ctx %>/resources/js/alertas.js"></script>
</body>
</html>
