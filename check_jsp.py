import sys
text = open(sys.argv[1], encoding='utf-8').read()
for token, label in [
    ('class="chk-insumo"', 'chk-insumo'),
    ('class="chk-todos"', 'chk-todos'),
    ('id="filtro-estado"', 'filtro-estado'),
    ('id="filtro-proveedor"', 'filtro-proveedor'),
    ('id="btn-limpiar-filtros"', 'limpiar-filtros'),
    ('id="modal-proveedor-lote"', 'modal-proveedor-lote'),
    ('class="vista-toggle"', 'vista-toggle'),
    ('id="vista-cuadricula"', 'vista-cuadricula'),
    ('id="vista-lista"', 'vista-lista'),
    ('id="barra-lote"', 'barra-lote'),
    ('wireModal', 'wireModal'),
    ('id="seleccionar-todos"', 'seleccionar-todos'),
    ('class="tabla-scroll"', 'tabla-scroll'),
]:
    print(label, '=>', 'ok' if token in text else 'MISSING')
