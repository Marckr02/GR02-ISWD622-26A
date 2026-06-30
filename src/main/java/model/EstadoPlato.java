package model;

/**
 * Estado de un plato del menu en funcion del stock de sus ingredientes:
 * DISPONIBLE cuando todos tienen stock, BLOQUEADO cuando falta alguno.
 */
public enum EstadoPlato {

    DISPONIBLE("Disponible"),
    BLOQUEADO("Bloqueado");

    private final String etiqueta;

    EstadoPlato(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
