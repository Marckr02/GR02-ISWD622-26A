package service;

import dao.InsumoDao;
import model.DetalleEntradaInsumo;
import model.Insumo;

import java.util.List;

/**
 * Logica de negocio del inventario: registro de entradas de insumos
 * (HU4) y reduccion manual de stock por mermas o desperdicios (HU5).
 */
public class InsumoService {

    private final InsumoDao insumoDao;
    private final ValidadorEntradaInsumo validador;

    public InsumoService() {
        this(new InsumoDao(), new ValidadorEntradaInsumo());
    }

    public InsumoService(InsumoDao insumoDao, ValidadorEntradaInsumo validador) {
        this.insumoDao = insumoDao;
        this.validador = validador;
    }

    public List<Insumo> listarInsumos() {
        return insumoDao.listarTodos();
    }

    public Insumo buscar(int insumoId) {
        return insumoDao.buscarPorId(insumoId);
    }

    /**
     * Reduce el stock de un insumo. La cantidad debe ser positiva y no puede
     * superar el stock disponible.
     * @throws IllegalArgumentException si el insumo no existe, la cantidad no
     *         es positiva o excede el stock actual.
     */
    public Insumo reducirStock(int insumoId, double cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a reducir debe ser mayor a cero");
        }
        Insumo insumo = insumoDao.buscarPorId(insumoId);
        if (insumo == null) {
            throw new IllegalArgumentException("No existe el insumo " + insumoId);
        }
        if (cantidad > insumo.getStock()) {
            throw new IllegalArgumentException(
                    "La cantidad a reducir excede el stock disponible");
        }
        insumo.setStock(insumo.getStock() - cantidad);
        insumoDao.actualizar(insumo);
        return insumo;
    }

    /**
     * Registra la entrada de un lote de insumos: valida los datos de la
     * compra y actualiza el stock del insumo correspondiente.
     */
    public DetalleEntradaInsumo registrarEntradaInsumos(int insumoId, double cantidad,
                                                        double costo, String ordenCompra,
                                                        String factura) {
        validador.validar(ordenCompra, factura, cantidad, costo);
        Insumo insumo = insumoDao.buscarPorId(insumoId);
        if (insumo == null) {
            throw new IllegalArgumentException("No existe el insumo " + insumoId);
        }
        return crearDetalleYActualizarStock(insumo, cantidad, costo, ordenCompra, factura);
    }

    /** Crea el detalle del lote y suma la cantidad recibida al stock. */
    public DetalleEntradaInsumo crearDetalleYActualizarStock(Insumo insumo, double cantidad,
                                                             double costo, String ordenCompra,
                                                             String factura) {
        DetalleEntradaInsumo detalle = new DetalleEntradaInsumo(
                insumo.getId(), cantidad, costo, ordenCompra, factura);
        sumarStock(insumo, cantidad);
        insumo.setCostoUnitario(costo);
        insumoDao.actualizar(insumo);
        return detalle;
    }

    /** Incrementa el stock del insumo con la cantidad recibida. */
    public void sumarStock(Insumo insumo, double cantidad) {
        insumo.setStock(insumo.getStock() + cantidad);
    }
}
