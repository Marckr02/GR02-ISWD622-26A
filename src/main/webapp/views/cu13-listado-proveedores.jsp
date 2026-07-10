<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Proveedor" %>
<%
    List<Proveedor> proveedores = (List<Proveedor>) request.getAttribute("proveedores");
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
    <title>Proveedores | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/gestion.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/main.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/toast.css">
</head>
<body>
<jsp:include page="navbar.jsp"><jsp:param name="activo" value="proveedores"/></jsp:include>

<% if (mensaje != null) { %>
<script>
    document.addEventListener("DOMContentLoaded", function () {
        var msg = "<%= js(mensaje) %>";
        var tipo = /eliminad/i.test(msg) ? "danger" : (/actualizad/i.test(msg) ? "info" : "success");
        var titulo = tipo === "danger" ? "Proveedor eliminado" : (tipo === "info" ? "Proveedor actualizado" : "Proveedor creado");
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

<main class="gestion-main gestion-main--grid">
    <section class="panel">
    <div class="gestion-toolbar">
        <div class="gestion-toolbar__buscar">
            <div class="buscador">
                <input type="text" id="buscador-proveedores" class="input-filtro"
                       placeholder="Buscar proveedor por nombre..." aria-label="Buscar proveedor por nombre">
            </div>
        </div>
        <div class="gestion-toolbar__acciones">
            <button type="button" id="btn-abrir-nuevo" class="btn-link destacado">+ Nuevo proveedor</button>
        </div>
    </div>
    <% if (proveedores == null || proveedores.isEmpty()) { %>
        <p class="vacio">No hay proveedores registrados</p>
    <% } else { %>
        <div class="proveedor-grid" id="lista-proveedores">
            <% for (Proveedor p : proveedores) { %>
                <article class="proveedor-card" data-nombre="<%= attr(p.getNombre().toLowerCase()) %>">
                    <div class="proveedor-card__header">
                        <span class="proveedor-card__icono" aria-hidden="true">&#128666;</span>
                        <h3 class="proveedor-card__nombre"><%= p.getNombre() %></h3>
                        <div class="proveedor-card__acciones">
                            <button type="button" class="tabla__accion-icono proveedor-editar" title="Editar proveedor" aria-label="Editar proveedor"
                                    data-id="<%= p.getId() %>"
                                    data-nombre="<%= attr(p.getNombre()) %>"
                                    data-telefono="<%= attr(p.getTelefono()) %>"
                                    data-correo="<%= attr(p.getCorreo()) %>">&#9998;</button>
                            <button type="button" class="tabla__accion-icono tabla__accion-icono--eliminar proveedor-eliminar" title="Eliminar proveedor" aria-label="Eliminar proveedor"
                                    data-id="<%= p.getId() %>"
                                    data-nombre="<%= attr(p.getNombre()) %>">&#128465;</button>
                        </div>
                    </div>
                    <div class="proveedor-card__contacto">
                        <div class="proveedor-card__fila">
                            <span class="proveedor-card__fila-icono" aria-hidden="true">&#128222;</span>
                            <span><%= p.getTelefono() %></span>
                        </div>
                        <div class="proveedor-card__fila">
                            <span class="proveedor-card__fila-icono" aria-hidden="true">&#9993;</span>
                            <span><%= p.getCorreo() %></span>
                        </div>
                    </div>
                </article>
            <% } %>
            <p class="vacio vacio--filtro" id="sin-resultados-proveedores" hidden>Ningun proveedor coincide con la busqueda.</p>
        </div>
    <% } %>
    </section>
</main>

<!-- Modal: nuevo / editar proveedor -->
<div class="modal-overlay" id="modal-proveedor" role="dialog" aria-modal="true" aria-labelledby="modal-proveedor-titulo">
    <div class="modal">
        <h3 id="modal-proveedor-titulo">Nuevo proveedor</h3>
        <p class="hint">Nombre 2-100 caracteres, tel&eacute;fono 7-15 d&iacute;gitos, correo usuario@dominio.ext.</p>
        <form method="post" action="<%= ctx %>/proveedores<%= rolQs %>" class="form" id="form-proveedor" novalidate>
            <input type="hidden" name="accion" id="proveedor-accion" value="guardar">
            <input type="hidden" name="id" id="proveedor-id" value="">
            <label>Nombre
                <input type="text" name="nombre" id="proveedor-nombre" maxlength="100" required placeholder="Ej: Distribuidora Sierra">
            </label>
            <label>Tel&eacute;fono
                <input type="tel" name="telefono" id="proveedor-telefono" maxlength="15" required placeholder="Ej: 0991234567" pattern="\d{7,15}">
            </label>
            <label>Correo electr&oacute;nico
                <input type="email" name="correo" id="proveedor-correo" required placeholder="Ej: contacto@proveedor.com">
            </label>
            <div class="modal__acciones">
                <button type="button" class="btn btn--ghost" id="modal-proveedor-cancelar">Cancelar</button>
                <button type="submit" class="btn btn--ok" id="proveedor-guardar-btn">Guardar</button>
            </div>
        </form>
    </div>
</div>

<!-- Formulario oculto real de eliminacion; el icono de papelera solo lo prepara y pide confirmacion -->
<form method="post" action="<%= ctx %>/proveedores<%= rolQs %>" id="form-eliminar-proveedor" style="display:none;">
    <input type="hidden" name="accion" value="eliminar">
    <input type="hidden" name="id" id="eliminar-proveedor-id">
</form>

<!-- Modal generico de confirmacion, reutilizado por guardar/actualizar/eliminar -->
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

<script src="<%= ctx %>/resources/js/toast.js"></script>
<script src="<%= ctx %>/resources/js/proveedores.js"></script>
</body>
</html>
