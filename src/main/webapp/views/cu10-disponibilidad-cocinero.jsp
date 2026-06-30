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
    <title>Disponibilidad del turno | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/menu.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/main.css">
</head>
<body>
<jsp:include page="navbar.jsp"/>

<header class="menu-head">
    <h1>Disponibilidad para el turno</h1>
    <p>Que puedes preparar antes de comenzar: estado de cada plato y su motivo de bloqueo (HU10).</p>
</header>

<% if (menu == null || menu.isEmpty()) { %>
    <div class="menu-vacio">No hay platos registrados. Contacta al administrador.</div>
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
