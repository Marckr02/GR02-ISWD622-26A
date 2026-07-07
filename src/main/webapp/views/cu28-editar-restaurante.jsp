<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.Restaurante" %>
<%
    Restaurante restaurante = (Restaurante) request.getAttribute("restaurante");
    String ctx = request.getContextPath();
    String rol = request.getParameter("rol");
    if ((rol == null || rol.isEmpty()) && session.getAttribute("rol") != null) {
        rol = (String) session.getAttribute("rol");
    }
    String rolQs = (rol == null || rol.isEmpty()) ? "" : ("?rol=" + rol);

    String error = (String) session.getAttribute("error");
    if (error != null) { session.removeAttribute("error"); }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Editar restaurante | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/gestion.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/main.css">
</head>
<body>
<jsp:include page="navbar.jsp"><jsp:param name="activo" value="restaurantes"/></jsp:include>

<header class="gestion-top">
    <div class="gestion-top__brand">
        <span class="gestion-top__dot"></span>
        <div>
            <h1>Editar restaurante</h1>
            <p>Actualiza el nombre o la descripcion de la marca.</p>
        </div>
    </div>
    <nav class="gestion-nav">
        <a href="<%= ctx %>/restaurantes<%= rolQs %>">&larr; Volver al listado</a>
    </nav>
</header>

<% if (error != null) { %>
    <div class="aviso aviso--error" role="alert"><%= error %></div>
<% } %>

<main class="gestion-main gestion-main--form">
    <section class="panel">
        <h2><%= restaurante.getNombre() %></h2>
        <form method="post" action="<%= ctx %>/restaurantes<%= rolQs %>" class="form">
            <input type="hidden" name="accion" value="actualizar">
            <input type="hidden" name="id" value="<%= restaurante.getId() %>">
            <label>Nombre
                <input type="text" name="nombre" maxlength="100" required value="<%= restaurante.getNombre() %>">
            </label>
            <label>Descripcion (opcional)
                <textarea name="descripcion" maxlength="255" rows="3"><%= restaurante.getDescripcion() == null ? "" : restaurante.getDescripcion() %></textarea>
            </label>
            <div class="form__actions">
                <button type="submit" class="btn btn--ok">Guardar cambios</button>
                <a class="btn-link" href="<%= ctx %>/restaurantes<%= rolQs %>">Cancelar</a>
            </div>
        </form>
    </section>
</main>
</body>
</html>
