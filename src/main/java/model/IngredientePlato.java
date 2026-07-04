package model;

/**
 * Ingrediente de la receta de un plato: el insumo requerido, la cantidad y
 * la unidad de medida de la receta (que puede diferir de la unidad base en
 * la que se almacena el insumo, p. ej. receta en gramos e insumo en kg).
 * Cuando unidadReceta es null se asume la unidad propia del insumo
 * (compatibilidad con platos creados antes de esta funcionalidad).
 */
public class IngredientePlato {

    private int insumoId;
    private double cantidad;
    private String unidadReceta;

    public IngredientePlato() {
    }

    public IngredientePlato(int insumoId, double cantidad, String unidadReceta) {
        this.insumoId = insumoId;
        this.cantidad = cantidad;
        this.unidadReceta = unidadReceta;
    }

    public int getInsumoId() {
        return insumoId;
    }

    public void setInsumoId(int insumoId) {
        this.insumoId = insumoId;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public String getUnidadReceta() {
        return unidadReceta;
    }

    public void setUnidadReceta(String unidadReceta) {
        this.unidadReceta = unidadReceta;
    }
}
