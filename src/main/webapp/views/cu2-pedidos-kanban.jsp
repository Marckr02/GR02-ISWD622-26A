<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Pedido" %>
<%@ page import="model.EstadoPedido" %>
<%@ page import="model.Rol" %>
<%@ page import="service.EstadoPedidoPolicy" %>
<%
    EstadoPedidoPolicy policy = (EstadoPedidoPolicy) request.getAttribute("policy");
    if (policy == null) { policy = new EstadoPedidoPolicy(); }

    // Tarea Tecnica de roles: solo COCINERO, ADMIN_BODEGA y ADMINISTRADOR.
    Rol rol = Rol.desde(request.getParameter("rol"));
    if (rol == null) { rol = Rol.COCINERO; }
    boolean puedeOperar = (rol == Rol.COCINERO || rol == Rol.ADMINISTRADOR);

    String ctx = request.getContextPath();
    String error = (String) session.getAttribute("error");
    if (error != null) { session.removeAttribute("error"); }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Pase de cocina | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/kanban.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/main.css">
</head>
<body data-rol="<%= rol.name() %>">
<jsp:include page="navbar.jsp"/>

<header class="pase-top">
    <div class="pase-top__brand">
        <span class="pase-top__dot"></span>
        <div>
            <h1>Pase de cocina</h1>
            <p>Tablero de produccion en tiempo real</p>
        </div>
    </div>
    <nav class="roles" aria-label="Cambiar rol de la vista">
        <span class="roles__label">Vista:</span>
        <a class="role <%= rol == Rol.COCINERO ? "role--on" : "" %>" href="<%= ctx %>/pedidos?rol=COCINERO">Cocinero</a>
        <a class="role <%= rol == Rol.ADMIN_BODEGA ? "role--on" : "" %>" href="<%= ctx %>/pedidos?rol=ADMIN_BODEGA">Admin. bodega</a>
        <a class="role <%= rol == Rol.ADMINISTRADOR ? "role--on" : "" %>" href="<%= ctx %>/pedidos?rol=ADMINISTRADOR">Administrador</a>
    </nav>
</header>

<% if (error != null) { %>
    <div class="aviso aviso--error" role="alert"><%= error %></div>
<% } %>

<main class="tablero">
    <% for (EstadoPedido estado : EstadoPedido.values()) {
           List<Pedido> columna = (List<Pedido>) request.getAttribute("col_" + estado.name());
           int total = (columna == null) ? 0 : columna.size();
    %>
    <section class="col col--<%= estado.name() %>">
        <div class="col__head">
            <h2><%= estado.getEtiqueta() %></h2>
            <span class="col__count"><%= total %></span>
        </div>
        <div class="col__cards">
            <% if (columna == null || columna.isEmpty()) { %>
                <p class="col__empty">Sin pedidos en esta etapa.</p>
            <% } else {
                   for (Pedido pedido : columna) { %>
                <article class="card">
                    <div class="card__id">#<%= pedido.getId() %></div>
                    <p class="card__desc"><%= pedido.getDescripcion() %></p>
                    <span class="card__marca"><%= pedido.getMarca() %></span>
                    <% if (puedeOperar && (policy.puedeAvanzar(estado) || policy.puedeRetroceder(estado))) { %>
                        <div class="card__acciones">
                            <% if (policy.puedeRetroceder(estado)) { %>
                                <form method="post" action="<%= ctx %>/pedidos" class="card__action"
                                      onsubmit="return confirm('Retroceder este pedido al estado anterior?');">
                                    <input type="hidden" name="accion" value="retroceder">
                                    <input type="hidden" name="pedidoId" value="<%= pedido.getId() %>">
                                    <input type="hidden" name="rol" value="<%= rol.name() %>">
                                    <button type="submit" class="btn--retro">&larr; <%= policy.etiquetaRetroceso(estado) %></button>
                                </form>
                            <% } %>
                            <% if (policy.puedeAvanzar(estado)) { %>
                                <form method="post" action="<%= ctx %>/pedidos" class="card__action">
                                    <input type="hidden" name="accion" value="mover">
                                    <input type="hidden" name="pedidoId" value="<%= pedido.getId() %>">
                                    <input type="hidden" name="rol" value="<%= rol.name() %>">
                                    <button type="submit"><%= policy.etiquetaSiguienteAccion(estado) %> &rarr;</button>
                                </form>
                            <% } %>
                        </div>
                    <% } else if (estado.esFinal()) { %>
                        <span class="card__done">Completado</span>
                    <% } %>
                </article>
            <%     }
               } %>
        </div>
    </section>
    <% } %>
</main>

<footer class="pase-bottom">
    <a href="<%= ctx %>/insumos">Ir a inventario &rarr;</a>
</footer>

<script src="<%= ctx %>/resources/js/kanban.js"></script>
<style>
    .card__acciones { display: flex; flex-wrap: wrap; gap: .4rem; margin-top: .2rem; }
    .btn--retro { background: transparent; border: 1px solid var(--border); color: var(--muted); }
    .btn--retro:hover { color: var(--text); border-color: #3a4350; }
</style>
</body>
</html>
