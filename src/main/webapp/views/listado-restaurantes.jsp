<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Restaurante" %>
<%@ page import="util.ColorMarca" %>
<%
    List<Restaurante> restaurantes = (List<Restaurante>) request.getAttribute("restaurantes");
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
    /** Color efectivo de la marca: el asignado explicitamente (persistido en BD; ver
     *  {@link dao.ConexionBD}, que ya rellena este campo para todo restaurante sin
     *  color al arrancar), o el mismo respaldo determinista que usa el resto del
     *  sistema como ultimo recurso. */
    private String colorEfectivo(Restaurante r) {
        if (r.getColor() != null && !r.getColor().isBlank()) {
            return r.getColor();
        }
        return ColorMarca.paraNombre(r.getNombre());
    }

    private String inicial(String nombre) {
        return (nombre == null || nombre.isEmpty()) ? "?" : nombre.substring(0, 1).toUpperCase();
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
    <title>Restaurantes | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/gestion.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/main.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/toast.css">
</head>
<body>
<jsp:include page="navbar.jsp"><jsp:param name="activo" value="restaurantes"/></jsp:include>

<% if (mensaje != null) { %>
<script>
    document.addEventListener("DOMContentLoaded", function () {
        var msg = "<%= js(mensaje) %>";
        var tipo = /eliminad/i.test(msg) ? "danger" : (/actualizad/i.test(msg) ? "info" : "success");
        var titulo = tipo === "danger" ? "Restaurante eliminado" : (tipo === "info" ? "Restaurante actualizado" : "Restaurante creado");
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

<main class="gestion-main">
    <section class="panel">
        <div class="gestion-toolbar">
            <div class="gestion-toolbar__buscar">
                <div class="buscador">
                    <input type="text" id="buscador-restaurantes" class="input-filtro"
                           placeholder="Buscar marca..." aria-label="Buscar restaurante por nombre">
                </div>
            </div>
            <div class="gestion-toolbar__acciones">
                <button type="button" id="btn-abrir-nuevo" class="btn-link destacado">+ Nuevo restaurante</button>
            </div>
        </div>
        <% if (restaurantes == null || restaurantes.isEmpty()) { %>
            <p class="vacio">No hay restaurantes registrados</p>
        <% } else { %>
            <div class="marca-lista" id="lista-restaurantes">
                <% for (Restaurante r : restaurantes) {
                       String descripcion = (r.getDescripcion() == null || r.getDescripcion().isEmpty())
                               ? "Sin descripción" : r.getDescripcion();
                       String colorMarca = colorEfectivo(r); %>
                    <article class="marca-card" data-nombre="<%= attr(r.getNombre().toLowerCase()) %>" style="--marca: <%= colorMarca %>;">
                        <div class="marca-card__avatar">
                            <%= inicial(r.getNombre()) %>
                        </div>
                        <div class="marca-card__info">
                            <h3 class="marca-card__nombre"><%= r.getNombre() %></h3>
                            <p class="marca-card__descripcion"><%= descripcion %></p>
                        </div>
                        <div class="marca-card__acciones">
                            <button type="button" class="tabla__accion-icono marca-editar" title="Editar restaurante" aria-label="Editar restaurante"
                                    data-id="<%= r.getId() %>"
                                    data-nombre="<%= attr(r.getNombre()) %>"
                                    data-descripcion="<%= attr(r.getDescripcion() == null ? "" : r.getDescripcion()) %>"
                                    data-color="<%= attr(colorMarca) %>">&#9998;</button>
                            <button type="button" class="tabla__accion-icono tabla__accion-icono--eliminar marca-eliminar" title="Eliminar restaurante" aria-label="Eliminar restaurante"
                                    data-id="<%= r.getId() %>"
                                    data-nombre="<%= attr(r.getNombre()) %>">&#128465;</button>
                        </div>
                    </article>
                <% } %>
                <p class="vacio vacio--filtro" id="sin-resultados-restaurantes" hidden>Ninguna marca coincide con la busqueda.</p>
            </div>
        <% } %>
    </section>
</main>

<!-- Modal: nuevo / editar restaurante -->
<div class="modal-overlay" id="modal-restaurante" role="dialog" aria-modal="true" aria-labelledby="modal-restaurante-titulo">
    <div class="modal">
        <h3 id="modal-restaurante-titulo">Nuevo restaurante</h3>
        <p class="hint">Nombre 2-100 caracteres. Descripci&oacute;n opcional, m&aacute;ximo 255 caracteres.</p>
        <form method="post" action="<%= ctx %>/restaurantes<%= rolQs %>" class="form" id="form-restaurante" novalidate>
            <input type="hidden" name="accion" id="restaurante-accion" value="guardar">
            <input type="hidden" name="id" id="restaurante-id" value="">
            <label>Nombre
                <input type="text" name="nombre" id="restaurante-nombre" maxlength="100" required placeholder="Ej: Green Bowl">
            </label>
            <label>Descripci&oacute;n (opcional)
                <textarea name="descripcion" id="restaurante-descripcion" maxlength="255" rows="3" placeholder="Ej: Bowls saludables y veganos."></textarea>
            </label>
            <label>Color de marca
                <div class="campo-color">
                    <input type="color" name="color" id="restaurante-color" value="#f97316" aria-label="Selector de color de marca">
                    <input type="text" id="restaurante-color-hex" class="campo-color__hex" maxlength="7"
                           placeholder="#F97316" aria-label="C&oacute;digo hexadecimal del color de marca">
                </div>
            </label>
            <div class="modal__acciones">
                <button type="button" class="btn btn--ghost" id="modal-restaurante-cancelar">Cancelar</button>
                <button type="submit" class="btn btn--ok" id="restaurante-guardar-btn">Guardar</button>
            </div>
        </form>
    </div>
</div>

<!-- Formulario oculto real de eliminacion; el icono de papelera solo lo prepara y pide confirmacion -->
<form method="post" action="<%= ctx %>/restaurantes<%= rolQs %>" id="form-eliminar-restaurante" style="display:none;">
    <input type="hidden" name="accion" value="eliminar">
    <input type="hidden" name="id" id="eliminar-restaurante-id">
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
<script src="<%= ctx %>/resources/js/restaurantes.js"></script>
</body>
</html>
