<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.DisponibilidadPlato" %>
<%@ page import="model.EstadoPlato" %>
<%
    List<DisponibilidadPlato> menu = (List<DisponibilidadPlato>) request.getAttribute("menu");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Menu y disponibilidad | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/menu.css">
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
            <article class="plato">
                <h2 class="plato__nombre"><%= d.getPlato().getNombre() %></h2>
                <span class="estado <%= bloqueado ? "estado--bloqueado" : "estado--disponible" %>">
                    <%= d.getEstado().getEtiqueta() %>
                </span>
                <% if (bloqueado) { %>
                    <p class="plato__motivo"><%= d.getMotivo() %></p>
                <% } %>
            </article>
        <% } %>
    </main>
<% } %>
</body>
</html>
