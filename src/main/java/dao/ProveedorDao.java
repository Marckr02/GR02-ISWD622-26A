package dao;

import model.Proveedor;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Persistencia en memoria de proveedores y de su vinculacion con insumos
 * del inventario. El almacen es estatico para compartir el mismo estado
 * entre peticiones del servlet (simula la BD).
 */
public class ProveedorDao {

    private static final Map<Integer, Proveedor> ALMACEN = new ConcurrentHashMap<>();
    private static final AtomicInteger SECUENCIA = new AtomicInteger(0);

    /** Vinculacion insumoId -> proveedorId (HU6). */
    private static final Map<Integer, Integer> VINCULOS = new ConcurrentHashMap<>();

    static {
        sembrar("Distribuidora Andina", "0991234567", "contacto@andina.ec");
        sembrar("AgroFresh Cia. Ltda.", "0987654321", "ventas@agrofresh.com");
    }

    private static void sembrar(String nombre, String telefono, String correo) {
        int id = SECUENCIA.incrementAndGet();
        ALMACEN.put(id, new Proveedor(id, nombre, telefono, correo));
    }

    public Proveedor guardar(Proveedor proveedor) {
        if (proveedor.getId() == 0) {
            proveedor.setId(SECUENCIA.incrementAndGet());
        }
        ALMACEN.put(proveedor.getId(), proveedor);
        return proveedor;
    }

    public Proveedor buscarPorId(int id) {
        return ALMACEN.get(id);
    }

    /** Busca un proveedor por nombre ignorando mayusculas/minusculas. */
    public Proveedor buscarPorNombre(String nombre) {
        if (nombre == null) {
            return null;
        }
        String objetivo = nombre.trim();
        return ALMACEN.values().stream()
                .filter(p -> p.getNombre().equalsIgnoreCase(objetivo))
                .findFirst()
                .orElse(null);
    }

    /** Listado ordenado alfabeticamente por nombre (HU13). */
    public List<Proveedor> listarTodos() {
        return ALMACEN.values().stream()
                .sorted(Comparator.comparing(Proveedor::getNombre, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    /** Reemplaza los datos de un proveedor existente (HU28: editar proveedor). */
    public Proveedor actualizar(Proveedor proveedor) {
        ALMACEN.put(proveedor.getId(), proveedor);
        return proveedor;
    }

    public void eliminar(int id) {
        ALMACEN.remove(id);
        VINCULOS.values().removeIf(proveedorId -> proveedorId == id);
    }

    /** Vincula un insumo con un proveedor (HU6), reemplazando el vinculo previo si existia. */
    public void vincularInsumo(int insumoId, int proveedorId) {
        VINCULOS.put(insumoId, proveedorId);
    }

    public void desvincularInsumo(int insumoId) {
        VINCULOS.remove(insumoId);
    }

    public Integer obtenerProveedorIdDeInsumo(int insumoId) {
        return VINCULOS.get(insumoId);
    }

    /** True si el proveedor tiene al menos un insumo vinculado (bloquea su eliminacion, HU25). */
    public boolean tieneInsumosVinculados(int proveedorId) {
        return VINCULOS.containsValue(proveedorId);
    }
}
