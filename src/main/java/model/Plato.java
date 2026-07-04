package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Plato del menu. Pertenece a un restaurante y su receta es la lista de
 * ingredientes (insumo + cantidad + unidad) que necesita; la disponibilidad
 * se calcula a partir del stock de esos insumos.
 *
 * El constructor con {@code List<Integer>} se conserva por compatibilidad
 * con los platos creados en Sprint 1/2 (sin restaurante ni cantidades):
 * cada insumo se interpreta con cantidad 1 en su propia unidad.
 */
public class Plato {

    private int id;
    private String nombre;
    private int restauranteId;
    private List<IngredientePlato> ingredientes;

    public Plato() {
        this.ingredientes = new ArrayList<>();
    }

    /** Constructor legado (Sprint 1/2): receta simple sin cantidad ni restaurante. */
    public Plato(int id, String nombre, List<Integer> insumoIds) {
        this.id = id;
        this.nombre = nombre;
        this.restauranteId = 0;
        this.ingredientes = new ArrayList<>();
        if (insumoIds != null) {
            for (Integer insumoId : insumoIds) {
                this.ingredientes.add(new IngredientePlato(insumoId, 1.0, null));
            }
        }
    }

    /** Constructor completo (Sprint 3): receta con cantidades, unidades y restaurante. */
    public Plato(int id, String nombre, int restauranteId, List<IngredientePlato> ingredientes) {
        this.id = id;
        this.nombre = nombre;
        this.restauranteId = restauranteId;
        this.ingredientes = (ingredientes == null) ? new ArrayList<>() : new ArrayList<>(ingredientes);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getRestauranteId() {
        return restauranteId;
    }

    public void setRestauranteId(int restauranteId) {
        this.restauranteId = restauranteId;
    }

    public List<IngredientePlato> getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(List<IngredientePlato> ingredientes) {
        this.ingredientes = ingredientes;
    }

    /** Ids de los insumos de la receta (derivado), usado por MenuService. */
    public List<Integer> getInsumoIds() {
        List<Integer> ids = new ArrayList<>();
        for (IngredientePlato ingrediente : ingredientes) {
            ids.add(ingrediente.getInsumoId());
        }
        return ids;
    }

    public void setInsumoIds(List<Integer> insumoIds) {
        this.ingredientes = new ArrayList<>();
        if (insumoIds != null) {
            for (Integer insumoId : insumoIds) {
                this.ingredientes.add(new IngredientePlato(insumoId, 1.0, null));
            }
        }
    }
}
