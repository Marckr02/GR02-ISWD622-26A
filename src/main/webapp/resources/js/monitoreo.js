/**
 * Panel de control (HU7 + HU9 fusionados): filtro rapido de platos por
 * estado (todos / disponibles / bloqueados) y popover de "Contactar
 * proveedor" para cada insumo critico, sin salir de la pantalla.
 */
(function () {
    "use strict";

    /** Filtro por estado sobre las tarjetas de plato del panel derecho. */
    function activarFiltroMenu() {
        var grid = document.getElementById("menu-grid");
        var pills = document.querySelectorAll("#filtros-menu .filtro-pill");
        var sinResultados = document.getElementById("sin-resultados-menu");
        if (!grid) {
            return;
        }
        var filtroActivo = "todos";

        function aplicar() {
            var tarjetas = grid.querySelectorAll(".plato");
            var visibles = 0;
            Array.prototype.forEach.call(tarjetas, function (tarjeta) {
                var visible = filtroActivo === "todos" || tarjeta.getAttribute("data-estado") === filtroActivo;
                tarjeta.style.display = visible ? "" : "none";
                if (visible) { visibles += 1; }
            });
            if (sinResultados) {
                sinResultados.hidden = visibles !== 0;
            }
        }

        pills.forEach(function (pill) {
            pill.addEventListener("click", function () {
                pills.forEach(function (p) { p.classList.remove("is-on"); });
                pill.classList.add("is-on");
                filtroActivo = pill.getAttribute("data-filtro");
                aplicar();
            });
        });

        aplicar();
    }

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
        activarFiltroMenu();
        activarContactoProveedor();
    });
})();
