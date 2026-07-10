import io, sys, re
src = sys.argv[1]
text = io.open(src, encoding='utf-8').read()

def svgIcon(paths, size=16, extra=''):
    return '<svg class="icono" width="%d" height="%d" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"%s>%s</svg>' % (size, size, extra, paths)

icons = {
    'search': svgIcon('<circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/>'),
    'sliders': svgIcon('<line x1="4" x2="4" y1="21" y2="14"/><line x1="4" x2="4" y1="10" y2="3"/><line x1="12" x2="12" y1="21" y2="12"/><line x1="12" x2="12" y1="8" y2="3"/><line x1="20" x2="20" y1="21" y2="16"/><line x1="20" x2="20" y1="12" y2="3"/><line x1="1" x2="7" y1="14" y2="14"/><line x1="9" x2="15" y1="8" y2="8"/><line x1="17" x2="23" y1="16" y2="16"/>'),
    'building': svgIcon('<path d="M6 22V4a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v18Z"/><path d="M6 12H4a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h2"/><path d="M18 9h2a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2h-2"/><path d="M10 6h4"/><path d="M10 10h4"/><path d="M10 14h4"/><path d="M10 18h4"/>'),
    'link': svgIcon('<path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>'),
    'unlink': svgIcon('<path d="m9 15-6-6 6-6"/>'),
    'x_circle': svgIcon('<circle cx="12" cy="12" r="10"/><path d="m15 9-6 6"/><path d="m9 9 6 6"/>'),
    'list': svgIcon('<line x1="8" x2="21" y1="6" y2="6"/><line x1="8" x2="21" y1="12" y2="12"/><line x1="8" x2="21" y1="18" y2="18"/><line x1="3" x2="3.01" y1="6" y2="6"/><line x1="3" x2="3.01" y1="12" y2="12"/><line x1="3" x2="3.01" y1="18" y2="18"/>'),
    'grid': svgIcon('<rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/>'),
    'refresh': svgIcon('<path d="M21 12a9 9 0 0 0-9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/><path d="M3 12a9 9 0 0 0 9 9 9.75 9.75 0 0 0 6.74-2.74L21 16"/><path d="M16 16h5v5"/>'),
    'info': svgIcon('<circle cx="12" cy="12" r="10"/><path d="M12 16v-4"/><path d="M12 8h.01"/>'),
    'clipboard': svgIcon('<rect x="8" y="2" width="8" height="4" rx="1" ry="1"/><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><path d="M12 11h4"/><path d="M12 16h4"/><path d="M12 7h1"/>'),
    'check_all': svgIcon('<path d="m9 11 3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>'),
}

replacements = [
    ('🔍 Buscar insumo...', '%s Buscar...' % icons['search']),
    ('📊 Todos los estados', '%s Todos los estados' % icons['sliders']),
    ('🏭 Sin proveedor', '%s Sin proveedor' % icons['building']),
    ('🏭 Proveedor</button>', '%s Proveedor</button>' % icons['building']),
    ('📊 Nivel mínimo</button>', '%s Nivel mínimo</button>' % icons['sliders']),
    ('🔗 Vincular proveedor</button>', '%s Vincular proveedor</button>' % icons['link']),
    ('❌ Quitar proveedor</button>', '%s Quitar proveedor</button>' % icons['unlink']),
    ('📋 Lista</button>', '%s Lista</button>' % icons['list']),
    ('⊞ Cuadrícula</button>', '%s Cuadrícula</button>' % icons['grid']),
    ('🔄 Limpiar filtros</button>', '%s Limpiar filtros</button>' % icons['refresh']),
    ('ℹ️ Elige "Sin proveedor"', '%s Elige "Sin proveedor"' % icons['info']),
    ('🏭 Proveedor del insumo</h3>', '%s Proveedor del insumo</h3>' % icons['building']),
    ('📊 Nivel mínimo de stock</h3>', '%s Nivel mínimo de stock</h3>' % icons['sliders']),
    ('📋</label>', '%s</label>' % icons['clipboard']),
]

for old, new in replacements:
    text = text.replace(old, new, 1)

# add .icono style
if '.icono {' not in text:
    text = text.replace('</style>', '.icono { width: 16px; height: 16px; vertical-align: middle; margin-right: .3rem; flex-shrink: 0; }\n</style>', 1)

io.open(src, 'w', encoding='utf-8').write(text)
print('svg icons patch done')
