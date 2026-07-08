<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Pedido" %>
<%@ page import="model.EstadoPedido" %>
<%@ page import="model.Rol" %>
<%@ page import="service.EstadoPedidoPolicy" %>
<%
    EstadoPedidoPolicy policy = (EstadoPedidoPolicy) request.getAttribute("policy");
    if (policy == null) { policy = new EstadoPedidoPolicy(); }

    Rol rol = Rol.desde(request.getParameter("rol"));
    if (rol == null) { rol = Rol.desde((String) session.getAttribute("rol")); }
    if (rol == null) { rol = Rol.COCINERO; }
    boolean puedeOperar = (rol == Rol.COCINERO);   // el cocinero opera el pase; bodega solo consulta

    String ctx = request.getContextPath();
    String error = (String) session.getAttribute("error");
    if (error != null) { session.removeAttribute("error"); }
    List<String> stockFaltantes = (List<String>) session.getAttribute("stockFaltantes");
    if (stockFaltantes != null) { session.removeAttribute("stockFaltantes"); }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Pase de cocina | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/kanban.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/main.css">
    <style>
        .card__acciones { display: flex; flex-wrap: wrap; gap: .4rem; margin-top: .2rem; }
        .btn--retro { background: transparent; border: 1px solid var(--border); color: var(--muted); }
        .btn--retro:hover { color: var(--text); border-color: #3a4350; }
        .sin-seccion {
            max-width: 460px; margin: 4.5rem auto; text-align: center;
            background: var(--surface); border: 1px solid var(--border);
            border-radius: var(--radius, 14px); box-shadow: var(--shadow); padding: 2.2rem 1.6rem;
        }
        .pase-top__hint { color: var(--muted); font-size: .8rem; font-weight: 600;
            border: 1px solid var(--border); border-radius: 999px; padding: .35rem .8rem; }
        .sin-seccion h2 { margin: .3rem 0 .4rem; font-size: 1.2rem; }
        .sin-seccion p { margin: 0; color: var(--muted); }
        .modal-alerta {
            position: fixed; inset: 0; z-index: 60; display: flex;
            align-items: center; justify-content: center; padding: 1rem;
            background: rgba(2, 6, 12, .58);
        }
        .modal-alerta__panel {
            width: 100%; max-width: 460px; background: var(--surface);
            color: var(--text); border: 1px solid var(--border);
            border-radius: var(--radius, 14px); box-shadow: var(--shadow);
            padding: 1.4rem;
        }
        .modal-alerta__panel h2 { margin: 0 0 .5rem; font-size: 1.15rem; }
        .modal-alerta__panel p { color: var(--muted); margin: 0 0 1rem; line-height: 1.45; }
        .modal-alerta__panel ul { margin: 0 0 1rem 1.2rem; padding: 0; color: var(--text); }
        .modal-alerta__panel li { margin: .35rem 0; }
        .modal-alerta__panel .btn { width: 100%; }
    </style>
</head>
<body data-rol="<%= rol.name() %>">
<jsp:include page="navbar.jsp"><jsp:param name="activo" value="tablero"/></jsp:include>

<% if (rol == Rol.ADMINISTRADOR) { %>
    <section class="sin-seccion">
        <div style="font-size:2.2rem;line-height:1;">&#127869;</div>
        <h2>El tablero no aplica a tu rol</h2>
        <p>Usa las pestanas de arriba para gestionar restaurantes, platos o el historial de alertas.</p>
    </section>
<% } else { %>

<header class="pase-top">
    <div class="pase-top__brand">
        <span class="pase-top__dot"></span>
        <div>
            <h1>Pase de cocina</h1>
            <p>Tablero de produccion en tiempo real</p>
        </div>
    </div>
    <% if (!puedeOperar) { %>
        <span class="pase-top__hint">Vista de consulta</span>
    <% } %>
</header>

<% if (error != null) { %>
    <div class="modal-alerta" role="alertdialog" aria-modal="true" aria-labelledby="stock-alerta-titulo">
        <div class="modal-alerta__panel">
            <h2 id="stock-alerta-titulo">Stock insuficiente</h2>
            <p><%= error %></p>
            <% if (stockFaltantes != null && !stockFaltantes.isEmpty()) { %>
                <ul>
                    <% for (String faltante : stockFaltantes) { %>
                        <li><%= faltante %></li>
                    <% } %>
                </ul>
            <% } %>
            <button type="button" class="btn" onclick="this.closest('.modal-alerta').remove()">Entendido</button>
        </div>
    </div>
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

<script src="<%= ctx %>/resources/js/kanban.js"></script>
<% } %>
</body>
</html>
