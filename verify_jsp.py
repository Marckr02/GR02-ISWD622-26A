import io
text = io.open(r'D:\SEMESTRE_2026-A\METODOLOGIAS_AGILES\GR02-ISWD622-26A\src\main\webapp\views\cu3-insumos-entrada.jsp', encoding='utf-8').read()
print('barra-lote ids:', text.count('id="barra-lote"'))
print('modal-proveedor-lote ids:', text.count('id="modal-proveedor-lote"'))
print('icono class count:', text.count('class="icono"'))
print('has unchecked emojis:', '🏭' in text or '📊' in text or '🔗' in text)
