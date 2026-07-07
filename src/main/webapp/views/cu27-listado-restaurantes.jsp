<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Restaurante" %>
<%
    List<Restaurante> restaurantes = (List<Restaurante>) request.getAttribute("restaurantes");
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
    <title>Restaurantes | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/gestion.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/main.css">
</head>
<body>
<jsp:include page="navbar.jsp"><jsp:param name="activo" value="restaurantes"/></jsp:include>

<header class="gestion-top">
    <div class="gestion-top__brand">
        <span class="gestion-top__dot"></span>
        <div>
            <h1>Restaurantes</h1>
            <p>Marcas que operan dentro de la dark kitchen colaborativa.</p>
        </div>
    </div>
    <nav class="gestion-nav">
        <a class="destacado" href="<%= ctx %>/restaurantes?accion=nueva<%= rolAmp %>">+ Nuevo restaurante</a>
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
        <h2>Listado de restaurantes</h2>
        <% if (restaurantes == null || restaurantes.isEmpty()) { %>
            <p class="vacio">No hay restaurantes registrados</p>
        <% } else { %>
            <table class="tabla">
                <thead>
                    <tr>
                        <th>Nombre</th>
                        <th>Descripcion</th>
                        <th>Accion</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Restaurante r : restaurantes) { %>
                        <tr>
                            <td><%= r.getNombre() %></td>
                            <td><%= (r.getDescripcion() == null || r.getDescripcion().isEmpty()) ? "\u2014" : r.getDescripcion() %></td>
                            <td class="tabla__acciones">
                                <a class="btn-link btn--sm" href="<%= ctx %>/restaurantes?accion=editar&id=<%= r.getId() %><%= rolAmp %>">Editar</a>
                                <a class="btn-link btn--sm" href="<%= ctx %>/restaurantes?accion=confirmarEliminar&id=<%= r.getId() %><%= rolAmp %>">Eliminar</a>
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
