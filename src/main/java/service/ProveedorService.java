package service;

import dao.InsumoDao;
import dao.ProveedorDao;
import model.Insumo;
import model.Proveedor;

import java.util.List;

/**
 * Logica de negocio de proveedores (F4.1) y de su vinculacion con insumos
 * del inventario (F4.2).
 */
public class ProveedorService {

    private static final String PATRON_NOMBRE = "[\\p{L}\\p{N} \\-]{2,100}";
    private static final String PATRON_TELEFONO = "\\d{7,15}";
    private static final String PATRON_CORREO = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";

    private final ProveedorDao proveedorDao;
    private final InsumoDao insumoDao;

    public ProveedorService() {
        this(new ProveedorDao(), new InsumoDao());
    }

    public ProveedorService(ProveedorDao proveedorDao, InsumoDao insumoDao) {
        this.proveedorDao = proveedorDao;
        this.insumoDao = insumoDao;
    }

    public List<Proveedor> listarProveedores() {
        return proveedorDao.listarTodos();
    }

    public Proveedor buscar(int id) {
        return proveedorDao.buscarPorId(id);
    }

    /**
     * Registra un proveedor nuevo (HU12). Valida nombre (2-100 caracteres,
     * unico, sin caracteres especiales), telefono (7-15 digitos) y correo
     * (usuario@dominio.ext).
     */
    public Proveedor registrarProveedor(String nombre, String telefono, String correo) {
        String nombreLimpio = validarNombre(nombre, -1);
        String telefonoLimpio = validarTelefono(telefono);
        String correoLimpio = validarCorreo(correo);
        return proveedorDao.guardar(new Proveedor(0, nombreLimpio, telefonoLimpio, correoLimpio));
    }

    /**
     * Elimina un proveedor (HU25).
     * @throws IllegalArgumentException si el proveedor no existe.
     * @throws IllegalStateException    si tiene insumos vinculados.
     */
    public void eliminarProveedor(int id) {
        Proveedor proveedor = proveedorDao.buscarPorId(id);
        if (proveedor == null) {
            throw new IllegalArgumentException("El proveedor indicado no existe en el sistema");
        }
        if (proveedorDao.tieneInsumosVinculados(id)) {
            throw new IllegalStateException(
                    "No se puede eliminar este proveedor porque esta vinculado a insumos del inventario. "
                            + "Desvincule los insumos antes de eliminarlo");
        }
        proveedorDao.eliminar(id);
    }

    /**
     * Vincula un proveedor a un insumo del inventario (HU6).
     * @throws IllegalArgumentException si el insumo o el proveedor no existen.
     */
    public void vincularAInsumo(int insumoId, int proveedorId) {
        Insumo insumo = insumoDao.buscarPorId(insumoId);
        Proveedor proveedor = proveedorDao.buscarPorId(proveedorId);
        if (insumo == null || proveedor == null) {
            throw new IllegalArgumentException("El insumo o el proveedor indicado no existe en el sistema");
        }
        proveedorDao.vincularInsumo(insumoId, proveedorId);
    }

    /** Proveedor vinculado al insumo indicado, o null si no tiene ninguno asociado. */
    public Proveedor obtenerProveedorDe(int insumoId) {
        Integer proveedorId = proveedorDao.obtenerProveedorIdDeInsumo(insumoId);
        return (proveedorId == null) ? null : proveedorDao.buscarPorId(proveedorId);
    }

    /**
     * Quita la vinculacion de proveedor de un insumo, dejandolo sin proveedor
     * asignado (HU6: un insumo puede no tener proveedor).
     * @throws IllegalArgumentException si el insumo no existe.
     */
    public void desvincularDeInsumo(int insumoId) {
        if (insumoDao.buscarPorId(insumoId) == null) {
            throw new IllegalArgumentException("El insumo indicado no existe en el sistema");
        }
        proveedorDao.desvincularInsumo(insumoId);
    }

    private String validarNombre(String nombre, int idActual) {
        String limpio = (nombre == null) ? "" : nombre.trim();
        if (limpio.isEmpty()) {
            throw new IllegalArgumentException("El nombre del proveedor no puede estar vacio");
        }
        if (!limpio.matches(PATRON_NOMBRE)) {
            throw new IllegalArgumentException(
                    "El nombre solo puede contener letras, numeros, espacios y guiones, "
                            + "con un maximo de 100 caracteres");
        }
        Proveedor duplicado = proveedorDao.buscarPorNombre(limpio);
        if (duplicado != null && duplicado.getId() != idActual) {
            throw new IllegalArgumentException("Ya existe un proveedor con ese nombre");
        }
        return limpio;
    }

    private String validarTelefono(String telefono) {
        String limpio = (telefono == null) ? "" : telefono.trim();
        if (!limpio.matches(PATRON_TELEFONO)) {
            throw new IllegalArgumentException("El telefono debe contener solo numeros, entre 7 y 15 digitos");
        }
        return limpio;
    }

    private String validarCorreo(String correo) {
        String limpio = (correo == null) ? "" : correo.trim();
        if (!limpio.matches(PATRON_CORREO)) {
            throw new IllegalArgumentException("Ingrese un correo electronico valido (usuario@dominio.ext)");
        }
        return limpio;
    }
}
