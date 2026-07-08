package darkkitchen;

import model.IngredientePlato;
import model.Plato;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TDD (tarea tecnica model): Plato soporta el constructor legado (lista de
 * ids de insumo sin cantidad ni restaurante) y el constructor completo con
 * receta de IngredientePlato; getInsumoIds()/setInsumoIds() derivan la
 * lista de ids desde/hacia la receta interna.
 */
class PlatoModeloTest {

    @Test
    void constructorLegadoCreaIngredientesConCantidadUnoYUnidadNull() {
        Plato plato = new Plato(1, "Plato Legado", Arrays.asList(10, 20));

        assertEquals(0, plato.getRestauranteId());
        assertEquals(2, plato.getIngredientes().size());
        assertEquals(10, plato.getIngredientes().get(0).getInsumoId());
        assertEquals(1.0, plato.getIngredientes().get(0).getCantidad(), 0.0001);
        assertEquals(null, plato.getIngredientes().get(0).getUnidadReceta());
    }

    @Test
    void constructorLegadoConListaNullDejaIngredientesVacios() {
        Plato plato = new Plato(1, "Plato Sin Insumos", (List<Integer>) null);

        assertTrue(plato.getIngredientes().isEmpty());
    }

    @Test
    void constructorCompletoAsignaRestauranteEIngredientes() {
        List<IngredientePlato> ingredientes = List.of(new IngredientePlato(5, 200, "g"));
        Plato plato = new Plato(2, "Plato Completo", 3, ingredientes);

        assertEquals(3, plato.getRestauranteId());
        assertEquals(1, plato.getIngredientes().size());
        assertEquals(5, plato.getIngredientes().get(0).getInsumoId());
    }

    @Test
    void constructorCompletoConIngredientesNullUsaListaVacia() {
        Plato plato = new Plato(2, "Plato Sin Receta", 3, null);

        assertTrue(plato.getIngredientes().isEmpty());
    }

    @Test
    void constructorVacioInicializaListaDeIngredientesVacia() {
        Plato plato = new Plato();

        assertTrue(plato.getIngredientes().isEmpty());
    }

    @Test
    void getInsumoIdsDerivaLosIdsDeLaReceta() {
        Plato plato = new Plato(4, "Plato Con Receta", 1,
                List.of(new IngredientePlato(1, 100, "g"), new IngredientePlato(2, 50, "g")));

        List<Integer> ids = plato.getInsumoIds();

        assertEquals(List.of(1, 2), ids);
    }

    @Test
    void setInsumoIdsReemplazaLaRecetaConCantidadUno() {
        Plato plato = new Plato();

        plato.setInsumoIds(Arrays.asList(7, 8));

        assertEquals(2, plato.getIngredientes().size());
        assertEquals(7, plato.getIngredientes().get(0).getInsumoId());
        assertEquals(1.0, plato.getIngredientes().get(0).getCantidad(), 0.0001);
    }

    @Test
    void setInsumoIdsConNullDejaListaVacia() {
        Plato plato = new Plato();
        plato.setInsumoIds(Arrays.asList(1));

        plato.setInsumoIds(null);

        assertTrue(plato.getIngredientes().isEmpty());
    }

    @Test
    void settersDeIdYNombreFuncionanCorrectamente() {
        Plato plato = new Plato();

        plato.setId(9);
        plato.setNombre("Nombre Nuevo");
        plato.setRestauranteId(6);

        assertEquals(9, plato.getId());
        assertEquals("Nombre Nuevo", plato.getNombre());
        assertEquals(6, plato.getRestauranteId());
    }
}
