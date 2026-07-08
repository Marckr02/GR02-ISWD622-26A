<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="model.AlertaStock" %>
<%
    List<AlertaStock> alertas = (List<AlertaStock>) request.getAttribute("alertas");
    String ctx = request.getContextPath();
    DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Historial de alertas | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/gestion.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/main.css">
</head>
<body>
<jsp:include page="navbar.jsp"><jsp:param name="activo" value="alertas"/></jsp:include>

<header class="gestion-top">
    <div class="gestion-top__brand">
        <span class="gestion-top__dot"></span>
        <div>
            <h1>Historial de alertas de stock critico</h1>
            <p>Registro de insumos que entraron en nivel critico, de lo mas reciente a lo mas antiguo.</p>
        </div>
    </div>
</header>

<main class="gestion-main">
    <section class="panel">
        <% if (alertas == null || alertas.isEmpty()) { %>
            <p class="vacio">Sin alertas criticas registradas</p>
        <% } else { %>
            <table class="tabla">
                <thead>
                    <tr>
                        <th>Insumo</th>
                        <th class="num">Stock al momento</th>
                        <th>Fecha y hora</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (AlertaStock a : alertas) { %>
                        <tr>
                            <td><%= a.getInsumoNombre() %></td>
                            <td class="num"><%= a.getStockAlMomento() %></td>
                            <td><%= a.getTimestamp().format(formato) %></td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        <% } %>
    </section>
</main>
</body>
</html>
