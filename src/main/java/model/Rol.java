package model;

/**
 * Roles validos del sistema (Tarea Tecnica de correccion de roles). El sistema
 * cuenta unicamente con estos tres roles; cualquier referencia a roles
 * inexistentes (p. ej. "repartidor") queda eliminada.
 */
public enum Rol {

    COCINERO("Cocinero"),
    ADMIN_BODEGA("Administrador de bodega"),
    ADMINISTRADOR("Administrador");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /**
     * Convierte un texto al rol correspondiente de forma tolerante (ignora
     * mayusculas/minusculas y espacios). Devuelve null si no coincide con
     * ninguno de los tres roles validos.
     */
    public static Rol desde(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim().toUpperCase().replace(' ', '_');
        for (Rol rol : values()) {
            if (rol.name().equals(limpio)) {
                return rol;
            }
        }
        return null;
    }
}
