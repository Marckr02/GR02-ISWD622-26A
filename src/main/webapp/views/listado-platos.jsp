<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Plato" %>
<%@ page import="model.IngredientePlato" %>
<%@ page import="model.Restaurante" %>
<%@ page import="model.Insumo" %>
<%@ page import="service.PlatoService" %>
<%@ page import="util.ColorMarca" %>
<%
    List<Plato> platos = (List<Plato>) request.getAttribute("platos");
    List<Restaurante> restaurantes = (List<Restaurante>) request.getAttribute("restaurantes");
    // Copia ordenada alfabeticamente (sin mutar la lista original) para que el
    // filtro por restaurante de la toolbar y el select del modal de nuevo/editar
    // plato listen en orden alfabetico -- asi el salto por teclado del <select>
    // nativo, al escribir una letra, es predecible.
    if (restaurantes != null) {
        restaurantes = new java.util.ArrayList<>(restaurantes);
        restaurantes.sort(java.util.Comparator.comparing(Restaurante::getNombre, String.CASE_INSENSITIVE_ORDER));
    }
    List<Insumo> insumos = (List<Insumo>) request.getAttribute("insumos");
    PlatoService platoService = (PlatoService) request.getAttribute("platoService");
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
    /** Serializa los ingredientes de un plato a JSON simple para poblar el modal de edicion via JS. */
    private String ingredientesJson(List<IngredientePlato> ingredientes) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < ingredientes.size(); i++) {
            IngredientePlato ing = ingredientes.get(i);
            if (i > 0) { sb.append(","); }
            String unidad = ing.getUnidadReceta() == null ? "unidades" : ing.getUnidadReceta();
            sb.append("{\"insumoId\":").append(ing.getInsumoId())
              .append(",\"cantidad\":").append(ing.getCantidad())
              .append(",\"unidad\":\"").append(unidad).append("\"}");
        }
        sb.append("]");
        return sb.toString();
    }

    /** Escapa comillas dobles para poder incrustar el valor dentro de un atributo HTML. */
    private String attr(String valor) {
        return valor == null ? "" : valor.replace("\"", "&quot;");
    }

    /** Color efectivo de la marca del restaurante (el asignado, o un respaldo determinista
     *  si no tiene ninguno o el plato quedo huerfano), mismo criterio que el resto del sistema. */
    private String colorDeMarca(Restaurante r) {
        if (r == null) { return "#8b97a6"; }
        return (r.getColor() != null && !r.getColor().isBlank()) ? r.getColor() : ColorMarca.paraNombre(r.getNombre());
    }

    /** Copia los ingredientes ordenados de mayor a menor cantidad (sin mutar la lista
     *  original del plato, que el modal de edicion sigue poblando en su orden real). */
    private List<IngredientePlato> ordenadosPorCantidad(List<IngredientePlato> ingredientes) {
        List<IngredientePlato> copia = new java.util.ArrayList<>(ingredientes);
        copia.sort((a, b) -> Double.compare(b.getCantidad(), a.getCantidad()));
        return copia;
    }

    /** Cantidad sin decimales innecesarios: 250.0 -> "250", 2.5 -> "2.5". */
    private String formatearCantidad(double cantidad) {
        if (cantidad == Math.rint(cantidad) && !Double.isInfinite(cantidad)) {
            return String.valueOf((long) cantidad);
        }
        return String.valueOf(cantidad);
    }

    /** Escapa un texto para incrustarlo como literal (comillas/backslash) dentro de un atributo JSON. */
    private String jsonTexto(String valor) {
        return valor == null ? "" : valor.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /** Serializa, desde el indice "desde" en adelante, nombre + cantidad ya formateada de cada
     *  ingrediente restante -- alimenta el popover flotante del boton "+X ingredientes". */
    private String extraIngredientesJson(List<IngredientePlato> ingredientes, int desde, PlatoService platoService) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = desde; i < ingredientes.size(); i++) {
            IngredientePlato ing = ingredientes.get(i);
            if (i > desde) { sb.append(","); }
            Insumo insumo = platoService.insumoDe(ing.getInsumoId());
            String nombre = (insumo == null) ? ("#" + ing.getInsumoId()) : insumo.getNombre();
            String unidad = (ing.getUnidadReceta() == null)
                    ? (insumo == null ? "" : insumo.getUnidad())
                    : ing.getUnidadReceta();
            String cantidadTexto = formatearCantidad(ing.getCantidad()) + " " + unidad;
            sb.append("{\"nombre\":\"").append(jsonTexto(nombre)).append("\"")
              .append(",\"cantidad\":\"").append(jsonTexto(cantidadTexto)).append("\"}");
        }
        sb.append("]");
        return sb.toString();
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
    <title>Platos | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/gestion.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/main.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/toast.css">
</head>
<body>
<jsp:include page="navbar.jsp"><jsp:param name="activo" value="platos"/></jsp:include>

<% if (mensaje != null) { %>
<script>
    document.addEventListener("DOMContentLoaded", function () {
        var msg = "<%= js(mensaje) %>";
        var tipo = /eliminad/i.test(msg) ? "danger" : (/actualizad/i.test(msg) ? "info" : "success");
        var titulo = tipo === "danger" ? "Plato eliminado" : (tipo === "info" ? "Plato actualizado" : "Plato creado");
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
                <div class="buscador buscador--platos">
                    <input type="text" id="buscador-platos" class="input-filtro"
                           placeholder="Buscar platillo o ingrediente..." aria-label="Buscar platillo o ingrediente">
                </div>
                <select id="filtro-restaurante" aria-label="Filtrar por restaurante">
                    <option value="">Todos los restaurantes</option>
                    <% if (restaurantes != null) { for (Restaurante r : restaurantes) { %>
                        <option value="<%= r.getId() %>"><%= r.getNombre() %></option>
                    <% } } %>
                </select>
            </div>
            <div class="gestion-toolbar__acciones">
                <button type="button" id="btn-abrir-nuevo" class="btn-link destacado">+ Nuevo plato</button>
            </div>
        </div>
        <% if (platos == null || platos.isEmpty()) { %>
            <p class="vacio">No hay platos registrados</p>
        <% } else { %>
            <div class="plato-lista" id="lista-platos">
                <% for (Plato p : platos) {
                       Restaurante restaurante = platoService.restauranteDe(p);
                       String colorPlato = colorDeMarca(restaurante);
                       String nombreRestaurante = restaurante == null ? "Sin restaurante" : restaurante.getNombre();
                       List<IngredientePlato> ingredientesOriginal = p.getIngredientes();
                       List<IngredientePlato> ingredientes = ordenadosPorCantidad(ingredientesOriginal);
                       int total = ingredientes.size();
                       int top = Math.min(3, total);

                       // Texto de busqueda: nombre del plato + nombre de todos los insumos (no solo
                       // el top 3 visible), para que el buscador tambien encuentre por ingrediente.
                       StringBuilder busqueda = new StringBuilder(p.getNombre().toLowerCase());
                       for (IngredientePlato ing : ingredientesOriginal) {
                           Insumo insumoBusqueda = platoService.insumoDe(ing.getInsumoId());
                           if (insumoBusqueda != null) { busqueda.append(' ').append(insumoBusqueda.getNombre().toLowerCase()); }
                       } %>
                    <article class="plato-card" data-restaurante-id="<%= p.getRestauranteId() %>"
                             data-nombre="<%= attr(p.getNombre().toLowerCase()) %>"
                             data-busqueda="<%= attr(busqueda.toString()) %>" style="--marca: <%= colorPlato %>;">
                        <div class="plato-card__fila-superior">
                            <div class="plato-card__titulos">
                                <h3 class="plato-card__nombre" title="<%= attr(p.getNombre()) %>"><%= p.getNombre() %></h3>
                                <p class="plato-card__restaurante"><span class="plato-card__dot" aria-hidden="true"></span><%= nombreRestaurante %></p>
                            </div>
                            <button type="button" class="plato-card__menu-btn" title="Más acciones" aria-label="Más acciones para <%= attr(p.getNombre()) %>"
                                    aria-haspopup="true" aria-expanded="false"
                                    data-id="<%= p.getId() %>"
                                    data-nombre="<%= attr(p.getNombre()) %>"
                                    data-restaurante-id="<%= p.getRestauranteId() %>"
                                    data-ingredientes='<%= ingredientesJson(ingredientesOriginal) %>'>&#8942;</button>
                        </div>
                        <% if (total == 0) { %>
                            <p class="plato-card__sin">Sin ingredientes</p>
                        <% } else { %>
                            <ul class="plato-card__ingredientes">
                                <% for (int idx = 0; idx < top; idx++) {
                                       IngredientePlato ing = ingredientes.get(idx);
                                       Insumo insumo = platoService.insumoDe(ing.getInsumoId());
                                       String nombreInsumo = (insumo == null) ? ("#" + ing.getInsumoId()) : insumo.getNombre();
                                       String unidad = (ing.getUnidadReceta() == null)
                                               ? (insumo == null ? "" : insumo.getUnidad())
                                               : ing.getUnidadReceta(); %>
                                    <li class="plato-card__fila-ingrediente">
                                        <span class="plato-card__ing-nombre"><%= nombreInsumo %></span>
                                        <span class="plato-card__ing-cantidad"><%= formatearCantidad(ing.getCantidad()) %> <%= unidad %></span>
                                    </li>
                                <% } %>
                            </ul>
                            <% if (total > top) { %>
                                <button type="button" class="plato-card__mas-btn"
                                        data-plato-nombre="<%= attr(p.getNombre()) %>"
                                        data-ingredientes='<%= extraIngredientesJson(ingredientes, 0, platoService) %>'>+ <%= (total - top) %> insumo<%= (total - top) == 1 ? "" : "s" %> m&aacute;s</button>
                            <% } %>
                        <% } %>
                    </article>
                <% } %>
                <p class="vacio" id="sin-resultados-platos" hidden>Ningun plato coincide con el filtro.</p>
            </div>
        <% } %>
    </section>
</main>

<!-- Modal: nuevo / editar plato (unificado, con scroll interno para la receta y acciones fijas) -->
<div class="modal-overlay" id="modal-plato" role="dialog" aria-modal="true" aria-labelledby="modal-plato-titulo">
    <div class="modal modal--plato">
        <h3 id="modal-plato-titulo">Nuevo plato</h3>
        <p class="hint">Nombre 2-100 caracteres. Agrega al menos un insumo con cantidad mayor a cero.</p>
        <form method="post" action="<%= ctx %>/platos<%= rolQs %>" class="form" id="form-plato" novalidate>
            <input type="hidden" name="accion" id="plato-accion" value="guardar">
            <input type="hidden" name="id" id="plato-id" value="">
            <div class="modal__scroll">
                <label>Nombre
                    <input type="text" name="nombre" id="plato-nombre" maxlength="100" required placeholder="Ej: Ensalada Cesar">
                </label>
                <label>Restaurante
                    <select name="restauranteId" id="plato-restaurante" required>
                        <option value="">-- Selecciona un restaurante --</option>
                        <% if (restaurantes != null) { for (Restaurante r : restaurantes) { %>
                            <option value="<%= r.getId() %>"><%= r.getNombre() %></option>
                        <% } } %>
                    </select>
                </label>

                <div>
                    <label style="margin-bottom:.5rem;">Receta (insumos)</label>
                    <div class="receta" id="receta-filas"></div>

                    <!-- Plantilla oculta para clonar filas; se usa tanto para "nuevo" como para poblar "editar" -->
                    <div class="receta__fila" id="receta-plantilla" style="display:none;">
                        <label>Insumo
                            <select name="insumoId[]" required disabled>
                                <option value="">-- Insumo --</option>
                                <% if (insumos != null) { for (Insumo i : insumos) { %>
                                    <option value="<%= i.getId() %>" data-unidad="<%= attr(i.getUnidad()) %>"><%= i.getNombre() %></option>
                                <% } } %>
                            </select>
                        </label>
                        <label>Cantidad
                            <input type="number" name="cantidad[]" step="0.01" min="0.01" required disabled>
                        </label>
                        <label>Unidad
                            <select name="unidad[]" required disabled>
                                <option value="g">g</option>
                                <option value="kg">kg</option>
                                <option value="ml">ml</option>
                                <option value="l">l</option>
                                <option value="unidades">unidades</option>
                            </select>
                        </label>
                        <button type="button" class="receta__quitar" title="Quitar insumo">&times;</button>
                    </div>

                    <button type="button" id="receta-agregar" class="receta__agregar">+ Agregar insumo</button>
                    <p class="form__error" aria-live="polite"></p>
                </div>
            </div>
            <div class="modal__acciones">
                <button type="button" class="btn btn--ghost" id="modal-plato-cancelar">Cancelar</button>
                <button type="submit" class="btn btn--ok" id="plato-guardar-btn">Guardar</button>
            </div>
        </form>
    </div>
</div>

<!-- Formulario oculto real de eliminacion; el icono de papelera solo lo prepara y pide confirmacion -->
<form method="post" action="<%= ctx %>/platos<%= rolQs %>" id="form-eliminar-plato" style="display:none;">
    <input type="hidden" name="accion" value="eliminar">
    <input type="hidden" name="id" id="eliminar-plato-id">
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
<script src="<%= ctx %>/resources/js/platos.js"></script>
</body>
</html>
