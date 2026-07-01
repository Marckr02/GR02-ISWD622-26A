<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Insumo" %>
<%
    List<Insumo> insumos = (List<Insumo>) request.getAttribute("insumos");
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
    <title>Crear insumo | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/inventario.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/main.css">
</head>
<body>
<jsp:include page="navbar.jsp"><jsp:param name="activo" value="crear"/></jsp:include>

<header class="inv-top">
    <div class="inv-top__brand">
        <span class="inv-top__dot"></span>
        <div>
            <h1>Crear insumo</h1>
            <p>Alta manual de un insumo con stock inicial en cero.</p>
        </div>
    </div>
    <nav class="inv-nav">
        <a href="<%= ctx %>/insumos<%= rolQs %>">&larr; Volver al inventario</a>
    </nav>
</header>

<% if (mensaje != null) { %>
    <div class="aviso aviso--ok" role="status"><%= mensaje %></div>
<% } %>
<% if (error != null) { %>
    <div class="aviso aviso--error" role="alert"><%= error %></div>
<% } %>

<main class="inv-main">
    <section class="panel panel--form">
        <h2>Nuevo insumo</h2>
        <p class="panel__hint">Entre 2 y 100 caracteres: letras, numeros, espacios y guiones.</p>
        <form method="post" action="<%= ctx %>/insumos/crear<%= rolQs %>" class="form">
            <input type="hidden" name="accion" value="crear">
            <label>Nombre
                <input type="text" name="nombre" maxlength="100" required
                       placeholder="Ej: Cilantro fresco">
            </label>
            <label>Unidad
                <select name="unidad" required>
                    <option value="kg">kg</option>
                    <option value="l">l</option>
                    <option value="unidades">unidades</option>
                </select>
            </label>
            <button type="submit" class="btn btn--ok">Guardar</button>
        </form>
    </section>

    <section class="panel panel--tabla">
        <h2>Stock actual</h2>
        <table class="tabla">
            <thead>
                <tr>
                    <th>#</th>
                    <th>Insumo</th>
                    <th>Unidad</th>
                    <th class="num">Stock</th>
                    <th class="num">Stock min.</th>
                    <th>Accion</th>
                </tr>
            </thead>
            <tbody>
                <% if (insumos == null || insumos.isEmpty()) { %>
                    <tr><td colspan="6" class="vacio">Sin insumos registrados.</td></tr>
                <% } else {
                       for (Insumo insumo : insumos) { %>
                    <tr>
                        <td>
                            <%= insumo.getId() %>
                            <form id="editar-<%= insumo.getId() %>" method="post" action="<%= ctx %>/insumos/crear<%= rolQs %>">
                                <input type="hidden" name="accion" value="editar">
                                <input type="hidden" name="insumoId" value="<%= insumo.getId() %>">
                            </form>
                        </td>
                        <td>
                            <input form="editar-<%= insumo.getId() %>" type="text" name="nombre" value="<%= insumo.getNombre() %>" maxlength="100" required>
                        </td>
                        <td>
                            <select form="editar-<%= insumo.getId() %>" name="unidad" required>
                                <option value="kg" <%= "kg".equals(insumo.getUnidad()) ? "selected" : "" %>>kg</option>
                                <option value="l" <%= "l".equals(insumo.getUnidad()) ? "selected" : "" %>>l</option>
                                <option value="unidades" <%= "unidades".equals(insumo.getUnidad()) ? "selected" : "" %>>unidades</option>
                            </select>
                        </td>
                        <td class="num"><%= insumo.getStock() %></td>
                        <td class="num"><%= insumo.getStockMinimo() %></td>
                        <td><button form="editar-<%= insumo.getId() %>" type="submit" class="btn btn--ok">Actualizar</button></td>
                    </tr>
                <%     }
                   } %>
            </tbody>
        </table>
    </section>
</main>
</body>
</html>
