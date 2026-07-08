package darkkitchen;

import model.EstadoPlato;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * TDD (tarea tecnica model): EstadoPlato expone una etiqueta legible no
 * vacia para cada uno de sus valores (DISPONIBLE/BLOQUEADO).
 */
class EstadoPlatoTest {

    @ParameterizedTest
    @EnumSource(EstadoPlato.class)
    void todosLosEstadosTienenEtiquetaNoVacia(EstadoPlato estado) {
        assertNotNull(estado.getEtiqueta());
        assertEquals(false, estado.getEtiqueta().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(EstadoPlato.class)
    void valueOfDevuelveElMismoEstado(EstadoPlato estado) {
        assertEquals(estado, EstadoPlato.valueOf(estado.name()));
    }

    @org.junit.jupiter.api.Test
    void etiquetasEspecificasSonLasEsperadas() {
        assertEquals("Disponible", EstadoPlato.DISPONIBLE.getEtiqueta());
        assertEquals("Bloqueado", EstadoPlato.BLOQUEADO.getEtiqueta());
    }
}
