<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Insumo" %>
<%@ page import="model.DisponibilidadPlato" %>
<%@ page import="model.EstadoPlato" %>
<%
    List<Insumo> criticos = (List<Insumo>) request.getAttribute("criticos");
    List<DisponibilidadPlato> menu = (List<DisponibilidadPlato>) request.getAttribute("menu");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Panel de monitoreo | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/monitoreo.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/main.css">
</head>
<body>
<jsp:include page="navbar.jsp"/>

<header class="mon-head">
    <h1>Panel de monitoreo de disponibilidad</h1>
    <p>Insumos bajo el minimo y estado del menu para decidir el reabastecimiento (HU9).</p>
</header>

<main class="mon-main">
    <section class="panel">
        <h2>Insumos criticos</h2>
        <% if (criticos == null || criticos.isEmpty()) { %>
            <p class="vacio">No hay insumos en nivel critico</p>
        <% } else { %>
            <table class="tabla">
                <thead>
                    <tr>
                        <th>Insumo</th>
                        <th class="num">Stock actual</th>
                        <th class="num">Stock minimo</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Insumo i : criticos) { %>
                        <tr>
                            <td><%= i.getNombre() %></td>
                            <td class="num critico"><%= i.getStock() %></td>
                            <td class="num"><%= i.getStockMinimo() %></td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        <% } %>
    </section>

    <section class="panel">
        <h2>Estado del menu</h2>
        <% if (menu == null || menu.isEmpty()) { %>
            <p class="vacio">No hay platos registrados en el sistema</p>
        <% } else { %>
            <table class="tabla">
                <thead>
                    <tr>
                        <th>Plato</th>
                        <th>Estado</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (DisponibilidadPlato d : menu) {
                           boolean bloqueado = d.getEstado() == EstadoPlato.BLOQUEADO; %>
                        <tr>
                            <td><%= d.getPlato().getNombre() %></td>
                            <td>
                                <span class="estado <%= bloqueado ? "estado--bloqueado" : "estado--disponible" %>">
                                    <%= d.getEstado().getEtiqueta() %>
                                </span>
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
