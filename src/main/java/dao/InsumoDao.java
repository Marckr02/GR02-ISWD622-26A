package dao;

import model.Insumo;

import java.util.ArrayList;
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
        sembrar("Harina de trigo", "kg", 40.0, 0.85);
        sembrar("Queso mozzarella", "kg", 18.5, 6.20);
        sembrar("Pechuga de pollo", "kg", 25.0, 4.50);
        sembrar("Aceite vegetal", "l", 30.0, 2.10);
        sembrar("Tomate", "kg", 22.0, 1.30);
        sembrar("Pan de hamburguesa", "unidades", 120.0, 0.35);
    }

    private static void sembrar(String nombre, String unidad, double stock, double costo) {
        int id = SECUENCIA.incrementAndGet();
        ALMACEN.put(id, new Insumo(id, nombre, unidad, stock, costo));
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

    public List<Insumo> listarTodos() {
        return ALMACEN.values().stream()
                .sorted(Comparator.comparingInt(Insumo::getId))
                .collect(Collectors.toList());
    }

    public void actualizar(Insumo insumo) {
        ALMACEN.put(insumo.getId(), insumo);
    }
}
