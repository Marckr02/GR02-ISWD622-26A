package util;

/**
 * Paleta de colores de marca (restaurantes) compartida por todo el sistema:
 * el listado de restaurantes, el respaldo para restaurantes sin color propio
 * asignado y el backfill de arranque en {@link dao.ConexionBD} usan esta
 * misma logica, para que un restaurante siempre reciba el mismo color
 * determinista en cualquier vista mientras no se le asigne uno manualmente
 * desde el selector de color del modal de edicion.
 */
public final class ColorMarca {

    private static final String[] PALETA = {
        "#F97316", "#38BDF8", "#A78BFA", "#34D399", "#F472B6",
        "#FACC15", "#FB7185", "#22D3EE", "#C084FC", "#4ADE80"
    };

    private static final String TEXTO_CLARO = "#FFFFFF";
    private static final String TEXTO_OSCURO = "#1A1A1A";

    /** Umbral de brillo percibido (formula W3C: 0-255) a partir del cual el fondo
     *  se considera "claro" y por lo tanto necesita texto oscuro encima. */
    private static final double UMBRAL_BRILLO = 150.0;

    private ColorMarca() {
    }

    /** Color hexadecimal estable derivado del nombre (mismo nombre => mismo color siempre). */
    public static String paraNombre(String nombre) {
        if (nombre == null) {
            return PALETA[0];
        }
        int hash = 0;
        for (int i = 0; i < nombre.length(); i++) {
            hash = hash * 31 + nombre.charAt(i);
        }
        return PALETA[Math.abs(hash) % PALETA.length];
    }

    /**
     * Color de texto (blanco o gris muy oscuro) que mejor contrasta sobre el color
     * de fondo dado, segun su brillo percibido. Algunos colores de la paleta (verdes
     * y morados oscuros) no se leen bien con tinta negra fija; este calculo evita
     * ese problema sin depender de que cada vista elija el color de texto a mano.
     */
    public static String textoLegibleSobre(String colorHex) {
        if (colorHex == null || colorHex.length() != 7 || colorHex.charAt(0) != '#') {
            return TEXTO_CLARO;
        }
        try {
            int r = Integer.parseInt(colorHex.substring(1, 3), 16);
            int g = Integer.parseInt(colorHex.substring(3, 5), 16);
            int b = Integer.parseInt(colorHex.substring(5, 7), 16);
            double brillo = (r * 299 + g * 587 + b * 114) / 1000.0;
            return brillo > UMBRAL_BRILLO ? TEXTO_OSCURO : TEXTO_CLARO;
        } catch (NumberFormatException ex) {
            return TEXTO_CLARO;
        }
    }
}
