/**
 * Popover de "Contactar proveedor" (tel:/whatsapp/mailto), compartido entre
 * Inventario (tarjetas de insumo) y el Panel de control (tabla de insumos
 * criticos). Ambas vistas incluyen el mismo modal en el HTML
 * (#modal-contacto-proveedor / #contacto-proveedor-cuerpo /
 * #modal-contacto-cerrar); lo unico que cambia es el selector de los
 * botones que lo disparan.
 */
(function () {
    "use strict";

    /** Numero en formato E.164 (sin espacios/guiones, con codigo de pais) para el
     *  enlace de WhatsApp (wa.me). Si el numero ya trae codigo de pais (11+ digitos,
     *  o empieza con 593) lo respeta; si es un movil ecuatoriano local (09XXXXXXXX,
     *  10 digitos) le antepone 593 quitando el 0 inicial; cualquier otro caso queda
     *  tal cual (mejor esfuerzo, sin un campo de pais explicito en el modelo). */
    function numeroWhatsapp(telefono) {
        var digitos = telefono.replace(/\D/g, "");
        if (digitos.length === 10 && digitos.charAt(0) === "0") {
            return "593" + digitos.substring(1);
        }
        return digitos;
    }

    function crearIcono(pathD) {
        var svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
        svg.setAttribute("viewBox", "0 0 24 24");
        svg.setAttribute("fill", "none");
        svg.setAttribute("stroke", "currentColor");
        svg.setAttribute("stroke-width", "2");
        svg.setAttribute("stroke-linecap", "round");
        svg.setAttribute("stroke-linejoin", "round");
        svg.setAttribute("aria-hidden", "true");
        var path = document.createElementNS("http://www.w3.org/2000/svg", "path");
        path.setAttribute("d", pathD);
        svg.appendChild(path);
        return svg;
    }

    /** Tarjeta de contacto clicable (telefono/correo/whatsapp): icono + etiqueta
     *  chica arriba + dato destacado abajo, todo dentro de un <a> de ancho
     *  completo para que un solo clic abra la app correspondiente. */
    function crearTarjetaContacto(opts) {
        var link = document.createElement("a");
        link.className = "contacto-card" + (opts.claseExtra ? " " + opts.claseExtra : "");
        link.href = opts.href;
        if (opts.target) { link.target = opts.target; link.rel = "noopener"; }

        var icono = document.createElement("span");
        icono.className = "contacto-card__icono";
        icono.appendChild(crearIcono(opts.iconoPath));

        var texto = document.createElement("span");
        texto.className = "contacto-card__texto";
        var etiqueta = document.createElement("span");
        etiqueta.className = "contacto-card__etiqueta";
        etiqueta.textContent = opts.etiqueta;
        var valor = document.createElement("span");
        valor.className = "contacto-card__valor";
        valor.textContent = opts.valor;
        texto.appendChild(etiqueta);
        texto.appendChild(valor);

        link.appendChild(icono);
        link.appendChild(texto);
        return link;
    }

    /** Panel de contacto rapido de proveedor: el nombre del proveedor es el
     *  protagonista, y telefono/correo/WhatsApp se ofrecen como tarjetas de
     *  accion de un solo clic (tel:, mailto:, wa.me) en vez de texto plano.
     *  @param selectorBotones selector CSS de los botones que abren el popover
     *         (cada uno con data-proveedor-nombre/telefono/correo). */
    function activarContactoProveedor(selectorBotones) {
        var overlay = document.getElementById("modal-contacto-proveedor");
        var cerrar = document.getElementById("modal-contacto-cerrar");
        var cuerpo = document.getElementById("contacto-proveedor-cuerpo");
        if (!overlay || !cuerpo) {
            return;
        }

        document.querySelectorAll(selectorBotones).forEach(function (boton) {
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

                    if (telefono || correo) {
                        var lista = document.createElement("div");
                        lista.className = "contacto-lista";

                        if (telefono) {
                            lista.appendChild(crearTarjetaContacto({
                                href: "tel:" + telefono.replace(/\s+/g, ""),
                                etiqueta: "Llamar al número",
                                valor: telefono,
                                iconoPath: "M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"
                            }));
                            lista.appendChild(crearTarjetaContacto({
                                href: "https://wa.me/" + numeroWhatsapp(telefono),
                                target: "_blank",
                                claseExtra: "contacto-card--whatsapp",
                                etiqueta: "Escribir por WhatsApp",
                                valor: telefono,
                                iconoPath: "M3 21l1.65-4.95A9 9 0 1 1 8.05 19.35L3 21zM8.5 8.5c0 4 3 7 7 7 .5 0 1.5-.5 1.5-1.5l-.3-1.2-2.2-.8-1 1a5.5 5.5 0 0 1-3.2-3.2l1-1-.8-2.2L9.5 6.5c-1 0-1 1-1 2z"
                            }));
                        }
                        if (correo) {
                            lista.appendChild(crearTarjetaContacto({
                                href: "mailto:" + correo,
                                etiqueta: "Enviar un correo",
                                valor: correo,
                                iconoPath: "M4 4h16v16H4V4zm0 0l8 8 8-8"
                            }));
                        }
                        cuerpo.appendChild(lista);
                    } else {
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

    window.activarContactoProveedor = activarContactoProveedor;
})();
