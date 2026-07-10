package service;

import dao.AlertaStockDao;
import model.AlertaStock;
import model.Insumo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Registro y consulta del historial de alertas de stock critico (F3.3).
 * Evita duplicar una alerta cuando el insumo sigue en el mismo nivel de
 * stock que la ultima vez que se registro.
 */
public class AlertaStockService {

    private final AlertaStockDao alertaStockDao;

    public AlertaStockService() {
        this(new AlertaStockDao());
    }

    public AlertaStockService(AlertaStockDao alertaStockDao) {
        this.alertaStockDao = alertaStockDao;
    }

    /**
     * Registra una alerta para el insumo indicado con su stock actual y la
     * marca de tiempo. Si la ultima alerta registrada para ese insumo tiene
     * exactamente el mismo stock, no crea una entrada duplicada.
     */
    public AlertaStock registrarAlerta(Insumo insumo) {
        if (insumo == null) {
            throw new IllegalArgumentException("El insumo es obligatorio para registrar la alerta");
        }
        AlertaStock ultima = alertaStockDao.buscarUltimaPorInsumo(insumo.getId());
        if (ultima != null && ultima.getStockAlMomento() == insumo.getStock()) {
            return ultima;
        }
        AlertaStock alerta = new AlertaStock(0, insumo.getId(), insumo.getNombre(),
                insumo.getStock(), LocalDateTime.now());
        return alertaStockDao.guardar(alerta);
    }

    /**
     * Registra la alerta solo si el insumo esta actualmente en nivel critico
     * (sin stock o por debajo del minimo). Pensado para invocarse justo
     * despues de cualquier cambio de stock o de nivel minimo (entrada,
     * reduccion, descuento por preparacion de pedido, edicion), en vez de
     * depender de que alguien visite el panel de monitoreo.
     * @return la alerta registrada, o null si el insumo no esta en nivel critico.
     */
    public AlertaStock evaluarYRegistrar(Insumo insumo) {
        if (insumo == null || !insumo.esCritico()) {
            return null;
        }
        return registrarAlerta(insumo);
    }

    /** Historial completo, de la alerta mas reciente a la mas antigua (HU11). */
    public List<AlertaStock> listarHistorial() {
        return alertaStockDao.listarOrdenadoDesc();
    }
}
