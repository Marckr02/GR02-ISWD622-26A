package dao;

import model.AlertaStock;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Persistencia en memoria del historial de alertas de stock critico.
 * Empieza vacio: las alertas se registran cuando el panel de monitoreo
 * detecta insumos por debajo de su minimo.
 */
public class AlertaStockDao {

    private static final Map<Integer, AlertaStock> ALMACEN = new ConcurrentHashMap<>();
    private static final AtomicInteger SECUENCIA = new AtomicInteger(0);

    public AlertaStock guardar(AlertaStock alerta) {
        if (alerta.getId() == 0) {
            alerta.setId(SECUENCIA.incrementAndGet());
        }
        ALMACEN.put(alerta.getId(), alerta);
        return alerta;
    }

    /** Ultima alerta registrada para un insumo, o null si nunca se registro una. */
    public AlertaStock buscarUltimaPorInsumo(int insumoId) {
        return ALMACEN.values().stream()
                .filter(a -> a.getInsumoId() == insumoId)
                .max(Comparator.comparing(AlertaStock::getTimestamp))
                .orElse(null);
    }

    /** Historial completo ordenado de la mas reciente a la mas antigua (HU11). */
    public List<AlertaStock> listarOrdenadoDesc() {
        return ALMACEN.values().stream()
                .sorted(Comparator.comparing(AlertaStock::getTimestamp).reversed())
                .collect(Collectors.toList());
    }
}
