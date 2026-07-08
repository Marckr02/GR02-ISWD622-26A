<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Restaurante" %>
<%@ page import="model.Insumo" %>
<%
    List<Restaurante> restaurantes = (List<Restaurante>) request.getAttribute("restaurantes");
    List<Insumo> insumos = (List<Insumo>) request.getAttribute("insumos");
    String ctx = request.getContextPath();
    String rol = request.getParameter("rol");
    if ((rol == null || rol.isEmpty()) && session.getAttribute("rol") != null) {
        rol = (String) session.getAttribute("rol");
    }
    String rolQs = (rol == null || rol.isEmpty()) ? "" : ("?rol=" + rol);

    String error = (String) session.getAttribute("error");
    if (error != null) { session.removeAttribute("error"); }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Registrar plato | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/gestion.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/main.css">
</head>
<body>
<jsp:include page="navbar.jsp"><jsp:param name="activo" value="platos"/></jsp:include>

<header class="gestion-top">
    <div class="gestion-top__brand">
        <span class="gestion-top__dot"></span>
        <div>
            <h1>Registrar plato</h1>
            <p>Nombre, restaurante y receta con insumos, cantidad y unidad.</p>
        </div>
    </div>
    <nav class="gestion-nav">
        <a href="<%= ctx %>/platos<%= rolQs %>">&larr; Volver al listado</a>
    </nav>
</header>

<% if (error != null) { %>
    <div class="aviso aviso--error" role="alert"><%= error %></div>
<% } %>

<main class="gestion-main gestion-main--form">
    <section class="panel">
        <h2>Nuevo plato</h2>
        <p class="panel__hint">Nombre 2-100 caracteres. Agrega al menos un insumo con cantidad mayor a cero.</p>
        <form method="post" action="<%= ctx %>/platos<%= rolQs %>" class="form">
            <input type="hidden" name="accion" value="guardar">
            <label>Nombre
                <input type="text" name="nombre" maxlength="100" required placeholder="Ej: Ensalada Cesar">
            </label>
            <label>Restaurante
                <select name="restauranteId" required>
                    <option value="">-- Selecciona un restaurante --</option>
                    <% if (restaurantes != null) { for (Restaurante r : restaurantes) { %>
                        <option value="<%= r.getId() %>"><%= r.getNombre() %></option>
                    <% } } %>
                </select>
            </label>

            <div>
                <label style="margin-bottom:.5rem;">Receta (insumos)</label>
                <div class="receta" id="receta-filas">
                    <div class="receta__fila">
                        <label>Insumo
                            <select name="insumoId[]" required>
                                <option value="">-- Insumo --</option>
                                <% if (insumos != null) { for (Insumo i : insumos) { %>
                                    <option value="<%= i.getId() %>"><%= i.getNombre() %></option>
                                <% } } %>
                            </select>
                        </label>
                        <label>Cantidad
                            <input type="number" name="cantidad[]" step="0.01" min="0.01" required>
                        </label>
                        <label>Unidad
                            <select name="unidad[]" required>
                                <option value="g">g</option>
                                <option value="kg">kg</option>
                                <option value="ml">ml</option>
                                <option value="l">l</option>
                                <option value="unidades">unidades</option>
                            </select>
                        </label>
                        <button type="button" class="receta__quitar" title="Quitar insumo">&times;</button>
                    </div>
                </div>

                <!-- Plantilla oculta para clonar nuevas filas; sus campos van "disabled" para no enviarse hasta que se clonen y habiliten -->
                <div class="receta__fila" id="receta-plantilla" style="display:none;">
                    <label>Insumo
                        <select name="insumoId[]" required disabled>
                            <option value="">-- Insumo --</option>
                            <% if (insumos != null) { for (Insumo i : insumos) { %>
                                <option value="<%= i.getId() %>"><%= i.getNombre() %></option>
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

            <div class="form__actions">
                <button type="submit" class="btn btn--ok">Guardar</button>
                <a class="btn-link" href="<%= ctx %>/platos<%= rolQs %>">Cancelar</a>
            </div>
        </form>
    </section>
</main>
<script src="<%= ctx %>/resources/js/receta.js"></script>
</body>
</html>
