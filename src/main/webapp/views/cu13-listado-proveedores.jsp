<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Proveedor" %>
<%
    List<Proveedor> proveedores = (List<Proveedor>) request.getAttribute("proveedores");
    String ctx = request.getContextPath();
    String rol = request.getParameter("rol");
    if ((rol == null || rol.isEmpty()) && session.getAttribute("rol") != null) {
        rol = (String) session.getAttribute("rol");
    }
    String rolQs = (rol == null || rol.isEmpty()) ? "" : ("?rol=" + rol);

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
    <title>Proveedores | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/gestion.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/main.css">
</head>
<body>
<jsp:include page="navbar.jsp"><jsp:param name="activo" value="proveedores"/></jsp:include>

<header class="gestion-top">
    <div class="gestion-top__brand">
        <span class="gestion-top__dot"></span>
        <div>
            <h1>Proveedores</h1>
            <p>Contactos para reabastecer insumos cuando entran en nivel critico.</p>
        </div>
    </div>
    <nav class="gestion-nav">
        <a class="destacado" href="<%= ctx %>/proveedores?accion=nueva<%= rolQs.isEmpty() ? "" : "&" + rolQs.substring(1) %>">+ Nuevo proveedor</a>
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
        <h2>Listado de proveedores</h2>
        <% if (proveedores == null || proveedores.isEmpty()) { %>
            <p class="vacio">No hay proveedores registrados</p>
        <% } else { %>
            <table class="tabla">
                <thead>
                    <tr>
                        <th>Nombre</th>
                        <th>Telefono</th>
                        <th>Correo</th>
                        <th>Accion</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Proveedor p : proveedores) { %>
                        <tr>
                            <td><%= p.getNombre() %></td>
                            <td><%= p.getTelefono() %></td>
                            <td><%= p.getCorreo() %></td>
                            <td class="tabla__acciones">
                                <a class="btn-link btn--sm" href="<%= ctx %>/proveedores?accion=confirmarEliminar&id=<%= p.getId() %><%= rolQs.isEmpty() ? "" : "&" + rolQs.substring(1) %>">Eliminar</a>
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
