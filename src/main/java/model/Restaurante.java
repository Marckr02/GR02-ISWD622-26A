package model;

/**
 * Marca/restaurante que opera dentro de la Dark Kitchen colaborativa.
 * Cada plato del menu pertenece a un restaurante.
 */
public class Restaurante {

    private int id;
    private String nombre;
    private String descripcion;

    public Restaurante() {
    }

    public Restaurante(int id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
