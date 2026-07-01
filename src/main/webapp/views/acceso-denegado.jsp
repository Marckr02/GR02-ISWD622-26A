<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String ctx = request.getContextPath();
    String mensaje = (String) request.getAttribute("mensajeAcceso");
    if (mensaje == null) { mensaje = "No tienes permisos para acceder a esta seccion"; }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Acceso denegado | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/menu.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/main.css">
</head>
<body>
<jsp:include page="navbar.jsp"><jsp:param name="activo" value=""/></jsp:include>

<main style="max-width:520px;margin:4rem auto;padding:0 1.5rem;text-align:center;">
    <div style="font-size:2.6rem;line-height:1;">&#128274;</div>
    <h1 style="margin:.8rem 0 .4rem;font-size:1.4rem;">Acceso denegado</h1>
    <p style="color:var(--muted);"><%= mensaje %></p>
    <p style="margin-top:1.4rem;">
        <a href="<%= ctx %>/pedidos" style="color:var(--text);font-weight:600;">&larr; Volver al tablero</a>
    </p>
</main>
</body>
</html>
