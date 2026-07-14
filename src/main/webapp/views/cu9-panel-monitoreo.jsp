<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="model.Insumo" %>
<%@ page import="model.Proveedor" %>
<%@ page import="model.DisponibilidadPlato" %>
<%@ page import="model.EstadoPlato" %>
<%@ page import="model.Restaurante" %>
<%@ page import="service.RestauranteService" %>
<%
    List<Insumo> criticos = (List<Insumo>) request.getAttribute("criticos");
    Map<Integer, Proveedor> proveedorPorInsumo = (Map<Integer, Proveedor>) request.getAttribute("proveedorPorInsumo");
    List<DisponibilidadPlato> menu = (List<DisponibilidadPlato>) request.getAttribute("menu");
    RestauranteService restauranteService = (RestauranteService) request.getAttribute("restauranteService");
    String ctx = request.getContextPath();
%>
<%!
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
    <title>Panel de control | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/monitoreo.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/main.css">
</head>
<body>
<jsp:include page="navbar.jsp"><jsp:param name="activo" value="monitoreo"/></jsp:include>

<main class="dashboard">
    <!-- Panel izquierdo: insumos criticos -->
    <section class="panel">
        <h2>Insumos criticos</h2>
        <% if (criticos == null || criticos.isEmpty()) { %>
            <p class="vacio">No hay insumos en nivel critico</p>
        <% } else { %>
            <table class="tabla">
                <thead>
                    <tr>
                        <th>Insumo</th>
                        <th class="num">Stock actual</th>
                        <th class="num">Stock minimo</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Insumo i : criticos) {
                           boolean sinStock = i.getStock() <= 0;
                           Proveedor proveedor = (proveedorPorInsumo == null) ? null : proveedorPorInsumo.get(i.getId()); %>
                        <tr>
                            <td><%= i.getNombre() %></td>
                            <td class="num <%= sinStock ? "critico--rojo" : "critico--amarillo" %>"><%= i.getStock() %></td>
                            <td class="num"><%= i.getStockMinimo() %></td>
                            <td class="tabla__accion">
                                <button type="button" class="mon-contacto" title="Contactar proveedor" aria-label="Contactar proveedor"
                                        data-proveedor-nombre="<%= proveedor != null ? attr(proveedor.getNombre()) : "" %>"
                                        data-proveedor-telefono="<%= proveedor != null ? attr(proveedor.getTelefono()) : "" %>"
                                        data-proveedor-correo="<%= proveedor != null ? attr(proveedor.getCorreo()) : "" %>">&#9993;</button>
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        <% } %>
    </section>

    <!-- Panel derecho: estado del menu -->
    <section class="panel panel--menu">
        <div class="panel--menu__cabecera">
            <h2>Estado del menu</h2>
            <div class="filtros-menu" id="filtros-menu" role="group" aria-label="Filtrar platos por estado">
                <button type="button" class="filtro-pill is-on" data-filtro="todos">Ver todos</button>
                <button type="button" class="filtro-pill filtro-pill--ok" data-filtro="disponible">Solo disponibles</button>
                <button type="button" class="filtro-pill filtro-pill--error" data-filtro="bloqueado">Bloqueados</button>
            </div>
        </div>
        <% if (menu == null || menu.isEmpty()) { %>
            <p class="vacio">No hay platos registrados en el sistema</p>
        <% } else { %>
            <div class="menu-grid" id="menu-grid">
                <% for (DisponibilidadPlato d : menu) {
                       boolean bloqueado = d.getEstado() == EstadoPlato.BLOQUEADO;
                       Restaurante restaurante = (restauranteService == null) ? null
                               : restauranteService.buscar(d.getPlato().getRestauranteId()); %>
                    <article class="plato" data-estado="<%= bloqueado ? "bloqueado" : "disponible" %>">
                        <div class="plato__cabecera">
                            <div class="plato__titulo">
                                <p class="plato__marca"><%= restaurante == null ? "Sin marca" : restaurante.getNombre() %></p>
                                <h3 class="plato__nombre"><%= d.getPlato().getNombre() %></h3>
                            </div>
                            <span class="estado <%= bloqueado ? "estado--bloqueado" : "estado--disponible" %>">
                                <%= d.getEstado().getEtiqueta() %>
                            </span>
                        </div>
                        <% if (bloqueado) { %>
                            <p class="plato__motivo"><%= d.getMotivo() %></p>
                        <% } %>
                    </article>
                <% } %>
                <p class="vacio vacio--filtro" id="sin-resultados-menu" hidden>Ningun plato coincide con el filtro.</p>
            </div>
        <% } %>
    </section>
</main>

<!-- Popover de contacto de proveedor (accion rapida desde insumos criticos) -->
<div class="modal-overlay modal-overlay--popover" id="modal-contacto-proveedor" role="dialog" aria-modal="true" aria-labelledby="modal-contacto-titulo">
    <div class="modal modal--popover">
        <h3 id="modal-contacto-titulo">Contactar proveedor</h3>
        <div id="contacto-proveedor-cuerpo"></div>
        <div class="modal__acciones">
            <button type="button" class="btn btn--ghost" id="modal-contacto-cerrar">Cerrar</button>
        </div>
    </div>
</div>

<script src="<%= ctx %>/resources/js/monitoreo.js"></script>
</body>
</html>
