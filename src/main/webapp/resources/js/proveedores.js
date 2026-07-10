/**
 * Agenda de proveedores (HU12/HU13/HU25 + edicion), todo dentro de una sola
 * pagina: modal unificado de "Nuevo proveedor" / "Editar proveedor"
 * (nombre + telefono + correo) y modal generico de confirmacion antes de
 * guardar, actualizar o eliminar. Las notificaciones de exito/error usan el
 * Toast Manager global (toast.js). Nada de esto usa AJAX: cada accion
 * confirmada envia el formulario real y el servidor responde con una
 * recarga completa de la pagina.
 */
(function () {
    "use strict";

    /** Busqueda en tiempo real por nombre sobre las tarjetas de proveedor. */
    function activarBuscador() {
        var buscador = document.getElementById("buscador-proveedores");
        var lista = document.getElementById("lista-proveedores");
        if (!buscador || !lista) {
            return;
        }
        var sinResultados = document.getElementById("sin-resultados-proveedores");

        function aplicar() {
            var consulta = buscador.value.trim().toLowerCase();
            var tarjetas = lista.querySelectorAll(".proveedor-card");
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

        // ---------- Modal "Nuevo proveedor" / "Editar proveedor" ----------
        var overlayProveedor = document.getElementById("modal-proveedor");
        var tituloProveedor = document.getElementById("modal-proveedor-titulo");
        var btnGuardarProveedor = document.getElementById("proveedor-guardar-btn");
        var formProveedor = document.getElementById("form-proveedor");
        var campoAccion = document.getElementById("proveedor-accion");
        var campoId = document.getElementById("proveedor-id");
        var campoNombre = document.getElementById("proveedor-nombre");
        var campoTelefono = document.getElementById("proveedor-telefono");
        var campoCorreo = document.getElementById("proveedor-correo");
        var btnCancelarProveedor = document.getElementById("modal-proveedor-cancelar");
        var btnAbrirNuevo = document.getElementById("btn-abrir-nuevo");

        function abrirModalProveedor() {
            if (overlayProveedor) { overlayProveedor.style.display = "flex"; }
        }
        function cerrarModalProveedor() {
            if (overlayProveedor) { overlayProveedor.style.display = "none"; }
        }

        function abrirNuevo() {
            if (campoAccion) { campoAccion.value = "guardar"; }
            if (campoId) { campoId.value = ""; }
            if (campoNombre) { campoNombre.value = ""; }
            if (campoTelefono) { campoTelefono.value = ""; }
            if (campoCorreo) { campoCorreo.value = ""; }
            if (tituloProveedor) { tituloProveedor.textContent = "Nuevo proveedor"; }
            if (btnGuardarProveedor) { btnGuardarProveedor.textContent = "Guardar"; }
            abrirModalProveedor();
        }

        function abrirEditar(boton) {
            if (campoAccion) { campoAccion.value = "actualizar"; }
            if (campoId) { campoId.value = boton.getAttribute("data-id"); }
            if (campoNombre) { campoNombre.value = boton.getAttribute("data-nombre"); }
            if (campoTelefono) { campoTelefono.value = boton.getAttribute("data-telefono"); }
            if (campoCorreo) { campoCorreo.value = boton.getAttribute("data-correo"); }
            if (tituloProveedor) { tituloProveedor.textContent = "Editar proveedor"; }
            if (btnGuardarProveedor) { btnGuardarProveedor.textContent = "Guardar cambios"; }
            abrirModalProveedor();
        }

        if (btnAbrirNuevo) { btnAbrirNuevo.addEventListener("click", abrirNuevo); }
        if (btnCancelarProveedor) { btnCancelarProveedor.addEventListener("click", cerrarModalProveedor); }
        if (overlayProveedor) {
            overlayProveedor.addEventListener("click", function (e) {
                if (e.target === overlayProveedor) { cerrarModalProveedor(); }
            });
        }

        document.querySelectorAll(".proveedor-editar").forEach(function (boton) {
            boton.addEventListener("click", function () { abrirEditar(boton); });
        });

        if (formProveedor) {
            formProveedor.addEventListener("submit", function (e) {
                e.preventDefault();
                if (!formProveedor.checkValidity()) {
                    formProveedor.reportValidity();
                    return;
                }
                var esEdicion = campoAccion.value === "actualizar";
                mostrarConfirmacion(formProveedor, {
                    titulo: esEdicion ? "Confirmar cambios" : "Confirmar creación",
                    campos: [
                        { clave: "Nombre", valor: campoNombre.value.trim() },
                        { clave: "Teléfono", valor: campoTelefono.value.trim() },
                        { clave: "Correo", valor: campoCorreo.value.trim() }
                    ],
                    confirmarTexto: esEdicion ? "Guardar cambios" : "Crear proveedor",
                    confirmarClase: "btn--ok",
                    overlayOrigen: overlayProveedor
                });
            });
        }

        // ---------- Eliminar proveedor ----------
        var formEliminar = document.getElementById("form-eliminar-proveedor");
        var campoEliminarId = document.getElementById("eliminar-proveedor-id");
        document.querySelectorAll(".proveedor-eliminar").forEach(function (boton) {
            boton.addEventListener("click", function () {
                var id = boton.getAttribute("data-id");
                var nombre = boton.getAttribute("data-nombre") || "este proveedor";
                if (campoEliminarId) { campoEliminarId.value = id; }
                mostrarConfirmacion(formEliminar, {
                    titulo: "Eliminar proveedor",
                    intro: "Esta acción no se puede deshacer si tiene insumos vinculados.",
                    campos: [{ clave: "Proveedor", valor: nombre }],
                    confirmarTexto: "Eliminar",
                    confirmarClase: "btn--danger"
                });
            });
        });
    });
})();
