<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String navCtx = request.getContextPath();
    String navRol = request.getParameter("rol");
    String navRolQs = (navRol == null || navRol.isEmpty()) ? "" : ("?rol=" + navRol);
%>
<nav class="appbar">
    <div class="appbar__brand">
        <span class="appbar__dot"></span>
        <span class="appbar__name">Dark Kitchen</span>
    </div>
    <div class="appbar__links">
        <a href="<%= navCtx %>/pedidos<%= navRolQs %>">Tablero</a>
        <a href="<%= navCtx %>/insumos">Inventario</a>
        <a href="<%= navCtx %>/menu">Menu</a>
        <a href="<%= navCtx %>/monitoreo<%= navRolQs %>">Monitoreo</a>
        <a href="<%= navCtx %>/disponibilidad<%= navRolQs %>">Disponibilidad</a>
    </div>
    <button type="button" id="toggle-tema" class="appbar__toggle" aria-pressed="false">&#9790; Modo oscuro</button>
</nav>
<script src="<%= navCtx %>/resources/js/tema.js"></script>
