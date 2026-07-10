<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Pedido" %>
<%@ page import="model.EstadoPedido" %>
<%@ page import="model.Rol" %>
<%@ page import="model.Restaurante" %>
<%@ page import="model.Plato" %>
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
    String stockPlato = (String) session.getAttribute("stockPlato");
    if (stockPlato != null) { session.removeAttribute("stockPlato"); }

    List<Restaurante> restaurantes = (List<Restaurante>) request.getAttribute("restaurantes");
    List<Plato> platos = (List<Plato>) request.getAttribute("platos");
    if (restaurantes == null) { restaurantes = java.util.Collections.emptyList(); }
    if (platos == null) { platos = java.util.Collections.emptyList(); }
%>
<%!
    /**
     * Separa un texto de faltante con formato "Nombre (disponible: X u, requerido: Y u)"
     * en sus tres partes para el modal de alerta. Devuelve null si el texto no sigue
     * ese formato (por ejemplo, el caso de respaldo "insumo #N"), y la vista muestra
     * el texto completo tal cual en ese caso.
     */
    private String[] parsearFaltante(String texto) {
        String marcaDisponible = " (disponible: ";
        String marcaRequerido = ", requerido: ";
        int idx = texto.indexOf(marcaDisponible);
        if (idx < 0) { return null; }
        String nombre = texto.substring(0, idx);
        String resto = texto.substring(idx + marcaDisponible.length());
        int coma = resto.indexOf(marcaRequerido);
        if (coma < 0) { return null; }
        String disponible = resto.substring(0, coma);
        String requerido = resto.substring(coma + marcaRequerido.length());
        if (requerido.endsWith(")")) { requerido = requerido.substring(0, requerido.length() - 1); }
        return new String[]{ nombre, disponible, requerido };
    }
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
            backdrop-filter: blur(4px);
            -webkit-backdrop-filter: blur(4px);
        }
        .modal-alerta__panel {
            width: 100%; max-width: 500px; background: var(--surface);
            color: var(--text); border: 1px solid rgba(245, 158, 11, .25);
            border-radius: 16px; box-shadow: var(--shadow);
            padding: 1.85rem;
            animation: aparece .18s ease both;
        }

        .modal-alerta__cabecera { display: flex; align-items: center; gap: .65rem; margin-bottom: .8rem; }
        .modal-alerta__icono {
            flex-shrink: 0; display: inline-flex; align-items: center; justify-content: center;
            width: 34px; height: 34px; border-radius: 50%;
            background: rgba(245, 158, 11, .14); color: #f59e0b; font-size: 1.15rem;
        }
        .modal-alerta__panel h2 { margin: 0; font-size: 1.15rem; color: #f5a524; letter-spacing: -.01em; }
        .modal-alerta__texto { color: var(--muted); margin: 0 0 1.2rem; line-height: 1.5; font-size: .93rem; }
        .modal-alerta__texto strong { color: var(--text); font-weight: 700; }

        .modal-alerta__lista {
            list-style: none; margin: 0 0 .6rem; padding: 0 .2rem 0 0;
            display: flex; flex-direction: column; gap: .55rem;
            max-height: 260px; overflow-y: auto;
        }
        .modal-alerta__fila {
            display: flex; align-items: center; justify-content: space-between; gap: .8rem;
            background: var(--surface-2); border: 1px solid var(--border);
            border-radius: 10px; padding: .6rem .85rem;
        }
        .modal-alerta__fila--oculta { display: none; }
        .modal-alerta__insumo { color: var(--text); font-size: .87rem; font-weight: 600; min-width: 0; overflow-wrap: break-word; }
        .modal-alerta__cantidades { flex-shrink: 0; text-align: right; display: flex; flex-direction: column; gap: .1rem; }
        .modal-alerta__disponible { color: var(--error); font-size: .8rem; font-weight: 700; white-space: nowrap; }
        .modal-alerta__requerido { color: var(--muted); font-size: .74rem; white-space: nowrap; }

        .btn-ver-mas {
            cursor: pointer; background: transparent; border: none;
            color: var(--muted); font-size: .82rem; font-weight: 600;
            padding: .4rem .2rem; margin: 0 0 1.1rem; text-align: left;
            transition: color .15s;
        }
        .btn-ver-mas:hover { color: var(--text); }

        .modal-alerta__acciones { display: flex; justify-content: flex-end; }
        .btn-descartar {
            cursor: pointer; background: transparent;
            border: 1px solid var(--border); color: var(--muted);
            border-radius: 999px; padding: .55rem 1.2rem;
            font-size: .85rem; font-weight: 600;
            transition: color .15s, border-color .15s, background .15s;
        }
        .btn-descartar:hover { color: #fca5a5; border-color: rgba(239, 68, 68, .5); background: rgba(239, 68, 68, .08); }

        /* ---------- Modal de confirmacion (reemplaza el confirm() nativo del navegador) ---------- */
        .modal-confirmar {
            position: fixed; inset: 0; z-index: 65; display: none;
            align-items: center; justify-content: center; padding: 1rem;
            background: rgba(2, 6, 12, .58);
            backdrop-filter: blur(4px);
            -webkit-backdrop-filter: blur(4px);
        }
        .modal-confirmar.is-abierto { display: flex; }
        .modal-confirmar__panel {
            width: 100%; max-width: 400px; background: var(--surface);
            color: var(--text); border: 1px solid var(--border);
            border-radius: 16px; box-shadow: var(--shadow);
            padding: 1.6rem;
            animation: aparece .18s ease both;
        }
        .modal-confirmar__cabecera { display: flex; align-items: center; gap: .65rem; margin-bottom: .7rem; }
        .modal-confirmar__icono {
            flex-shrink: 0; display: inline-flex; align-items: center; justify-content: center;
            width: 34px; height: 34px; border-radius: 50%;
            background: rgba(245, 165, 36, .14); color: #f5a524; font-size: 1.15rem;
        }
        .modal-confirmar__panel h2 { margin: 0; font-size: 1.1rem; color: var(--text); letter-spacing: -.01em; }
        .modal-confirmar__texto { color: var(--muted); margin: 0 0 1.1rem; line-height: 1.5; font-size: .9rem; }

        /* ---------- Bloque visual de transicion: [Fase actual] -> [Fase destino] ---------- */
        .modal-confirmar__flujo {
            display: flex; justify-content: center; align-items: center;
            gap: 1rem; margin: 0 0 1.4rem;
        }
        .badge-fase {
            display: inline-flex; align-items: center; justify-content: center;
            padding: .5rem 1.1rem; border-radius: 999px;
            font-size: .85rem; font-weight: 700; white-space: nowrap;
            border: 1px solid transparent;
        }
        .badge-fase--RECIBIDO { background: rgba(245, 165, 36, .16); color: var(--recibido); border-color: rgba(245, 165, 36, .35); }
        .badge-fase--EN_PREPARACION { background: rgba(59, 130, 246, .16); color: var(--preparacion); border-color: rgba(59, 130, 246, .35); }
        .badge-fase--LISTO { background: rgba(34, 197, 94, .16); color: var(--listo); border-color: rgba(34, 197, 94, .35); }
        .badge-fase--ENTREGADO { background: rgba(100, 116, 139, .16); color: var(--entregado); border-color: rgba(100, 116, 139, .35); }

        .modal-confirmar__flecha { flex-shrink: 0; color: var(--muted); font-size: 1.4rem; line-height: 1; }

        .modal-confirmar__acciones { display: flex; justify-content: center; gap: 1.6rem; margin-top: 3rem; }
        .btn-confirmar-retroceso {
            cursor: pointer; border: none; border-radius: 999px;
            padding: .55rem 1.2rem; font-size: .85rem; font-weight: 700;
            background: #f5a524; color: #2b1a02;
            transition: filter .12s, transform .12s;
        }
        .btn-confirmar-retroceso:hover { filter: brightness(1.08); }
        .btn-confirmar-retroceso:active { transform: scale(.97); }

        .pase-top__right { display: flex; align-items: center; gap: .7rem; }
        .btn-simular {
            cursor: pointer;
            background: transparent;
            color: var(--accent, #34d399);
            border: 1px solid var(--accent, #34d399);
            border-radius: 999px;
            padding: .55rem 1.1rem;
            font-size: .85rem;
            font-weight: 700;
            white-space: nowrap;
            transition: background .15s, color .15s, transform .12s;
        }
        .btn-simular:hover { background: var(--accent-soft, rgba(52,211,153,.14)); }
        .btn-simular:active { transform: scale(.97); }

        .modal-overlay {
            display: none;
            position: fixed; inset: 0; z-index: 70;
            align-items: center; justify-content: center;
            padding: 1rem;
            background: rgba(2, 6, 12, .6);
            backdrop-filter: blur(2px);
        }
        .modal-overlay.is-abierto { display: flex; }

        .modal--pedido {
            width: 100%; max-width: 420px;
            background: var(--surface);
            color: var(--text);
            border: 1px solid var(--border);
            border-radius: var(--radius, 14px);
            box-shadow: var(--shadow);
            padding: 1.5rem;
            animation: aparece .18s ease both;
        }
        .modal--pedido h2 { margin: 0 0 .4rem; font-size: 1.15rem; }
        .modal--pedido .modal__ayuda { margin: 0 0 1.1rem; color: var(--muted); font-size: .84rem; line-height: 1.4; }

        .campo { display: block; margin-bottom: 1rem; }
        .campo span { display: block; margin-bottom: .35rem; font-size: .8rem; font-weight: 600; color: var(--muted); }
        .campo select {
            width: 100%;
            background: var(--surface-2);
            color: var(--text);
            border: 1px solid var(--border);
            border-radius: 9px;
            padding: .6rem .7rem;
            font-size: .88rem;
            cursor: pointer;
        }
        .campo select:disabled { opacity: .5; cursor: not-allowed; }
        .campo select:focus-visible { outline: 2px solid var(--accent, #34d399); outline-offset: 1px; }

        .modal__acciones { display: flex; justify-content: flex-end; gap: .6rem; margin-top: .4rem; }
        .modal__acciones .btn {
            cursor: pointer;
            border-radius: 9px;
            padding: .6rem 1.1rem;
            font-size: .85rem;
            font-weight: 700;
            border: none;
            transition: transform .12s, filter .12s, opacity .12s;
        }
        .modal__acciones .btn:active { transform: scale(.97); }
        .btn--secundario { background: transparent; color: var(--muted); border: 1px solid var(--border) !important; }
        .btn--secundario:hover { color: var(--text); border-color: #3a4350 !important; }
        .btn--primario { background: var(--accent, #34d399); color: var(--accent-ink, #04231a); }
        .btn--primario:hover { filter: brightness(1.07); }
        .btn--primario:disabled { opacity: .5; cursor: not-allowed; }
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

<% if (puedeOperar) { %>
<script type="application/json" id="datos-simulador">
    {
      "restaurantes": [
    <%
        for (int i = 0; i < restaurantes.size(); i++) {
            Restaurante r = restaurantes.get(i);
    %>
    { "id": <%= r.getId() %>, "nombre": "<%= r.getNombre().replace("\\", "\\\\").replace("\"", "\\\"") %>" }<%= (i < restaurantes.size() - 1) ? "," : "" %>
    <% } %>
    ],
    "platos": [
    <%
        for (int i = 0; i < platos.size(); i++) {
            Plato p = platos.get(i);
    %>
    { "id": <%= p.getId() %>, "nombre": "<%= p.getNombre().replace("\\", "\\\\").replace("\"", "\\\"") %>", "restauranteId": <%= p.getRestauranteId() %> }<%= (i < platos.size() - 1) ? "," : "" %>
    <% } %>
    ]
  }
</script>

<div class="modal-overlay" id="overlay-simular-pedido">
    <div class="modal--pedido" role="dialog" aria-modal="true" aria-labelledby="simular-pedido-titulo">
        <h2 id="simular-pedido-titulo">Simular pedido</h2>
        <p class="modal__ayuda">Elige el restaurante y luego el plato para generar un pedido nuevo en la columna "Recibido".</p>

        <form method="post" action="<%= ctx %>/pedidos" id="form-simular-pedido">
            <input type="hidden" name="accion" value="crear">
            <input type="hidden" name="rol" value="<%= rol.name() %>">

            <label class="campo">
                <span>Restaurante</span>
                <select name="restauranteId" id="select-restaurante" required>
                    <option value="" selected disabled>Selecciona un restaurante...</option>
                </select>
            </label>

            <label class="campo">
                <span>Plato</span>
                <select name="platoId" id="select-plato" required disabled>
                    <option value="" selected disabled>Primero elige un restaurante</option>
                </select>
            </label>

            <div class="modal__acciones">
                <button type="button" class="btn btn--secundario" id="btn-cancelar-simular">Cancelar</button>
                <button type="submit" class="btn btn--primario" id="btn-agregar-pedido" disabled>Agregar pedido</button>
            </div>
        </form>
    </div>
</div>
<% } %>

<% if (error != null) { %>
<div class="modal-alerta" role="alertdialog" aria-modal="true" aria-labelledby="stock-alerta-titulo">
    <div class="modal-alerta__panel">
        <div class="modal-alerta__cabecera">
            <span class="modal-alerta__icono" aria-hidden="true">&#9888;</span>
            <h2 id="stock-alerta-titulo">Stock insuficiente</h2>
        </div>
        <p class="modal-alerta__texto">
            Falta stock para <strong><%= stockPlato != null ? stockPlato : "este pedido" %></strong>. Revisa los insumos antes de continuar.
        </p>
        <% if (stockFaltantes != null && !stockFaltantes.isEmpty()) {
               int totalFaltantes = stockFaltantes.size();
               int restantes = totalFaltantes - 3; %>
        <ul class="modal-alerta__lista">
            <% for (int idx = 0; idx < totalFaltantes; idx++) {
                   String faltante = stockFaltantes.get(idx);
                   String[] partes = parsearFaltante(faltante);
                   String claseFila = "modal-alerta__fila" + (idx >= 3 ? " modal-alerta__fila--oculta" : ""); %>
            <li class="<%= claseFila %>">
                <% if (partes != null) { %>
                <span class="modal-alerta__insumo"><%= partes[0] %></span>
                <span class="modal-alerta__cantidades">
                    <span class="modal-alerta__disponible">disp. <%= partes[1] %></span>
                    <span class="modal-alerta__requerido">req. <%= partes[2] %></span>
                </span>
                <% } else { %>
                <span class="modal-alerta__insumo"><%= faltante %></span>
                <% } %>
            </li>
            <% } %>
        </ul>
        <% if (restantes > 0) { %>
        <button type="button" class="btn-ver-mas" id="btn-ver-mas-faltantes">+ <%= restantes %> insumo<%= restantes == 1 ? "" : "s" %> m&aacute;s (Ver todos)</button>
        <% } %>
        <% } %>
        <div class="modal-alerta__acciones">
            <button type="button" class="btn-descartar" onclick="this.closest('.modal-alerta').remove()">Entendido</button>
        </div>
    </div>
</div>
<script>
    (function () {
        var botonVerMas = document.getElementById("btn-ver-mas-faltantes");
        if (!botonVerMas) { return; }
        botonVerMas.addEventListener("click", function () {
            var ocultas = document.querySelectorAll(".modal-alerta__fila--oculta");
            ocultas.forEach(function (fila) { fila.classList.remove("modal-alerta__fila--oculta"); });
            botonVerMas.remove();
        });
    })();
</script>
<% } %>

<div class="tablero-toolbar">
    <% if (!puedeOperar) { %>
    <span class="pase-top__hint">Vista de consulta</span>
    <% } %>
    <% if (puedeOperar) { %>
    <button type="button" id="btn-simular-pedido" class="btn-simular">+ Simular Pedido</button>
    <% } %>
</div>

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
                    <form method="post" action="<%= ctx %>/pedidos" class="card__action form-retroceder"
                          data-fase-actual="<%= estado.getEtiqueta() %>"
                          data-fase-actual-clave="<%= estado.name() %>"
                          data-fase-destino="<%= estado.anterior().getEtiqueta() %>"
                          data-fase-destino-clave="<%= estado.anterior().name() %>">
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

<!-- Modal de confirmacion para "Retroceder" (reemplaza el confirm() nativo del navegador) -->
<div class="modal-confirmar" id="modal-confirmar-retroceso" role="alertdialog" aria-modal="true" aria-labelledby="confirmar-retroceso-titulo">
    <div class="modal-confirmar__panel">
        <div class="modal-confirmar__cabecera">
            <span class="modal-confirmar__icono" aria-hidden="true">&#8634;</span>
            <h2 id="confirmar-retroceso-titulo">&iquest;Retroceder pedido?</h2>
        </div>
        <p class="modal-confirmar__texto">Confirma que deseas devolver este pedido a la fase anterior:</p>
        <div class="modal-confirmar__flujo">
            <span class="badge-fase" id="confirmar-fase-actual"></span>
            <span class="modal-confirmar__flecha" aria-hidden="true">&#10132;</span>
            <span class="badge-fase" id="confirmar-fase-destino"></span>
        </div>
        <div class="modal-confirmar__acciones">
            <button type="button" class="btn-descartar" id="btn-cancelar-retroceso">Cancelar</button>
            <button type="button" class="btn-confirmar-retroceso" id="btn-aceptar-retroceso">S&iacute;, retroceder</button>
        </div>
    </div>
</div>
<script>
    (function () {
        var modal = document.getElementById("modal-confirmar-retroceso");
        var btnCancelar = document.getElementById("btn-cancelar-retroceso");
        var btnAceptar = document.getElementById("btn-aceptar-retroceso");
        var faseActualEl = document.getElementById("confirmar-fase-actual");
        var faseDestinoEl = document.getElementById("confirmar-fase-destino");
        if (!modal) { return; }
        var formPendiente = null;

        // Pinta un badge con el nombre de la fase y el color semantico de su
        // columna en el tablero (mismo criterio que .col--<ESTADO>).
        function pintarBadge(el, etiqueta, clave) {
            if (!el) { return; }
            el.textContent = etiqueta;
            el.className = "badge-fase" + (clave ? " badge-fase--" + clave : "");
        }

        // Abrir solo muestra el modal: el boton de la tarjeta no cambia de
        // estado todavia (eso solo ocurre si el usuario confirma).
        function abrir(form) {
            formPendiente = form;
            pintarBadge(faseActualEl, form.getAttribute("data-fase-actual") || "Fase actual", form.getAttribute("data-fase-actual-clave"));
            pintarBadge(faseDestinoEl, form.getAttribute("data-fase-destino") || "Fase anterior", form.getAttribute("data-fase-destino-clave"));
            modal.classList.add("is-abierto");
        }

        // Cierra y limpia todo el estado local del modal (formulario pendiente
        // y los badges de las fases), tanto al cancelar como al confirmar.
        function cerrar() {
            modal.classList.remove("is-abierto");
            formPendiente = null;
            pintarBadge(faseActualEl, "", null);
            pintarBadge(faseDestinoEl, "", null);
        }

        document.querySelectorAll("form.form-retroceder").forEach(function (form) {
            form.addEventListener("submit", function (e) {
                e.preventDefault();
                abrir(form);
            });
        });

        if (btnCancelar) { btnCancelar.addEventListener("click", cerrar); }
        if (btnAceptar) {
            btnAceptar.addEventListener("click", function () {
                if (formPendiente) {
                    // El estado de carga se activa recien aqui, al confirmar;
                    // form.submit() no dispara el evento "submit" del formulario,
                    // asi que el listener generico de kanban.js nunca lo veria.
                    var card = formPendiente.closest(".card");
                    if (card) { card.classList.add("card--moviendo"); }
                    var boton = formPendiente.querySelector("button");
                    if (boton) {
                        boton.disabled = true;
                        boton.textContent = "Moviendo...";
                    }
                    formPendiente.submit();
                }
                cerrar();
            });
        }
        modal.addEventListener("click", function (e) {
            if (e.target === modal) { cerrar(); }
        });
    })();
</script>

<script src="<%= ctx %>/resources/js/kanban.js"></script>
<% } %>
</body>
</html>