/**
 * Vista de metricas por restaurante (F5.1/F5.2, HU35-HU39).
 * - Valida en cliente que haya un restaurante seleccionado antes de "Ver
 *   metricas" o "Exportar PDF" (backstop; el servidor valida igual, sin
 *   usar alert()/confirm() en ningun caso, solo el Toast Manager global).
 * - Dibuja los graficos de Chart.js (barras para platos, dona para
 *   insumos) a partir de los datos que la vista deja en window.METRICAS_DATA.
 * Se espera que Chart.js ya este cargado (CDN) antes de este script.
 */
(function () {
    "use strict";

    function colorPaleta() {
        return ["#34d399", "#38bdf8", "#f5a524", "#a78bfa", "#f472b6", "#22c55e", "#f97316", "#60a5fa"];
    }

    function estaEnModoClaro() {
        return document.body.classList.contains("tema-claro");
    }

    function colorTexto() {
        return estaEnModoClaro() ? "#16202c" : "#e6edf3";
    }

    function colorGrilla() {
        return estaEnModoClaro() ? "rgba(22,32,44,.08)" : "rgba(230,237,243,.08)";
    }

    var TOP_N = 5;

    /** Recorta a los primeros N y agrupa el resto en un elemento "Otros" que suma su cantidad. */
    function top5ConOtros(datos, etiquetaCantidad) {
        if (!datos || datos.length <= TOP_N) {
            return datos || [];
        }
        var top = datos.slice(0, TOP_N);
        var resto = datos.slice(TOP_N);
        var sumaResto = resto.reduce(function (acc, d) { return acc + d.cantidad; }, 0);
        var otros = { nombre: "Otros (" + resto.length + ")", cantidad: sumaResto };
        if (etiquetaCantidad) { otros.unidad = etiquetaCantidad; }
        return top.concat([otros]);
    }

    function renderGraficoPlatos(datos) {
        var canvas = document.getElementById("grafico-platos");
        if (!canvas || typeof Chart === "undefined" || !datos || !datos.length) {
            return;
        }
        var agrupados = top5ConOtros(datos);
        var colores = agrupados.map(function (d, i) {
            return d.nombre.indexOf("Otros") === 0 ? "#4b5563" : colorPaleta()[i % colorPaleta().length];
        });
        new Chart(canvas.getContext("2d"), {
            type: "bar",
            data: {
                labels: agrupados.map(function (d) { return d.nombre; }),
                datasets: [{
                    label: "Pedidos entregados",
                    data: agrupados.map(function (d) { return d.cantidad; }),
                    backgroundColor: colores,
                    borderRadius: 6,
                    maxBarThickness: 38
                }]
            },
            options: {
                responsive: true,
                plugins: { legend: { display: false } },
                scales: {
                    x: { ticks: { color: colorTexto() }, grid: { display: false } },
                    y: { beginAtZero: true, ticks: { color: colorTexto(), precision: 0 }, grid: { color: colorGrilla() } }
                }
            }
        });
    }

    function renderGraficoInsumos(datos) {
        var canvas = document.getElementById("grafico-insumos");
        if (!canvas || typeof Chart === "undefined" || !datos || !datos.length) {
            return;
        }
        var agrupados = top5ConOtros(datos, "");
        var colores = agrupados.map(function (d, i) {
            return d.nombre.indexOf("Otros") === 0 ? "#4b5563" : colorPaleta()[i % colorPaleta().length];
        });
        new Chart(canvas.getContext("2d"), {
            type: "doughnut",
            data: {
                labels: agrupados.map(function (d) {
                    return d.unidad ? (d.nombre + " (" + d.unidad + ")") : d.nombre;
                }),
                datasets: [{
                    data: agrupados.map(function (d) { return d.cantidad; }),
                    backgroundColor: colores,
                    borderColor: estaEnModoClaro() ? "#ffffff" : "#161b22",
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                plugins: { legend: { position: "bottom", labels: { color: colorTexto(), boxWidth: 12, padding: 12 } } }
            }
        });
    }

    /** Abre/cierra un modal-overlay por id, con click fuera y boton "Cerrar" para salir. */
    function activarModal(idOverlay, idBotonAbrir, idBotonCerrar) {
        var overlay = document.getElementById(idOverlay);
        var abrir = document.getElementById(idBotonAbrir);
        var cerrar = document.getElementById(idBotonCerrar);
        if (!overlay) { return; }
        if (abrir) {
            abrir.addEventListener("click", function () { overlay.style.display = "flex"; });
        }
        if (cerrar) {
            cerrar.addEventListener("click", function () { overlay.style.display = "none"; });
        }
        overlay.addEventListener("click", function (e) {
            if (e.target === overlay) { overlay.style.display = "none"; }
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        var datos = window.METRICAS_DATA || {};
        renderGraficoPlatos(datos.platos);
        renderGraficoInsumos(datos.insumos);

        activarModal("modal-platos-completo", "btn-ver-todos-platos", "modal-platos-cerrar");
        activarModal("modal-insumos-completo", "btn-ver-todos-insumos", "modal-insumos-cerrar");

        var formBuscar = document.getElementById("form-metricas-buscar");
        var selectRestaurante = document.getElementById("select-restaurante");
        if (formBuscar && selectRestaurante) {
            formBuscar.addEventListener("submit", function (e) {
                if (!selectRestaurante.value) {
                    e.preventDefault();
                    showToast("danger", "Selecciona un restaurante", "Debe seleccionar un restaurante para ver sus métricas");
                }
            });
        }

        var formExportar = document.getElementById("form-exportar-pdf");
        var campoExportarId = document.getElementById("exportar-restaurante-id");
        var huboResultados = document.body.getAttribute("data-hay-resultados") === "true";
        if (formExportar) {
            formExportar.addEventListener("submit", function (e) {
                if (campoExportarId) { campoExportarId.value = selectRestaurante ? selectRestaurante.value : ""; }
                if (!huboResultados || !campoExportarId || !campoExportarId.value) {
                    e.preventDefault();
                    showToast("danger", "No se pudo exportar",
                            "Debe seleccionar un restaurante y ver sus métricas antes de exportar");
                }
            });
        }
    });
})();
