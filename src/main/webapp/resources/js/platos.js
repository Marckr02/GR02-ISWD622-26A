/**
 * Listado de platos (HU30/HU31/HU32/HU33), todo dentro de una sola pagina:
 *  - filtro por restaurante sobre las filas de la tabla;
 *  - modal unificado de "Nuevo plato" / "Editar plato" con receta dinamica
 *    de insumos (clona una fila plantilla, igual que en receta.js) y con
 *    scroll interno para la lista de ingredientes;
 *  - modal generico de confirmacion antes de guardar o eliminar, reutilizado
 *    por los tres flujos;
 *  - popover de la receta completa ("+X mas").
 * Las notificaciones de exito/error usan el Toast Manager global (toast.js),
 * no un componente propio de esta vista. Nada de esto usa AJAX: cada accion
 * confirmada envia el formulario real
 * y el servidor responde con una recarga completa de la pagina.
 */
(function () {
    "use strict";

    function textoOpcion(select) {
        if (!select || select.selectedIndex < 0) {
            return "";
        }
        return select.options[select.selectedIndex].text;
    }

    /**
     * Popover flotante anclado al boton "+X mas" de cada fila: muestra la
     * receta completa sin sacar al usuario de la tabla. Se posiciona con
     * getBoundingClientRect() (sin libreria externa), se recoloca si no cabe
     * en pantalla, y se cierra al hacer clic fuera, con Escape, al hacer
     * scroll/resize o al volver a pulsar el mismo boton.
     */
    function activarPopoverIngredientes() {
        var popover = document.getElementById("popover-ingredientes");
        var cuerpo = document.getElementById("popover-ingredientes-cuerpo");
        if (!popover || !cuerpo) {
            return;
        }
        var botonActivo = null;

        function cerrar() {
            popover.hidden = true;
            botonActivo = null;
        }

        function posicionar(boton) {
            var rect = boton.getBoundingClientRect();
            var ancho = popover.offsetWidth;
            var alto = popover.offsetHeight;
            var margen = 10;

            var left = rect.left;
            if (left + ancho > window.innerWidth - margen) {
                left = Math.max(margen, window.innerWidth - ancho - margen);
            }
            var top = rect.bottom + 6;
            if (top + alto > window.innerHeight - margen) {
                top = rect.top - alto - 6;
            }
            popover.style.left = left + "px";
            popover.style.top = top + "px";
        }

        function abrir(boton) {
            var ingredientes = [];
            try {
                ingredientes = JSON.parse(boton.getAttribute("data-ingredientes-completos") || "[]");
            } catch (err) {
                ingredientes = [];
            }

            cuerpo.innerHTML = "";
            ingredientes.forEach(function (ing) {
                var fila = document.createElement("div");
                fila.className = "popover-ingredientes__fila";

                var nombre = document.createElement("span");
                nombre.className = "popover-ingredientes__nombre";
                nombre.textContent = ing.nombre;

                var cantidad = document.createElement("span");
                cantidad.className = "popover-ingredientes__cantidad";
                cantidad.textContent = ing.cantidad + " " + ing.unidad;

                fila.appendChild(nombre);
                fila.appendChild(cantidad);
                cuerpo.appendChild(fila);
            });

            popover.hidden = false;
            posicionar(boton);
            botonActivo = boton;
        }

        document.querySelectorAll(".pill--mas").forEach(function (boton) {
            boton.addEventListener("click", function (e) {
                e.stopPropagation();
                if (botonActivo === boton) {
                    cerrar();
                    return;
                }
                abrir(boton);
            });
        });

        document.addEventListener("click", function (e) {
            if (!popover.hidden && !popover.contains(e.target)) {
                cerrar();
            }
        });
        document.addEventListener("keydown", function (e) {
            if (e.key === "Escape") { cerrar(); }
        });
        window.addEventListener("scroll", cerrar, true);
        window.addEventListener("resize", cerrar);
    }

    /** Filtro por restaurante + busqueda por nombre, combinados, sobre las filas de la tabla de platos. */
    function activarFiltroRestaurante() {
        var select = document.getElementById("filtro-restaurante");
        var buscador = document.getElementById("buscador-platos");
        var tabla = document.getElementById("tabla-platos");
        if (!tabla) {
            return;
        }
        var filaSinResultados = document.getElementById("sin-resultados-fila");

        function aplicar() {
            var restauranteId = select ? select.value : "";
            var consulta = buscador ? buscador.value.trim().toLowerCase() : "";
            var filas = tabla.querySelectorAll("tbody tr[data-restaurante-id]");
            var visibles = 0;
            Array.prototype.forEach.call(filas, function (fila) {
                var coincideRestaurante = !restauranteId || fila.getAttribute("data-restaurante-id") === restauranteId;
                var nombre = fila.getAttribute("data-nombre") || "";
                var coincideTexto = !consulta || nombre.indexOf(consulta) !== -1;
                var visible = coincideRestaurante && coincideTexto;
                fila.style.display = visible ? "" : "none";
                if (visible) { visibles += 1; }
            });
            if (filaSinResultados) {
                filaSinResultados.hidden = visibles !== 0;
            }
        }

        if (select) { select.addEventListener("change", aplicar); }
        if (buscador) { buscador.addEventListener("input", aplicar); }
        aplicar();
    }

    document.addEventListener("DOMContentLoaded", function () {
        activarFiltroRestaurante();
        activarPopoverIngredientes();

        // ---------- Receta dinamica dentro del modal (nuevo / editar) ----------
        var contenedor = document.getElementById("receta-filas");
        var plantilla = document.getElementById("receta-plantilla");
        var botonAgregar = document.getElementById("receta-agregar");
        var formPlato = document.getElementById("form-plato");
        var errorGlobal = formPlato ? formPlato.querySelector(".form__error") : null;

        function selectsDeInsumo() {
            return Array.prototype.slice.call(
                contenedor.querySelectorAll("select[name='insumoId[]']"));
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

        function crearFila(datos) {
            var fila = plantilla.cloneNode(true);
            fila.removeAttribute("id");
            fila.style.display = "";
            var selInsumo = fila.querySelector("select[name='insumoId[]']");
            var inpCantidad = fila.querySelector("input[name='cantidad[]']");
            var selUnidad = fila.querySelector("select[name='unidad[]']");
            [selInsumo, inpCantidad, selUnidad].forEach(function (campo) { campo.disabled = false; });
            if (datos) {
                selInsumo.value = datos.insumoId;
                inpCantidad.value = datos.cantidad;
                selUnidad.value = datos.unidad;
            } else {
                selInsumo.value = "";
                inpCantidad.value = "";
                selUnidad.value = "g";
            }
            contenedor.appendChild(fila);
            return fila;
        }

        function agregarFilaVacia() {
            crearFila(null);
            actualizarBotonesQuitar();
        }

        function poblarReceta(ingredientes) {
            contenedor.innerHTML = "";
            if (!ingredientes || ingredientes.length === 0) {
                crearFila(null);
            } else {
                ingredientes.forEach(function (ing) { crearFila(ing); });
            }
            actualizarBotonesQuitar();
            if (errorGlobal) { errorGlobal.textContent = ""; }
        }

        if (contenedor && plantilla && botonAgregar) {
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

            botonAgregar.addEventListener("click", agregarFilaVacia);
        }

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

        // ---------- Modal "Nuevo plato" / "Editar plato" ----------
        var overlayPlato = document.getElementById("modal-plato");
        var tituloPlato = document.getElementById("modal-plato-titulo");
        var btnGuardarPlato = document.getElementById("plato-guardar-btn");
        var campoAccion = document.getElementById("plato-accion");
        var campoId = document.getElementById("plato-id");
        var campoNombre = document.getElementById("plato-nombre");
        var campoRestaurante = document.getElementById("plato-restaurante");
        var btnCancelarPlato = document.getElementById("modal-plato-cancelar");
        var btnAbrirNuevo = document.getElementById("btn-abrir-nuevo");

        function abrirModalPlato() {
            if (overlayPlato) { overlayPlato.style.display = "flex"; }
        }
        function cerrarModalPlato() {
            if (overlayPlato) { overlayPlato.style.display = "none"; }
        }

        function abrirNuevo() {
            if (campoAccion) { campoAccion.value = "guardar"; }
            if (campoId) { campoId.value = ""; }
            if (campoNombre) { campoNombre.value = ""; }
            if (campoRestaurante) { campoRestaurante.value = ""; }
            if (tituloPlato) { tituloPlato.textContent = "Nuevo plato"; }
            if (btnGuardarPlato) { btnGuardarPlato.textContent = "Guardar"; }
            poblarReceta(null);
            abrirModalPlato();
        }

        function abrirEditar(boton) {
            if (campoAccion) { campoAccion.value = "actualizar"; }
            if (campoId) { campoId.value = boton.getAttribute("data-id"); }
            if (campoNombre) { campoNombre.value = boton.getAttribute("data-nombre"); }
            if (campoRestaurante) { campoRestaurante.value = boton.getAttribute("data-restaurante-id"); }
            if (tituloPlato) { tituloPlato.textContent = "Editar plato"; }
            if (btnGuardarPlato) { btnGuardarPlato.textContent = "Guardar cambios"; }
            var ingredientes = [];
            try {
                ingredientes = JSON.parse(boton.getAttribute("data-ingredientes") || "[]");
            } catch (err) {
                ingredientes = [];
            }
            poblarReceta(ingredientes);
            abrirModalPlato();
        }

        if (btnAbrirNuevo) { btnAbrirNuevo.addEventListener("click", abrirNuevo); }
        if (btnCancelarPlato) { btnCancelarPlato.addEventListener("click", cerrarModalPlato); }
        if (overlayPlato) {
            overlayPlato.addEventListener("click", function (e) {
                if (e.target === overlayPlato) { cerrarModalPlato(); }
            });
        }

        document.querySelectorAll(".plato-editar").forEach(function (boton) {
            boton.addEventListener("click", function () { abrirEditar(boton); });
        });

        if (formPlato) {
            formPlato.addEventListener("submit", function (e) {
                e.preventDefault();
                if (marcarRepetidos()) {
                    return;
                }
                if (!formPlato.checkValidity()) {
                    formPlato.reportValidity();
                    return;
                }
                var esEdicion = campoAccion.value === "actualizar";
                mostrarConfirmacion(formPlato, {
                    titulo: esEdicion ? "Confirmar cambios" : "Confirmar creación",
                    campos: [
                        { clave: "Nombre", valor: campoNombre.value.trim() },
                        { clave: "Restaurante", valor: textoOpcion(campoRestaurante) }
                    ],
                    confirmarTexto: esEdicion ? "Guardar cambios" : "Crear plato",
                    confirmarClase: "btn--ok",
                    overlayOrigen: overlayPlato
                });
            });
        }

        // ---------- Eliminar plato ----------
        var formEliminar = document.getElementById("form-eliminar-plato");
        var campoEliminarId = document.getElementById("eliminar-plato-id");
        document.querySelectorAll(".plato-eliminar").forEach(function (boton) {
            boton.addEventListener("click", function () {
                var id = boton.getAttribute("data-id");
                var nombre = boton.getAttribute("data-nombre") || "este plato";
                if (campoEliminarId) { campoEliminarId.value = id; }
                mostrarConfirmacion(formEliminar, {
                    titulo: "Eliminar plato",
                    intro: "Esta acción no se puede deshacer.",
                    campos: [{ clave: "Plato", valor: nombre }],
                    confirmarTexto: "Eliminar",
                    confirmarClase: "btn--danger"
                });
            });
        });
    });
})();
