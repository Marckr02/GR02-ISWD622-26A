<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.Rol" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.List" %>
<%
    String navCtx = request.getContextPath();
    Rol navRol = Rol.desde(request.getParameter("rol"));
    if (navRol == null) { navRol = Rol.desde((String) session.getAttribute("rol")); }
    if (navRol == null) { navRol = Rol.COCINERO; }
    String activo = request.getParameter("activo");
    if (activo == null) { activo = ""; }

    // Pestanas visibles por rol (solo lo que le corresponde a cada vista).
    List<String> visibles;
    switch (navRol) {
        case ADMIN_BODEGA:
            visibles = Arrays.asList("tablero", "inventario", "menu", "monitoreo", "proveedores");
            break;
        case ADMINISTRADOR:
            visibles = Arrays.asList("restaurantes", "platos", "alertas");
            break;
        case COCINERO:
        default:
            visibles = Arrays.asList("tablero", "disponibilidad");
    }

    // Definicion de cada pestana: id | etiqueta | ruta
    String[][] tabs = {
        {"tablero",        "Tablero",        "/pedidos"},
        {"disponibilidad", "Disponibilidad", "/disponibilidad"},
        {"inventario",     "Inventario",     "/insumos"},
        {"crear",          "Crear insumo",   "/insumos/crear"},
        {"menu",           "Men\u00fa",       "/menu"},
        {"monitoreo",      "Monitoreo",      "/monitoreo"},
        {"proveedores",    "Proveedores",    "/proveedores"},
        {"restaurantes",   "Restaurantes",   "/restaurantes"},
        {"platos",         "Platos",         "/platos"},
        {"alertas",        "Alertas",        "/alertas"}
    };

    // Pagina de aterrizaje de cada rol al cambiar de vista.
    String landingCocinero = "/pedidos?rol=COCINERO";
    String landingBodega = "/pedidos?rol=ADMIN_BODEGA";
    String landingAdmin = "/restaurantes?rol=ADMINISTRADOR";
%>
<header class="topbar">
    <div class="topbar__brand">
        <span class="topbar__logo"></span>
        <span class="topbar__name">Dark&nbsp;Kitchen</span>
    </div>

    <div class="topbar__right">
        <div class="vista" role="group" aria-label="Cambiar vista de rol">
            <span class="vista__label">Vista</span>
            <a class="vista__pill <%= navRol == Rol.COCINERO ? "is-on" : "" %>"
               href="<%= navCtx %><%= landingCocinero %>">Cocinero</a>
            <a class="vista__pill <%= navRol == Rol.ADMIN_BODEGA ? "is-on" : "" %>"
               href="<%= navCtx %><%= landingBodega %>">Admin.&nbsp;bodega</a>
            <a class="vista__pill <%= navRol == Rol.ADMINISTRADOR ? "is-on" : "" %>"
               href="<%= navCtx %><%= landingAdmin %>">Administrador</a>
        </div>
        <button type="button" id="toggle-tema" class="toggle" aria-pressed="false">&#9790;&nbsp;Modo oscuro</button>
    </div>
</header>

<nav class="tabsbar" aria-label="Secciones disponibles">
<%
    boolean hayTabs = false;
    for (String[] t : tabs) {
        if (!visibles.contains(t[0])) { continue; }
        hayTabs = true;
        String clase = activo.equals(t[0]) ? "tab is-on" : "tab";
        String href = navCtx + t[2] + "?rol=" + navRol.name() + "&activo=" + t[0];
%>
    <a class="<%= clase %>" href="<%= href %>"><%= t[1] %></a>
<%
    }
    if (!hayTabs) {
%>
    <span class="tabsbar__empty">Sin secciones asignadas por ahora</span>
<%
    }
%>
</nav>
<script src="<%= navCtx %>/resources/js/tema.js"></script>
