<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Plato" %>
<%@ page import="model.IngredientePlato" %>
<%@ page import="model.Restaurante" %>
<%@ page import="model.Insumo" %>
<%
    Plato plato = (Plato) request.getAttribute("plato");
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
    <title>Editar plato | Dark Kitchen</title>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/gestion.css">
    <link rel="stylesheet" href="<%= ctx %>/resources/css/main.css">
</head>
<body>
<jsp:include page="navbar.jsp"><jsp:param name="activo" value="platos"/></jsp:include>

<header class="gestion-top">
    <div class="gestion-top__brand">
        <span class="gestion-top__dot"></span>
        <div>
            <h1>Editar plato</h1>
            <p>Actualiza nombre, restaurante o los ingredientes de la receta.</p>
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
        <h2><%= plato.getNombre() %></h2>
        <form method="post" action="<%= ctx %>/platos<%= rolQs %>" class="form">
            <input type="hidden" name="accion" value="actualizar">
            <input type="hidden" name="id" value="<%= plato.getId() %>">
            <label>Nombre
                <input type="text" name="nombre" maxlength="100" required value="<%= plato.getNombre() %>">
            </label>
            <label>Restaurante
                <select name="restauranteId" required>
                    <option value="">-- Selecciona un restaurante --</option>
                    <% if (restaurantes != null) { for (Restaurante r : restaurantes) { %>
                        <option value="<%= r.getId() %>" <%= r.getId() == plato.getRestauranteId() ? "selected" : "" %>><%= r.getNombre() %></option>
                    <% } } %>
                </select>
            </label>

            <div>
                <label style="margin-bottom:.5rem;">Receta (insumos)</label>
                <div class="receta" id="receta-filas">
                    <% for (IngredientePlato ing : plato.getIngredientes()) { %>
                        <div class="receta__fila">
                            <label>Insumo
                                <select name="insumoId[]" required>
                                    <option value="">-- Insumo --</option>
                                    <% if (insumos != null) { for (Insumo i : insumos) { %>
                                        <option value="<%= i.getId() %>" <%= i.getId() == ing.getInsumoId() ? "selected" : "" %>><%= i.getNombre() %></option>
                                    <% } } %>
                                </select>
                            </label>
                            <label>Cantidad
                                <input type="number" name="cantidad[]" step="0.01" min="0.01" required value="<%= ing.getCantidad() %>">
                            </label>
                            <label>Unidad
                                <% String unidadIng = ing.getUnidadReceta() == null ? "unidades" : ing.getUnidadReceta(); %>
                                <select name="unidad[]" required>
                                    <option value="g" <%= "g".equals(unidadIng) ? "selected" : "" %>>g</option>
                                    <option value="kg" <%= "kg".equals(unidadIng) ? "selected" : "" %>>kg</option>
                                    <option value="ml" <%= "ml".equals(unidadIng) ? "selected" : "" %>>ml</option>
                                    <option value="l" <%= "l".equals(unidadIng) ? "selected" : "" %>>l</option>
                                    <option value="unidades" <%= "unidades".equals(unidadIng) ? "selected" : "" %>>unidades</option>
                                </select>
                            </label>
                            <button type="button" class="receta__quitar" title="Quitar insumo">&times;</button>
                        </div>
                    <% } %>
                </div>

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
                <button type="submit" class="btn btn--ok">Guardar cambios</button>
                <a class="btn-link" href="<%= ctx %>/platos<%= rolQs %>">Cancelar</a>
            </div>
        </form>
    </section>
</main>
<script src="<%= ctx %>/resources/js/receta.js"></script>
</body>
</html>
