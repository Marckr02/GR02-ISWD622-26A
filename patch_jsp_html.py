import io, sys
src = sys.argv[1]
text = io.open(src, encoding='utf-8').read()

# 1) tabla-scroll
text = text.replace('<table class="tabla">', '<div class="tabla-scroll">\n<table class="tabla">')
text = text.replace('</table>\n</div>\n<div class="sin-insumos"', '</table>\n</div>\n</div>\n<div class="sin-insumos"')

# 2) add filters row after .leyenda-stock
old = '<p class="leyenda-stock">\n<span class="leyenda-item"><span class="status-dot status-dot--rojo"></span>Sin stock</span>\n<span class="leyenda-item"><span class="status-dot status-dot--amarillo"></span>Bajo el minimo</span>\n<span class="leyenda-item"><span class="status-dot status-dot--verde"></span>Nivel optimo</span>\n</p>\n<table class="tabla">'
new = '''<p class="leyenda-stock">
<span class="leyenda-item"><span class="status-dot status-dot--rojo"></span>Sin stock</span>
<span class="leyenda-item"><span class="status-dot status-dot--amarillo"></span>Bajo el minimo</span>
<span class="leyenda-item"><span class="status-dot status-dot--verde"></span>Nivel optimo</span>
</p>
<div class="filtros">
<input type="text" id="filtro-insumo" class="input-filtro" placeholder="Buscar insumo..." aria-label="Buscar insumo por nombre" style="flex: 1 1 200px; min-width: 140px;">
<select id="filtro-estado" class="input-filtro" aria-label="Filtrar por estado" style="flex: 0 1 160px; min-width: 120px;">
<option value="">Todos los estados</option>
<option value="rojo">Sin stock</option>
<option value="amarillo">Bajo el minimo</option>
<option value="verde">Nivel optimo</option>
</select>
<select id="filtro-proveedor" class="input-filtro" aria-label="Filtrar por proveedor" style="flex: 0 1 200px; min-width: 130px;">
<option value="">Todos los proveedores</option>
<option value="0">Sin proveedor</option>
<%
if (proveedores != null) {
  for (model.Proveedor p : proveedores) {
%>
<option value="<%= p.getId() %>"><%= p.getNombre() %></option>
<%
  }
}
%>
</select>
<div class="vista-toggle" role="group" aria-label="Vista">
<button type="button" class="vista-toggle__pill is-on" data-vista="lista" aria-pressed="true">Lista</button>
<button type="button" class="vista-toggle__pill" data-vista="cuadricula" aria-pressed="false">Cuadricula</button>
</div>
</div>
<table class="tabla">'''
text = text.replace(old, new)

# 3) add checkbox column to table header
text = text.replace('<tr>\n<th>Insumo</th>\n<th>Unidad</th>', '<tr>\n<th><input type="checkbox" class="chk-todos" id="seleccionar-todos" aria-label="Seleccionar todos"></th>\n<th>Insumo</th>\n<th>Unidad</th>')

# 4) add checkbox cell to all table data rows (only inside this table's tbody)
# simplest: add as first td in rows after thead
marker = '<td><%= insumo.getNombre() %></td>'
text = text.replace(marker, '<td><input type="checkbox" class="chk-insumo" value="<%= insumo.getId() %>" aria-label="Seleccionar <%= insumo.getNombre() %>"></td>\n<td><%= insumo.getNombre() %></td>', 1)

# 5) add checkbox to each tarjeta-insumo
text = text.replace('<header class="tarjeta-insumo__head">\n<span class="status-dot', '<header class="tarjeta-insumo__head">\n<input type="checkbox" class="chk-insumo" value="<%= insumo.getId() %>" aria-label="Seleccionar <%= insumo.getNombre() %>">\n<span class="status-dot')

# 6) add barra-lote
barra = '''<div class="barra-lote" id="barra-lote" role="toolbar" aria-label="Acciones en lote" style="display: none;">
<span class="barra-lote__texto" id="barra-lote-contador">0 insumos seleccionados</span>
<button type="button" class="btn--mini" id="btn-vincular-lote">Vincular proveedor</button>
<button type="button" class="btn--mini" id="btn-desvincular-lote">Quitar proveedor</button>
</div>'''
text = text.replace('<div id="vista-lista">', barra + '\n<div id="vista-lista">')

# 7) add modal-proveedor-lote right before modal-minimo
modal = '''
<div class="modal-overlay" id="modal-proveedor-lote" role="dialog" aria-modal="true" aria-labelledby="modal-proveedor-lote-titulo">
<div class="modal">
<h3 id="modal-proveedor-lote-titulo">Vincular proveedor a insumos seleccionados</h3>
<p class="hint" id="modal-proveedor-lote-count"></p>
<p class="hint">Elige "Sin proveedor" para quitar el proveedor asignado a todos.</p>
<form method="post" action="<%= ctx %>/insumos<%= rolQs %>" class="form" id="form-proveedor-lote">
<input type="hidden" name="accion" value="vincularProveedorMasivo">
<div id="insumos-ids-lote"></div>
<label>Proveedor
<select name="proveedorId" id="proveedor-lote-select" required>
<option value="0">Sin proveedor</option>
<%
if (proveedores != null) {
  for (model.Proveedor p : proveedores) {
%>
<option value="<%= p.getId() %>"><%= p.getNombre() %></option>
<%
  }
}
%>
</select>
</label>
<div class="modal__acciones">
<button type="button" class="btn btn--ghost" id="modal-proveedor-lote-cancelar">Cancelar</button>
<button type="submit" class="btn btn--ok">Guardar</button>
</div>
</form>
</div>
</div>
'''
text = text.replace('<div class="modal-overlay" id="modal-minimo"', modal + '\n<div class="modal-overlay" id="modal-minimo"')

io.open(src, 'w', encoding='utf-8').write(text)
print('html patch done')
