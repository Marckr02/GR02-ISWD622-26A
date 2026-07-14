/**
 * Disponibilidad del turno (HU10, vista de consulta del cocinero): combina
 * la busqueda por nombre con el filtro rapido por estado (todos/disponibles/
 * bloqueados) sobre las tarjetas de plato, mas el popover de insumos
 * faltantes en los platos bloqueados. La logica compartida con el Panel de
 * control vive en menu-filtros.js.
 */
(function () {
    "use strict";

    document.addEventListener("DOMContentLoaded", function () {
        if (window.activarFiltrosMenu) {
            window.activarFiltrosMenu({ buscadorId: "buscador-disponibilidad" });
        }
        if (window.activarPopoverFaltantes) {
            window.activarPopoverFaltantes();
        }
    });
})();
