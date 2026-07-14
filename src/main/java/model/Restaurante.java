package model;

/**
 * Marca/restaurante que opera dentro de la Dark Kitchen colaborativa.
 * Cada plato del menu pertenece a un restaurante.
 */
public class Restaurante {

    private int id;
    private String nombre;
    private String descripcion;
    private String color;

    public Restaurante() {
    }

    public Restaurante(int id, String nombre, String descripcion) {
        this(id, nombre, descripcion, null);
    }

    public Restaurante(int id, String nombre, String descripcion, String color) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.color = color;
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

    /** Color de marca en hexadecimal (ej. "#F97316"), elegido en el modal de edicion. Puede ser null. */
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
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
