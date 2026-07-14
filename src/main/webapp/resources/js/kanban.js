// Micro-interacciones del tablero Kanban. Mantiene la pagina usable
// aunque JavaScript este deshabilitado: el envio del formulario es nativo.
(function () {
  "use strict";

  document.addEventListener("DOMContentLoaded", function () {
    // Los formularios "form-retroceder" gestionan su propio estado de carga
    // (solo despues de confirmar en el modal personalizado), asi que quedan
    // excluidos de este listener generico para no congelar el boton al
    // simplemente abrir el modal.
    var forms = document.querySelectorAll("form.card__action:not(.form-retroceder)");

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

    // Simulador de llegada de pedidos manuales (restaurante -> plato).
    var overlay = document.getElementById("overlay-simular-pedido");
    var btnAbrir = document.getElementById("btn-simular-pedido");
    var btnCancelar = document.getElementById("btn-cancelar-simular");
    var selectRestaurante = document.getElementById("select-restaurante");
    var selectPlato = document.getElementById("select-plato");
    var btnAgregar = document.getElementById("btn-agregar-pedido");
    var datosEl = document.getElementById("datos-simulador");

    if (overlay && btnAbrir && datosEl) {
      var datos = JSON.parse(datosEl.textContent);
      var restaurantes = datos.restaurantes || [];
      var platos = datos.platos || [];

      restaurantes.forEach(function (r) {
        var opcion = document.createElement("option");
        opcion.value = r.id;
        opcion.textContent = r.nombre;
        selectRestaurante.appendChild(opcion);
      });

      function cerrarModal() {
        overlay.classList.remove("is-abierto");
      }

      function resetearPlatos() {
        selectPlato.innerHTML = "";
        var placeholder = document.createElement("option");
        placeholder.value = "";
        placeholder.disabled = true;
        placeholder.selected = true;
        placeholder.textContent = "Primero elige un restaurante";
        selectPlato.appendChild(placeholder);
        selectPlato.disabled = true;
        btnAgregar.disabled = true;
      }

      btnAbrir.addEventListener("click", function () {
        selectRestaurante.value = "";
        resetearPlatos();
        overlay.classList.add("is-abierto");
      });

      btnCancelar.addEventListener("click", cerrarModal);

      overlay.addEventListener("click", function (evento) {
        if (evento.target === overlay) {
          cerrarModal();
        }
      });

      document.addEventListener("keydown", function (evento) {
        if (evento.key === "Escape" && overlay.classList.contains("is-abierto")) {
          cerrarModal();
        }
      });

      selectRestaurante.addEventListener("change", function () {
        var restauranteId = parseInt(selectRestaurante.value, 10);
        var platosDelRestaurante = platos.filter(function (p) {
          return p.restauranteId === restauranteId;
        });

        selectPlato.innerHTML = "";
        var placeholder = document.createElement("option");
        placeholder.value = "";
        placeholder.disabled = true;
        placeholder.selected = true;
        placeholder.textContent = platosDelRestaurante.length
            ? "Selecciona un plato..."
            : "Este restaurante no tiene platos";
        selectPlato.appendChild(placeholder);

        platosDelRestaurante.forEach(function (p) {
          var opcion = document.createElement("option");
          opcion.value = p.id;
          opcion.textContent = p.nombre;
          selectPlato.appendChild(opcion);
        });

        selectPlato.disabled = platosDelRestaurante.length === 0;
        btnAgregar.disabled = true;
      });

      selectPlato.addEventListener("change", function () {
        btnAgregar.disabled = !selectPlato.value;
      });
    }
  });
})();