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
<%!
    /** Numero legible: entero para "unidades", 2 decimales para el resto (evita ruido de punto flotante). */
    private String formatearCantidad(double valor, String unidad) {
        if ("unidades".equals(unidad)) {
            return String.valueOf(Math.round(valor));
        }
        return String.format(java.util.Locale.US, "%.2f", valor);
    }

    /** Escapa comillas dobles para poder incrustar el valor dentro de un atributo HTML. */
    private String attr(String valor) {
        return valor == null ? "" : valor.replace("\"", "&quot;");
    }

    /** Escapa un texto para incrustarlo como literal dentro de un &lt;script&gt; (toast). */
    private String js(String valor) {
        if (valor == null) { return ""; }
        return valor.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "").replace("\n", " ");
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Inventario de bodega | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/inventario.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/main.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/toast.css">
    <style>
        .form__error { color: var(--error, #ef4444); font-size: .82rem; min-height: 1em; }
        .modal-overlay {
            display: none; position: fixed; inset: 0; z-index: 50;
            background: rgba(2, 6, 12, .6);
            align-items: center; justify-content: center; padding: 1rem;
        }
        .modal {
            width: 100%; max-width: 400px;
            background: var(--surface); color: var(--text);
            border: 1px solid var(--border); border-radius: var(--radius, 14px);
            box-shadow: var(--shadow); padding: 1.3rem 1.4rem;
            max-height: 90vh; overflow-y: auto;
        }
        .modal h3 { margin: 0 0 .2rem; font-size: 1.1rem; }
        .modal p.hint { margin: 0 0 1rem; color: var(--muted); font-size: .82rem; }
        .modal .form { gap: .7rem; }
        .modal__acciones { display: flex; gap: .6rem; justify-content: flex-end; margin-top: .3rem; }
        .modal__acciones .btn { min-width: 96px; }
        .btn--ghost { background: transparent; border: 1px solid var(--border); color: var(--text); }

        /* ---------- Cabecera con icono + resumen estructurado (modal de confirmacion) ---------- */
        .modal__cabecera { display: flex; align-items: center; gap: .6rem; }
        .modal__cabecera h3 { margin: 0; }
        .modal__icono {
            flex-shrink: 0; display: inline-flex; align-items: center; justify-content: center;
            width: 32px; height: 32px; border-radius: 50%;
            background: rgba(34, 197, 94, .14); color: var(--ok); font-size: 1rem; font-weight: 800;
        }
        .modal__icono--danger { background: rgba(239, 68, 68, .14); color: var(--error); }
        .modal__resumen-grid {
            display: grid; grid-template-columns: max-content 1fr;
            gap: .55rem 1.1rem;
            background: var(--surface-2); border: 1px solid var(--border);
            border-radius: 10px; padding: .9rem 1rem; margin: 0 0 1rem;
        }
        .modal__resumen-grid[hidden] { display: none; }
        .modal__resumen-clave {
            color: var(--muted); font-size: .76rem; font-weight: 600;
            text-transform: uppercase; letter-spacing: .02em;
            align-self: center; white-space: nowrap;
        }
        .modal__resumen-valor { color: #fff; font-weight: 700; font-size: .88rem; overflow-wrap: break-word; align-self: center; }
        .modal__resumen { margin: 0 0 .3rem; color: var(--muted); font-size: .9rem; line-height: 1.5; }
    </style>
</head>
<body>
<jsp:include page="navbar.jsp"><jsp:param name="activo" value="inventario"/></jsp:include>

<% if (mensaje != null) { %>
<script>
    document.addEventListener("DOMContentLoaded", function () {
        var msg = "<%= js(mensaje) %>";
        var tipo = /eliminad/i.test(msg) ? "danger" : (/actualizad|reducid/i.test(msg) ? "info" : "success");
        var titulo = tipo === "danger" ? "Insumo eliminado" : (tipo === "info" ? "Stock actualizado" : "Listo");
        showToast(tipo, titulo, msg);
    });
</script>
<% } %>
<% if (error != null) { %>
<script>
    document.addEventListener("DOMContentLoaded", function () {
        showToast("danger", "No se pudo completar la acción", "<%= js(error) %>");
    });
</script>
<% } %>

<main class="inv-main inv-main--grid">
    <section class="panel">
    <div class="inv-toolbar">
        <div class="inv-toolbar__buscar">
            <div class="buscador">
                <input type="text" id="filtro-insumo" class="input-filtro"
                       placeholder="Buscar insumo por nombre..." aria-label="Buscar insumo">
            </div>
            <div class="filtros-stock" id="filtros-stock" role="group" aria-label="Filtrar por nivel de stock">
                <button type="button" class="filtro-pill is-on" data-filtro="todos">Todos</button>
                <button type="button" class="filtro-pill filtro-pill--rojo" data-filtro="rojo">
                    <span class="status-dot status-dot--rojo"></span>Sin stock
                </button>
                <button type="button" class="filtro-pill filtro-pill--amarillo" data-filtro="amarillo">
                    <span class="status-dot status-dot--amarillo"></span>Bajo el mínimo
                </button>
                <button type="button" class="filtro-pill filtro-pill--verde" data-filtro="verde">
                    <span class="status-dot status-dot--verde"></span>Nivel óptimo
                </button>
            </div>
        </div>
        <div class="inv-toolbar__acciones">
            <button type="button" id="btn-abrir-crear" class="btn btn--ghost btn--sm">+ Crear insumo</button>
            <button type="button" id="btn-abrir-agregar" class="btn btn--icon btn--add">
                <span class="btn__icono">&uarr;</span> Añadir stock
            </button>
            <button type="button" id="btn-abrir-reducir" class="btn btn--icon btn--reduce">
                <span class="btn__icono">&darr;</span> Reducir stock
            </button>
        </div>
    </div>

    <section class="insumos-grid" id="insumos-grid">
        <% if (insumos == null || insumos.isEmpty()) { %>
        <p class="vacio">Sin insumos registrados.</p>
        <% } else {
            for (Insumo insumo : insumos) {
                Proveedor proveedor = (proveedorPorInsumo == null) ? null : proveedorPorInsumo.get(insumo.getId());
                String estado = insumo.getStock() <= 0 ? "rojo"
                        : (insumo.getStock() < insumo.getStockMinimo() ? "amarillo" : "verde");
                boolean enAlerta = "rojo".equals(estado) || "amarillo".equals(estado); %>
        <article class="insumo-card insumo-card--<%= estado %>"
                 data-nombre="<%= insumo.getNombre().toLowerCase() %>"
                 data-estado="<%= estado %>">
            <button type="button" class="insumo-card__editar" title="Editar insumo" aria-label="Editar insumo"
                    data-id="<%= insumo.getId() %>"
                    data-nombre="<%= attr(insumo.getNombre()) %>"
                    data-unidad="<%= insumo.getUnidad() %>"
                    data-minimo="<%= insumo.getStockMinimo() %>"
                    data-proveedor-id="<%= proveedor != null ? proveedor.getId() : 0 %>">&#9998;</button>
            <span class="insumo-card__estado status-dot status-dot--<%= estado %>"
                  title="Estado del stock"></span>
            <h3 class="insumo-card__nombre"><%= insumo.getNombre() %></h3>
            <div class="insumo-card__cantidad">
                <%= formatearCantidad(insumo.getStock(), insumo.getUnidad()) %>
                <span class="insumo-card__unidad"><%= insumo.getUnidad() %></span>
            </div>
            <div class="insumo-card__minimo">
                Mínimo: <%= formatearCantidad(insumo.getStockMinimo(), insumo.getUnidad()) %> <%= insumo.getUnidad() %>
            </div>
            <% if (enAlerta) { %>
            <button type="button" class="insumo-card__contacto"
                    data-proveedor-nombre="<%= proveedor != null ? attr(proveedor.getNombre()) : "" %>"
                    data-proveedor-telefono="<%= proveedor != null ? attr(proveedor.getTelefono()) : "" %>"
                    data-proveedor-correo="<%= proveedor != null ? attr(proveedor.getCorreo()) : "" %>">
                &#128222; Contactar proveedor
            </button>
            <% } %>
        </article>
        <%     }
        } %>
        <p class="vacio vacio--filtro" id="sin-resultados" hidden>Ningún insumo coincide con el filtro.</p>
    </section>
    </section>
</main>

<!-- Modal: crear insumo -->
<div class="modal-overlay" id="modal-crear-insumo" role="dialog" aria-modal="true" aria-labelledby="modal-crear-titulo">
    <div class="modal">
        <h3 id="modal-crear-titulo">Crear insumo</h3>
        <p class="hint">Registra un insumo nuevo con su nivel mínimo y proveedor (opcional).</p>
        <form class="form" id="form-crear-insumo" novalidate>
            <label>Nombre
                <input type="text" id="crear-nombre" maxlength="100" required placeholder="Ej: Cilantro fresco">
            </label>
            <label>Unidad
                <select id="crear-unidad" required>
                    <option value="kg">kg</option>
                    <option value="l">l</option>
                    <option value="unidades">unidades</option>
                </select>
            </label>
            <label>Stock mínimo
                <input type="number" id="crear-minimo" step="0.01" min="0" required>
            </label>
            <label>Proveedor (opcional)
                <select id="crear-proveedor">
                    <option value="0">Sin proveedor</option>
                    <% if (proveedores != null) { for (Proveedor p : proveedores) { %>
                    <option value="<%= p.getId() %>"><%= p.getNombre() %></option>
                    <% } } %>
                </select>
            </label>
            <div class="modal__acciones">
                <button type="button" class="btn btn--ghost" id="modal-crear-cancelar">Cancelar</button>
                <button type="submit" class="btn btn--ok">Crear insumo</button>
            </div>
        </form>
    </div>
</div>

<!-- Modal: editar insumo (unificado: nombre, unidad, minimo y proveedor en un solo guardado) -->
<div class="modal-overlay" id="modal-editar-insumo" role="dialog" aria-modal="true" aria-labelledby="modal-editar-titulo">
    <div class="modal">
        <h3 id="modal-editar-titulo">Editar insumo</h3>
        <p class="hint" id="editar-encabezado">Actualiza el insumo y guarda todos los cambios de una vez.</p>
        <form class="form" id="form-editar-insumo" novalidate>
            <input type="hidden" id="editar-id">
            <label>Nombre
                <input type="text" id="editar-nombre" maxlength="100" required>
            </label>
            <label>Unidad
                <select id="editar-unidad" required>
                    <option value="kg">kg</option>
                    <option value="l">l</option>
                    <option value="unidades">unidades</option>
                </select>
            </label>
            <label>Stock mínimo
                <input type="number" id="editar-minimo" step="0.01" min="0" required>
            </label>
            <label>Proveedor
                <select id="editar-proveedor">
                    <option value="0">Sin proveedor</option>
                    <% if (proveedores != null) { for (Proveedor p : proveedores) { %>
                    <option value="<%= p.getId() %>"><%= p.getNombre() %></option>
                    <% } } %>
                </select>
            </label>
            <div class="modal__acciones modal__acciones--split">
                <button type="button" class="btn btn--danger-ghost" id="btn-abrir-eliminar">Eliminar insumo</button>
                <div class="modal__acciones-derecha">
                    <button type="button" class="btn btn--ghost" id="modal-editar-cancelar">Cancelar</button>
                    <button type="submit" class="btn btn--ok">Guardar cambios</button>
                </div>
            </div>
        </form>
    </div>
</div>

<!-- Formularios reales que se envian al servidor (uno por accion); los modales de arriba solo recolectan datos. -->
<form method="post" action="<%= ctx %>/insumos<%= rolQs %>" id="form-envio-crear" style="display:none;">
    <input type="hidden" name="accion" value="crear">
    <input type="hidden" name="nombre" id="envio-crear-nombre">
    <input type="hidden" name="unidad" id="envio-crear-unidad">
    <input type="hidden" name="stockMinimo" id="envio-crear-minimo">
    <input type="hidden" name="proveedorId" id="envio-crear-proveedor">
</form>
<form method="post" action="<%= ctx %>/insumos<%= rolQs %>" id="form-envio-editar" style="display:none;">
    <input type="hidden" name="accion" value="actualizarInsumo">
    <input type="hidden" name="insumoId" id="envio-editar-id">
    <input type="hidden" name="nombre" id="envio-editar-nombre">
    <input type="hidden" name="unidad" id="envio-editar-unidad">
    <input type="hidden" name="stockMinimo" id="envio-editar-minimo">
    <input type="hidden" name="proveedorId" id="envio-editar-proveedor">
</form>
<form method="post" action="<%= ctx %>/insumos<%= rolQs %>" id="form-envio-eliminar" style="display:none;">
    <input type="hidden" name="accion" value="eliminar">
    <input type="hidden" name="insumoId" id="envio-eliminar-id">
</form>

<!-- Modal: añadir stock -->
<div class="modal-overlay" id="modal-agregar-stock" role="dialog" aria-modal="true" aria-labelledby="modal-agregar-titulo">
    <div class="modal">
        <h3 id="modal-agregar-titulo">Añadir stock</h3>
        <p class="hint">Selecciona el insumo y la cantidad a ingresar.</p>
        <form method="post" action="<%= ctx %>/insumos<%= rolQs %>" class="form form--confirmable"
              data-entero="false" data-titulo="Confirmar entrada" data-confirmar="Registrar">
            <input type="hidden" name="accion" value="registrar">
            <label>Insumo
                <select name="insumoId" required autofocus>
                    <option value="">-- Selecciona un insumo --</option>
                    <% if (insumos != null) {
                        for (Insumo insumo : insumos) { %>
                    <option value="<%= insumo.getId() %>" data-unidad="<%= insumo.getUnidad() %>"><%= insumo.getNombre() %></option>
                    <%     }
                    } %>
                </select>
            </label>
            <label>Cantidad <span class="unidad-hint"></span>
                <input type="number" name="cantidad" step="0.01" min="0.01" required>
            </label>
            <p class="form__error" aria-live="polite"></p>
            <div class="modal__acciones">
                <button type="button" class="btn btn--ghost" id="modal-agregar-cancelar">Cancelar</button>
                <button type="submit" class="btn btn--ok">Registrar</button>
            </div>
        </form>
    </div>
</div>

<!-- Modal: reducir stock -->
<div class="modal-overlay" id="modal-reducir-stock" role="dialog" aria-modal="true" aria-labelledby="modal-reducir-titulo">
    <div class="modal">
        <h3 id="modal-reducir-titulo">Reducir stock</h3>
        <p class="hint">Mermas, pérdidas o desperdicios.</p>
        <form method="post" action="<%= ctx %>/insumos<%= rolQs %>" class="form form--confirmable"
              data-entero="false" data-titulo="Confirmar reduccion" data-confirmar="Confirmar reduccion">
            <input type="hidden" name="accion" value="reducir">
            <label>Insumo
                <select name="insumoId" required>
                    <option value="">-- Selecciona un insumo --</option>
                    <% if (insumos != null) {
                        for (Insumo insumo : insumos) { %>
                    <option value="<%= insumo.getId() %>" data-unidad="<%= insumo.getUnidad() %>" data-stock="<%= insumo.getStock() %>"><%= insumo.getNombre() %></option>
                    <%     }
                    } %>
                </select>
            </label>
            <label>Cantidad a reducir <span class="unidad-hint"></span>
                <input type="number" name="cantidad" step="0.01" min="0.01" required>
            </label>
            <p class="panel__hint stock-disponible-hint"></p>
            <p class="form__error" aria-live="polite"></p>
            <div class="modal__acciones">
                <button type="button" class="btn btn--ghost" id="modal-reducir-cancelar">Cancelar</button>
                <button type="submit" class="btn btn--warn">Reducir stock</button>
            </div>
        </form>
    </div>
</div>

<!-- Modal generico de confirmacion, reutilizado por todos los flujos (crear/editar/eliminar/stock) -->
<div class="modal-overlay" id="modal-confirm" role="dialog" aria-modal="true" aria-labelledby="modal-titulo">
    <div class="modal">
        <div class="modal__cabecera">
            <span class="modal__icono" id="modal-icono" aria-hidden="true">&#10003;</span>
            <h3 id="modal-titulo">Confirmar</h3>
        </div>
        <p class="hint" id="modal-intro">Por favor, verifica los datos antes de guardar:</p>
        <div class="modal__resumen-grid" id="modal-resumen-grid"></div>
        <p class="modal__resumen" id="modal-resumen"></p>
        <div class="modal__acciones">
            <button type="button" class="btn btn--ghost" id="modal-cancelar">Cancelar</button>
            <button type="button" class="btn btn--ok" id="modal-confirmar">Confirmar</button>
        </div>
    </div>
</div>

<!-- Popover/modal rapido de contacto de proveedor (solo tarjetas en alerta) -->
<div class="modal-overlay modal-overlay--popover" id="modal-contacto-proveedor" role="dialog" aria-modal="true" aria-labelledby="modal-contacto-titulo">
    <div class="modal modal--popover">
        <h3 id="modal-contacto-titulo">Contactar proveedor</h3>
        <div id="contacto-proveedor-cuerpo"></div>
        <div class="modal__acciones">
            <button type="button" class="btn btn--ghost" id="modal-contacto-cerrar">Cerrar</button>
        </div>
    </div>
</div>

<script src="<%= ctx %>/resources/js/toast.js"></script>
<script src="<%= ctx %>/resources/js/inventario.js"></script>
</body>
</html>