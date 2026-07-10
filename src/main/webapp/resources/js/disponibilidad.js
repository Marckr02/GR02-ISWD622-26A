/**
 * Disponibilidad del turno (HU10, vista de consulta del cocinero): combina
 * la busqueda por nombre con el filtro rapido por estado (todos/disponibles/
 * bloqueados) sobre las tarjetas de plato.
 */
(function () {
    "use strict";

    document.addEventListener("DOMContentLoaded", function () {
        var grid = document.getElementById("menu-grid");
        var buscador = document.getElementById("buscador-disponibilidad");
        var pills = document.querySelectorAll("#filtros-menu .filtro-pill");
        var sinResultados = document.getElementById("sin-resultados-menu");
        if (!grid) {
            return;
        }
        var filtroActivo = "todos";

        function aplicar() {
            var consulta = buscador ? buscador.value.trim().toLowerCase() : "";
            var tarjetas = grid.querySelectorAll(".plato");
            var visibles = 0;
            Array.prototype.forEach.call(tarjetas, function (tarjeta) {
                var coincideEstado = filtroActivo === "todos" || tarjeta.getAttribute("data-estado") === filtroActivo;
                var nombre = tarjeta.getAttribute("data-nombre") || "";
                var coincideTexto = !consulta || nombre.indexOf(consulta) !== -1;
                var visible = coincideEstado && coincideTexto;
                tarjeta.style.display = visible ? "" : "none";
                if (visible) { visibles += 1; }
            });
            if (sinResultados) {
                sinResultados.hidden = visibles !== 0;
            }
        }

        if (buscador) { buscador.addEventListener("input", aplicar); }
        pills.forEach(function (pill) {
            pill.addEventListener("click", function () {
                pills.forEach(function (p) { p.classList.remove("is-on"); });
                pill.classList.add("is-on");
                filtroActivo = pill.getAttribute("data-filtro");
                aplicar();
            });
        });

        aplicar();
    });
})();
