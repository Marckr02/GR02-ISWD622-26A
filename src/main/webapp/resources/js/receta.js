/**
 * Filas dinamicas de ingredientes para el formulario de platos (HU30/HU32).
 * Cada fila tiene un select de insumo, una cantidad y una unidad de receta;
 * "Agregar insumo" clona la fila plantilla y "Quitar" elimina una fila
 * (siempre deja al menos una visible).
 */
(function () {
    "use strict";

    document.addEventListener("DOMContentLoaded", function () {
        var contenedor = document.getElementById("receta-filas");
        var plantilla = document.getElementById("receta-plantilla");
        var botonAgregar = document.getElementById("receta-agregar");
        if (!contenedor || !plantilla || !botonAgregar) {
            return;
        }

        function actualizarBotonesQuitar() {
            var filas = contenedor.querySelectorAll(".receta__fila");
            filas.forEach(function (fila) {
                var boton = fila.querySelector(".receta__quitar");
                if (boton) {
                    boton.disabled = filas.length <= 1;
                }
            });
        }

        function agregarFila() {
            var fila = plantilla.cloneNode(true);
            fila.removeAttribute("id");
            fila.style.display = "";
            var campos = fila.querySelectorAll("select, input");
            campos.forEach(function (campo) {
                campo.value = "";
                campo.disabled = false;
            });
            contenedor.appendChild(fila);
            actualizarBotonesQuitar();
        }

        contenedor.addEventListener("click", function (e) {
            var boton = e.target.closest(".receta__quitar");
            if (!boton) {
                return;
            }
            var filas = contenedor.querySelectorAll(".receta__fila");
            if (filas.length > 1) {
                boton.closest(".receta__fila").remove();
                actualizarBotonesQuitar();
            }
        });

        botonAgregar.addEventListener("click", agregarFila);
        actualizarBotonesQuitar();
    });
})();
