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
<%!
    /** Numero legible: entero para "unidades", 2 decimales para el resto (evita ruido de punto flotante). */
    private String formatearCantidad(double valor, String unidad) {
        if ("unidades".equals(unidad)) {
            return String.valueOf(Math.round(valor));
        }
        return String.format(java.util.Locale.US, "%.2f", valor);
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Crear insumo | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/inventario.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/main.css">
    <style>
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
        .modal__acciones { display: flex; gap: .6rem; justify-content: flex-end; margin-top: .3rem; }
        .modal__acciones .btn { min-width: 96px; }
        .btn--ghost { background: transparent; border: 1px solid var(--border); color: var(--text); }
        .btn--editar {
            background: transparent; border: 1px solid var(--border); color: var(--text);
            border-radius: 8px; padding: .4rem .8rem; font-size: .82rem; font-weight: 600; cursor: pointer;
        }
        .btn--editar:hover { border-color: var(--ok); color: var(--ok); }
    </style>
</head>
<body>
<jsp:include page="navbar.jsp"><jsp:param name="activo" value="crear"/></jsp:include>

<header class="inv-top">
    <div class="inv-top__brand">
        <span class="inv-top__dot"></span>
        <div>
            <h1>Crear / Editar insumo</h1>
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
        <h2>Crear / Editar insumo</h2>
        <p class="panel__hint">Entre 2 y 100 caracteres: letras, numeros, espacios y guiones.</p>
        <form method="post" action="<%= ctx %>/insumos/crear<%= rolQs %>" class="form form--insumo">
            <input type="hidden" name="accion" value="crear">
            <label>Nombre
                <input type="text" name="nombre" maxlength="100" required autofocus
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
        <div class="buscador">
            <input type="text" id="filtro-insumo" class="input-filtro"
                   placeholder="Buscar insumo por nombre..." aria-label="Buscar insumo">
        </div>
        <p class="leyenda-stock">
            <span class="leyenda-item"><span class="status-dot status-dot--rojo"></span>Sin stock</span>
            <span class="leyenda-item"><span class="status-dot status-dot--amarillo"></span>Bajo el minimo</span>
            <span class="leyenda-item"><span class="status-dot status-dot--verde"></span>Nivel optimo</span>
        </p>
        <table class="tabla">
            <thead>
                <tr>
                    <th>Insumo</th>
                    <th>Unidad</th>
                    <th class="num">Stock</th>
                    <th class="num">Stock min.</th>
                    <th>Accion</th>
                </tr>
            </thead>
            <tbody>
                <% if (insumos == null || insumos.isEmpty()) { %>
                    <tr><td colspan="5" class="vacio">Sin insumos registrados.</td></tr>
                <% } else {
                       for (Insumo insumo : insumos) {
                           String estado = insumo.getStock() <= 0 ? "rojo"
                                   : (insumo.getStock() < insumo.getStockMinimo() ? "amarillo" : "verde"); %>
                    <tr>
                        <td><%= insumo.getNombre() %></td>
                        <td><%= insumo.getUnidad() %></td>
                        <td class="num">
                            <span class="status-dot status-dot--<%= estado %>"></span><%= formatearCantidad(insumo.getStock(), insumo.getUnidad()) %>
                        </td>
                        <td class="num"><%= formatearCantidad(insumo.getStockMinimo(), insumo.getUnidad()) %></td>
                        <td>
                            <button type="button" class="btn--editar"
                                    data-id="<%= insumo.getId() %>"
                                    data-nombre="<%= insumo.getNombre() %>"
                                    data-unidad="<%= insumo.getUnidad() %>">Editar</button>
                        </td>
                    </tr>
                <%     }
                   } %>
            </tbody>
        </table>
    </section>
</main>

<div class="modal-overlay" id="modal-editar" role="dialog" aria-modal="true" aria-labelledby="modal-editar-titulo">
    <div class="modal">
        <h3 id="modal-editar-titulo">Editar insumo</h3>
        <p class="hint">Actualiza el nombre o la unidad de medida.</p>
        <form method="post" action="<%= ctx %>/insumos/crear<%= rolQs %>" class="form form--insumo" id="form-editar-insumo">
            <input type="hidden" name="accion" value="editar">
            <input type="hidden" name="insumoId" id="editar-id">
            <label>Nombre
                <input type="text" name="nombre" id="editar-nombre" maxlength="100" required>
            </label>
            <label>Unidad
                <select name="unidad" id="editar-unidad" required>
                    <option value="kg">kg</option>
                    <option value="l">l</option>
                    <option value="unidades">unidades</option>
                </select>
            </label>
            <div class="modal__acciones">
                <button type="button" class="btn btn--ghost" id="modal-editar-cancelar">Cancelar</button>
                <button type="submit" class="btn btn--ok">Guardar cambios</button>
            </div>
        </form>
    </div>
</div>

<script src="<%= ctx %>/resources/js/inventario.js"></script>
<script>
(function () {
    "use strict";
    var overlay = document.getElementById("modal-editar");
    var campoId = document.getElementById("editar-id");
    var campoNombre = document.getElementById("editar-nombre");
    var campoUnidad = document.getElementById("editar-unidad");

    document.querySelectorAll(".btn--editar").forEach(function (boton) {
        boton.addEventListener("click", function () {
            campoId.value = boton.getAttribute("data-id");
            campoNombre.value = boton.getAttribute("data-nombre");
            campoUnidad.value = boton.getAttribute("data-unidad");
            overlay.style.display = "flex";
            campoNombre.focus();
        });
    });

    function cerrar() { overlay.style.display = "none"; }

    document.getElementById("modal-editar-cancelar").addEventListener("click", cerrar);
    overlay.addEventListener("click", function (e) {
        if (e.target === overlay) { cerrar(); }
    });
})();
</script>
</body>
</html>
