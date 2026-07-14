<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.DisponibilidadPlato" %>
<%@ page import="model.EstadoPlato" %>
<%@ page import="model.Restaurante" %>
<%@ page import="service.RestauranteService" %>
<%
    List<DisponibilidadPlato> menu = (List<DisponibilidadPlato>) request.getAttribute("menu");
    RestauranteService restauranteService = (RestauranteService) request.getAttribute("restauranteService");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Disponibilidad del turno | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/menu.css">
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
                               : restauranteService.buscar(d.getPlato().getRestauranteId()); %>
                    <article class="plato <%= bloqueado ? "plato--bloqueado" : "" %>"
                             data-estado="<%= bloqueado ? "bloqueado" : "disponible" %>"
                             data-nombre="<%= d.getPlato().getNombre().toLowerCase() %>">
                        <p class="plato__marca"><%= restaurante == null ? "Sin marca" : restaurante.getNombre() %></p>
                        <h2 class="plato__nombre"><%= d.getPlato().getNombre() %></h2>
                        <span class="estado <%= bloqueado ? "estado--bloqueado" : "estado--disponible" %>">
                            <%= d.getEstado().getEtiqueta() %>
                        </span>
                        <% if (bloqueado) { %>
                            <p class="plato__motivo"><span class="plato__motivo-icono" aria-hidden="true">&#9888;</span><%= d.getMotivo() %></p>
                        <% } %>
                    </article>
                <% } %>
                <p class="menu-vacio menu-vacio--filtro" id="sin-resultados-menu" hidden>Ningun plato coincide con el filtro.</p>
            </div>
        </section>
    </main>
<% } %>
<script src="<%= ctx %>/resources/js/disponibilidad.js"></script>
</body>
</html>
