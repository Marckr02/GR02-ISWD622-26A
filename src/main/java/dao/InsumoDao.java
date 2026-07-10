package dao;

import model.Insumo;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Persistencia en memoria de insumos. El almacen es estatico para
 * compartir el mismo estado entre peticiones del servlet (simula la BD).
 */
public class InsumoDao {

    private static final Map<Integer, Insumo> ALMACEN = new ConcurrentHashMap<>();
    private static final AtomicInteger SECUENCIA = new AtomicInteger(0);

    static {
        sembrar("Harina de trigo", "kg", 40.0, 0.85, 10.0);
        sembrar("Queso mozzarella", "kg", 18.5, 6.20, 5.0);
        sembrar("Pechuga de pollo", "kg", 25.0, 4.50, 8.0);
        sembrar("Aceite vegetal", "l", 30.0, 2.10, 5.0);
        sembrar("Tomate", "kg", 22.0, 1.30, 6.0);
        sembrar("Pan de hamburguesa", "unidades", 120.0, 0.35, 20.0);
        sembrar("Albahaca fresca", "kg", 0.0, 3.00, 2.0);   // agotada: bloquea platos y es critica
        sembrar("Salsa de tomate", "l", 3.0, 1.80, 5.0);    // bajo el minimo: critica (no agotada)
    }

    private static void sembrar(String nombre, String unidad, double stock,
                                double costo, double stockMinimo) {
        int id = SECUENCIA.incrementAndGet();
        ALMACEN.put(id, new Insumo(id, nombre, unidad, stock, costo, stockMinimo));
    }

    public Insumo guardar(Insumo insumo) {
        if (insumo.getId() == 0) {
            insumo.setId(SECUENCIA.incrementAndGet());
        }
        ALMACEN.put(insumo.getId(), insumo);
        return insumo;
    }

    public Insumo buscarPorId(int id) {
        return ALMACEN.get(id);
    }

    /** Busca un insumo por nombre ignorando mayusculas/minusculas. */
    public Insumo buscarPorNombre(String nombre) {
        if (nombre == null) {
            return null;
        }
        String objetivo = nombre.trim();
        return ALMACEN.values().stream()
                .filter(i -> i.getNombre().equalsIgnoreCase(objetivo))
                .findFirst()
                .orElse(null);
    }

    /** Listado ordenado alfabeticamente por nombre, para facilitar la busqueda visual en bodega. */
    public List<Insumo> listarTodos() {
        return ALMACEN.values().stream()
                .sorted(Comparator.comparing(Insumo::getNombre, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public void actualizar(Insumo insumo) {
        ALMACEN.put(insumo.getId(), insumo);
    }

    public void eliminar(int id) {
        ALMACEN.remove(id);
    }
}