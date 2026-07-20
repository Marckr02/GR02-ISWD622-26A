/**
 * Panel de control (HU7 + HU9 fusionados): filtro rapido de platos por
 * estado (todos / disponibles / bloqueados, logica compartida en
 * menu-filtros.js), popover de insumos faltantes en platos bloqueados, y
 * popover de "Contactar proveedor" para cada insumo critico.
 */
(function () {
    "use strict";

    document.addEventListener("DOMContentLoaded", function () {
        if (window.activarFiltrosMenu) {
            window.activarFiltrosMenu({ buscadorId: "buscador-menu-monitoreo", selectMarcaId: "filtro-marca-menu" });
        }
        if (window.activarFiltrosInsumosCriticos) {
            window.activarFiltrosInsumosCriticos({ buscadorId: "buscador-criticos" });
        }
        if (window.activarPopoverFaltantes) { window.activarPopoverFaltantes(); }
        if (window.activarContactoProveedor) { window.activarContactoProveedor(".mon-contacto"); }
    });
})();
