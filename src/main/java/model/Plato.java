package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Plato del menu. Su receta es la lista de insumos (por id) que necesita;
 * la disponibilidad se calcula a partir del stock de esos insumos.
 */
public class Plato {

    private int id;
    private String nombre;
    private List<Integer> insumoIds;

    public Plato() {
        this.insumoIds = new ArrayList<>();
    }

    public Plato(int id, String nombre, List<Integer> insumoIds) {
        this.id = id;
        this.nombre = nombre;
        this.insumoIds = (insumoIds == null) ? new ArrayList<>() : new ArrayList<>(insumoIds);
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

    public List<Integer> getInsumoIds() {
        return insumoIds;
    }

    public void setInsumoIds(List<Integer> insumoIds) {
        this.insumoIds = insumoIds;
    }
}
