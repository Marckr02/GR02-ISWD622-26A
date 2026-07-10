/**
 * Toast Manager global. Expone window.showToast(tipo, titulo, descripcion)
 * como unica forma de disparar notificaciones desde Inventario, Proveedores,
 * Restaurantes y Platos. Crea (una sola vez) el contenedor fijo en la
 * esquina superior derecha, y cada toast se autodestruye tras ~3.5s con una
 * animacion de entrada (deslizamiento) y salida (desvanecimiento).
 * No se incluye ni se usa en la vista del Tablero (Pase de cocina).
 */
(function (global) {
    "use strict";

    var ICONOS = {
        success: "✓",
        info: "ℹ",
        danger: "✕"
    };

    var TIEMPO_VISIBLE_MS = 3500;
    var TIEMPO_SALIDA_MS = 300;

    var contenedor = null;

    function obtenerContenedor() {
        if (contenedor) {
            return contenedor;
        }
        contenedor = document.createElement("div");
        contenedor.className = "toast-contenedor";
        contenedor.setAttribute("aria-live", "polite");
        contenedor.setAttribute("aria-atomic", "true");
        document.body.appendChild(contenedor);
        return contenedor;
    }

    /**
     * Muestra una notificacion estandarizada.
     * @param {"success"|"info"|"danger"} tipo  success = crear/anadir (verde),
     *        info = modificar/actualizar (azul), danger = eliminar (rojo).
     * @param {string} titulo       Titulo corto, ej. "Plato creado".
     * @param {string} [descripcion] Detalle opcional en gris claro.
     */
    function showToast(tipo, titulo, descripcion) {
        if (!ICONOS[tipo]) {
            tipo = "success";
        }
        var host = obtenerContenedor();

        var toast = document.createElement("div");
        toast.className = "toast toast--" + tipo;
        toast.setAttribute("role", "status");

        var icono = document.createElement("span");
        icono.className = "toast__icono";
        icono.setAttribute("aria-hidden", "true");
        icono.textContent = ICONOS[tipo];

        var cuerpo = document.createElement("div");
        cuerpo.className = "toast__cuerpo";

        var tituloEl = document.createElement("p");
        tituloEl.className = "toast__titulo";
        tituloEl.textContent = titulo || "";
        cuerpo.appendChild(tituloEl);

        if (descripcion) {
            var descEl = document.createElement("p");
            descEl.className = "toast__descripcion";
            descEl.textContent = descripcion;
            cuerpo.appendChild(descEl);
        }

        toast.appendChild(icono);
        toast.appendChild(cuerpo);
        host.appendChild(toast);

        // Se agrega en el siguiente frame para que la transicion de entrada corra.
        window.requestAnimationFrame(function () {
            toast.classList.add("toast--visible");
        });

        var cerrado = false;
        function cerrar() {
            if (cerrado) { return; }
            cerrado = true;
            toast.classList.remove("toast--visible");
            toast.classList.add("toast--saliendo");
            setTimeout(function () { toast.remove(); }, TIEMPO_SALIDA_MS);
        }

        setTimeout(cerrar, TIEMPO_VISIBLE_MS);
        toast.addEventListener("click", cerrar);
    }

    global.showToast = showToast;
})(window);
