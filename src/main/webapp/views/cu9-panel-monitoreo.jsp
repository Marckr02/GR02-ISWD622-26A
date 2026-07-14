<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="model.Insumo" %>
<%@ page import="model.Proveedor" %>
<%@ page import="model.DisponibilidadPlato" %>
<%@ page import="model.EstadoPlato" %>
<%@ page import="model.Restaurante" %>
<%@ page import="service.RestauranteService" %>
<%@ page import="util.ColorMarca" %>
<%
    List<Insumo> criticos = (List<Insumo>) request.getAttribute("criticos");
    Map<Integer, Proveedor> proveedorPorInsumo = (Map<Integer, Proveedor>) request.getAttribute("proveedorPorInsumo");
    List<DisponibilidadPlato> menu = (List<DisponibilidadPlato>) request.getAttribute("menu");
    RestauranteService restauranteService = (RestauranteService) request.getAttribute("restauranteService");
    String ctx = request.getContextPath();

    // Marcas presentes en el menu actual (nombre -> color), en orden de primera
    // aparicion, para pintar el filtro por restaurante de "Estado del menu".
    java.util.LinkedHashMap<String, String> marcasMenu = new java.util.LinkedHashMap<>();
    if (menu != null) {
        for (DisponibilidadPlato d : menu) {
            Restaurante r = (restauranteService == null) ? null : restauranteService.buscar(d.getPlato().getRestauranteId());
            String nombreMarca = (r == null) ? "Sin marca" : r.getNombre();
            marcasMenu.putIfAbsent(nombreMarca, (r == null) ? "#8b97a6"
                    : ((r.getColor() != null && !r.getColor().isBlank()) ? r.getColor() : ColorMarca.paraNombre(r.getNombre())));
        }
    }
%>
<%!
    /** Escapa comillas dobles para poder incrustar el valor dentro de un atributo HTML. */
    private String attr(String valor) {
        return valor == null ? "" : valor.replace("\"", "&quot;");
    }

    /** Color de marca del restaurante (el asignado, o uno de respaldo si no tiene o no existe). */
    private String colorDeMarca(Restaurante r) {
        if (r == null) { return "#8b97a6"; }
        return (r.getColor() != null && !r.getColor().isBlank()) ? r.getColor() : ColorMarca.paraNombre(r.getNombre());
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Panel de control | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/monitoreo.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/dish-circle.css">
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
            <div class="ins-toolbar">
                <div class="buscador">
                    <input type="text" id="buscador-criticos" class="input-filtro"
                           placeholder="Buscar insumo..." aria-label="Buscar insumo por nombre">
                </div>
                <div class="filtros-stock" id="filtros-criticos" role="group" aria-label="Filtrar insumos criticos por nivel">
                    <button type="button" class="filtro-pill is-on" data-filtro="todos">Todos</button>
                    <button type="button" class="filtro-pill filtro-pill--amarillo" data-filtro="amarillo">
                        <span class="status-dot status-dot--amarillo"></span>Bajo el m&iacute;nimo
                    </button>
                    <button type="button" class="filtro-pill filtro-pill--rojo" data-filtro="rojo">
                        <span class="status-dot status-dot--rojo"></span>Sin stock
                    </button>
                </div>
            </div>
            <table class="tabla" id="tabla-criticos">
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
                        <tr data-estado-stock="<%= sinStock ? "rojo" : "amarillo" %>"
                            data-nombre="<%= attr(i.getNombre().toLowerCase()) %>">
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
            <p class="vacio vacio--filtro" id="sin-resultados-criticos" hidden>Ningun insumo coincide con el filtro.</p>
        <% } %>
    </section>

    <!-- Panel derecho: estado del menu -->
    <section class="panel panel--menu">
        <div class="panel--menu__cabecera">
            <h2>Estado del menu</h2>
        </div>
        <div class="menu-toolbar__buscar">
            <div class="buscador">
                <input type="text" id="buscador-menu-monitoreo" class="input-filtro"
                       placeholder="Buscar plato..." aria-label="Buscar plato por nombre">
            </div>
            <% if (!marcasMenu.isEmpty()) { %>
            <div class="selector-marca">
                <select id="filtro-marca-menu" aria-label="Filtrar platos por restaurante">
                    <option value="todos" selected>Todos los restaurantes</option>
                    <% for (Map.Entry<String, String> entradaMarca : marcasMenu.entrySet()) { %>
                    <option value="<%= attr(entradaMarca.getKey().toLowerCase()) %>"><%= entradaMarca.getKey() %></option>
                    <% } %>
                </select>
            </div>
            <% } %>
            <div class="filtros-menu" id="filtros-menu" role="group" aria-label="Filtrar platos por estado">
                <button type="button" class="filtro-pill is-on" data-filtro="todos">Ver todos</button>
                <button type="button" class="filtro-pill filtro-pill--ok" data-filtro="disponible">
                    <span class="status-dot status-dot--verde"></span>Solo disponibles
                </button>
                <button type="button" class="filtro-pill filtro-pill--error" data-filtro="bloqueado">
                    <span class="status-dot status-dot--rojo"></span>Bloqueados
                </button>
            </div>
        </div>
        <% if (menu == null || menu.isEmpty()) { %>
            <p class="vacio">No hay platos registrados en el sistema</p>
        <% } else { %>
            <div class="menu-grid" id="menu-grid">
                <% for (DisponibilidadPlato d : menu) {
                       boolean bloqueado = d.getEstado() == EstadoPlato.BLOQUEADO;
                       Restaurante restaurante = (restauranteService == null) ? null
                               : restauranteService.buscar(d.getPlato().getRestauranteId());
                       String nombreMarca = restaurante == null ? "Sin marca" : restaurante.getNombre(); %>
                    <article class="dish-circle <%= bloqueado ? "dish-circle--bloqueado" : "" %>"
                             data-estado="<%= bloqueado ? "bloqueado" : "disponible" %>"
                             data-nombre="<%= attr(d.getPlato().getNombre().toLowerCase()) %>"
                             data-restaurante="<%= attr(nombreMarca.toLowerCase()) %>"
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

<script src="<%= ctx %>/resources/js/menu-filtros.js"></script>
<script src="<%= ctx %>/resources/js/monitoreo.js"></script>
</body>
</html>
