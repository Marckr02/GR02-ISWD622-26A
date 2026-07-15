/**
 * Listado de platos (HU30/HU31/HU32/HU33), todo dentro de una sola pagina:
 *  - filtro por restaurante + busqueda (por nombre de plato o de ingrediente)
 *    sobre las tarjetas, todas de altura identica;
 *  - popover flotante ("+X insumos mas") con la receta completa del plato
 *    cuando excede el top 3 visible (no expande la tarjeta -- todas deben
 *    medir siempre lo mismo);
 *  - menu contextual flotante ("⋮") con Editar/Eliminar, que reemplaza los
 *    iconos de accion fijos para no competir con el titulo de la receta;
 *  - modal unificado de "Nuevo plato" / "Editar plato" con receta dinamica
 *    de insumos (clona una fila plantilla, igual que en receta.js) y con
 *    scroll interno para la lista de ingredientes;
 *  - modal generico de confirmacion antes de guardar o eliminar, reutilizado
 *    por los tres flujos.
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

    var GRUPOS_UNIDAD = {
        "g": ["g", "kg"],
        "kg": ["g", "kg"],
        "ml": ["ml", "l"],
        "l": ["ml", "l"],
        "unidades": ["unidades"]
    };

    /** Oculta en el select de unidad las opciones incompatibles con la unidad de
     *  almacenamiento del insumo seleccionado en esa misma fila (segun el
     *  data-unidad de la opcion elegida) -- mismos pares que
     *  ConversionUnidades.java en el servidor, para que nunca se pueda enviar
     *  una combinacion que el backend fuera a rechazar. Si "unidadPreferida"
     *  sigue siendo compatible la deja seleccionada; si no, cae a la unidad
     *  propia del insumo. */
    function actualizarUnidadesDisponibles(fila, unidadPreferida) {
        var selInsumo = fila.querySelector("select[name='insumoId[]']");
        var selUnidad = fila.querySelector("select[name='unidad[]']");
        if (!selInsumo || !selUnidad) {
            return;
        }
        var opcionInsumo = selInsumo.options[selInsumo.selectedIndex];
        var unidadInsumo = opcionInsumo ? opcionInsumo.getAttribute("data-unidad") : null;
        var permitidas = unidadInsumo ? (GRUPOS_UNIDAD[unidadInsumo] || [unidadInsumo]) : null;
        var valorPreferido = unidadPreferida || selUnidad.value;

        Array.prototype.forEach.call(selUnidad.options, function (opcion) {
            opcion.hidden = !!permitidas && permitidas.indexOf(opcion.value) === -1;
        });

        if (permitidas && permitidas.indexOf(valorPreferido) === -1) {
            selUnidad.value = unidadInsumo;
        } else if (valorPreferido) {
            selUnidad.value = valorPreferido;
        }
    }

    /** Posiciona un panel flotante (ya en el DOM, position:fixed) justo debajo
     *  de "boton", alineado a su borde derecho y sin salirse del viewport. */
    function posicionarFlotante(panel, boton) {
        var rect = boton.getBoundingClientRect();
        var ancho = panel.offsetWidth;
        var left = Math.max(8, Math.min(rect.right - ancho, window.innerWidth - ancho - 8));
        panel.style.left = left + "px";
        panel.style.top = (rect.bottom + 6) + "px";
    }

    /** Popover flotante del boton "+X insumos mas": la tarjeta jamas cambia de
     *  tamaño, asi que la receta completa (no solo el resto: todos los
     *  insumos, incluidos los 3 ya visibles en la tarjeta) vive en este panel
     *  superpuesto -- fondo con desenfoque, anclado por JS junto al boton que
     *  lo abre, con su propio boton de cierre (X). El JS lee la receta ya
     *  serializada por el JSP en data-ingredientes, sin volver a tocar el
     *  servidor. */
    function activarPopoverIngredientesExtra() {
        var botones = document.querySelectorAll(".plato-card__mas-btn");
        if (!botones.length) {
            return;
        }
        var popoverActual = null;
        var botonActual = null;

        function cerrar() {
            if (popoverActual) { popoverActual.remove(); popoverActual = null; }
            botonActual = null;
        }

        function abrir(boton) {
            if (botonActual === boton) { cerrar(); return; }
            cerrar();
            botonActual = boton;

            var ingredientes = [];
            try { ingredientes = JSON.parse(boton.getAttribute("data-ingredientes") || "[]"); } catch (err) { ingredientes = []; }

            var pop = document.createElement("div");
            pop.className = "plato-card__popover";

            var cabecera = document.createElement("div");
            cabecera.className = "plato-card__popover-cabecera";
            var titulo = document.createElement("span");
            titulo.className = "plato-card__popover-titulo";
            titulo.textContent = boton.getAttribute("data-plato-nombre") || "Receta completa";
            var btnCerrar = document.createElement("button");
            btnCerrar.type = "button";
            btnCerrar.className = "plato-card__popover-cerrar";
            btnCerrar.setAttribute("aria-label", "Cerrar");
            btnCerrar.innerHTML = "&times;";
            btnCerrar.addEventListener("click", cerrar);
            cabecera.appendChild(titulo);
            cabecera.appendChild(btnCerrar);
            pop.appendChild(cabecera);

            var lista = document.createElement("div");
            lista.className = "plato-card__popover-lista";
            ingredientes.forEach(function (ing) {
                var fila = document.createElement("div");
                fila.className = "plato-card__popover-fila";
                var nombre = document.createElement("span");
                nombre.textContent = ing.nombre;
                var cantidad = document.createElement("span");
                cantidad.className = "plato-card__popover-cantidad";
                cantidad.textContent = ing.cantidad;
                fila.appendChild(nombre);
                fila.appendChild(cantidad);
                lista.appendChild(fila);
            });
            pop.appendChild(lista);

            document.body.appendChild(pop);
            popoverActual = pop;
            posicionarFlotante(pop, boton);
        }

        botones.forEach(function (boton) {
            boton.addEventListener("click", function (e) {
                e.stopPropagation();
                abrir(boton);
            });
        });

        document.addEventListener("click", function (e) {
            if (popoverActual && !popoverActual.contains(e.target) && e.target !== botonActual) {
                cerrar();
            }
        });
        document.addEventListener("keydown", function (e) {
            if (e.key === "Escape") { cerrar(); }
        });
        window.addEventListener("scroll", cerrar, true);
    }

    /** Menu contextual flotante ("⋮" -> Editar / Eliminar) de cada tarjeta:
     *  recibe las mismas funciones que ya manejan esas acciones (definidas
     *  mas abajo) para no duplicar logica, solo cambia de donde se disparan. */
    function activarMenuAccionesPlato(abrirEditarFn, eliminarFn) {
        var botones = document.querySelectorAll(".plato-card__menu-btn");
        if (!botones.length) {
            return;
        }
        var menuActual = null;
        var botonActual = null;

        function cerrar() {
            if (menuActual) { menuActual.remove(); menuActual = null; }
            if (botonActual) { botonActual.setAttribute("aria-expanded", "false"); }
            botonActual = null;
        }

        function abrir(boton) {
            if (botonActual === boton) { cerrar(); return; }
            cerrar();
            botonActual = boton;
            boton.setAttribute("aria-expanded", "true");

            var menu = document.createElement("div");
            menu.className = "plato-card__menu-flotante";
            menu.setAttribute("role", "menu");

            var itemEditar = document.createElement("button");
            itemEditar.type = "button";
            itemEditar.className = "plato-card__menu-item";
            itemEditar.setAttribute("role", "menuitem");
            itemEditar.textContent = "Editar";
            itemEditar.addEventListener("click", function () {
                cerrar();
                abrirEditarFn(boton);
            });

            var itemEliminar = document.createElement("button");
            itemEliminar.type = "button";
            itemEliminar.className = "plato-card__menu-item plato-card__menu-item--danger";
            itemEliminar.setAttribute("role", "menuitem");
            itemEliminar.textContent = "Eliminar";
            itemEliminar.addEventListener("click", function () {
                cerrar();
                eliminarFn(boton);
            });

            menu.appendChild(itemEditar);
            menu.appendChild(itemEliminar);
            document.body.appendChild(menu);
            menuActual = menu;
            posicionarFlotante(menu, boton);
        }

        botones.forEach(function (boton) {
            boton.addEventListener("click", function (e) {
                e.stopPropagation();
                abrir(boton);
            });
        });

        document.addEventListener("click", function (e) {
            if (menuActual && !menuActual.contains(e.target) && e.target !== botonActual) {
                cerrar();
            }
        });
        document.addEventListener("keydown", function (e) {
            if (e.key === "Escape") { cerrar(); }
        });
        window.addEventListener("scroll", cerrar, true);
    }

    /** Filtro por restaurante + busqueda (nombre del plato o de cualquiera de
     *  sus ingredientes, via data-busqueda), combinados, sobre las tarjetas. */
    function activarFiltroRestaurante() {
        var select = document.getElementById("filtro-restaurante");
        var buscador = document.getElementById("buscador-platos");
        var lista = document.getElementById("lista-platos");
        if (!lista) {
            return;
        }
        var sinResultados = document.getElementById("sin-resultados-platos");

        function aplicar() {
            var restauranteId = select ? select.value : "";
            var consulta = buscador ? buscador.value.trim().toLowerCase() : "";
            var tarjetas = lista.querySelectorAll(".plato-card");
            var visibles = 0;
            Array.prototype.forEach.call(tarjetas, function (tarjeta) {
                var coincideRestaurante = !restauranteId || tarjeta.getAttribute("data-restaurante-id") === restauranteId;
                var haystack = tarjeta.getAttribute("data-busqueda") || tarjeta.getAttribute("data-nombre") || "";
                var coincideTexto = !consulta || haystack.indexOf(consulta) !== -1;
                var visible = coincideRestaurante && coincideTexto;
                tarjeta.style.display = visible ? "" : "none";
                if (visible) { visibles += 1; }
            });
            if (sinResultados) {
                sinResultados.hidden = visibles !== 0;
            }
        }

        if (select) { select.addEventListener("change", aplicar); }
        if (buscador) { buscador.addEventListener("input", aplicar); }
        aplicar();
    }

    document.addEventListener("DOMContentLoaded", function () {
        activarFiltroRestaurante();
        activarPopoverIngredientesExtra();

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
            actualizarUnidadesDisponibles(fila, datos ? datos.unidad : null);
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
                    actualizarUnidadesDisponibles(e.target.closest(".receta__fila"));
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
        function accionEliminar(boton) {
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
        }

        // El menu contextual ("⋮") de cada tarjeta dispara estas mismas
        // funciones de edicion/eliminacion -- ver activarMenuAccionesPlato.
        activarMenuAccionesPlato(abrirEditar, accionEliminar);
    });
})();
