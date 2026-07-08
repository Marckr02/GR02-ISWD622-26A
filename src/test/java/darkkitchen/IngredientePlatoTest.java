package darkkitchen;

import model.IngredientePlato;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * TDD (tarea tecnica model): IngredientePlato expone getters/setters para
 * insumo, cantidad y unidad de receta; unidadReceta puede ser null
 * (compatibilidad con recetas antiguas sin unidad explicita).
 */
class IngredientePlatoTest {

    @Test
    void constructorConDatosAsignaTodosLosCampos() {
        IngredientePlato ingrediente = new IngredientePlato(3, 250.0, "g");

        assertEquals(3, ingrediente.getInsumoId());
        assertEquals(250.0, ingrediente.getCantidad(), 0.0001);
        assertEquals("g", ingrediente.getUnidadReceta());
    }

    @Test
    void constructorConUnidadNullEsValido() {
        IngredientePlato ingrediente = new IngredientePlato(4, 1.0, null);

        assertNull(ingrediente.getUnidadReceta());
    }

    @Test
    void constructorVacioPermiteUsarSetters() {
        IngredientePlato ingrediente = new IngredientePlato();

        ingrediente.setInsumoId(8);
        ingrediente.setCantidad(15.5);
        ingrediente.setUnidadReceta("kg");

        assertEquals(8, ingrediente.getInsumoId());
        assertEquals(15.5, ingrediente.getCantidad(), 0.0001);
        assertEquals("kg", ingrediente.getUnidadReceta());
    }
}
