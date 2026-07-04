package dao;

import model.IngredientePlato;
import model.Plato;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Persistencia en memoria de los platos del menu y su receta (insumos con
 * cantidad y unidad). El almacen es estatico para compartir el estado entre
 * peticiones del servlet (simula la BD). Los ids de insumo coinciden con la
 * semilla de InsumoDao; los ids de restaurante con la semilla de RestauranteDao.
 */
public class PlatoDao {

    private static final Map<Integer, Plato> ALMACEN = new ConcurrentHashMap<>();
    private static final AtomicInteger SECUENCIA = new AtomicInteger(0);

    static {
        // Insumos semilla: 1 Harina,2 Queso,3 Pollo,4 Aceite,5 Tomate,6 Pan,
        // 7 Albahaca(stock 0),8 Salsa de tomate(bajo el minimo).
        // Restaurantes semilla: 1 Napoli,2 Burger Lab,3 Sakura,4 El Fogon.
        sembrar("Pizza Margarita", 1, List.of(
                ing(1, 300, "g"), ing(2, 200, "g"), ing(5, 100, "g"), ing(7, 10, "g")));
        sembrar("Hamburguesa Clasica", 2, List.of(
                ing(6, 1, "unidades"), ing(3, 150, "g"), ing(5, 50, "g")));
        sembrar("Pollo a la plancha", 1, List.of(
                ing(3, 250, "g"), ing(4, 20, "ml")));
        sembrar("Pasta Pomodoro", 3, List.of(
                ing(1, 200, "g"), ing(8, 100, "ml")));
    }

    private static IngredientePlato ing(int insumoId, double cantidad, String unidad) {
        return new IngredientePlato(insumoId, cantidad, unidad);
    }

    private static void sembrar(String nombre, int restauranteId, List<IngredientePlato> ingredientes) {
        int id = SECUENCIA.incrementAndGet();
        ALMACEN.put(id, new Plato(id, nombre, restauranteId, ingredientes));
    }

    public Plato guardar(Plato plato) {
        if (plato.getId() == 0) {
            plato.setId(SECUENCIA.incrementAndGet());
        }
        ALMACEN.put(plato.getId(), plato);
        return plato;
    }

    public Plato buscarPorId(int id) {
        return ALMACEN.get(id);
    }

    /** Busca un plato por nombre ignorando mayusculas/minusculas. */
    public Plato buscarPorNombre(String nombre) {
        if (nombre == null) {
            return null;
        }
        String objetivo = nombre.trim();
        return ALMACEN.values().stream()
                .filter(p -> p.getNombre().equalsIgnoreCase(objetivo))
                .findFirst()
                .orElse(null);
    }

    public List<Plato> listarTodos() {
        return ALMACEN.values().stream()
                .sorted(Comparator.comparingInt(Plato::getId))
                .collect(Collectors.toList());
    }

    public void actualizar(Plato plato) {
        ALMACEN.put(plato.getId(), plato);
    }

    public void eliminar(int id) {
        ALMACEN.remove(id);
    }

    /** True si algun plato pertenece al restaurante indicado (bloquea su eliminacion, HU29). */
    public boolean existePlatoConRestaurante(int restauranteId) {
        return ALMACEN.values().stream().anyMatch(p -> p.getRestauranteId() == restauranteId);
    }
}
