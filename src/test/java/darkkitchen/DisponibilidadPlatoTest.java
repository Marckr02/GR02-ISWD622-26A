package darkkitchen;

import model.DisponibilidadPlato;
import model.EstadoPlato;
import model.Plato;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TDD (tarea tecnica model): DisponibilidadPlato expone el plato, el estado
 * y el motivo recibidos en el constructor, y estaBloqueado() refleja
 * correctamente si el estado es BLOQUEADO o DISPONIBLE.
 */
class DisponibilidadPlatoTest {

    @Test
    void estaBloqueadoEsFalsoCuandoElEstadoEsDisponible() {
        Plato plato = new Plato(1, "Plato Disponible", Collections.emptyList());
        DisponibilidadPlato disponibilidad = new DisponibilidadPlato(plato, EstadoPlato.DISPONIBLE, "");

        assertFalse(disponibilidad.estaBloqueado());
        assertEquals(plato, disponibilidad.getPlato());
        assertEquals(EstadoPlato.DISPONIBLE, disponibilidad.getEstado());
        assertEquals("", disponibilidad.getMotivo());
    }

    @Test
    void estaBloqueadoEsVerdaderoCuandoElEstadoEsBloqueado() {
        Plato plato = new Plato(2, "Plato Bloqueado", Collections.emptyList());
        DisponibilidadPlato disponibilidad = new DisponibilidadPlato(plato, EstadoPlato.BLOQUEADO, "Sin stock: Harina");

        assertTrue(disponibilidad.estaBloqueado());
        assertEquals("Sin stock: Harina", disponibilidad.getMotivo());
    }
}
