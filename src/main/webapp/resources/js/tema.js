/**
 * Alternancia de tema claro/oscuro. Anade o quita la clase
 * "tema-claro" en <body> y guarda la preferencia en sessionStorage, de modo
 * que se mantiene mientras dura la sesion pero NO entre sesiones distintas.
 */
(function () {
    "use strict";

    var CLASE = "tema-claro";
    var LLAVE = "darkkitchen-tema";

    function aplicar(tema) {
        if (tema === "claro") {
            document.body.classList.add(CLASE);
        } else {
            document.body.classList.remove(CLASE);
        }
        var btn = document.getElementById("toggle-tema");
        if (btn) {
            var esClaro = document.body.classList.contains(CLASE);
            btn.innerHTML = esClaro ? "&#9728; Modo claro" : "&#9790; Modo oscuro";
            btn.setAttribute("aria-pressed", String(esClaro));
        }
    }

    function temaGuardado() {
        try {
            return sessionStorage.getItem(LLAVE) || "oscuro";
        } catch (e) {
            return "oscuro";
        }
    }

    document.addEventListener("DOMContentLoaded", function () {
        aplicar(temaGuardado());
        var btn = document.getElementById("toggle-tema");
        if (!btn) {
            return;
        }
        btn.addEventListener("click", function () {
            var nuevo = document.body.classList.contains(CLASE) ? "oscuro" : "claro";
            try {
                sessionStorage.setItem(LLAVE, nuevo);
            } catch (e) {
                /* sessionStorage no disponible: el cambio sigue siendo visual */
            }
            aplicar(nuevo);
        });
    });
})();
