/**
 * Historial de alertas de stock critico (HU11): filtro combinado en tiempo
 * real por nombre de insumo, severidad y rango de fechas (calendario nativo
 * del navegador via <input type="date">) sobre las filas ya renderizadas
 * por el servidor.
 */
(function () {
    "use strict";

    document.addEventListener("DOMContentLoaded", function () {
        var buscador = document.getElementById("buscador-alertas");
        var selectSeveridad = document.getElementById("filtro-severidad");
        var fechaDesde = document.getElementById("filtro-fecha-desde");
        var fechaHasta = document.getElementById("filtro-fecha-hasta");
        var btnLimpiar = document.getElementById("btn-limpiar-filtros-alertas");
        var tabla = document.getElementById("tabla-alertas");
        if (!tabla) {
            return;
        }
        var sinResultados = document.getElementById("sin-resultados-alertas");
        var filas = tabla.querySelectorAll("tbody tr");

        function aplicarFiltros() {
            var consulta = buscador ? buscador.value.trim().toLowerCase() : "";
            var severidad = selectSeveridad ? selectSeveridad.value : "";
            var desde = fechaDesde ? fechaDesde.value : "";
            var hasta = fechaHasta ? fechaHasta.value : "";
            var visibles = 0;

            Array.prototype.forEach.call(filas, function (fila) {
                var nombre = fila.getAttribute("data-nombre") || "";
                var severidadFila = fila.getAttribute("data-severidad") || "";
                var fechaFila = fila.getAttribute("data-fecha") || "";

                var coincideTexto = !consulta || nombre.indexOf(consulta) !== -1;
                var coincideSeveridad = !severidad || severidadFila === severidad;
                var coincideDesde = !desde || fechaFila >= desde;
                var coincideHasta = !hasta || fechaFila <= hasta;

                var visible = coincideTexto && coincideSeveridad && coincideDesde && coincideHasta;
                fila.style.display = visible ? "" : "none";
                if (visible) { visibles += 1; }
            });

            tabla.hidden = visibles === 0;
            if (sinResultados) { sinResultados.hidden = visibles !== 0; }
        }

        function limpiarFiltros() {
            if (buscador) { buscador.value = ""; }
            if (selectSeveridad) { selectSeveridad.value = ""; }
            if (fechaDesde) { fechaDesde.value = ""; }
            if (fechaHasta) { fechaHasta.value = ""; }
            aplicarFiltros();
        }

        [buscador, selectSeveridad, fechaDesde, fechaHasta].forEach(function (campo) {
            if (campo) {
                campo.addEventListener("input", aplicarFiltros);
                campo.addEventListener("change", aplicarFiltros);
            }
        });
        if (btnLimpiar) { btnLimpiar.addEventListener("click", limpiarFiltros); }
    });
})();
