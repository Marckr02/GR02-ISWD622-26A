import io, sys
src = sys.argv[1]
text = io.open(src, encoding='utf-8').read()

# ---- 1) HEADER: quitar columna checkbox aparte, poner "select all" EN la celda Insumo ----
old_header = '''<tr>
<th><input type="checkbox" class="chk-todos" id="seleccionar-todos" aria-label="Seleccionar todos"></th>
<th>Insumo</th>
<th>Unidad</th>'''
new_header = '''<tr>
<th>Insumo</th>
<th>Unidad</th>'''
text = text.replace(old_header, new_header)

# ---- 2) FILA TABLA: mover checkbox DENTRO de la celda nombre + iconos en botones ----
old_row = '''<tr data-estado="<%= estado %>" data-proveedor-id="<%= proveedor != null ? proveedor.getId() : 0 %>">

<td><input type="checkbox" class="chk-insumo" value="<%= insumo.getId() %>" aria-label="Seleccionar <%= insumo.getNombre() %>"></td>
<td><%= insumo.getNombre() %></td>'''
new_row = '''<tr data-estado="<%= estado %>" data-proveedor-id="<%= proveedor != null ? proveedor.getId() : 0 %>">

<td style="display:flex;align-items:center;gap:.65rem;">
<label style="display:flex;align-items:center;gap:.4rem;cursor:pointer;margin:0;white-space:nowrap;" title="Seleccionar <%= insumo.getNombre() %>">
<input type="checkbox" class="chk-insumo" value="<%= insumo.getId() %>" aria-label="Seleccionar <%= insumo.getNombre() %>">
<span class="status-dot status-dot--<%= estado %>"></span>
<%= insumo.getNombre() %>
</label>
</td>'''
text = text.replace(old_row, new_row)

# ---- 3) HEADER TABLA: agregar checkbox "seleccionar todo" dentro del th Insumo ----
old_inner_th = '<th>Insumo</th>\n<th>Unidad</th>'
new_inner_th = '<th style="display:flex;align-items:center;gap:.55rem;">\n<label style="display:flex;align-items:center;gap:.35rem;cursor:pointer;margin:0;" title="Seleccionar todos">\n<input type="checkbox" class="chk-todos" id="seleccionar-todos" aria-label="Seleccionar todos">\n📋\n</label>\nInsumo\n</th>\n<th>Unidad</th>'
text = text.replace(old_inner_th, new_inner_th)

# ---- 4) TARJETAS: icono de status ya está, agregar emoji en botones ----
text = text.replace(
    '>Proveedor</button>',
    '>🏭 Proveedor</button>', 1
)
text = text.replace(
    '>Nivel minimo</button>',
    '>📊 Nivel mínimo</button>', 1
)

# ---- 5) BOTONES DE ACCIONES EN TABLA: iconos ----
text = text.replace('>Proveedor</button>', '>🏭 Proveedor</button>')
text = text.replace('>Nivel minimo</button>', '>📊 Nivel mínimo</button>')

# ---- 6) BOTONES BARRA LOTE: iconos ----
text = text.replace(
    'id="btn-vincular-lote">Vincular proveedor</button>',
    'id="btn-vincular-lote">🔗 Vincular proveedor</button>'
)
text = text.replace(
    'id="btn-desvincular-lote">Quitar proveedor</button>',
    'id="btn-desvincular-lote">❌ Quitar proveedor</button>'
)

# ---- 7) FILTROS: iconos en labels ----
text = text.replace(
    'placeholder="Buscar insumo..." aria-label="Buscar insumo por nombre"',
    'placeholder="🔍 Buscar insumo..." aria-label="Buscar insumo por nombre"'
)
text = text.replace(
    '<option value="">Todos los estados</option>',
    '<option value="">📊 Todos los estados</option>'
)
text = text.replace(
    '<option value="0">Sin proveedor</option>',
    '<option value="0">🏭 Sin proveedor</option>', 1
)

# ---- 8) VISTA TOGGLE: iconos en botones ----
text = text.replace(
    '>Lista</button>',
    '>📋 Lista</button>'
)
text = text.replace(
    '>Cuadricula</button>',
    '>⊞ Cuadrícula</button>'
)

# ---- 9) BOTON LIMPIAR FILTROS: icono ----
text = text.replace(
    'id="btn-limpiar-filtros">Limpiar filtros</button>',
    'id="btn-limpiar-filtros">🔄 Limpiar filtros</button>'
)

# ---- 10) MODAL LOTE: iconos ----
text = text.replace(
    'Vincular proveedor a insumos seleccionados</h3>',
    '🔗 Vincular proveedor a insumos seleccionados</h3>'
)
text = text.replace(
    'Elige "Sin proveedor" para quitar el proveedor asignado a todos.</p>',
    'ℹ️ Elige "Sin proveedor" para quitar el proveedor asignado a todos.</p>'
)

# ---- 11) MODAL PROVEEDOR INDIVIDUAL: iconos ----
text = text.replace(
    '<h3 id="modal-proveedor-titulo">Proveedor del insumo</h3>',
    '<h3 id="modal-proveedor-titulo">🏭 Proveedor del insumo</h3>'
)
text = text.replace(
    '<p class="hint">Elige "Sin proveedor" para quitar el proveedor asignado.</p>',
    '<p class="hint">ℹ️ Elige "Sin proveedor" para quitar el proveedor asignado.</p>'
)

# ---- 12) MODAL MINIMO: icono ----
text = text.replace(
    '<h3 id="modal-minimo-titulo">Nivel minimo de stock</h3>',
    '<h3 id="modal-minimo-titulo">📊 Nivel mínimo de stock</h3>'
)

io.open(src, 'w', encoding='utf-8').write(text)
print('icon+checkbox patch done')
