<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="model.Insumo" %>
<%@ page import="model.Proveedor" %>
<%
    List<Insumo> insumos = (List<Insumo>) request.getAttribute("insumos");
    List<Proveedor> proveedores = (List<Proveedor>) request.getAttribute("proveedores");
    Map<Integer, Proveedor> proveedorPorInsumo = (Map<Integer, Proveedor>) request.getAttribute("proveedorPorInsumo");
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
    <title>Inventario de bodega | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/inventario.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/main.css">
    <style>
        .form__error { color: var(--error, #ef4444); font-size: .82rem; min-height: 1em; }
        .modal-overlay {
            display: none; position: fixed; inset: 0; z-index: 50;
            background: rgba(2, 6, 12, .6);
            align-items: center; justify-content: center; padding: 1rem;
        }
        .modal {
            width: 100%; max-width: 380px;
            background: var(--surface); color: var(--text);
            border: 1px solid var(--border); border-radius: var(--radius, 14px);
            box-shadow: var(--shadow); padding: 1.3rem 1.4rem;
        }
        .modal h3 { margin: 0 0 .2rem; font-size: 1.1rem; }
        .modal p.hint { margin: 0 0 1rem; color: var(--muted); font-size: .82rem; }
        .modal .form { gap: .7rem; }
        .modal__resumen {
            margin: .2rem 0 1.1rem; padding: .7rem .8rem;
            background: var(--surface-2); border-radius: 10px; font-size: .92rem;
        }
        .modal__acciones { display: flex; gap: .6rem; justify-content: flex-end; margin-top: .3rem; }
        .modal__acciones .btn { min-width: 96px; }
        .btn--ghost { background: transparent; border: 1px solid var(--border); color: var(--text); }
        .btn--mini {
            background: transparent; border: 1px solid var(--border); color: var(--text);
            border-radius: 8px; padding: .35rem .7rem; font-size: .78rem; font-weight: 600; cursor: pointer;
        }
        .btn--mini:hover { border-color: var(--ok); color: var(--ok); }
        .proveedor-tag { font-size: .82rem; color: var(--muted); }
        .proveedor-tag--asignado { color: var(--text); }
    </style>
</head>
<body>
<jsp:include page="navbar.jsp"><jsp:param name="activo" value="inventario"/></jsp:include>

<header class="inv-top">
    <div class="inv-top__brand">
        <span class="inv-top__dot"></span>
        <div>
            <h1>Inventario de bodega</h1>
            <p>Entrada simplificada y ajustes de stock para todas las marcas</p>
        </div>
    </div>
    <nav class="inv-nav">
        <a href="<%= ctx %>/insumos/crear<%= rolQs %>">+ Crear insumo</a>
        <a href="<%= ctx %>/pedidos<%= rolQs %>">&larr; Volver al tablero</a>
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
                    <th class="num">Stock min.</th>
                    <th>Proveedor</th>
                    <th>Acciones</th>
                </tr>
            </thead>
            <tbody>
                <% if (insumos == null || insumos.isEmpty()) { %>
                    <tr><td colspan="7" class="vacio">Sin insumos registrados.</td></tr>
                <% } else {
                       for (Insumo insumo : insumos) {
                           Proveedor proveedor = (proveedorPorInsumo == null) ? null : proveedorPorInsumo.get(insumo.getId()); %>
                    <tr>
                        <td><%= insumo.getId() %></td>
                        <td><%= insumo.getNombre() %></td>
                        <td><%= insumo.getUnidad() %></td>
                        <td class="num"><%= insumo.getStock() %></td>
                        <td class="num"><%= insumo.getStockMinimo() %></td>
                        <td>
                            <% if (proveedor != null) { %>
                                <span class="proveedor-tag proveedor-tag--asignado"><%= proveedor.getNombre() %></span>
                            <% } else { %>
                                <span class="proveedor-tag">Sin proveedor</span>
                            <% } %>
                        </td>
                        <td style="display:flex;gap:.4rem;flex-wrap:wrap;">
                            <button type="button" class="btn--mini btn-asociar-proveedor"
                                    data-id="<%= insumo.getId() %>" data-nombre="<%= insumo.getNombre() %>">Proveedor</button>
                            <button type="button" class="btn--mini btn-editar-minimo"
                                    data-id="<%= insumo.getId() %>" data-nombre="<%= insumo.getNombre() %>"
                                    data-minimo="<%= insumo.getStockMinimo() %>">Nivel minimo</button>
                        </td>
                    </tr>
                <%     }
                   } %>
            </tbody>
        </table>
    </section>

    <div class="formularios">
        <section class="panel panel--form">
            <h2>Registrar entrada</h2>
            <p class="panel__hint">Selecciona el insumo y la cantidad a ingresar.</p>
            <form method="post" action="<%= ctx %>/insumos<%= rolQs %>" class="form form--confirmable"
                  data-entero="false" data-titulo="Confirmar entrada" data-confirmar="Registrar">
                <input type="hidden" name="accion" value="registrar">
                <label>Insumo
                    <select name="insumoId" required>
                        <option value="">-- Selecciona un insumo --</option>
                        <% if (insumos != null) {
                               for (Insumo insumo : insumos) { %>
                            <option value="<%= insumo.getId() %>" data-unidad="<%= insumo.getUnidad() %>"><%= insumo.getNombre() %></option>
                        <%     }
                           } %>
                    </select>
                </label>
                <label>Cantidad
                    <input type="number" name="cantidad" step="0.01" min="0.01" required>
                </label>
                <p class="form__error" aria-live="polite"></p>
                <button type="submit" class="btn btn--ok">Registrar</button>
            </form>
        </section>

        <section class="panel panel--form">
            <h2>Reducir stock</h2>
            <p class="panel__hint">Mermas, perdidas o desperdicios.</p>
            <form method="post" action="<%= ctx %>/insumos<%= rolQs %>" class="form form--confirmable"
                  data-entero="false" data-titulo="Confirmar reduccion" data-confirmar="Confirmar reduccion">
                <input type="hidden" name="accion" value="reducir">
                <label>Insumo
                    <select name="insumoId" required>
                        <option value="">-- Selecciona un insumo --</option>
                        <% if (insumos != null) {
                               for (Insumo insumo : insumos) { %>
                            <option value="<%= insumo.getId() %>" data-unidad="<%= insumo.getUnidad() %>"><%= insumo.getNombre() %></option>
                        <%     }
                           } %>
                    </select>
                </label>
                <label>Cantidad a reducir
                    <input type="number" name="cantidad" step="0.01" min="0.01" required>
                </label>
                <p class="form__error" aria-live="polite"></p>
                <button type="submit" class="btn btn--warn">Reducir stock</button>
            </form>
        </section>
    </div>
</main>

<div class="modal-overlay" id="modal-confirm" role="dialog" aria-modal="true" aria-labelledby="modal-titulo">
    <div class="modal">
        <h3 id="modal-titulo">Confirmar</h3>
        <p class="modal__resumen" id="modal-resumen"></p>
        <div class="modal__acciones">
            <button type="button" class="btn btn--ghost" id="modal-cancelar">Cancelar</button>
            <button type="button" class="btn btn--ok" id="modal-confirmar">Confirmar</button>
        </div>
    </div>
</div>

<div class="modal-overlay" id="modal-proveedor" role="dialog" aria-modal="true" aria-labelledby="modal-proveedor-titulo">
    <div class="modal">
        <h3 id="modal-proveedor-titulo">Asociar proveedor</h3>
        <p class="hint" id="modal-proveedor-insumo"></p>
        <form method="post" action="<%= ctx %>/insumos<%= rolQs %>" class="form">
            <input type="hidden" name="accion" value="vincularProveedor">
            <input type="hidden" name="insumoId" id="proveedor-insumo-id">
            <label>Proveedor
                <select name="proveedorId" id="proveedor-select" required>
                    <option value="">-- Selecciona un proveedor --</option>
                    <% if (proveedores != null) { for (Proveedor p : proveedores) { %>
                        <option value="<%= p.getId() %>"><%= p.getNombre() %></option>
                    <% } } %>
                </select>
            </label>
            <% if (proveedores == null || proveedores.isEmpty()) { %>
                <p class="panel__hint">Aun no hay proveedores registrados. Crea uno primero en la seccion Proveedores.</p>
            <% } %>
            <div class="modal__acciones">
                <button type="button" class="btn btn--ghost" id="modal-proveedor-cancelar">Cancelar</button>
                <button type="submit" class="btn btn--ok">Guardar</button>
            </div>
        </form>
    </div>
</div>

<div class="modal-overlay" id="modal-minimo" role="dialog" aria-modal="true" aria-labelledby="modal-minimo-titulo">
    <div class="modal">
        <h3 id="modal-minimo-titulo">Nivel minimo de stock</h3>
        <p class="hint" id="modal-minimo-insumo"></p>
        <form method="post" action="<%= ctx %>/insumos<%= rolQs %>" class="form">
            <input type="hidden" name="accion" value="actualizarMinimo">
            <input type="hidden" name="insumoId" id="minimo-insumo-id">
            <label>Nivel minimo
                <input type="number" name="stockMinimo" id="minimo-valor" step="0.01" min="0" required>
            </label>
            <div class="modal__acciones">
                <button type="button" class="btn btn--ghost" id="modal-minimo-cancelar">Cancelar</button>
                <button type="submit" class="btn btn--ok">Guardar</button>
            </div>
        </form>
    </div>
</div>

<script src="<%= ctx %>/resources/js/inventario.js"></script>
<script>
(function () {
    "use strict";

    function cablearModal(overlayId, abrirSelector, cancelarId, alAbrir) {
        var overlay = document.getElementById(overlayId);
        document.querySelectorAll(abrirSelector).forEach(function (boton) {
            boton.addEventListener("click", function () {
                alAbrir(boton);
                overlay.style.display = "flex";
            });
        });
        document.getElementById(cancelarId).addEventListener("click", function () {
            overlay.style.display = "none";
        });
        overlay.addEventListener("click", function (e) {
            if (e.target === overlay) { overlay.style.display = "none"; }
        });
    }

    cablearModal("modal-proveedor", ".btn-asociar-proveedor", "modal-proveedor-cancelar", function (boton) {
        document.getElementById("proveedor-insumo-id").value = boton.getAttribute("data-id");
        document.getElementById("modal-proveedor-insumo").textContent = "Insumo: " + boton.getAttribute("data-nombre");
        document.getElementById("proveedor-select").value = "";
    });

    cablearModal("modal-minimo", ".btn-editar-minimo", "modal-minimo-cancelar", function (boton) {
        document.getElementById("minimo-insumo-id").value = boton.getAttribute("data-id");
        document.getElementById("modal-minimo-insumo").textContent = "Insumo: " + boton.getAttribute("data-nombre");
        document.getElementById("minimo-valor").value = boton.getAttribute("data-minimo");
    });
})();
</script>
</body>
</html>
