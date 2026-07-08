package service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Redondeo de cantidades de stock a 2 decimales para evitar el ruido de
 * precision del punto flotante en sumas/restas sucesivas (p. ej.
 * 18.5 - 18.4 en double da 0.10000000000000142 en lugar de 0.10).
 */
final class Numeros {

    private Numeros() {
    }

    static double redondear(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
