/**
 * Confirmacion modal para registrar entrada y reducir stock. El modal
 * solo se abre si los datos del formulario son validos; "Cancelar"
 * lo cierra conservando los valores ingresados sin tocar el inventario
 *. La validacion replica los mensajes del backend.
 */
(function () {
    "use strict";

    function textoInsumo(select) {
        if (!select || select.selectedIndex < 0) {
            return "";
        }
        return select.options[select.selectedIndex].text;
    }

    function validarCantidad(valor, soloEntero) {
        if (valor === null || valor.trim() === "") {
            return { ok: false, msg: soloEntero ? "Ingrese un numero entero positivo valido" : "Ingrese un numero positivo valido" };
        }
        var patron = soloEntero ? /^\d+$/ : /^\d+(\.\d+)?$/;
        if (!patron.test(valor.trim())) {
            return { ok: false, msg: soloEntero ? "Ingrese un numero entero positivo valido" : "Ingrese un numero positivo valido" };
        }
        if (!(Number(valor) > 0)) {
            return { ok: false, msg: soloEntero ? "La cantidad debe ser un numero entero positivo mayor a cero" : "La cantidad debe ser mayor a cero" };
        }
        return { ok: true };
    }

    /** Filtro en tiempo real: compara lo escrito contra la primera celda (columna Insumo) de cada fila. */
    function activarFiltroTabla(inputId, tablaSelector) {
        var input = document.getElementById(inputId);
        var tabla = document.querySelector(tablaSelector);
        if (!input || !tabla) {
            return;
        }
        input.addEventListener("input", function () {
            var consulta = input.value.trim().toLowerCase();
            var filas = tabla.querySelectorAll("tbody tr");
            Array.prototype.forEach.call(filas, function (fila) {
                var primeraCelda = fila.querySelector("td:first-child");
                var nombreInsumo = primeraCelda ? primeraCelda.textContent.toLowerCase() : "";
                fila.style.display = (consulta === "" || nombreInsumo.indexOf(consulta) !== -1) ? "" : "none";
            });
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        activarFiltroTabla("filtro-insumo", ".tabla");

        var overlay = document.getElementById("modal-confirm");
        var resumen = document.getElementById("modal-resumen");
        var titulo = document.getElementById("modal-titulo");
        var btnConfirmar = document.getElementById("modal-confirmar");
        var btnCancelar = document.getElementById("modal-cancelar");
        var formPendiente = null;

        function cerrar() {
            if (overlay) { overlay.style.display = "none"; }
            formPendiente = null;
        }

        var forms = document.querySelectorAll("form.form--confirmable");
        Array.prototype.forEach.call(forms, function (form) {
            var select = form.querySelector("select[name='insumoId']");
            var cantidad = form.querySelector("input[name='cantidad']");
            var unidad = form.querySelector("select[name='unidad']");
            var errorBox = form.querySelector(".form__error");
            var pistaUnidad = form.querySelector(".unidad-hint");
            var pistaStock = form.querySelector(".stock-disponible-hint");
            var soloEntero = form.getAttribute("data-entero") === "true";

            function sincronizarUnidad() {
                if (!select || select.selectedIndex < 0) {
                    return;
                }
                var opcion = select.options[select.selectedIndex];
                var unidadActual = opcion.getAttribute("data-unidad");
                if (unidad && unidadActual) {
                    unidad.value = unidadActual;
                }
                if (pistaUnidad) {
                    pistaUnidad.textContent = (unidadActual && opcion.value) ? "(" + unidadActual + ")" : "";
                }
                if (pistaStock) {
                    var stockDisponible = opcion.getAttribute("data-stock");
                    pistaStock.textContent = (stockDisponible && opcion.value)
                        ? "Stock disponible: " + Number(stockDisponible) + " " + unidadActual
                        : "";
                }
            }

            function unidadSeleccionada() {
                if (unidad) {
                    return unidad.value;
                }
                if (!select || select.selectedIndex < 0) {
                    return "";
                }
                return select.options[select.selectedIndex].getAttribute("data-unidad") || "";
            }

            if (select) {
                select.addEventListener("change", sincronizarUnidad);
                sincronizarUnidad();
            }

            form.addEventListener("submit", function (e) {
                e.preventDefault();
                if (!select.value) {
                    if (errorBox) { errorBox.textContent = "Debe seleccionar un insumo de la lista"; }
                    return;
                }
                var res = validarCantidad(cantidad.value, soloEntero);
                if (!res.ok) {
                    if (errorBox) { errorBox.textContent = res.msg; }
                    return;
                }
                if (unidadSeleccionada() === "unidades" && !/^\d+$/.test(cantidad.value.trim())) {
                    if (errorBox) { errorBox.textContent = "La cantidad en unidades debe ser un numero entero"; }
                    return;
                }
                var stockDisponible = select.options[select.selectedIndex].getAttribute("data-stock");
                if (stockDisponible !== null && Number(cantidad.value) > Number(stockDisponible)) {
                    if (errorBox) {
                        errorBox.textContent = "La cantidad a reducir excede el stock disponible ("
                            + Number(stockDisponible) + " " + unidadSeleccionada() + ")";
                    }
                    return;
                }
                if (errorBox) { errorBox.textContent = ""; }
                formPendiente = form;
                if (titulo) { titulo.textContent = form.getAttribute("data-titulo") || "Confirmar"; }
                if (resumen) {
                    var unidadTexto = unidadSeleccionada() ? (" " + unidadSeleccionada()) : "";
                    resumen.textContent = textoInsumo(select) + "  \u2014  cantidad: " + cantidad.value + unidadTexto;
                }
                if (btnConfirmar) { btnConfirmar.textContent = form.getAttribute("data-confirmar") || "Confirmar"; }
                if (overlay) { overlay.style.display = "flex"; }
            });
        });

        if (btnConfirmar) {
            btnConfirmar.addEventListener("click", function () {
                if (formPendiente) { formPendiente.submit(); }
            });
        }
        if (btnCancelar) { btnCancelar.addEventListener("click", cerrar); }
        if (overlay) {
            overlay.addEventListener("click", function (e) {
                if (e.target === overlay) { cerrar(); }
            });
        }
    });
})();
