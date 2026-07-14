<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.DisponibilidadPlato" %>
<%@ page import="model.EstadoPlato" %>
<%
    List<DisponibilidadPlato> menu = (List<DisponibilidadPlato>) request.getAttribute("menu");
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
    <title>Menu y disponibilidad | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/menu.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/dish-circle.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/main.css">
</head>
<body>
<jsp:include page="navbar.jsp"><jsp:param name="activo" value="menu"/></jsp:include>

<header class="menu-head">
    <h1>Menu y disponibilidad</h1>
    <p>Disponibilidad de cada plato segun el stock actual.</p>
</header>

<% if (menu == null || menu.isEmpty()) { %>
    <div class="menu-vacio">No hay platos registrados en el sistema.</div>
<% } else { %>
    <main class="menu-grid">
        <% for (DisponibilidadPlato d : menu) {
               boolean bloqueado = d.getEstado() == EstadoPlato.BLOQUEADO; %>
            <article class="dish-circle <%= bloqueado ? "dish-circle--bloqueado" : "" %>"
                     data-estado="<%= bloqueado ? "bloqueado" : "disponible" %>">
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
    </main>
<% } %>
<script src="<%= ctx %>/resources/js/menu-filtros.js"></script>
<script>
    document.addEventListener("DOMContentLoaded", function () {
        if (window.activarPopoverFaltantes) { window.activarPopoverFaltantes(); }
    });
</script>
</body>
</html>
