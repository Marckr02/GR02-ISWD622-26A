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

    /** Igualdad por id: dos instancias representan el mismo restaurante si vienen de la misma fila en BD. */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (!(obj instanceof Restaurante)) { return false; }
        return id == ((Restaurante) obj).id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
