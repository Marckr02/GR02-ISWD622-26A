// Micro-interacciones del tablero Kanban. Mantiene la pagina usable
// aunque JavaScript este deshabilitado: el envio del formulario es nativo.
(function () {
  "use strict";

  document.addEventListener("DOMContentLoaded", function () {
    var forms = document.querySelectorAll("form.card__action");

    forms.forEach(function (form) {
      form.addEventListener("submit", function () {
        var card = form.closest(".card");
        if (card) {
          card.classList.add("card--moviendo");
        }
        var boton = form.querySelector("button");
        if (boton) {
          boton.disabled = true;
          boton.textContent = "Moviendo...";
        }
      });
    });

    // Resalta brevemente la columna recien actualizada si la URL lo indica.
    var params = new URLSearchParams(window.location.search);
    var destino = params.get("foco");
    if (destino) {
      var columna = document.querySelector('[data-estado="' + destino + '"]');
      if (columna) {
        columna.classList.add("col--foco");
        setTimeout(function () {
          columna.classList.remove("col--foco");
        }, 1200);
      }
    }
  });
})();