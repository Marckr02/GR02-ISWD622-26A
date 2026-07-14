<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.DisponibilidadPlato" %>
<%@ page import="model.EstadoPlato" %>
<%@ page import="model.Restaurante" %>
<%@ page import="service.RestauranteService" %>
<%@ page import="util.ColorMarca" %>
<%
    List<DisponibilidadPlato> menu = (List<DisponibilidadPlato>) request.getAttribute("menu");
    RestauranteService restauranteService = (RestauranteService) request.getAttribute("restauranteService");
    String ctx = request.getContextPath();
%>
<%!
    /** Color de marca del restaurante (el asignado, o uno de respaldo si no tiene o no existe). */
    private String colorDeMarca(Restaurante r) {
        if (r == null) { return "#8b97a6"; }
        return (r.getColor() != null && !r.getColor().isBlank()) ? r.getColor() : ColorMarca.paraNombre(r.getNombre());
    }

    /** Escapa comillas dobles para poder incrustar el valor dentro de un atributo HTML. */
    private String attr(String valor) {
        return valor == null ? "" : valor.replace("\"", "&quot;");
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Disponibilidad del turno | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/menu.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/dish-circle.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/main.css">
</head>
<body>
<jsp:include page="navbar.jsp"><jsp:param name="activo" value="disponibilidad"/></jsp:include>

<% if (menu == null || menu.isEmpty()) { %>
    <div class="menu-vacio">No hay platos registrados. Contacta al administrador.</div>
<% } else { %>
    <main class="menu-wrap">
        <section class="panel">
            <div class="menu-toolbar">
                <div class="menu-toolbar__buscar">
                    <div class="buscador">
                        <input type="text" id="buscador-disponibilidad" class="input-filtro"
                               placeholder="Buscar plato..." aria-label="Buscar plato por nombre">
                    </div>
                    <div class="filtros-menu" id="filtros-menu" role="group" aria-label="Filtrar platos por estado">
                        <button type="button" class="filtro-pill is-on" data-filtro="todos">Todos</button>
                        <button type="button" class="filtro-pill filtro-pill--ok" data-filtro="disponible">Disponibles</button>
                        <button type="button" class="filtro-pill filtro-pill--error" data-filtro="bloqueado">Bloqueados</button>
                    </div>
                </div>
            </div>

            <div class="menu-grid" id="menu-grid">
                <% for (DisponibilidadPlato d : menu) {
                       boolean bloqueado = d.getEstado() == EstadoPlato.BLOQUEADO;
                       Restaurante restaurante = (restauranteService == null) ? null
                               : restauranteService.buscar(d.getPlato().getRestauranteId());
                       String nombreMarca = restaurante == null ? "Sin marca" : restaurante.getNombre(); %>
                    <article class="dish-circle <%= bloqueado ? "dish-circle--bloqueado" : "" %>"
                             data-estado="<%= bloqueado ? "bloqueado" : "disponible" %>"
                             data-nombre="<%= d.getPlato().getNombre().toLowerCase() %>"
                             style="--marca: <%= colorDeMarca(restaurante) %>;">
                        <p class="dish-circle__marca"><%= nombreMarca %></p>
                        <h3 class="dish-circle__nombre"><%= d.getPlato().getNombre() %></h3>
                        <span class="estado <%= bloqueado ? "estado--bloqueado" : "estado--disponible" %>">
                            <%= d.getEstado().getEtiqueta() %>
                        </span>
                        <% if (bloqueado) { %>
                            <button type="button" class="dish-circle__alerta" title="Ver insumos faltantes"
                                    aria-label="Ver insumos faltantes" data-motivo="<%= attr(d.getMotivo()) %>">&#9888;</button>
                        <% } %>
                    </article>
                <% } %>
                <p class="menu-vacio menu-vacio--filtro" id="sin-resultados-menu" hidden>Ningun plato coincide con el filtro.</p>
            </div>
        </section>
    </main>
<% } %>
<script src="<%= ctx %>/resources/js/menu-filtros.js"></script>
<script src="<%= ctx %>/resources/js/disponibilidad.js"></script>
</body>
</html>
