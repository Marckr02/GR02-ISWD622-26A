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
    <title>Registrar proveedor | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/gestion.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/main.css">
</head>
<body>
<jsp:include page="navbar.jsp"><jsp:param name="activo" value="proveedores"/></jsp:include>

<header class="gestion-top">
    <div class="gestion-top__brand">
        <span class="gestion-top__dot"></span>
        <div>
            <h1>Registrar proveedor</h1>
            <p>Datos de contacto para reabastecer insumos criticos.</p>
        </div>
    </div>
    <nav class="gestion-nav">
        <a href="<%= ctx %>/proveedores<%= rolQs %>">&larr; Volver al listado</a>
    </nav>
</header>

<% if (error != null) { %>
    <div class="aviso aviso--error" role="alert"><%= error %></div>
<% } %>

<main class="gestion-main gestion-main--form">
    <section class="panel">
        <h2>Nuevo proveedor</h2>
        <p class="panel__hint">Nombre 2-100 caracteres, telefono 7-15 digitos, correo usuario@dominio.ext.</p>
        <form method="post" action="<%= ctx %>/proveedores<%= rolQs %>" class="form">
            <input type="hidden" name="accion" value="guardar">
            <label>Nombre
                <input type="text" name="nombre" maxlength="100" required placeholder="Ej: Distribuidora Sierra">
            </label>
            <label>Telefono
                <input type="tel" name="telefono" maxlength="15" required placeholder="Ej: 0991234567" pattern="\d{7,15}">
            </label>
            <label>Correo electronico
                <input type="email" name="correo" required placeholder="Ej: contacto@proveedor.com">
            </label>
            <div class="form__actions">
                <button type="submit" class="btn btn--ok">Guardar</button>
                <a class="btn-link" href="<%= ctx %>/proveedores<%= rolQs %>">Cancelar</a>
            </div>
        </form>
    </section>
</main>
</body>
</html>
