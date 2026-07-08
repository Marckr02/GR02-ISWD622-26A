<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
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
    <title>Registrar restaurante | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/gestion.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/main.css">
</head>
<body>
<jsp:include page="navbar.jsp"><jsp:param name="activo" value="restaurantes"/></jsp:include>

<header class="gestion-top">
    <div class="gestion-top__brand">
        <span class="gestion-top__dot"></span>
        <div>
            <h1>Registrar restaurante</h1>
            <p>Marcas que operan dentro de la dark kitchen colaborativa.</p>
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
        <h2>Nuevo restaurante</h2>
        <p class="panel__hint">Nombre 2-100 caracteres. Descripcion opcional, maximo 255 caracteres.</p>
        <form method="post" action="<%= ctx %>/restaurantes<%= rolQs %>" class="form">
            <input type="hidden" name="accion" value="guardar">
            <label>Nombre
                <input type="text" name="nombre" maxlength="100" required placeholder="Ej: Green Bowl">
            </label>
            <label>Descripcion (opcional)
                <textarea name="descripcion" maxlength="255" rows="3" placeholder="Ej: Bowls saludables y veganos."></textarea>
            </label>
            <div class="form__actions">
                <button type="submit" class="btn btn--ok">Guardar</button>
                <a class="btn-link" href="<%= ctx %>/restaurantes<%= rolQs %>">Cancelar</a>
            </div>
        </form>
    </section>
</main>
</body>
</html>
