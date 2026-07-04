package service;

/**
 * Conversion de unidades entre la unidad de una receta y la unidad base en
 * la que se almacena el insumo (tarea tecnica de F6.2). Soporta los pares
 * g<->kg y ml<->l; cualquier otro par de unidades distintas es incompatible.
 */
public class ConversionUnidades {

    /**
     * Convierte un valor de una unidad a otra.
     * @return el valor convertido; si origen y destino son iguales, el mismo valor.
     * @throws IllegalArgumentException si el par de unidades no es convertible.
     */
    public double convertir(double valor, String unidadOrigen, String unidadDestino) {
        if (unidadOrigen == null || unidadDestino == null) {
            throw new IllegalArgumentException("Las unidades de origen y destino son obligatorias");
        }
        String origen = unidadOrigen.trim().toLowerCase();
        String destino = unidadDestino.trim().toLowerCase();

        if (origen.equals(destino)) {
            return valor;
        }
        if (origen.equals("g") && destino.equals("kg")) {
            return valor / 1000.0;
        }
        if (origen.equals("kg") && destino.equals("g")) {
            return valor * 1000.0;
        }
        if (origen.equals("ml") && destino.equals("l")) {
            return valor / 1000.0;
        }
        if (origen.equals("l") && destino.equals("ml")) {
            return valor * 1000.0;
        }
        throw new IllegalArgumentException(
                "No se puede convertir de " + unidadOrigen + " a " + unidadDestino);
    }
}
