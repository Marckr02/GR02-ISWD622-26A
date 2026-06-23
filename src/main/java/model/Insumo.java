package model;

/**
 * Insumo de bodega disponible para todas las marcas de la Dark Kitchen.
 * El stock se expresa en la unidad de medida indicada (kg, l, unidades, etc.).
 */
public class Insumo {

    private int id;
    private String nombre;
    private String unidad;
    private double stock;
    private double costoUnitario;

    public Insumo() {
    }

    public Insumo(int id, String nombre, String unidad, double stock, double costoUnitario) {
        this.id = id;
        this.nombre = nombre;
        this.unidad = unidad;
        this.stock = stock;
        this.costoUnitario = costoUnitario;
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

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public double getStock() {
        return stock;
    }

    public void setStock(double stock) {
        this.stock = stock;
    }

    public double getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(double costoUnitario) {
        this.costoUnitario = costoUnitario;
    }
}
