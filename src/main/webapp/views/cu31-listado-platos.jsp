<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Plato" %>
<%@ page import="model.IngredientePlato" %>
<%@ page import="model.Restaurante" %>
<%@ page import="model.Insumo" %>
<%@ page import="service.PlatoService" %>
<%
    List<Plato> platos = (List<Plato>) request.getAttribute("platos");
    PlatoService platoService = (PlatoService) request.getAttribute("platoService");
    String ctx = request.getContextPath();
    String rol = request.getParameter("rol");
    if ((rol == null || rol.isEmpty()) && session.getAttribute("rol") != null) {
        rol = (String) session.getAttribute("rol");
    }
    String rolQs = (rol == null || rol.isEmpty()) ? "" : ("?rol=" + rol);
    String rolAmp = rolQs.isEmpty() ? "" : "&" + rolQs.substring(1);

    String mensaje = (String) session.getAttribute("mensaje");
    if (mensaje != null) { session.removeAttribute("mensaje"); }
    String error = (String) session.getAttribute("error");
    if (error != null) { session.removeAttribute("error"); }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Platos | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/gestion.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/main.css">
</head>
<body>
<jsp:include page="navbar.jsp"><jsp:param name="activo" value="platos"/></jsp:include>

<header class="gestion-top">
    <div class="gestion-top__brand">
        <span class="gestion-top__dot"></span>
        <div>
            <h1>Platos del menu</h1>
            <p>Recetas por restaurante: insumos, cantidades y unidades.</p>
        </div>
    </div>
    <nav class="gestion-nav">
        <a class="destacado" href="<%= ctx %>/platos?accion=nueva<%= rolAmp %>">+ Nuevo plato</a>
    </nav>
</header>

<% if (mensaje != null) { %>
    <div class="aviso aviso--ok" role="status"><%= mensaje %></div>
<% } %>
<% if (error != null) { %>
    <div class="aviso aviso--error" role="alert"><%= error %></div>
<% } %>

<main class="gestion-main">
    <section class="panel">
        <h2>Listado de platos</h2>
        <% if (platos == null || platos.isEmpty()) { %>
            <p class="vacio">No hay platos registrados</p>
        <% } else { %>
            <table class="tabla">
                <thead>
                    <tr>
                        <th>Plato</th>
                        <th>Restaurante</th>
                        <th>Ingredientes</th>
                        <th>Accion</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Plato p : platos) {
                           Restaurante restaurante = platoService.restauranteDe(p); %>
                        <tr>
                            <td><%= p.getNombre() %></td>
                            <td><%= restaurante == null ? "\u2014" : restaurante.getNombre() %></td>
                            <td>
                                <% for (IngredientePlato ing : p.getIngredientes()) {
                                       Insumo insumo = platoService.insumoDe(ing.getInsumoId());
                                       String nombreInsumo = (insumo == null) ? ("#" + ing.getInsumoId()) : insumo.getNombre();
                                       String unidad = (ing.getUnidadReceta() == null)
                                               ? (insumo == null ? "" : insumo.getUnidad())
                                               : ing.getUnidadReceta(); %>
                                    <span class="pill"><%= nombreInsumo %> &middot; <%= ing.getCantidad() %> <%= unidad %></span>
                                <% } %>
                            </td>
                            <td class="tabla__acciones">
                                <a class="btn-link btn--sm" href="<%= ctx %>/platos?accion=editar&id=<%= p.getId() %><%= rolAmp %>">Editar</a>
                                <a class="btn-link btn--sm" href="<%= ctx %>/platos?accion=confirmarEliminar&id=<%= p.getId() %><%= rolAmp %>">Eliminar</a>
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        <% } %>
    </section>
</main>
</body>
</html>
