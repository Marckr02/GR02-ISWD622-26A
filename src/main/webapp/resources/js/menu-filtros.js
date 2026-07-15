/**
 * Logica compartida por las vistas de menu/disponibilidad (disponibilidad-cocinero.jsp
 * y panel-monitoreo.jsp): filtro por estado (todos/disponible/bloqueado) + busqueda opcional por nombre
 * sobre las tarjetas ".dish-circle", y el popover flotante de insumos faltantes
 * para los platos bloqueados. Una sola implementacion evita que las vistas se
 * desincronicen entre si (el bug anterior: cada vista tenia su propia copia).
 */
(function () {
    "use strict";

    /**
     * Activa el filtro de estado + restaurante + busqueda sobre una cuadricula de
     * platos. Los tres criterios se combinan con AND (interseccion), no OR: un
     * plato solo queda visible si coincide con el texto buscado, el estado
     * seleccionado y la marca seleccionada al mismo tiempo.
     * @param {Object} opciones
     * @param {string} [opciones.gridId="menu-grid"]
     * @param {string} [opciones.filtrosId="filtros-menu"] filtro de estado (disponible/bloqueado)
     * @param {string} [opciones.selectMarcaId] id del &lt;select&gt; de restaurante (opcional)
     * @param {string} [opciones.vacioId="sin-resultados-menu"]
     * @param {string} [opciones.buscadorId] id del input de busqueda (opcional)
     */
    function activarFiltrosMenu(opciones) {
        opciones = opciones || {};
        var grid = document.getElementById(opciones.gridId || "menu-grid");
        if (!grid) {
            return;
        }
        var pills = document.querySelectorAll("#" + (opciones.filtrosId || "filtros-menu") + " .filtro-pill");
        var selectMarca = opciones.selectMarcaId ? document.getElementById(opciones.selectMarcaId) : null;
        var sinResultados = document.getElementById(opciones.vacioId || "sin-resultados-menu");
        var buscador = opciones.buscadorId ? document.getElementById(opciones.buscadorId) : null;
        var filtroActivo = "todos";

        function aplicar() {
            var consulta = buscador ? buscador.value.trim().toLowerCase() : "";
            var marcaActiva = selectMarca ? (selectMarca.value || "todos") : "todos";
            var tarjetas = grid.querySelectorAll(".dish-circle");
            var visibles = 0;
            Array.prototype.forEach.call(tarjetas, function (tarjeta) {
                var coincideEstado = filtroActivo === "todos" || tarjeta.getAttribute("data-estado") === filtroActivo;
                var coincideMarca = marcaActiva === "todos" || tarjeta.getAttribute("data-restaurante") === marcaActiva;
                var nombre = tarjeta.getAttribute("data-nombre") || "";
                var coincideTexto = !consulta || nombre.indexOf(consulta) !== -1;
                var visible = coincideEstado && coincideMarca && coincideTexto;
                tarjeta.classList.toggle("hidden", !visible);
                if (visible) { visibles += 1; }
            });
            if (sinResultados) { sinResultados.hidden = visibles !== 0; }
        }

        if (buscador) { buscador.addEventListener("input", aplicar); }
        if (selectMarca) { selectMarca.addEventListener("change", aplicar); }
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

    /**
     * Filtro por nivel de stock (todos/bajo el minimo/sin stock) + busqueda por
     * nombre (combinados con AND) sobre las filas de la tabla de insumos
     * criticos del Panel de control.
     * @param {Object} opciones
     * @param {string} [opciones.tablaId="tabla-criticos"]
     * @param {string} [opciones.filtrosId="filtros-criticos"]
     * @param {string} [opciones.vacioId="sin-resultados-criticos"]
     * @param {string} [opciones.buscadorId] id del input de busqueda (opcional)
     */
    function activarFiltrosInsumosCriticos(opciones) {
        opciones = opciones || {};
        var tabla = document.getElementById(opciones.tablaId || "tabla-criticos");
        if (!tabla) {
            return;
        }
        var pills = document.querySelectorAll("#" + (opciones.filtrosId || "filtros-criticos") + " .filtro-pill");
        var sinResultados = document.getElementById(opciones.vacioId || "sin-resultados-criticos");
        var buscador = opciones.buscadorId ? document.getElementById(opciones.buscadorId) : null;
        var filtroActivo = "todos";

        function aplicar() {
            var consulta = buscador ? buscador.value.trim().toLowerCase() : "";
            var filas = tabla.querySelectorAll("tbody tr");
            var visibles = 0;
            Array.prototype.forEach.call(filas, function (fila) {
                var coincideEstado = filtroActivo === "todos" || fila.getAttribute("data-estado-stock") === filtroActivo;
                var nombre = fila.getAttribute("data-nombre") || "";
                var coincideTexto = !consulta || nombre.indexOf(consulta) !== -1;
                var visible = coincideEstado && coincideTexto;
                fila.classList.toggle("hidden", !visible);
                if (visible) { visibles += 1; }
            });
            if (sinResultados) { sinResultados.hidden = visibles !== 0; }
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
    }

    /**
     * Popover flotante con los insumos faltantes de un plato bloqueado. Se abre
     * con clic o al pasar el cursor sobre el boton de alerta, y flota sobre la
     * cuadricula (position: fixed) sin desplazar ningun otro circulo.
     */
    function activarPopoverFaltantes() {
        var botones = document.querySelectorAll(".dish-circle__alerta");
        if (!botones.length) {
            return;
        }
        var popoverActual = null;
        var botonActual = null;
        var temporizadorCierre = null;

        function cerrarPopover() {
            clearTimeout(temporizadorCierre);
            if (popoverActual) {
                popoverActual.remove();
                popoverActual = null;
            }
            botonActual = null;
        }

        function programarCierre() {
            clearTimeout(temporizadorCierre);
            temporizadorCierre = setTimeout(cerrarPopover, 180);
        }

        function mostrarPopover(boton) {
            clearTimeout(temporizadorCierre);
            if (botonActual === boton) {
                return;
            }
            cerrarPopover();
            botonActual = boton;

            var pop = document.createElement("div");
            pop.className = "dish-popover";
            pop.textContent = boton.getAttribute("data-motivo") || "Sin stock";
            pop.addEventListener("mouseenter", function () { clearTimeout(temporizadorCierre); });
            pop.addEventListener("mouseleave", programarCierre);
            document.body.appendChild(pop);
            popoverActual = pop;

            var rect = boton.getBoundingClientRect();
            var anchoPop = pop.offsetWidth;
            var left = rect.left + rect.width / 2 - anchoPop / 2;
            left = Math.max(8, Math.min(left, window.innerWidth - anchoPop - 8));
            pop.style.left = left + "px";
            pop.style.top = (rect.bottom + 8) + "px";
        }

        botones.forEach(function (boton) {
            boton.addEventListener("click", function (e) {
                e.stopPropagation();
                if (botonActual === boton) {
                    cerrarPopover();
                } else {
                    mostrarPopover(boton);
                }
            });
            boton.addEventListener("mouseenter", function () { mostrarPopover(boton); });
            boton.addEventListener("mouseleave", programarCierre);
        });

        document.addEventListener("click", function (e) {
            if (popoverActual && !popoverActual.contains(e.target) && e.target !== botonActual) {
                cerrarPopover();
            }
        });
        document.addEventListener("keydown", function (e) {
            if (e.key === "Escape") { cerrarPopover(); }
        });
        window.addEventListener("scroll", cerrarPopover, true);
    }

    window.activarFiltrosMenu = activarFiltrosMenu;
    window.activarFiltrosInsumosCriticos = activarFiltrosInsumosCriticos;
    window.activarPopoverFaltantes = activarPopoverFaltantes;
})();
