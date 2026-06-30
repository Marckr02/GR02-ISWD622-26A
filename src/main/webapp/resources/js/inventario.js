/**
 * Confirmacion modal para registrar entrada y reducir stock (HU22). El modal
 * solo se abre si los datos del formulario son validos (HU22 neg2); "Cancelar"
 * lo cierra conservando los valores ingresados sin tocar el inventario
 * (HU22 neg1). La validacion replica los mensajes del backend (HU21).
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
            return { ok: false, msg: "Ingrese un numero entero positivo valido" };
        }
        var patron = soloEntero ? /^\d+$/ : /^\d+(\.\d+)?$/;
        if (!patron.test(valor.trim())) {
            return { ok: false, msg: "Ingrese un numero entero positivo valido" };
        }
        if (!(Number(valor) > 0)) {
            return { ok: false, msg: "La cantidad debe ser un numero entero positivo mayor a cero" };
        }
        return { ok: true };
    }

    document.addEventListener("DOMContentLoaded", function () {
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
            var errorBox = form.querySelector(".form__error");
            var soloEntero = form.getAttribute("data-entero") === "true";

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
                if (errorBox) { errorBox.textContent = ""; }
                formPendiente = form;
                if (titulo) { titulo.textContent = form.getAttribute("data-titulo") || "Confirmar"; }
                if (resumen) {
                    resumen.textContent = textoInsumo(select) + "  \u2014  cantidad: " + cantidad.value;
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
