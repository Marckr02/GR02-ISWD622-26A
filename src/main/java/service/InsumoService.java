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
    private static final List<String> UNIDADES_PERMITIDAS = List.of("kg", "l", "unidades");

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
        return registrarEntradaSimplificada(insumoId, cantidadTexto, null);
    }

    public Insumo registrarEntradaSimplificada(int insumoId, String cantidadTexto, String unidadTexto) {
        double cantidad = parsearCantidadPositiva(cantidadTexto);
        String unidad = normalizarUnidad(unidadTexto);
        Insumo insumo = insumoDao.buscarPorId(insumoId);
        if (insumo == null) {
            throw new IllegalArgumentException("Debe seleccionar un insumo de la lista");
        }
        String unidadFinal = (unidad == null) ? insumo.getUnidad() : unidad;
        if ("unidades".equals(unidadFinal) && cantidad != Math.rint(cantidad)) {
            throw new IllegalArgumentException("La cantidad en unidades debe ser un numero entero");
        }
        if (unidad != null && !unidad.equals(insumo.getUnidad())) {
            throw new IllegalArgumentException(
                    "La unidad seleccionada no coincide con la unidad actual del insumo");
        }
        insumo.setStock(insumo.getStock() + cantidad);
        insumoDao.actualizar(insumo);
        return insumo;
    }

    private double parsearCantidadPositiva(String cantidadTexto) {
        if (cantidadTexto == null || cantidadTexto.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingrese un numero positivo valido");
        }
        double cantidad;
        try {
            cantidad = Double.parseDouble(cantidadTexto.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Ingrese un numero positivo valido");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        return cantidad;
    }

    private String normalizarUnidad(String unidadTexto) {
        if (unidadTexto == null || unidadTexto.trim().isEmpty()) {
            return null;
        }
        String unidad = unidadTexto.trim().toLowerCase();
        if (!UNIDADES_PERMITIDAS.contains(unidad)) {
            throw new IllegalArgumentException("Seleccione una unidad valida");
        }
        return unidad;
    }

    /**
     * Crea un insumo nuevo con stock inicial en cero (HU23). El nombre debe
     * tener entre 2 y 100 caracteres alfanumericos (letras, numeros, espacios
     * y guiones) y no puede duplicar uno existente (sin distincion de mayusculas).
     */
    public Insumo crearInsumo(String nombre) {
        return crearInsumo(nombre, "unidades");
    }

    public Insumo crearInsumo(String nombre, String unidadTexto) {
        String limpio = (nombre == null) ? "" : nombre.trim();
        String unidad = requerirUnidad(unidadTexto);
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
        return insumoDao.guardar(new Insumo(0, limpio, unidad, 0.0, 0.0, 0.0));
    }

    public Insumo editarInsumo(int insumoId, String nombre, String unidadTexto) {
        Insumo insumo = insumoDao.buscarPorId(insumoId);
        if (insumo == null) {
            throw new IllegalArgumentException("No existe el insumo " + insumoId);
        }
        String limpio = (nombre == null) ? "" : nombre.trim();
        String unidad = requerirUnidad(unidadTexto);
        if (limpio.isEmpty()) {
            throw new IllegalArgumentException("El nombre del insumo no puede estar vacio");
        }
        if (!limpio.matches(PATRON_NOMBRE)) {
            throw new IllegalArgumentException(
                    "El nombre solo puede contener letras, numeros, espacios y guiones, "
                            + "con un maximo de 100 caracteres");
        }
        Insumo duplicado = insumoDao.buscarPorNombre(limpio);
        if (duplicado != null && duplicado.getId() != insumoId) {
            throw new IllegalArgumentException("Ya existe un insumo con ese nombre");
        }
        if ("unidades".equals(unidad) && insumo.getStock() != Math.rint(insumo.getStock())) {
            throw new IllegalArgumentException(
                    "No se puede cambiar a unidades porque el stock actual tiene decimales");
        }
        insumo.setNombre(limpio);
        insumo.setUnidad(unidad);
        insumoDao.actualizar(insumo);
        return insumo;
    }

    private String requerirUnidad(String unidadTexto) {
        String unidad = normalizarUnidad(unidadTexto);
        if (unidad == null) {
            throw new IllegalArgumentException("Seleccione una unidad valida");
        }
        return unidad;
    }
}
