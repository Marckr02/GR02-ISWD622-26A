package service;

import dao.InsumoDao;
import model.DetalleEntradaInsumo;
import model.Insumo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Logica de negocio del inventario: registro de entradas (completa y
 * simplificada), reduccion de stock, creacion de insumos y consulta de
 * insumos criticos para el panel de monitoreo.
 */
public class InsumoService {

    private static final String PATRON_NOMBRE = "[\\p{L}\\p{N} \\-]{2,100}";

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

    /** Insumos cuyo stock actual esta por debajo del minimo configurado (HU9). */
    public List<Insumo> listarInsumosCriticos() {
        return insumoDao.listarTodos().stream()
                .filter(Insumo::esCritico)
                .collect(Collectors.toList());
    }

    /**
     * Reduce el stock de un insumo. La cantidad debe ser positiva y no puede
     * superar el stock disponible.
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
     * Registra una entrada completa de un lote (con orden de compra y factura)
     * y actualiza el stock del insumo correspondiente.
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

    /**
     * Registra una entrada simplificada (HU21): solo insumo y cantidad. La
     * cantidad debe ser un numero entero positivo mayor a cero.
     * @throws IllegalArgumentException con el mensaje correspondiente si la
     *         cantidad no es valida o el insumo no fue seleccionado/existe.
     */
    public Insumo registrarEntradaSimplificada(int insumoId, String cantidadTexto) {
        int cantidad = parsearCantidadEntera(cantidadTexto);
        Insumo insumo = insumoDao.buscarPorId(insumoId);
        if (insumo == null) {
            throw new IllegalArgumentException("Debe seleccionar un insumo de la lista");
        }
        insumo.setStock(insumo.getStock() + cantidad);
        insumoDao.actualizar(insumo);
        return insumo;
    }

    private int parsearCantidadEntera(String cantidadTexto) {
        if (cantidadTexto == null || cantidadTexto.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingrese un numero entero positivo valido");
        }
        int cantidad;
        try {
            cantidad = Integer.parseInt(cantidadTexto.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Ingrese un numero entero positivo valido");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser un numero entero positivo mayor a cero");
        }
        return cantidad;
    }

    /**
     * Crea un insumo nuevo con stock inicial en cero (HU23). El nombre debe
     * tener entre 2 y 100 caracteres alfanumericos (letras, numeros, espacios
     * y guiones) y no puede duplicar uno existente (sin distincion de mayusculas).
     */
    public Insumo crearInsumo(String nombre) {
        String limpio = (nombre == null) ? "" : nombre.trim();
        if (limpio.isEmpty()) {
            throw new IllegalArgumentException("El nombre del insumo no puede estar vacio");
        }
        if (!limpio.matches(PATRON_NOMBRE)) {
            throw new IllegalArgumentException(
                    "El nombre solo puede contener letras, numeros, espacios y guiones, "
                            + "con un maximo de 100 caracteres");
        }
        if (insumoDao.buscarPorNombre(limpio) != null) {
            throw new IllegalArgumentException("Ya existe un insumo con ese nombre");
        }
        return insumoDao.guardar(new Insumo(0, limpio, "unidades", 0.0, 0.0, 0.0));
    }
}
