/**
 * Directorio de restaurantes (HU26/HU27/HU28/HU29), todo dentro de una sola
 * pagina: modal unificado de "Nuevo restaurante" / "Editar restaurante"
 * (nombre + descripcion) y modal generico de confirmacion antes de guardar,
 * actualizar o eliminar. Las notificaciones de exito/error usan el Toast
 * Manager global (toast.js). Nada de esto usa AJAX: cada accion confirmada
 * envia el formulario real y el servidor responde con una recarga completa
 * de la pagina.
 */
(function () {
    "use strict";

    /** Busqueda en tiempo real por nombre sobre las tarjetas de marca. */
    function activarBuscador() {
        var buscador = document.getElementById("buscador-restaurantes");
        var lista = document.getElementById("lista-restaurantes");
        if (!buscador || !lista) {
            return;
        }
        var sinResultados = document.getElementById("sin-resultados-restaurantes");

        function aplicar() {
            var consulta = buscador.value.trim().toLowerCase();
            var tarjetas = lista.querySelectorAll(".marca-card");
            var visibles = 0;
            Array.prototype.forEach.call(tarjetas, function (tarjeta) {
                var nombre = tarjeta.getAttribute("data-nombre") || "";
                var visible = !consulta || nombre.indexOf(consulta) !== -1;
                tarjeta.style.display = visible ? "" : "none";
                if (visible) { visibles += 1; }
            });
            if (sinResultados) {
                sinResultados.hidden = visibles !== 0;
            }
        }

        buscador.addEventListener("input", aplicar);
    }

    document.addEventListener("DOMContentLoaded", function () {
        activarBuscador();

        // ---------- Modal generico de confirmacion ----------
        var overlayConfirm = document.getElementById("modal-confirm");
        var resumenEl = document.getElementById("modal-resumen");
        var introEl = document.getElementById("modal-intro");
        var gridEl = document.getElementById("modal-resumen-grid");
        var iconoEl = document.getElementById("modal-icono");
        var tituloEl = document.getElementById("modal-titulo");
        var btnConfirmarEl = document.getElementById("modal-confirmar");
        var btnCancelarConfirmEl = document.getElementById("modal-cancelar");
        var formPendiente = null;
        var overlayOrigenActual = null;

        /** Muestra el modal de confirmacion. Si opts.campos viene informado (lista de
         *  {clave, valor}), pinta la tarjeta de resumen estructurado; si no, cae al
         *  parrafo de texto plano (usado por acciones simples como eliminar). */
        function mostrarConfirmacion(form, opts) {
            opts = opts || {};
            overlayOrigenActual = opts.overlayOrigen || form.closest(".modal-overlay");
            if (overlayOrigenActual) { overlayOrigenActual.style.display = "none"; }
            formPendiente = form;
            if (tituloEl) { tituloEl.textContent = opts.titulo || "Confirmar"; }

            var esDanger = opts.confirmarClase === "btn--danger";
            if (iconoEl) {
                iconoEl.classList.toggle("modal__icono--danger", esDanger);
                iconoEl.innerHTML = esDanger ? "&#10005;" : "&#10003;";
            }

            if (gridEl) {
                gridEl.innerHTML = "";
                gridEl.hidden = !opts.campos;
                if (opts.campos) {
                    opts.campos.forEach(function (campo) {
                        var clave = document.createElement("span");
                        clave.className = "modal__resumen-clave";
                        clave.textContent = campo.clave;
                        var valor = document.createElement("span");
                        valor.className = "modal__resumen-valor";
                        valor.textContent = campo.valor;
                        gridEl.appendChild(clave);
                        gridEl.appendChild(valor);
                    });
                }
            }
            if (introEl) {
                introEl.hidden = !opts.campos;
                introEl.textContent = opts.intro || "Por favor, verifica los datos antes de guardar:";
            }
            if (resumenEl) {
                resumenEl.hidden = !!opts.campos;
                resumenEl.textContent = opts.campos ? "" : (opts.resumen || "");
            }

            if (btnConfirmarEl) {
                btnConfirmarEl.textContent = opts.confirmarTexto || "Confirmar";
                btnConfirmarEl.classList.remove("btn--ok", "btn--danger", "btn--warn");
                btnConfirmarEl.classList.add(opts.confirmarClase || "btn--ok");
            }
            if (overlayConfirm) { overlayConfirm.style.display = "flex"; }
        }

        function cerrarConfirmacion() {
            if (overlayConfirm) { overlayConfirm.style.display = "none"; }
            if (overlayOrigenActual) {
                overlayOrigenActual.style.display = "flex";
                overlayOrigenActual = null;
            }
            formPendiente = null;
        }

        if (btnConfirmarEl) {
            btnConfirmarEl.addEventListener("click", function () {
                if (formPendiente) { formPendiente.submit(); }
            });
        }
        if (btnCancelarConfirmEl) { btnCancelarConfirmEl.addEventListener("click", cerrarConfirmacion); }
        if (overlayConfirm) {
            overlayConfirm.addEventListener("click", function (e) {
                if (e.target === overlayConfirm) { cerrarConfirmacion(); }
            });
        }

        // ---------- Modal "Nuevo restaurante" / "Editar restaurante" ----------
        var overlayRestaurante = document.getElementById("modal-restaurante");
        var tituloRestaurante = document.getElementById("modal-restaurante-titulo");
        var btnGuardarRestaurante = document.getElementById("restaurante-guardar-btn");
        var formRestaurante = document.getElementById("form-restaurante");
        var campoAccion = document.getElementById("restaurante-accion");
        var campoId = document.getElementById("restaurante-id");
        var campoNombre = document.getElementById("restaurante-nombre");
        var campoDescripcion = document.getElementById("restaurante-descripcion");
        var campoColor = document.getElementById("restaurante-color");
        var campoColorHex = document.getElementById("restaurante-color-hex");
        var btnCancelarRestaurante = document.getElementById("modal-restaurante-cancelar");
        var btnAbrirNuevo = document.getElementById("btn-abrir-nuevo");
        var COLOR_POR_DEFECTO = "#f97316";

        /** Normaliza un texto escrito a mano a "#RRGGBB", o null si no es un hex valido. */
        function normalizarColorHex(valor) {
            if (!valor) { return null; }
            var limpio = valor.trim();
            if (limpio.charAt(0) !== "#") { limpio = "#" + limpio; }
            return /^#[0-9A-Fa-f]{6}$/.test(limpio) ? limpio.toUpperCase() : null;
        }

        function fijarColor(valorHex) {
            var normalizado = normalizarColorHex(valorHex) || COLOR_POR_DEFECTO;
            if (campoColor) { campoColor.value = normalizado; }
            if (campoColorHex) { campoColorHex.value = normalizado; }
        }

        if (campoColor) {
            campoColor.addEventListener("input", function () {
                if (campoColorHex) { campoColorHex.value = campoColor.value.toUpperCase(); }
            });
        }
        if (campoColorHex) {
            campoColorHex.addEventListener("input", function () {
                var normalizado = normalizarColorHex(campoColorHex.value);
                if (normalizado && campoColor) { campoColor.value = normalizado; }
            });
        }

        function abrirModalRestaurante() {
            if (overlayRestaurante) { overlayRestaurante.style.display = "flex"; }
        }
        function cerrarModalRestaurante() {
            if (overlayRestaurante) { overlayRestaurante.style.display = "none"; }
        }

        function abrirNuevo() {
            if (campoAccion) { campoAccion.value = "guardar"; }
            if (campoId) { campoId.value = ""; }
            if (campoNombre) { campoNombre.value = ""; }
            if (campoDescripcion) { campoDescripcion.value = ""; }
            fijarColor(COLOR_POR_DEFECTO);
            if (tituloRestaurante) { tituloRestaurante.textContent = "Nuevo restaurante"; }
            if (btnGuardarRestaurante) { btnGuardarRestaurante.textContent = "Guardar"; }
            abrirModalRestaurante();
        }

        function abrirEditar(boton) {
            if (campoAccion) { campoAccion.value = "actualizar"; }
            if (campoId) { campoId.value = boton.getAttribute("data-id"); }
            if (campoNombre) { campoNombre.value = boton.getAttribute("data-nombre"); }
            if (campoDescripcion) { campoDescripcion.value = boton.getAttribute("data-descripcion"); }
            fijarColor(boton.getAttribute("data-color"));
            if (tituloRestaurante) { tituloRestaurante.textContent = "Editar restaurante"; }
            if (btnGuardarRestaurante) { btnGuardarRestaurante.textContent = "Guardar cambios"; }
            abrirModalRestaurante();
        }

        if (btnAbrirNuevo) { btnAbrirNuevo.addEventListener("click", abrirNuevo); }
        if (btnCancelarRestaurante) { btnCancelarRestaurante.addEventListener("click", cerrarModalRestaurante); }
        if (overlayRestaurante) {
            overlayRestaurante.addEventListener("click", function (e) {
                if (e.target === overlayRestaurante) { cerrarModalRestaurante(); }
            });
        }

        document.querySelectorAll(".marca-editar").forEach(function (boton) {
            boton.addEventListener("click", function () { abrirEditar(boton); });
        });

        if (formRestaurante) {
            formRestaurante.addEventListener("submit", function (e) {
                e.preventDefault();
                if (!formRestaurante.checkValidity()) {
                    formRestaurante.reportValidity();
                    return;
                }
                var esEdicion = campoAccion.value === "actualizar";
                var campos = [{ clave: "Nombre", valor: campoNombre.value.trim() }];
                if (campoDescripcion && campoDescripcion.value.trim()) {
                    campos.push({ clave: "Descripción", valor: campoDescripcion.value.trim() });
                }
                if (campoColor && campoColor.value) {
                    campos.push({ clave: "Color de marca", valor: campoColor.value.toUpperCase() });
                }
                mostrarConfirmacion(formRestaurante, {
                    titulo: esEdicion ? "Confirmar cambios" : "Confirmar creación",
                    campos: campos,
                    confirmarTexto: esEdicion ? "Guardar cambios" : "Crear restaurante",
                    confirmarClase: "btn--ok",
                    overlayOrigen: overlayRestaurante
                });
            });
        }

        // ---------- Eliminar restaurante ----------
        var formEliminar = document.getElementById("form-eliminar-restaurante");
        var campoEliminarId = document.getElementById("eliminar-restaurante-id");
        document.querySelectorAll(".marca-eliminar").forEach(function (boton) {
            boton.addEventListener("click", function () {
                var id = boton.getAttribute("data-id");
                var nombre = boton.getAttribute("data-nombre") || "este restaurante";
                if (campoEliminarId) { campoEliminarId.value = id; }
                mostrarConfirmacion(formEliminar, {
                    titulo: "Eliminar restaurante",
                    intro: "Esta acción no se puede deshacer si tiene platos asociados.",
                    campos: [{ clave: "Restaurante", valor: nombre }],
                    confirmarTexto: "Eliminar",
                    confirmarClase: "btn--danger"
                });
            });
        });
    });
})();
