package dao;

import model.Restaurante;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Persistencia en memoria de restaurantes (marcas) de la Dark Kitchen.
 * El almacen es estatico para compartir el mismo estado entre peticiones
 * del servlet (simula la BD). Los ids sembrados aqui (1..4) son los que
 * usa la semilla de PlatoDao para asociar cada plato a su restaurante.
 */
public class RestauranteDao {

    private static final Map<Integer, Restaurante> ALMACEN = new ConcurrentHashMap<>();
    private static final AtomicInteger SECUENCIA = new AtomicInteger(0);

    static {
        sembrar("Napoli", "Pizzeria artesanal de horno de lena.");
        sembrar("Burger Lab", "Hamburguesas gourmet y experimentales.");
        sembrar("Sakura", "Cocina japonesa: sushi y roles.");
        sembrar("El Fogon", "Comida mexicana tradicional.");
    }

    private static void sembrar(String nombre, String descripcion) {
        int id = SECUENCIA.incrementAndGet();
        ALMACEN.put(id, new Restaurante(id, nombre, descripcion));
    }

    public Restaurante guardar(Restaurante restaurante) {
        if (restaurante.getId() == 0) {
            restaurante.setId(SECUENCIA.incrementAndGet());
        }
        ALMACEN.put(restaurante.getId(), restaurante);
        return restaurante;
    }

    public Restaurante buscarPorId(int id) {
        return ALMACEN.get(id);
    }

    /** Busca un restaurante por nombre ignorando mayusculas/minusculas. */
    public Restaurante buscarPorNombre(String nombre) {
        if (nombre == null) {
            return null;
        }
        String objetivo = nombre.trim();
        return ALMACEN.values().stream()
                .filter(r -> r.getNombre().equalsIgnoreCase(objetivo))
                .findFirst()
                .orElse(null);
    }

    /** Listado ordenado alfabeticamente por nombre (HU27). */
    public List<Restaurante> listarTodos() {
        return ALMACEN.values().stream()
                .sorted(Comparator.comparing(Restaurante::getNombre, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public void actualizar(Restaurante restaurante) {
        ALMACEN.put(restaurante.getId(), restaurante);
    }

    public void eliminar(int id) {
        ALMACEN.remove(id);
    }
}
