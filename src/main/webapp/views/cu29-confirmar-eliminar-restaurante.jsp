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
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Eliminar restaurante | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/gestion.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/main.css">
</head>
<body>
<jsp:include page="navbar.jsp"><jsp:param name="activo" value="restaurantes"/></jsp:include>

<main>
    <section class="confirmar">
        <div class="confirmar__icono">&#128465;&#65039;</div>
        <h1>Eliminar restaurante</h1>
        <p>Esta accion no se puede deshacer. Si el restaurante tiene platos asociados, no podra eliminarse.</p>
        <div class="confirmar__detalle">
            <strong><%= restaurante.getNombre() %></strong><br>
            <%= (restaurante.getDescripcion() == null || restaurante.getDescripcion().isEmpty()) ? "Sin descripcion" : restaurante.getDescripcion() %>
        </div>
        <form method="post" action="<%= ctx %>/restaurantes<%= rolQs %>">
            <input type="hidden" name="accion" value="eliminar">
            <input type="hidden" name="id" value="<%= restaurante.getId() %>">
            <div class="confirmar__acciones">
                <a class="btn-link" href="<%= ctx %>/restaurantes<%= rolQs %>">Cancelar</a>
                <button type="submit" class="btn btn--danger">Eliminar</button>
            </div>
        </form>
    </section>
</main>
</body>
</html>
