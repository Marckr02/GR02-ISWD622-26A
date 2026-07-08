/**
 * Filas dinamicas de ingredientes para el formulario de platos (HU30/HU32).
 * Cada fila tiene un select de insumo, una cantidad y una unidad de receta;
 * "Agregar insumo" clona la fila plantilla y "Quitar" elimina una fila
 * (siempre deja al menos una visible). Tambien evita insumos repetidos en
 * la misma receta, tanto al elegirlos como al enviar el formulario.
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
        var form = contenedor.closest("form");
        var errorGlobal = form ? form.querySelector(".form__error") : null;

        function selectsDeInsumo() {
            return Array.prototype.slice.call(
                contenedor.querySelectorAll(".receta__fila:not(#receta-plantilla) select[name='insumoId[]']"));
        }

        function insumoRepetido(select) {
            if (!select.value) {
                return false;
            }
            return selectsDeInsumo().some(function (otro) {
                return otro !== select && otro.value === select.value;
            });
        }

        function marcarRepetidos() {
            var haySolapes = false;
            selectsDeInsumo().forEach(function (select) {
                var repetido = insumoRepetido(select);
                select.setCustomValidity(repetido ? "Este insumo ya esta en la receta" : "");
                if (repetido) {
                    haySolapes = true;
                }
            });
            if (errorGlobal) {
                errorGlobal.textContent = haySolapes ? "No puedes agregar el mismo insumo dos veces en la receta" : "";
            }
            return haySolapes;
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
                marcarRepetidos();
            }
        });

        contenedor.addEventListener("change", function (e) {
            if (e.target.matches("select[name='insumoId[]']")) {
                marcarRepetidos();
            }
        });

        if (form) {
            form.addEventListener("submit", function (e) {
                if (marcarRepetidos()) {
                    e.preventDefault();
                }
            });
        }

        botonAgregar.addEventListener("click", agregarFila);
        actualizarBotonesQuitar();
    });
})();
