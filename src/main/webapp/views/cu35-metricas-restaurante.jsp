<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Restaurante" %>
<%@ page import="model.MetricaPlato" %>
<%@ page import="model.MetricaInsumo" %>
<%@ page import="model.MetricaRestaurantePedidos" %>
<%
    List<Restaurante> restaurantes = (List<Restaurante>) request.getAttribute("restaurantes");
    Integer restauranteIdSeleccionado = (Integer) request.getAttribute("restauranteIdSeleccionado");
    Boolean vistaGeneral = (Boolean) request.getAttribute("vistaGeneral");
    boolean esVistaGeneral = Boolean.TRUE.equals(vistaGeneral);
    boolean sinSeleccion = Boolean.TRUE.equals(request.getAttribute("sinSeleccion"));
    boolean hayResultados = restauranteIdSeleccionado != null;
    String nombreSeleccionado = (String) request.getAttribute("nombreRestauranteSeleccionado");

    List<MetricaPlato> platos = (List<MetricaPlato>) request.getAttribute("platosMasVendidos");
    List<MetricaInsumo> insumos = (List<MetricaInsumo>) request.getAttribute("insumosMasUtilizados");
    List<MetricaRestaurantePedidos> totalesPorRestaurante =
            (List<MetricaRestaurantePedidos>) request.getAttribute("totalesPorRestaurante");
    Long totalPedidos = (Long) request.getAttribute("totalPedidos");

    final int TOP_N = 5;
    int totalPlatos = (platos == null) ? 0 : platos.size();
    int totalInsumos = (insumos == null) ? 0 : insumos.size();
    int mostrarPlatos = Math.min(TOP_N, totalPlatos);
    int mostrarInsumos = Math.min(TOP_N, totalInsumos);
    String menosVendidoNombre = totalPlatos > 0 ? platos.get(totalPlatos - 1).getNombre() : null;
    String menosUtilizadoNombre = totalInsumos > 0 ? insumos.get(totalInsumos - 1).getNombre() : null;

    String ctx = request.getContextPath();
    String rol = request.getParameter("rol");
    if ((rol == null || rol.isEmpty()) && session.getAttribute("rol") != null) {
        rol = (String) session.getAttribute("rol");
    }
    String rolQs = (rol == null || rol.isEmpty()) ? "" : ("rol=" + rol);

    String error = (String) session.getAttribute("error");
    if (error != null) { session.removeAttribute("error"); }
%>
<%!
    private String attr(String valor) {
        return valor == null ? "" : valor.replace("\"", "&quot;");
    }

    private String js(String valor) {
        if (valor == null) { return ""; }
        return valor.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "").replace("\n", " ");
    }

    private String formatearCantidad(double valor) {
        if (valor == Math.rint(valor)) {
            return String.valueOf((long) valor);
        }
        return String.format(java.util.Locale.US, "%.3f", valor);
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Métricas | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/gestion.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/metricas.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/main.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/toast.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.4/dist/chart.umd.min.js"></script>
</head>
<body data-hay-resultados="<%= hayResultados %>">
<jsp:include page="navbar.jsp"><jsp:param name="activo" value="metricas"/></jsp:include>

<% if (error != null) { %>
<script>
    document.addEventListener("DOMContentLoaded", function () {
        showToast("danger", "No se pudo completar la acción", "<%= js(error) %>");
    });
</script>
<% } %>

<main class="gestion-main">
    <section class="panel">
        <div class="gestion-toolbar">
            <form method="get" action="<%= ctx %>/metricas" id="form-metricas-buscar" class="gestion-toolbar__buscar">
                <% if (rol != null && !rol.isEmpty()) { %>
                    <input type="hidden" name="rol" value="<%= attr(rol) %>">
                <% } %>
                <input type="hidden" name="activo" value="metricas">
                <input type="hidden" name="buscar" value="1">
                <label class="visually-hidden" for="select-restaurante">Restaurante</label>
                <select name="restauranteId" id="select-restaurante">
                    <option value="" <%= (restauranteIdSeleccionado == null) ? "selected" : "" %> disabled>Seleccione un restaurante</option>
                    <option value="0" <%= (restauranteIdSeleccionado != null && restauranteIdSeleccionado == 0) ? "selected" : "" %>>Todos los restaurantes</option>
                    <% if (restaurantes != null) {
                        for (Restaurante r : restaurantes) { %>
                    <option value="<%= r.getId() %>" <%= (restauranteIdSeleccionado != null && restauranteIdSeleccionado == r.getId()) ? "selected" : "" %>><%= r.getNombre() %></option>
                    <% } } %>
                </select>
                <button type="submit" class="btn btn--ok btn--sm">Ver métricas</button>
            </form>
            <div class="gestion-toolbar__acciones">
                <form method="post" action="<%= ctx %>/reporte" id="form-exportar-pdf">
                    <% if (rol != null && !rol.isEmpty()) { %>
                        <input type="hidden" name="rol" value="<%= attr(rol) %>">
                    <% } %>
                    <input type="hidden" name="restauranteId" id="exportar-restaurante-id"
                           value="<%= restauranteIdSeleccionado == null ? "" : restauranteIdSeleccionado %>">
                    <button type="submit" class="btn-link destacado">⬇ Exportar PDF</button>
                </form>
            </div>
        </div>

        <% if (sinSeleccion) { %>
            <p class="vacio">Debe seleccionar un restaurante para ver sus métricas</p>
        <% } else if (!hayResultados) { %>
            <p class="vacio">Selecciona un restaurante y haz clic en "Ver métricas" para consultar sus indicadores.</p>
        <% } else { %>

            <div class="metricas-stats">
                <div class="stat-card">
                    <p class="stat-card__label">Restaurante</p>
                    <p class="stat-card__valor" style="font-size:1.3rem;"><%= nombreSeleccionado == null ? "-" : nombreSeleccionado %></p>
                </div>
                <div class="stat-card">
                    <p class="stat-card__label">Total de pedidos completados</p>
                    <p class="stat-card__valor"><%= totalPedidos == null ? 0 : totalPedidos %></p>
                </div>
            </div>

            <% if (esVistaGeneral && totalesPorRestaurante != null && !totalesPorRestaurante.isEmpty()) {
                long sumaTotal = 0;
                for (MetricaRestaurantePedidos fila : totalesPorRestaurante) { sumaTotal += fila.getTotalPedidos(); }
            %>
            <div class="metricas-seccion">
                <h2>Pedidos completados por restaurante</h2>
                <table class="tabla">
                    <thead><tr><th>Restaurante</th><th class="num">Total de pedidos completados</th></tr></thead>
                    <tbody>
                    <% for (MetricaRestaurantePedidos fila : totalesPorRestaurante) { %>
                        <tr><td><%= fila.getNombreRestaurante() %></td><td class="num"><%= fila.getTotalPedidos() %></td></tr>
                    <% } %>
                    </tbody>
                    <tfoot><tr><td>Total</td><td class="num"><%= sumaTotal %></td></tr></tfoot>
                </table>
            </div>
            <% } %>

            <!-- HU35: platos mas y menos vendidos (Top 5 + ver todos) -->
            <div class="metricas-seccion">
                <h2>Platos más y menos vendidos
                    <% if (totalPlatos > TOP_N) { %>
                        <span class="pill">Top <%= TOP_N %> de <%= totalPlatos %></span>
                    <% } %>
                </h2>
                <% if (platos == null || platos.isEmpty()) { %>
                    <p class="vacio">No hay pedidos entregados para este restaurante</p>
                <% } else { %>
                    <div class="metricas-grid">
                        <div>
                            <table class="tabla">
                                <thead><tr><th>Plato</th><th class="num">Pedidos entregados</th></tr></thead>
                                <tbody>
                                <% for (int i = 0; i < mostrarPlatos; i++) {
                                    MetricaPlato fila = platos.get(i);
                                    boolean top = i == 0;
                                    boolean bottom = totalPlatos <= TOP_N && fila.getNombre().equals(menosVendidoNombre) && !top;
                                    String claseFila = top ? "fila--top" : (bottom ? "fila--bottom" : "");
                                %>
                                    <tr class="<%= claseFila %>">
                                        <td><%= fila.getNombre() %><% if (top) { %><span class="badge-extremo">Más vendido</span><% } %><% if (bottom) { %><span class="badge-extremo">Menos vendido</span><% } %></td>
                                        <td class="num"><%= fila.getPedidosEntregados() %></td>
                                    </tr>
                                <% } %>
                                </tbody>
                            </table>
                            <% if (totalPlatos > TOP_N) { %>
                                <p class="metricas-nota">Menos vendido en total: <strong><%= menosVendidoNombre %></strong>
                                    (<%= platos.get(totalPlatos - 1).getPedidosEntregados() %> pedidos)</p>
                                <button type="button" class="btn-link" id="btn-ver-todos-platos">Ver los <%= totalPlatos %> platos completos</button>
                            <% } %>
                        </div>
                        <div class="metricas-chart"><canvas id="grafico-platos"></canvas></div>
                    </div>
                <% } %>
            </div>

            <!-- HU36: insumos mas y menos utilizados (Top 5 + ver todos) -->
            <div class="metricas-seccion">
                <h2>Insumos más y menos utilizados
                    <% if (totalInsumos > TOP_N) { %>
                        <span class="pill">Top <%= TOP_N %> de <%= totalInsumos %></span>
                    <% } %>
                </h2>
                <% if (insumos == null || insumos.isEmpty()) { %>
                    <p class="vacio">No hay consumo de insumos registrado para este restaurante</p>
                <% } else { %>
                    <div class="metricas-grid">
                        <div>
                            <table class="tabla">
                                <thead><tr><th>Insumo</th><th class="num">Cantidad total</th><th>Unidad</th></tr></thead>
                                <tbody>
                                <% for (int i = 0; i < mostrarInsumos; i++) {
                                    MetricaInsumo fila = insumos.get(i);
                                    boolean top = i == 0;
                                    boolean bottom = totalInsumos <= TOP_N && fila.getNombre().equals(menosUtilizadoNombre) && !top;
                                    String claseFila = top ? "fila--top" : (bottom ? "fila--bottom" : "");
                                %>
                                    <tr class="<%= claseFila %>">
                                        <td><%= fila.getNombre() %><% if (top) { %><span class="badge-extremo">Más utilizado</span><% } %><% if (bottom) { %><span class="badge-extremo">Menos utilizado</span><% } %></td>
                                        <td class="num"><%= formatearCantidad(fila.getCantidadTotal()) %></td>
                                        <td><%= fila.getUnidad() %></td>
                                    </tr>
                                <% } %>
                                </tbody>
                            </table>
                            <% if (totalInsumos > TOP_N) { %>
                                <p class="metricas-nota">Menos utilizado en total: <strong><%= menosUtilizadoNombre %></strong>
                                    (<%= formatearCantidad(insumos.get(totalInsumos - 1).getCantidadTotal()) %> <%= insumos.get(totalInsumos - 1).getUnidad() %>)</p>
                                <button type="button" class="btn-link" id="btn-ver-todos-insumos">Ver los <%= totalInsumos %> insumos completos</button>
                            <% } %>
                        </div>
                        <div class="metricas-chart"><canvas id="grafico-insumos"></canvas></div>
                    </div>
                <% } %>
            </div>

        <% } %>
    </section>
</main>

<!-- Modal: listado completo de platos -->
<div class="modal-overlay" id="modal-platos-completo" role="dialog" aria-modal="true" aria-labelledby="modal-platos-titulo">
    <div class="modal modal--ancho">
        <h3 id="modal-platos-titulo">Todos los platos (<%= totalPlatos %>)</h3>
        <div class="modal__scroll">
            <table class="tabla">
                <thead><tr><th>Plato</th><th class="num">Pedidos entregados</th></tr></thead>
                <tbody>
                <% if (platos != null) {
                    for (int i = 0; i < totalPlatos; i++) {
                        MetricaPlato fila = platos.get(i);
                        boolean top = i == 0;
                        boolean bottom = i == totalPlatos - 1 && totalPlatos > 1;
                        String claseFila = top ? "fila--top" : (bottom ? "fila--bottom" : "");
                %>
                    <tr class="<%= claseFila %>">
                        <td><%= fila.getNombre() %><% if (top) { %><span class="badge-extremo">Más vendido</span><% } %><% if (bottom) { %><span class="badge-extremo">Menos vendido</span><% } %></td>
                        <td class="num"><%= fila.getPedidosEntregados() %></td>
                    </tr>
                <%  }
                } %>
                </tbody>
            </table>
        </div>
        <div class="modal__acciones">
            <button type="button" class="btn btn--ghost" id="modal-platos-cerrar">Cerrar</button>
        </div>
    </div>
</div>

<!-- Modal: listado completo de insumos -->
<div class="modal-overlay" id="modal-insumos-completo" role="dialog" aria-modal="true" aria-labelledby="modal-insumos-titulo">
    <div class="modal modal--ancho">
        <h3 id="modal-insumos-titulo">Todos los insumos (<%= totalInsumos %>)</h3>
        <div class="modal__scroll">
            <table class="tabla">
                <thead><tr><th>Insumo</th><th class="num">Cantidad total</th><th>Unidad</th></tr></thead>
                <tbody>
                <% if (insumos != null) {
                    for (int i = 0; i < totalInsumos; i++) {
                        MetricaInsumo fila = insumos.get(i);
                        boolean top = i == 0;
                        boolean bottom = i == totalInsumos - 1 && totalInsumos > 1;
                        String claseFila = top ? "fila--top" : (bottom ? "fila--bottom" : "");
                %>
                    <tr class="<%= claseFila %>">
                        <td><%= fila.getNombre() %><% if (top) { %><span class="badge-extremo">Más utilizado</span><% } %><% if (bottom) { %><span class="badge-extremo">Menos utilizado</span><% } %></td>
                        <td class="num"><%= formatearCantidad(fila.getCantidadTotal()) %></td>
                        <td><%= fila.getUnidad() %></td>
                    </tr>
                <%  }
                } %>
                </tbody>
            </table>
        </div>
        <div class="modal__acciones">
            <button type="button" class="btn btn--ghost" id="modal-insumos-cerrar">Cerrar</button>
        </div>
    </div>
</div>

<script>
    window.METRICAS_DATA = {
        platos: [
            <% if (platos != null) {
                for (int i = 0; i < platos.size(); i++) {
                    MetricaPlato fila = platos.get(i); %>
            { nombre: "<%= js(fila.getNombre()) %>", cantidad: <%= fila.getPedidosEntregados() %> }<%= (i < platos.size() - 1) ? "," : "" %>
            <%  }
            } %>
        ],
        insumos: [
            <% if (insumos != null) {
                for (int i = 0; i < insumos.size(); i++) {
                    MetricaInsumo fila = insumos.get(i); %>
            { nombre: "<%= js(fila.getNombre()) %>", cantidad: <%= fila.getCantidadTotal() %>, unidad: "<%= js(fila.getUnidad()) %>" }<%= (i < insumos.size() - 1) ? "," : "" %>
            <%  }
            } %>
        ]
    };
</script>
<script src="<%= ctx %>/resources/js/toast.js"></script>
<script src="<%= ctx %>/resources/js/metricas.js"></script>
</body>
</html>
