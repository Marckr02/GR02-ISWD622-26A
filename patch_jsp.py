import io, sys
src = sys.argv[1]
text = io.open(src, encoding='utf-8').read()

start_tag = '<script src="<%= ctx %>/resources/js/inventario.js"></script>'
start = text.find(start_tag)
if start == -1:
    print('start not found')
    sys.exit(1)

rest = text[start:]
# find the second </script> (the inline one)
first = rest.find('</script>')
if first == -1:
    end = len(rest)
else:
    second = rest.find('</script>', first + 9)
    if second == -1:
        end = len(rest)
    else:
        end = second + len('</script>')

script_block = u'''<script src="<%= ctx %>/resources/js/inventario.js"></script>
<script>
(function () {
"use strict";

var vistaActual = localStorage.getItem("inventario-vista") || "lista";
function aplicarVista(vista) {
  vistaActual = vista;
  document.getElementById("vista-lista").style.display = vista === "lista" ? "" : "none";
  document.getElementById("vista-cuadricula").style.display = vista === "cuadricula" ? "" : "none";
  document.querySelectorAll(".vista-toggle__pill").forEach(function (p) {
    var activo = p.getAttribute("data-vista") === vista;
    p.classList.toggle("is-on", activo);
    p.setAttribute("aria-pressed", activo);
  });
  localStorage.setItem("inventario-vista", vista);
}
document.querySelectorAll(".vista-toggle__pill").forEach(function (pill) {
  pill.addEventListener("click", function () { aplicarVista(pill.getAttribute("data-vista")); });
});
aplicarVista(vistaActual);

var checkboxes = document.querySelectorAll(".chk-insumo");
var barraLote = document.getElementById("barra-lote");
var contador = document.getElementById("barra-lote-contador");
function actualizarBarra() {
  var seleccionados = document.querySelectorAll(".chk-insumo:checked");
  contador.textContent = seleccionados.length + " insumo" + (seleccionados.length !== 1 ? "s" : "") + " seleccionado" + (seleccionados.length !== 1 ? "s" : "");
  barraLote.classList.toggle("barra-lote--activa", seleccionados.length > 0);
}
checkboxes.forEach(function (cb) { cb.addEventListener("change", actualizarBarra); });

var chkTodos = document.getElementById("seleccionar-todos");
if (chkTodos) {
  chkTodos.addEventListener("change", function () {
    checkboxes.forEach(function (cb) { cb.checked = chkTodos.checked; });
    actualizarBarra();
  });
  checkboxes.forEach(function (cb) {
    cb.addEventListener("change", function () {
      chkTodos.checked = Array.prototype.every.call(checkboxes, function (c) { return c.checked; });
      actualizarBarra();
    });
  });
}

function currentIdsSeleccionados() {
  return Array.from(document.querySelectorAll(".chk-insumo:checked")).map(function (cb) { return cb.value; });
}

var goLote = document.getElementById("btn-vincular-lote");
if (goLote) {
  goLote.addEventListener("click", function () {
    var ids = currentIdsSeleccionados();
    if (ids.length === 0) { return; }
    document.getElementById("modal-proveedor-lote-count").textContent = ids.length + " insumo(s) seleccionado(s)";
    var c = document.getElementById("insumos-ids-lote");
    if (c) { c.innerHTML = ""; }
    ids.forEach(function (id) {
      var h = document.createElement("input"); h.type = "hidden"; h.name = "insumoIds"; h.value = id;
      if (c) { c.appendChild(h); }
    });
    document.getElementById("modal-proveedor-lote").style.display = "flex";
  });
}
(function () {
  var cerrarBtn = document.getElementById("modal-proveedor-lote-cancelar");
  var overlay = document.getElementById("modal-proveedor-lote");
  if (!cerrarBtn || !overlay) { return; }
  cerrarBtn.addEventListener("click", function () { overlay.style.display = "none"; });
  overlay.addEventListener("click", function (e) { if (e.target === overlay) { overlay.style.display = "none"; } });
})();

if (document.getElementById("btn-limpiar-filtros")) {
  document.getElementById("btn-limpiar-filtros").addEventListener("click", function () {
    document.getElementById("filtro-insumo").value = "";
    document.getElementById("filtro-estado").value = "";
    document.getElementById("filtro-proveedor").value = "";
    document.querySelectorAll(".chk-insumo").forEach(function (cb) { cb.checked = false; });
    if (chkTodos) { chkTodos.checked = false; }
    document.querySelectorAll("tbody tr, .tarjeta-insumo").forEach(function (el) { el.style.display = ""; });
    document.getElementById("vista-sin-coincidencias").style.display = "none";
    var filaVacia = document.getElementById("fila-sin-insumos");
    if (filaVacia) { filaVacia.style.display = ""; }
  });
}

function wireModal(overlayId, selector, cancelarId, fn) {
  var overlay = document.getElementById(overlayId); if (!overlay) { return; }
  document.querySelectorAll(selector).forEach(function (b) {
    b.addEventListener("click", function () { fn(b); overlay.style.display = "flex"; });
  });
  var c = document.getElementById(cancelarId);
  if (c) { c.addEventListener("click", function () { overlay.style.display = "none"; }); }
  overlay.addEventListener("click", function (e) { if (e.target === overlay) { overlay.style.display = "none"; } });
}
wireModal("modal-proveedor", ".btn-asociar-proveedor", "modal-proveedor-cancelar", function (b) {
  document.getElementById("proveedor-insumo-id").value = b.getAttribute("data-id");
  document.getElementById("modal-proveedor-insumo").textContent = "Insumo: " + b.getAttribute("data-nombre");
  document.getElementById("proveedor-select").value = b.getAttribute("data-proveedor-id") || "0";
});
wireModal("modal-minimo", ".btn-editar-minimo", "modal-minimo-cancelar", function (b) {
  document.getElementById("minimo-insumo-id").value = b.getAttribute("data-id");
  document.getElementById("modal-minimo-insumo").textContent = "Insumo: " + b.getAttribute("data-nombre");
  document.getElementById("minimo-valor").value = b.getAttribute("data-minimo");
});

})();
</script>
'''

newtext = text[:start] + script_block + text[start+end:]
io.open(src, 'w', encoding='utf-8').write(newtext)
print('done')
