package dao;

import model.Plato;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Persistencia en memoria de los platos del menu y su receta (insumos que
 * requieren). El almacen es estatico para compartir el estado entre
 * peticiones del servlet (simula la BD). Los ids de insumo coinciden con la
 * semilla de InsumoDao.
 */
public class PlatoDao {

    private static final Map<Integer, Plato> ALMACEN = new ConcurrentHashMap<>();
    private static final AtomicInteger SECUENCIA = new AtomicInteger(0);

    static {
        // Insumos semilla: 1 Harina, 2 Queso, 3 Pollo, 4 Aceite, 5 Tomate,
        // 6 Pan, 7 Albahaca (stock 0), 8 Salsa de tomate (bajo el minimo).
        sembrar("Pizza Margarita", 1, 2, 5, 7);     // requiere albahaca agotada -> BLOQUEADO
        sembrar("Hamburguesa Clasica", 6, 3, 5);    // todos con stock -> DISPONIBLE
        sembrar("Pollo a la plancha", 3, 4);        // todos con stock -> DISPONIBLE
        sembrar("Pasta Pomodoro", 1, 8);            // salsa con stock bajo pero > 0 -> DISPONIBLE
    }

    private static void sembrar(String nombre, Integer... insumoIds) {
        int id = SECUENCIA.incrementAndGet();
        ALMACEN.put(id, new Plato(id, nombre, Arrays.asList(insumoIds)));
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

    public List<Plato> listarTodos() {
        return ALMACEN.values().stream()
                .sorted(Comparator.comparingInt(Plato::getId))
                .collect(Collectors.toList());
    }
}
