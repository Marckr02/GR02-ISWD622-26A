package service;

import dao.InsumoDao;
import dao.PlatoDao;
import model.DisponibilidadPlato;
import model.EstadoPlato;
import model.Insumo;
import model.Plato;

import java.util.ArrayList;
import java.util.List;

/**
 * Sincroniza la oferta del menu con el inventario (HU7): un plato esta
 * DISPONIBLE solo si todos sus ingredientes tienen stock; en caso contrario
 * queda BLOQUEADO indicando que insumos faltan.
 */
public class MenuService {

    private final PlatoDao platoDao;
    private final InsumoDao insumoDao;

    public MenuService() {
        this(new PlatoDao(), new InsumoDao());
    }

    public MenuService(PlatoDao platoDao, InsumoDao insumoDao) {
        this.platoDao = platoDao;
        this.insumoDao = insumoDao;
    }

    /**
     * Evalua un plato contra el stock actual de sus ingredientes.
     * @return DISPONIBLE si todos los insumos tienen stock; BLOQUEADO con el
     *         motivo (insumos faltantes) en caso contrario.
     */
    public DisponibilidadPlato verificarDisponibilidadPlato(Plato plato) {
        List<String> faltantes = new ArrayList<>();
        for (int insumoId : plato.getInsumoIds()) {
            Insumo insumo = insumoDao.buscarPorId(insumoId);
            if (insumo == null || insumo.getStock() <= 0) {
                faltantes.add(insumo == null ? ("insumo #" + insumoId) : insumo.getNombre());
            }
        }
        if (faltantes.isEmpty()) {
            return new DisponibilidadPlato(plato, EstadoPlato.DISPONIBLE, "");
        }
        return new DisponibilidadPlato(plato, EstadoPlato.BLOQUEADO,
                "Sin stock: " + String.join(", ", faltantes));
    }

    /**
     * Recalcula el estado de todos los platos del menu segun el inventario
     * actual. Es el endpoint de sincronizacion: al recuperar el stock del
     * ultimo ingrediente faltante, el plato vuelve a aparecer como DISPONIBLE.
     */
    public List<DisponibilidadPlato> sincronizarMenuConInventario() {
        List<DisponibilidadPlato> menu = new ArrayList<>();
        for (Plato plato : platoDao.listarTodos()) {
            menu.add(verificarDisponibilidadPlato(plato));
        }
        return menu;
    }
}
