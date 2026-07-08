package darkkitchen;

import model.Insumo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TDD (tarea tecnica model): Insumo soporta el constructor de 5 argumentos
 * (stockMinimo por defecto en 0.0) y el de 6 argumentos completo; esCritico()
 * es verdadero solo cuando el stock actual esta por debajo del minimo.
 */
class InsumoModeloTest {

    @org.junit.jupiter.api.Test
    void constructorDeCincoArgumentosUsaStockMinimoCero() {
        Insumo insumo = new Insumo(1, "Sal", "kg", 10.0, 0.5);

        assertEquals(0.0, insumo.getStockMinimo(), 0.0001);
        assertFalse(insumo.esCritico());
    }

    @org.junit.jupiter.api.Test
    void constructorDeSeisArgumentosAsignaStockMinimo() {
        Insumo insumo = new Insumo(2, "Azucar", "kg", 4.0, 1.2, 5.0);

        assertEquals(5.0, insumo.getStockMinimo(), 0.0001);
        assertTrue(insumo.esCritico());
    }

    @ParameterizedTest
    @CsvSource({
            "10.0, 5.0, false",
            "3.0, 5.0, true",
            "5.0, 5.0, false",
            "0.0, 0.1, true"
    })
    void esCriticoComparaStockContraStockMinimo(double stock, double stockMinimo, boolean esperado) {
        Insumo insumo = new Insumo(3, "Insumo Critico Test", "kg", stock, 1.0, stockMinimo);

        assertEquals(esperado, insumo.esCritico());
    }

    @org.junit.jupiter.api.Test
    void settersModificanTodosLosCampos() {
        Insumo insumo = new Insumo();

        insumo.setId(9);
        insumo.setNombre("Nuevo Insumo");
        insumo.setUnidad("l");
        insumo.setStock(12.0);
        insumo.setCostoUnitario(3.3);
        insumo.setStockMinimo(2.0);

        assertEquals(9, insumo.getId());
        assertEquals("Nuevo Insumo", insumo.getNombre());
        assertEquals("l", insumo.getUnidad());
        assertEquals(12.0, insumo.getStock(), 0.0001);
        assertEquals(3.3, insumo.getCostoUnitario(), 0.0001);
        assertEquals(2.0, insumo.getStockMinimo(), 0.0001);
    }
}
