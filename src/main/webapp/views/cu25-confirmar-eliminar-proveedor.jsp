<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.Proveedor" %>
<%
    Proveedor proveedor = (Proveedor) request.getAttribute("proveedor");
    String ctx = request.getContextPath();
    String rol = request.getParameter("rol");
    if ((rol == null || rol.isEmpty()) && session.getAttribute("rol") != null) {
        rol = (String) session.getAttribute("rol");
    }
    String rolQs = (rol == null || rol.isEmpty()) ? "" : ("?rol=" + rol);
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Eliminar proveedor | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/gestion.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/main.css">
</head>
<body>
<jsp:include page="navbar.jsp"><jsp:param name="activo" value="proveedores"/></jsp:include>

<main>
    <section class="confirmar">
        <div class="confirmar__icono">&#128465;&#65039;</div>
        <h1>Eliminar proveedor</h1>
        <p>Esta accion no se puede deshacer. Verifica los datos antes de confirmar.</p>
        <div class="confirmar__detalle">
            <strong><%= proveedor.getNombre() %></strong><br>
            <%= proveedor.getTelefono() %> &middot; <%= proveedor.getCorreo() %>
        </div>
        <form method="post" action="<%= ctx %>/proveedores<%= rolQs %>">
            <input type="hidden" name="accion" value="eliminar">
            <input type="hidden" name="id" value="<%= proveedor.getId() %>">
            <div class="confirmar__acciones">
                <a class="btn-link" href="<%= ctx %>/proveedores<%= rolQs %>">Cancelar</a>
                <button type="submit" class="btn btn--danger">Eliminar</button>
            </div>
        </form>
    </section>
</main>
</body>
</html>
