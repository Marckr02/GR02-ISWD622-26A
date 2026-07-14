/**
 * Inventario de bodega (vista de cuadricula). Centraliza:
 *  - filtro de texto + filtro por nivel de stock sobre las tarjetas;
 *  - modal de "Crear insumo" y modal unificado de "Editar insumo"
 *    (nombre, unidad, minimo y proveedor se guardan de una sola vez);
 *  - baja de insumo desde el propio modal de edicion;
 *  - modal generico de confirmacion, reutilizado por los flujos de
 *    stock (añadir/reducir) y por crear/editar/eliminar insumo;
 *  - popover rapido de contacto de proveedor en tarjetas en alerta.
 * Las notificaciones de exito/error usan el Toast Manager global (toast.js).
 * Nada de esto usa AJAX: cada accion confirmada envia un formulario real
 * y el servidor responde con una recarga completa de la pagina.
 */
(function () {
    "use strict";

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

    function textoInsumo(select) {
        if (!select || select.selectedIndex < 0) {
            return "";
        }
        return select.options[select.selectedIndex].text;
    }

    /** Filtro en tiempo real por nombre, combinado con el filtro de estado de las pills. */
    function activarFiltrosGrid() {
        var grid = document.getElementById("insumos-grid");
        var input = document.getElementById("filtro-insumo");
        var pills = document.querySelectorAll("#filtros-stock .filtro-pill");
        var sinResultados = document.getElementById("sin-resultados");
        if (!grid) {
            return;
        }
        var consulta = "";
        var filtroActivo = "todos";

        function aplicar() {
            var tarjetas = grid.querySelectorAll(".insumo-card");
            var visibles = 0;
            Array.prototype.forEach.call(tarjetas, function (tarjeta) {
                var coincideEstado = filtroActivo === "todos" || tarjeta.getAttribute("data-estado") === filtroActivo;
                var nombre = tarjeta.getAttribute("data-nombre") || "";
                var coincideTexto = consulta === "" || nombre.indexOf(consulta) !== -1;
                var visible = coincideEstado && coincideTexto;
                tarjeta.style.display = visible ? "" : "none";
                if (visible) { visibles += 1; }
            });
            if (sinResultados) {
                sinResultados.hidden = visibles !== 0;
            }
        }

        if (input) {
            input.addEventListener("input", function () {
                consulta = input.value.trim().toLowerCase();
                aplicar();
            });
        }

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

    /** Wiring generico de abrir/cerrar para un modal simple, sin poblar datos dinamicos. */
    function cablearModalSimple(overlayId, abrirId, cancelarId) {
        var overlay = document.getElementById(overlayId);
        var abrir = document.getElementById(abrirId);
        var cancelar = document.getElementById(cancelarId);
        if (!overlay) {
            return;
        }
        if (abrir) {
            abrir.addEventListener("click", function () {
                overlay.style.display = "flex";
            });
        }
        if (cancelar) {
            cancelar.addEventListener("click", function () {
                overlay.style.display = "none";
            });
        }
        overlay.addEventListener("click", function (e) {
            if (e.target === overlay) { overlay.style.display = "none"; }
        });
    }

    /** Popover de contacto de proveedor: cubre el caso sin proveedor y sin telefono/correo. */
    function activarContactoProveedor() {
        var overlay = document.getElementById("modal-contacto-proveedor");
        var cerrar = document.getElementById("modal-contacto-cerrar");
        var cuerpo = document.getElementById("contacto-proveedor-cuerpo");
        if (!overlay || !cuerpo) {
            return;
        }

        document.querySelectorAll(".insumo-card__contacto").forEach(function (boton) {
            boton.addEventListener("click", function () {
                var nombre = (boton.getAttribute("data-proveedor-nombre") || "").trim();
                var telefono = (boton.getAttribute("data-proveedor-telefono") || "").trim();
                var correo = (boton.getAttribute("data-proveedor-correo") || "").trim();

                cuerpo.textContent = "";
                if (!nombre) {
                    var sinProveedor = document.createElement("p");
                    sinProveedor.className = "hint";
                    sinProveedor.textContent = "Sin proveedor asignado";
                    cuerpo.appendChild(sinProveedor);
                } else {
                    var pNombre = document.createElement("p");
                    pNombre.className = "contacto-nombre";
                    pNombre.textContent = nombre;
                    cuerpo.appendChild(pNombre);
                    if (telefono) {
                        var pTel = document.createElement("p");
                        pTel.textContent = "Tel: " + telefono;
                        cuerpo.appendChild(pTel);
                    }
                    if (correo) {
                        var pCorreo = document.createElement("p");
                        pCorreo.textContent = "Correo: " + correo;
                        cuerpo.appendChild(pCorreo);
                    }
                    if (!telefono && !correo) {
                        var sinDatos = document.createElement("p");
                        sinDatos.className = "hint";
                        sinDatos.textContent = "Sin datos de contacto registrados";
                        cuerpo.appendChild(sinDatos);
                    }
                }
                overlay.style.display = "flex";
            });
        });

        if (cerrar) {
            cerrar.addEventListener("click", function () { overlay.style.display = "none"; });
        }
        overlay.addEventListener("click", function (e) {
            if (e.target === overlay) { overlay.style.display = "none"; }
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        activarFiltrosGrid();
        activarContactoProveedor();

        cablearModalSimple("modal-agregar-stock", "btn-abrir-agregar", "modal-agregar-cancelar");
        cablearModalSimple("modal-reducir-stock", "btn-abrir-reducir", "modal-reducir-cancelar");
        cablearModalSimple("modal-crear-insumo", "btn-abrir-crear", "modal-crear-cancelar");
        cablearModalSimple("modal-editar-insumo", null, "modal-editar-cancelar");

        // ---------- Modal de confirmacion generico ----------
        var overlayConfirm = document.getElementById("modal-confirm");
        var resumenEl = document.getElementById("modal-resumen");
        var tituloEl = document.getElementById("modal-titulo");
        var btnConfirmarEl = document.getElementById("modal-confirmar");
        var btnCancelarConfirmEl = document.getElementById("modal-cancelar");
        var formPendiente = null;
        var overlayOrigenActual = null;

        var introEl = document.getElementById("modal-intro");
        var gridEl = document.getElementById("modal-resumen-grid");
        var iconoEl = document.getElementById("modal-icono");

        /** Muestra el modal de confirmacion. Si opts.campos viene informado (lista de
         *  {clave, valor}), pinta la tarjeta de resumen estructurado; si no, cae al
         *  parrafo de texto plano (usado por acciones simples como stock/eliminar). */
        function mostrarConfirmacion(form, opts) {
            opts = opts || {};
            overlayOrigenActual = opts.overlayOrigen || form.closest(".modal-overlay");
            if (overlayOrigenActual) { overlayOrigenActual.style.display = "none"; }
            formPendiente = form;
            if (tituloEl) { tituloEl.textContent = opts.titulo || form.getAttribute("data-titulo") || "Confirmar"; }

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
                btnConfirmarEl.textContent = opts.confirmarTexto || form.getAttribute("data-confirmar") || "Confirmar";
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

        // ---------- Formularios de stock (añadir / reducir), con validacion propia ----------
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
                var unidadTexto = unidadSeleccionada() ? (" " + unidadSeleccionada()) : "";
                mostrarConfirmacion(form, {
                    campos: [
                        { clave: "Insumo", valor: textoInsumo(select) },
                        { clave: "Cantidad", valor: cantidad.value + unidadTexto }
                    ]
                });
            });
        });

        // ---------- Crear insumo ----------
        var formCrear = document.getElementById("form-crear-insumo");
        var overlayCrear = document.getElementById("modal-crear-insumo");
        if (formCrear) {
            formCrear.addEventListener("submit", function (e) {
                e.preventDefault();
                if (!formCrear.checkValidity()) { formCrear.reportValidity(); return; }
                var nombre = document.getElementById("crear-nombre").value.trim();
                var unidad = document.getElementById("crear-unidad").value;
                var minimo = document.getElementById("crear-minimo").value;
                var provSelect = document.getElementById("crear-proveedor");
                var provTexto = textoInsumo(provSelect);

                document.getElementById("envio-crear-nombre").value = nombre;
                document.getElementById("envio-crear-unidad").value = unidad;
                document.getElementById("envio-crear-minimo").value = minimo;
                document.getElementById("envio-crear-proveedor").value = provSelect.value;

                mostrarConfirmacion(document.getElementById("form-envio-crear"), {
                    titulo: "Confirmar creación",
                    campos: [
                        { clave: "Nombre", valor: nombre },
                        { clave: "Unidad", valor: unidad },
                        { clave: "Stock mínimo", valor: minimo },
                        { clave: "Proveedor", valor: provSelect.value !== "0" ? provTexto : "Sin proveedor asignado" }
                    ],
                    confirmarTexto: "Crear insumo",
                    confirmarClase: "btn--ok",
                    overlayOrigen: overlayCrear
                });
            });
        }

        // ---------- Editar insumo (nombre, unidad, minimo y proveedor en un solo guardado) ----------
        var formEditar = document.getElementById("form-editar-insumo");
        var overlayEditar = document.getElementById("modal-editar-insumo");
        if (formEditar) {
            formEditar.addEventListener("submit", function (e) {
                e.preventDefault();
                if (!formEditar.checkValidity()) { formEditar.reportValidity(); return; }
                var nombre = document.getElementById("editar-nombre").value.trim();
                var unidad = document.getElementById("editar-unidad").value;
                var minimo = document.getElementById("editar-minimo").value;
                var provSelect = document.getElementById("editar-proveedor");
                var provTexto = textoInsumo(provSelect);

                document.getElementById("envio-editar-id").value = document.getElementById("editar-id").value;
                document.getElementById("envio-editar-nombre").value = nombre;
                document.getElementById("envio-editar-unidad").value = unidad;
                document.getElementById("envio-editar-minimo").value = minimo;
                document.getElementById("envio-editar-proveedor").value = provSelect.value;

                mostrarConfirmacion(document.getElementById("form-envio-editar"), {
                    titulo: "Confirmar cambios",
                    campos: [
                        { clave: "Nombre", valor: nombre },
                        { clave: "Unidad", valor: unidad },
                        { clave: "Stock mínimo", valor: minimo },
                        { clave: "Proveedor", valor: provSelect.value !== "0" ? provTexto : "Sin proveedor asignado" }
                    ],
                    confirmarTexto: "Guardar cambios",
                    confirmarClase: "btn--ok",
                    overlayOrigen: overlayEditar
                });
            });
        }

        // Icono de lapiz en cada tarjeta: abre y precarga el modal de edicion.
        document.querySelectorAll(".insumo-card__editar").forEach(function (boton) {
            boton.addEventListener("click", function () {
                document.getElementById("editar-id").value = boton.getAttribute("data-id");
                document.getElementById("editar-nombre").value = boton.getAttribute("data-nombre");
                document.getElementById("editar-unidad").value = boton.getAttribute("data-unidad");
                document.getElementById("editar-minimo").value = boton.getAttribute("data-minimo");
                document.getElementById("editar-proveedor").value = boton.getAttribute("data-proveedor-id") || "0";
                if (overlayEditar) { overlayEditar.style.display = "flex"; }
            });
        });

        // ---------- Eliminar insumo (desde dentro del modal de edicion) ----------
        var btnEliminar = document.getElementById("btn-abrir-eliminar");
        if (btnEliminar) {
            btnEliminar.addEventListener("click", function () {
                var id = document.getElementById("editar-id").value;
                var nombre = document.getElementById("editar-nombre").value || "este insumo";
                if (!id) {
                    return;
                }
                document.getElementById("envio-eliminar-id").value = id;
                mostrarConfirmacion(document.getElementById("form-envio-eliminar"), {
                    titulo: "Eliminar insumo",
                    intro: "Esta acción no se puede deshacer.",
                    campos: [{ clave: "Insumo", valor: nombre }],
                    confirmarTexto: "Eliminar",
                    confirmarClase: "btn--danger",
                    overlayOrigen: overlayEditar
                });
            });
        }
    });
})();