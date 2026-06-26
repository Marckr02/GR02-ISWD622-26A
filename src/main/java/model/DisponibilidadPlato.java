package model;

/**
 * Resultado de evaluar la disponibilidad de un plato: el plato, su estado
 * calculado (DISPONIBLE/BLOQUEADO) y, si esta bloqueado, el motivo con los
 * ingredientes sin stock. Es el dato que consumen las vistas del menu.
 */
public class DisponibilidadPlato {

    private final Plato plato;
    private final EstadoPlato estado;
    private final String motivo;

    public DisponibilidadPlato(Plato plato, EstadoPlato estado, String motivo) {
        this.plato = plato;
        this.estado = estado;
        this.motivo = motivo;
    }

    public Plato getPlato() {
        return plato;
    }

    public EstadoPlato getEstado() {
        return estado;
    }

    public String getMotivo() {
        return motivo;
    }

    public boolean estaBloqueado() {
        return estado == EstadoPlato.BLOQUEADO;
    }
}
