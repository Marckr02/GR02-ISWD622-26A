import io,sys
src=sys.argv[1]
text=io.open(src,encoding='utf-8').read()

# remove duplicate checkbox td from rows
# pattern: a checkbox td is followed immediately by another checkbox td with the same content
clean=[]
i=0
while i < len(text):
    if text.startswith('<td><input type="checkbox" class="chk-insumo"', i) and i > 0:
        prev = text[i:].split('</td>',1)
        line1 = text[i:i+len(prev[0])+5]
        rest = text[i+len(prev[0])+5:]
        if rest.startswith('\n<td><input type="checkbox" class="chk-insumo"'):
            prev2 = rest.split('</td>',1)
            # skip the next checkbox, it is the duplicate
            i += len(prev[0]) + 5 + len(prev2[0]) + 5
            continue
    clean.append(text[i])
    i += 1
text = ''.join(clean)

# fix double tabla-scroll
text = text.replace('<div class="tabla-scroll">\n<div class="tabla-scroll">', '<div class="tabla-scroll">\n')

io.open(src,'w',encoding='utf-8').write(text)
print('cleanup done')
