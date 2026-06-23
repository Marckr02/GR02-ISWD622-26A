<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Insumo" %>
<%
    List<Insumo> insumos = (List<Insumo>) request.getAttribute("insumos");
    String ctx = request.getContextPath();

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
    <title>Inventario de bodega | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/inventario.css">
</head>
<body>
<header class="inv-top">
    <div class="inv-top__brand">
        <span class="inv-top__dot"></span>
        <div>
            <h1>Inventario de bodega</h1>
            <p>Entradas de lotes y ajustes de stock para todas las marcas</p>
        </div>
    </div>
    <nav class="inv-nav">
        <a href="<%= ctx %>/pedidos">&larr; Volver al tablero</a>
    </nav>
</header>

<% if (mensaje != null) { %>
    <div class="aviso aviso--ok" role="status"><%= mensaje %></div>
<% } %>
<% if (error != null) { %>
    <div class="aviso aviso--error" role="alert"><%= error %></div>
<% } %>

<main class="inv-main">
    <section class="panel panel--tabla">
        <h2>Stock actual</h2>
        <table class="tabla">
            <thead>
                <tr>
                    <th>#</th>
                    <th>Insumo</th>
                    <th>Unidad</th>
                    <th class="num">Stock</th>
                    <th class="num">Costo unit.</th>
                </tr>
            </thead>
            <tbody>
                <% if (insumos == null || insumos.isEmpty()) { %>
                    <tr><td colspan="5" class="vacio">Sin insumos registrados.</td></tr>
                <% } else {
                       for (Insumo insumo : insumos) { %>
                    <tr>
                        <td><%= insumo.getId() %></td>
                        <td><%= insumo.getNombre() %></td>
                        <td><%= insumo.getUnidad() %></td>
                        <td class="num"><%= insumo.getStock() %></td>
                        <td class="num">$<%= insumo.getCostoUnitario() %></td>
                    </tr>
                <%     }
                   } %>
            </tbody>
        </table>
    </section>

    <div class="formularios">
        <section class="panel panel--form">
            <h2>Registrar entrada</h2>
            <p class="panel__hint">Ingreso de un lote de insumos (HU4).</p>
            <form method="post" action="<%= ctx %>/insumos" class="form">
                <input type="hidden" name="accion" value="registrar">
                <label>Insumo
                    <select name="insumoId" required>
                        <% if (insumos != null) {
                               for (Insumo insumo : insumos) { %>
                            <option value="<%= insumo.getId() %>"><%= insumo.getNombre() %></option>
                        <%     }
                           } %>
                    </select>
                </label>
                <label>Cantidad
                    <input type="number" name="cantidad" step="0.01" min="0.01" required>
                </label>
                <label>Costo unitario
                    <input type="number" name="costo" step="0.01" min="0" required>
                </label>
                <label>Orden de compra
                    <input type="text" name="ordenCompra" placeholder="OC-2026-001" required>
                </label>
                <label>Factura
                    <input type="text" name="factura" placeholder="FAC-0001" required>
                </label>
                <button type="submit" class="btn btn--ok">Registrar entrada</button>
            </form>
        </section>

        <section class="panel panel--form">
            <h2>Reducir stock</h2>
            <p class="panel__hint">Mermas, perdidas o desperdicios (HU5).</p>
            <form method="post" action="<%= ctx %>/insumos" class="form">
                <input type="hidden" name="accion" value="reducir">
                <label>Insumo
                    <select name="insumoId" required>
                        <% if (insumos != null) {
                               for (Insumo insumo : insumos) { %>
                            <option value="<%= insumo.getId() %>"><%= insumo.getNombre() %></option>
                        <%     }
                           } %>
                    </select>
                </label>
                <label>Cantidad a reducir
                    <input type="number" name="cantidad" step="0.01" min="0.01" required>
                </label>
                <button type="submit" class="btn btn--warn">Reducir stock</button>
            </form>
        </section>
    </div>
</main>
</body>
</html>