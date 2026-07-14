/**
 * Panel de control (HU7 + HU9 fusionados): filtro rapido de platos por
 * estado (todos / disponibles / bloqueados, logica compartida en
 * menu-filtros.js), popover de insumos faltantes en platos bloqueados, y
 * popover de "Contactar proveedor" para cada insumo critico.
 */
(function () {
    "use strict";

    /** Popover de contacto de proveedor desde la tabla de insumos criticos. */
    function activarContactoProveedor() {
        var overlay = document.getElementById("modal-contacto-proveedor");
        var cerrar = document.getElementById("modal-contacto-cerrar");
        var cuerpo = document.getElementById("contacto-proveedor-cuerpo");
        if (!overlay || !cuerpo) {
            return;
        }

        document.querySelectorAll(".mon-contacto").forEach(function (boton) {
            boton.addEventListener("click", function () {
                var nombre = (boton.getAttribute("data-proveedor-nombre") || "").trim();
                var telefono = (boton.getAttribute("data-proveedor-telefono") || "").trim();
                var correo = (boton.getAttribute("data-proveedor-correo") || "").trim();

                cuerpo.textContent = "";
                if (!nombre) {
                    var sinProveedor = document.createElement("p");
                    sinProveedor.className = "hint";
                    sinProveedor.textContent = "Sin proveedor asignado";
                    cuerpo.appendChild(sinProveedor);
                } else {
                    var pNombre = document.createElement("p");
                    pNombre.className = "contacto-nombre";
                    pNombre.textContent = nombre;
                    cuerpo.appendChild(pNombre);
                    if (telefono) {
                        var pTel = document.createElement("p");
                        pTel.textContent = "Tel: " + telefono;
                        cuerpo.appendChild(pTel);
                    }
                    if (correo) {
                        var pCorreo = document.createElement("p");
                        pCorreo.textContent = "Correo: " + correo;
                        cuerpo.appendChild(pCorreo);
                    }
                    if (!telefono && !correo) {
                        var sinDatos = document.createElement("p");
                        sinDatos.className = "hint";
                        sinDatos.textContent = "Sin datos de contacto registrados";
                        cuerpo.appendChild(sinDatos);
                    }
                }
                overlay.style.display = "flex";
            });
        });

        if (cerrar) {
            cerrar.addEventListener("click", function () { overlay.style.display = "none"; });
        }
        overlay.addEventListener("click", function (e) {
            if (e.target === overlay) { overlay.style.display = "none"; }
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        if (window.activarFiltrosMenu) {
            window.activarFiltrosMenu({ buscadorId: "buscador-menu-monitoreo", selectMarcaId: "filtro-marca-menu" });
        }
        if (window.activarFiltrosInsumosCriticos) {
            window.activarFiltrosInsumosCriticos({ buscadorId: "buscador-criticos" });
        }
        if (window.activarPopoverFaltantes) { window.activarPopoverFaltantes(); }
        activarContactoProveedor();
    });
})();
