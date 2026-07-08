package darkkitchen;

import model.Restaurante;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TDD (tarea tecnica model): Restaurante expone el constructor vacio (para
 * frameworks), el constructor completo y los getters/setters de cada campo.
 */
class RestauranteGettersSettersTest {

    @Test
    void constructorVacioPermiteUsarSetters() {
        Restaurante restaurante = new Restaurante();

        restaurante.setId(5);
        restaurante.setNombre("Nombre Nuevo");
        restaurante.setDescripcion("Descripcion Nueva");

        assertEquals(5, restaurante.getId());
        assertEquals("Nombre Nuevo", restaurante.getNombre());
        assertEquals("Descripcion Nueva", restaurante.getDescripcion());
    }

    @Test
    void constructorCompletoAsignaTodosLosCampos() {
        Restaurante restaurante = new Restaurante(7, "Napoli Test", "Pizzeria de prueba");

        assertEquals(7, restaurante.getId());
        assertEquals("Napoli Test", restaurante.getNombre());
        assertEquals("Pizzeria de prueba", restaurante.getDescripcion());
    }
}
